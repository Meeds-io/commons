/**
 * @license Copyright (c) 2003-2020, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or https://ckeditor.com/legal/ckeditor-oss-license
 */

( function() {
	'use strict';

	CKEDITOR.plugins.add( 'embedsemantic', {
		icons: 'embedsemantic', // %REMOVE_LINE_CORE%
		hidpi: true, // %REMOVE_LINE_CORE%
		requires: 'embedbase',

		onLoad: function() {
			this.registerOembedTag();
		},

		init: function( editor ) {
			let widgetDefinition = CKEDITOR.plugins.embedBase.createWidgetBaseDefinition( editor ),
				origInit = widgetDefinition.init;

			CKEDITOR.tools.extend( widgetDefinition, {
				// Use a dialog exposed by the embedbase plugin.
				dialog: 'embedBase',
				button: editor.lang.embedbase.button,
				allowedContent: 'oembed',
				requiredContent: 'oembed',
				styleableElements: 'oembed',
				// Share config with the embed plugin.
				providerUrl: new CKEDITOR.template(
					editor.config.embed_provider ||
					'//ckeditor.iframe.ly/api/oembed?url={url}&callback={callback}&omit_script=1'
				),

				init: function() {
					let that = this;

					origInit.call( this );

          this.on( 'handleResponse', function( response ) {
            editor.fire('embedHandleResponse', response);
          }, this );
          this.on( 'requestCanceled', function() {
            editor.fire('embedCanceled');
          }, this );


					// Need to wait for #ready with the initial content loading, because on #init there's no data yet.
					this.once( 'ready', function() {
						// When widget is created using dialog, the dialog's code will handle loading the content
						// (because it handles success and error), so do load the content only when loading data.
						if ( this.data.loadOnReady ) {
							this.loadContent( this.data.url, {
                noNotifications: true,
								callback: function() {
									// Do not load the content again on widget's next initialization (e.g. after undo or paste).
									// Plus, this is a small trick that we change loadOnReady now, inside the callback.
									// It guarantees that if the content was not loaded (an error occurred or someone
									// undid/copied sth to fast) the content will be loaded on the next initialization.
									that.setData( 'loadOnReady', false );
									editor.fire( 'updateSnapshot' );
                                    ensureFollowingBlock( that );
								}
							} );
						}
					} );
				},

				upcast: function( element, data ) {
					if ( element.name != 'oembed' ) {
						return;
					}

					let text = element.children[ 0 ],
						div;

					if ( text && text.type == CKEDITOR.NODE_TEXT && text.value ) {
						data.url = text.value;
						data.loadOnReady = true;
						div = new CKEDITOR.htmlParser.element( 'div' );
						element.replaceWith( div );

						// Transfer widget classes from data to widget element (https://dev.ckeditor.com/ticket/13199).
						div.attributes[ 'class' ] = element.attributes[ 'class' ];

						return div;
					}
				},

				downcast: function( element ) {
					let ret = new CKEDITOR.htmlParser.element( 'oembed' );
					ret.add( new CKEDITOR.htmlParser.text( encodeURIComponent(this.data.url) ) );

					// Transfer widget classes from widget element back to data (https://dev.ckeditor.com/ticket/13199).
					if ( element.attributes[ 'class' ] ) {
						ret.attributes[ 'class' ] = element.attributes[ 'class' ];
					}

					return ret;
				}
			}, true );

            editor.widgets.add('embedSemantic', widgetDefinition);
            editor.widgets.on('instanceCreated', function(evt) {
				let widget = evt.data;
				if (widget.name !== 'embedSemantic') return;

				widget.on('ready', function() {
					let wrapper = widget.wrapper ? widget.wrapper.$ : null;
					if (!wrapper) return;

					if (wrapper.querySelector('.cke-embed-add-line-btn')) return;

					let btn = document.createElement('button');
					btn.textContent = '↵';
					btn.title = 'Ajouter une ligne en dessous';
					btn.className = 'cke-embed-add-line-btn';
					Object.assign(btn.style, {
						position: 'absolute',
						right: '6px',
						bottom: '6px',
						fontSize: '13px',
						lineHeight: '14px',
						width: '22px',
						height: '22px',
						textAlign: 'center',
						background: '#fff',
						border: '1px solid #ccc',
						borderRadius: '4px',
						cursor: 'pointer',
						boxShadow: '0 1px 2px rgba(0,0,0,0.1)',
						display: 'none',
						zIndex: 9999
					});

					wrapper.style.position = 'relative';
					wrapper.appendChild(btn);

					function toggleAddLineButton() {
						let next = widget.wrapper.getNext();
						if (next && (next.getName() === 'p' || next.getName() === 'span')) {
							btn.style.display = 'none';
						} else {
							btn.style.display = 'block';
						}
					}

					wrapper.addEventListener('mouseenter', toggleAddLineButton);
					wrapper.addEventListener('mouseleave', function() {
						btn.style.display = 'none';
					});

					btn.addEventListener('click', function(e) {
						e.preventDefault();
						e.stopPropagation();

						let wrapperEl = widget.wrapper;
						if (!wrapperEl) return;

						let next = wrapperEl.getNext();
						if (!next || next.getName() !== 'p') {
							let newParagraph = editor.document.createElement('p');
							newParagraph.setHtml('<br>'); // Ligne vide <br>
							wrapperEl.insertAfter(newParagraph);
							let range = editor.createRange();
							range.moveToElementEditStart(newParagraph);
							editor.getSelection().selectRanges([range]);
							editor.focus();
						}

						editor.fire('saveSnapshot');
						toggleAddLineButton();
					});
				});
			});
    	},
		// Extends CKEDITOR.dtd so editor accepts <oembed> tag.
		registerOembedTag: function() {
			let dtd = CKEDITOR.dtd,
				name;

			// The oembed tag may contain text only.
			dtd.oembed = { '#': 1 };

			// Register oembed tag as allowed child, in each tag that can contain a div.
			// It also registers the oembed tag in objects like $block, $blockLimit, etc.
			for ( name in dtd ) {
				if ( dtd[ name ].div ) {
					dtd[ name ].oembed = 1;
				}
			}
		}
    });
})();
