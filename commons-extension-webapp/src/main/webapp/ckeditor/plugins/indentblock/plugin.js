/**
 * @license Copyright (c) 2003-2017, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

/**
 * @fileOverview Handles the indentation of block elements.
 */

( function() {
	'use strict';

	var $listItem = CKEDITOR.dtd.$listItem,
		$list = CKEDITOR.dtd.$list,
		TRISTATE_DISABLED = CKEDITOR.TRISTATE_DISABLED,
		TRISTATE_OFF = CKEDITOR.TRISTATE_OFF;

	CKEDITOR.plugins.add( 'indentblock', {
		requires: 'indent,list',
		init: function( editor ) {
			var globalHelpers = CKEDITOR.plugins.indent,
				classes = editor.config.indentClasses;

			// Register commands.
			globalHelpers.registerCommands( editor, {
				indentblock: new commandDefinition( editor, 'indentblock', true ),
				outdentblock: new commandDefinition( editor, 'outdentblock' )
			} );

			var mixedListContext = { ol: 1, ul: 1 },
				mixedClassNameRegex = classes ? new RegExp( '(?:^|\\s+)(' + classes.join( '|' ) + ')(?=$|\\s)' ) : null;

			editor.on( 'pluginsLoaded', function() {
				hookMixedSelectionIndent( editor.getCommand( 'indent' ), true );
				hookMixedSelectionIndent( editor.getCommand( 'outdent' ), false );
			} );

			function hookMixedSelectionIndent( command, isIndent ) {
				if ( !command ) {
					return;
				}

				command.on( 'exec', function( evt ) {
					if ( evt.data.done ) {
						return;
					}

					var selection = editor.getSelection(),
						ranges = selection && selection.getRanges(),
						handled = false,
						database = {};

					if ( !ranges || !ranges.length ) {
						return;
					}

					editor.fire( 'lockSnapshot' );

					for ( var i = 0; i < ranges.length; i++ ) {
						if ( handleMixedRange( ranges[ i ], isIndent, database ) ) {
							handled = true;
						}
					}

					CKEDITOR.dom.element.clearAllMarkers( database );

					editor.fire( 'unlockSnapshot' );

					if ( handled ) {
						editor.fire( 'contentDomInvalidated' );
						evt.data.done = true;
					}
				}, null, null, 1 );

				command.on( 'refresh', function( evt ) {
					var selection = editor.getSelection(),
						ranges = selection && selection.getRanges(),
						mixed = false;

					if ( ranges ) {
						for ( var i = 0; i < ranges.length; i++ ) {
							if ( isMixedRange( ranges[ i ] ) ) {
								mixed = true;
								break;
							}
						}
					}

					if ( mixed ) {
						if ( !evt.data.states ) {
							evt.data.states = {};
						}
						evt.data.states[ 'mixedSelectionIndent@' + ( isIndent ? 'indent' : 'outdent' ) ] = CKEDITOR.TRISTATE_OFF;
					}
				}, null, null, 1 );
			}

			function analyzeMixedRange( range ) {
				var blocks = collectTopLevelBlocksInRange( range );
				if ( !blocks || blocks.length < 2 ) {
					return null;
				}

				var hasList = false,
					hasPlain = false;

				for ( var i = 0; i < blocks.length; i++ ) {
					if ( blocks[ i ].type != CKEDITOR.NODE_ELEMENT ) {
						continue;
					}
					if ( blocks[ i ].getName() in mixedListContext ) {
						hasList = true;
					} else {
						hasPlain = true;
					}
				}

				return { blocks: blocks, hasList: hasList, hasPlain: hasPlain };
			}

			function isMixedRange( range ) {
				var info = analyzeMixedRange( range );
				return !!info && info.hasList && info.hasPlain;
			}

			function handleMixedRange( range, isIndent, database ) {
				var info = analyzeMixedRange( range );
				if ( !info || !info.hasList || !info.hasPlain ) {
					return false;
				}

				var blocks = info.blocks;

				for ( var i = 0; i < blocks.length; i++ ) {
					if ( blocks[ i ].type != CKEDITOR.NODE_ELEMENT ) {
						continue;
					}

					if ( !isIndent && blocks[ i ].getName() in mixedListContext && getCurrentIndentOffset( blocks[ i ] ) <= 0 ) {
						collapseListToParagraphs( editor, blocks[ i ], database );
						continue;
					}

					indentElement.call( { editor: editor, isIndent: isIndent, database: database }, blocks[ i ], classes );
				}

				return true;
			}

			function collectTopLevelBlocksInRange( range ) {
				var commonAncestor = range.getCommonAncestor();
				if ( !commonAncestor ) {
					return null;
				}

				while ( commonAncestor && commonAncestor.type != CKEDITOR.NODE_ELEMENT ) {
					commonAncestor = commonAncestor.getParent();
				}

				if ( !commonAncestor ) {
					return null;
				}

				var startBlock = ascendToChildOf( range.startContainer, commonAncestor ),
					endBlock = ascendToChildOf( range.endContainer, commonAncestor );

				if ( !startBlock || !endBlock ) {
					return null;
				}

				var blocks = [],
					node = startBlock,
					guard = 0;

				while ( node ) {
					blocks.push( node );

					if ( node.equals( endBlock ) || ++guard > 1000 ) {
						break;
					}

					node = node.getNext();
				}

				return blocks;
			}

			function ascendToChildOf( node, ancestor ) {
				while ( node ) {
					var parent = node.getParent && node.getParent();
					if ( !parent ) {
						return null;
					}
					if ( parent.equals( ancestor ) ) {
						return node;
					}
					node = parent;
				}
				return null;
			}

			function getCurrentIndentOffset( element ) {
				if ( classes ) {
					var indentClassMatch = element.$.className.match( mixedClassNameRegex );
					return indentClassMatch ? CKEDITOR.tools.indexOf( classes, indentClassMatch[ 1 ] ) + 1 : 0;
				}
				return getIndent( element ) || 0;
			}

			function collapseListToParagraphs( editor, listNode, database ) {
				var listArray = CKEDITOR.plugins.list.listToArray( listNode, database );

				for ( var i = 0; i < listArray.length; i++ ) {
					listArray[ i ].indent = -1;
				}

				var newList = CKEDITOR.plugins.list.arrayToList( listArray, database, null, editor.config.enterMode, listNode.getDirection() );

				if ( newList ) {
					newList.listNode.replace( listNode );
				} else {
					listNode.remove();
				}
			}

			function commandDefinition() {
				globalHelpers.specificDefinition.apply( this, arguments );

				this.allowedContent = {
					'div h1 h2 h3 h4 h5 h6 ol p pre ul': {
						// Do not add elements, but only text-align style if element is validated by other rule.
						propertiesOnly: true,
						styles: !classes ? 'margin-left,margin-right' : null,
						classes: classes || null
					}
				};

				this.contentTransformations = [
					[ 'div: splitMarginShorthand' ],
					[ 'h1: splitMarginShorthand' ],
					[ 'h2: splitMarginShorthand' ],
					[ 'h3: splitMarginShorthand' ],
					[ 'h4: splitMarginShorthand' ],
					[ 'h5: splitMarginShorthand' ],
					[ 'h6: splitMarginShorthand' ],
					[ 'ol: splitMarginShorthand' ],
					[ 'p: splitMarginShorthand' ],
					[ 'pre: splitMarginShorthand' ],
					[ 'ul: splitMarginShorthand' ]
				];

				if ( this.enterBr )
					this.allowedContent.div = true;

				this.requiredContent = ( this.enterBr ? 'div' : 'p' ) +
					( classes ? '(' + classes.join( ',' ) + ')' : '{margin-left}' );

				this.jobs = {
					'20': {
						refresh: function( editor, path ) {
							var firstBlock = path.block || path.blockLimit;

							// Switch context from somewhere inside list item to list item,
							// if not found just assign self (doing nothing).
							if ( !firstBlock.is( $listItem ) ) {
								var ascendant = firstBlock.getAscendant( $listItem );

								firstBlock = ( ascendant && path.contains( ascendant ) ) || firstBlock;
							}

							// Switch context from list item to list
							// because indentblock can indent entire list
							// but not a single list element.

							if ( firstBlock.is( $listItem ) )
								firstBlock = firstBlock.getParent();

							//	[-] Context in the path or ENTER_BR
							//
							//		Don't try to indent if the element is out of
							//		this plugin's scope. This assertion is omitted
							//		if ENTER_BR is in use since there may be no block
							//		in the path.

							if ( !this.enterBr && !this.getContext( path ) )
								return TRISTATE_DISABLED;

							else if ( classes ) {

								//	[+] Context in the path or ENTER_BR
								//	[+] IndentClasses
								//
								//		If there are indentation classes, check if reached
								//		the highest level of indentation. If so, disable
								//		the command.

								if ( indentClassLeft.call( this, firstBlock, classes ) )
									return TRISTATE_OFF;
								else
									return TRISTATE_DISABLED;
							} else {

								//	[+] Context in the path or ENTER_BR
								//	[-] IndentClasses
								//	[+] Indenting
								//
								//		No indent-level limitations due to indent classes.
								//		Indent-like command can always be executed.

								if ( this.isIndent )
									return TRISTATE_OFF;

								//	[+] Context in the path or ENTER_BR
								//	[-] IndentClasses
								//	[-] Indenting
								//	[-] Block in the path
								//
								//		No block in path. There's no element to apply indentation
								//		so disable the command.

								else if ( !firstBlock )
									return TRISTATE_DISABLED;

								//	[+] Context in the path or ENTER_BR
								//	[-] IndentClasses
								//	[-] Indenting
								//	[+] Block in path.
								//
								//		Not using indentClasses but there is firstBlock.
								//		We can calculate current indentation level and
								//		try to increase/decrease it.

								else {
									return CKEDITOR[
										( getIndent( firstBlock ) || 0 ) <= 0 ? 'TRISTATE_DISABLED' : 'TRISTATE_OFF'
									];
								}
							}
						},

						exec: function( editor ) {
							var selection = editor.getSelection(),
								range = selection && selection.getRanges()[ 0 ],
								nearestListBlock;

							// If there's some list in the path, then it will be
							// a full-list indent by increasing or decreasing margin property.
							if ( ( nearestListBlock = editor.elementPath().contains( $list ) ) )
								indentElement.call( this, nearestListBlock, classes );

							// If no list in the path, use iterator to indent all the possible
							// paragraphs in the range, creating them if necessary.
							else {
								var iterator = range.createIterator(),
									enterMode = editor.config.enterMode,
									block;

								iterator.enforceRealBlocks = true;
								iterator.enlargeBr = enterMode != CKEDITOR.ENTER_BR;

								while ( ( block = iterator.getNextParagraph( enterMode == CKEDITOR.ENTER_P ? 'p' : 'div' ) ) ) {
									if ( !block.isReadOnly() )
										indentElement.call( this, block, classes );
								}
							}

							return true;
						}
					}
				};
			}

			CKEDITOR.tools.extend( commandDefinition.prototype, globalHelpers.specificDefinition.prototype, {
				// Elements that, if in an elementpath, will be handled by this
				// command. They restrict the scope of the plugin.
				context: { div: 1, dl: 1, h1: 1, h2: 1, h3: 1, h4: 1, h5: 1, h6: 1, ul: 1, ol: 1, p: 1, pre: 1, table: 1 },

				// A regex built on config#indentClasses to detect whether an
				// element has some indentClass or not.
				classNameRegex: classes ? new RegExp( '(?:^|\\s+)(' + classes.join( '|' ) + ')(?=$|\\s)' ) : null
			} );
		}
	} );

	// Generic indentation procedure for indentation of any element
	// either with margin property or config#indentClass.
	function indentElement( element, classes, dir ) {
		if ( element.getCustomData( 'indent_processed' ) )
			return;

		var editor = this.editor,
			isIndent = this.isIndent;

		if ( classes ) {
			// Transform current class f to indent step index.
			var indentClass = element.$.className.match( this.classNameRegex ),
				indentStep = 0;

			if ( indentClass ) {
				indentClass = indentClass[ 1 ];
				indentStep = CKEDITOR.tools.indexOf( classes, indentClass ) + 1;
			}

			// Operate on indent step index, transform indent step index
			// back to class name.
			if ( ( indentStep += isIndent ? 1 : -1 ) < 0 )
				return;

			indentStep = Math.min( indentStep, classes.length );
			indentStep = Math.max( indentStep, 0 );
			element.$.className = CKEDITOR.tools.ltrim( element.$.className.replace( this.classNameRegex, '' ) );

			if ( indentStep > 0 )
				element.addClass( classes[ indentStep - 1 ] );
		} else {
			var indentCssProperty = getIndentCss( element, dir ),
				currentOffset = parseInt( element.getStyle( indentCssProperty ), 10 ),
				indentOffset = editor.config.indentOffset || 40;

			if ( isNaN( currentOffset ) )
				currentOffset = 0;

			currentOffset += ( isIndent ? 1 : -1 ) * indentOffset;

			if ( currentOffset < 0 )
				return;

			currentOffset = Math.max( currentOffset, 0 );
			currentOffset = Math.ceil( currentOffset / indentOffset ) * indentOffset;

			element.setStyle(
				indentCssProperty,
				currentOffset ? currentOffset + ( editor.config.indentUnit || 'px' ) : ''
			);

			if ( element.getAttribute( 'style' ) !== '' ) {
				let currentStyle = element.getAttribute('style');
				currentStyle = currentStyle.substring(0, currentStyle.length - 1 ).concat(' !important;')
				element.setAttribute('style', currentStyle);
			} else {
				element.removeAttribute( 'style' );
			}
		}

		CKEDITOR.dom.element.setMarker( this.database, element, 'indent_processed', 1 );

		return;
	}

	// Method that checks if current indentation level for an element
	// reached the limit determined by config#indentClasses.
	function indentClassLeft( node, classes ) {
		var indentClass = node.$.className.match( this.classNameRegex ),
			isIndent = this.isIndent;

		// If node has one of the indentClasses:
		//	* If it holds the topmost indentClass, then
		//	  no more classes have left.
		//	* If it holds any other indentClass, it can use the next one
		//	  or the previous one.
		//	* Outdent is always possible. We can remove indentClass.
		if ( indentClass )
			return isIndent ? indentClass[ 1 ] != classes.slice( -1 ) : true;

		// If node has no class which belongs to indentClasses,
		// then it is at 0-level. It can be indented but not outdented.
		else
			return isIndent;
	}

	// Determines indent CSS property for an element according to
	// what is the direction of such element. It can be either `margin-left`
	// or `margin-right`.
	function getIndentCss( element, dir ) {
		return ( dir || element.getComputedStyle( 'direction' ) ) == 'ltr' ? 'margin-left' : 'margin-right';
	}

	// Return the numerical indent value of margin-left|right of an element,
	// considering element's direction. If element has no margin specified,
	// NaN is returned.
	function getIndent( element ) {
		return parseInt( element.getStyle( getIndentCss( element ) ), 10 );
	}
} )();

/**
 * A list of classes to use for indenting the contents. If set to `null`, no classes will be used
 * and instead the {@link #indentUnit} and {@link #indentOffset} properties will be used.
 *
 *		// Use the 'Indent1', 'Indent2', 'Indent3' classes.
 *		config.indentClasses = ['Indent1', 'Indent2', 'Indent3'];
 *
 * @cfg {Array} [indentClasses=null]
 * @member CKEDITOR.config
 */

/**
 * The size in {@link CKEDITOR.config#indentUnit indentation units} of each indentation step.
 *
 *		config.indentOffset = 4;
 *
 * @cfg {Number} [indentOffset=40]
 * @member CKEDITOR.config
 */

/**
 * The unit used for {@link CKEDITOR.config#indentOffset indentation offset}.
 *
 *		config.indentUnit = 'em';
 *
 * @cfg {String} [indentUnit='px']
 * @member CKEDITOR.config
 */
