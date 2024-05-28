/**
 * @license Copyright (c) 2003-2020, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or https://ckeditor.com/legal/ckeditor-oss-license
 */

( function() {
	'use strict';

	var Jsonp = {

		_attachScript: function( url, errorCallback ) {
			// ATM we cannot use CKE scriptloader here, because it will make sure that script
			// with given URL is added only once.
			var script = new CKEDITOR.dom.element( 'script' );
			script.setAttribute( 'src', url );
			script.on( 'error', errorCallback );

			CKEDITOR.document.getBody().append( script );

			return script;
		},

		sendRequest: function( urlTemplate, urlParams, callback, errorCallback ) {
			var request = {};
			urlParams = urlParams || {};

			var callbackKey = CKEDITOR.tools.getNextNumber(),
				scriptElement;

			urlParams.callback = 'CKEDITOR._.jsonpCallbacks[' + callbackKey + ']';

			CKEDITOR._.jsonpCallbacks[ callbackKey ] = function( response ) {
				// On IEs scripts are sometimes loaded synchronously. It is bad for two reasons:
				// * nature of sendRequest() is unstable,
				// * scriptElement does not exist yet.
				setTimeout( function() {
					cleanUp();
					callback( response );
				} );
			};

			scriptElement = this._attachScript( urlTemplate.output( urlParams ), function() {
				cleanUp();
				errorCallback && errorCallback();
			} );

			request.cancel = cleanUp;

			function cleanUp() {
				if ( scriptElement ) {
					scriptElement.remove();
					delete CKEDITOR._.jsonpCallbacks[ callbackKey ];
					scriptElement = null;
				}
			}

			return request;
		}
	};

	CKEDITOR.plugins.add( 'embedbaseOnlyVideo', {
		lang: 'ar,az,bg,ca,cs,da,de,de-ch,en,en-au,eo,es,es-mx,et,eu,fr,gl,hr,hu,id,it,ja,ko,ku,lv,nb,nl,oc,pl,pt,pt-br,ro,ru,sk,sq,sr,sr-latn,sv,tr,ug,uk,zh,zh-cn', // %REMOVE_LINE_CORE%
		requires: 'dialog,widget,notificationaggregator',

		onLoad: function() {
			CKEDITOR._.jsonpCallbacks = {};
		},

		init: function() {
			CKEDITOR.dialog.add( 'embedBaseOnlyVideo', this.path + 'dialogs/embedBaseOnlyVideo.js' );
		}
	} );

	function createWidgetBaseDefinition( editor ) {
		var aggregator,
			lang = editor.lang.embedBaseOnlyVideo;

		return {
			mask: true,
			template: '<div></div>',
			pathName: lang.pathName,
			_cache: {},
			urlRegExp: /^((https?:)?\/\/|www\.)/i,

			init: function() {
				this.on( 'sendRequest', function( evt ) {
					this._sendRequest( evt.data );
				}, this, null, 999 );

				this.on( 'handleResponse', function( evt ) {
					if ( evt.data.html ) {
						return;
					}

					var retHtml = this._responseToHtml( evt.data.url, evt.data.response );

					if ( retHtml !== null ) {
						evt.data.html = retHtml;
					} else {
						evt.data.errorMessage = 'unsupportedUrl';
						evt.cancel();
					}
				}, this, null, 999 );
			},

			loadContent: function( url, opts ) {
				opts = opts || {};

				var that = this,
					cachedResponse = this._getCachedResponse( url ),
					request = {
						noNotifications: opts.noNotifications,
						url: url,
						callback: finishLoading,
						errorCallback: function( msg ) {
							that._handleError( request, msg );
							if ( opts.errorCallback ) {
								opts.errorCallback( msg );
							}
						}
					};

				if ( cachedResponse ) {
					// Keep the async nature (it caused a bug the very first day when the loadContent()
					// was synchronous when cache was hit :D).
					setTimeout( function() {
						finishLoading( cachedResponse );
					} );
					return;
				}

				if ( !opts.noNotifications ) {
					request.task = this._createTask();
				}

				// The execution will be followed by #sendRequest's listener.
				this.fire( 'sendRequest', request );

				function finishLoading( response ) {
					request.response = response;

					// Check if widget is still valid.
					if ( !that.editor.widgets.instances[ that.id ] ) {
						CKEDITOR.warn( 'embedbase-widget-invalid' );

						if ( request.task ) {
							request.task.done();
						}

						return;
					}

					if ( that._handleResponse( request ) ) {
						that._cacheResponse( url, response );
						if ( opts.callback ) {
							opts.callback();
						}
					}
				}

				return request;
			},

			isUrlValid: function( url ) {
				return this.urlRegExp.test( url ) && this.fire( 'validateUrl', url ) !== false;
			},

			getErrorMessage: function( messageTypeOrMessage, url, suffix ) {
				var message = editor.lang.embedBaseOnlyVideo[ messageTypeOrMessage + ( suffix || '' ) ];
				if ( !message ) {
					message = messageTypeOrMessage;
				}

				return new CKEDITOR.template( message ).output( { url: url || '' } );
			},
			_sendRequest: function( request ) {
				var that = this,
					jsonpRequest = Jsonp.sendRequest(
						this.providerUrl,
						{
							url: encodeURIComponent( request.url )
						},
						request.callback,
						function() {
							request.errorCallback( 'fetchingFailed' );
						}
					);

				request.cancel = function() {
					jsonpRequest.cancel();
					that.fire( 'requestCanceled', request );
				};
			},
			_handleResponse: function( request ) {
				var evtData = {
					url: request.url,
					html: '',
					response: request.response
				};

				if ( this.fire( 'handleResponse', evtData ) !== false ) {
					if ( request.task ) {
						request.task.done();
					}

					this._setContent( request.url, evtData.html );
					return true;
				} else {
					request.errorCallback( evtData.errorMessage );
					return false;
				}
			},
			_handleError: function( request, messageTypeOrMessage ) {
				if ( request.task ) {
					request.task.cancel();

					if ( !request.noNotifications ) {
						editor.showNotification( this.getErrorMessage( messageTypeOrMessage, request.url ), 'warning' );
					}
				}
			},

			_responseToHtml: function( url, response ) {
				if ( response.type == 'photo' ) {
					return '<img src="' + CKEDITOR.tools.htmlEncodeAttr( response.url ) + '" ' +
						'alt="' + CKEDITOR.tools.htmlEncodeAttr( response.title || '' ) + '" style="max-width:100%;height:auto" />';
				} else if ( response.type == 'video' || response.type == 'rich' ) {
					// Embedded iframes are added to page's focus list. Adding negative tabindex attribute
					// removes their ability to be focused by user. (https://dev.ckeditor.com/ticket/14538)
					response.html = response.html.replace( /<iframe/g, '<iframe tabindex="-1"' );

					return response.html;
				}

				return null;
			},
			_setContent: function( url, content ) {
				this.setData( 'url', url );
				this.element.setHtml( content );
			},
			_createTask: function() {
				if ( !aggregator || aggregator.isFinished() ) {
					aggregator = new CKEDITOR.plugins.notificationAggregator( editor, lang.fetchingMany, lang.fetchingOne );

					aggregator.on( 'finished', function() {
						aggregator.notification.hide();
					} );
				}

				return aggregator.createTask();
			},
			_cacheResponse: function( url, response ) {
				this._cache[ url ] = response;
			},
			_getCachedResponse: function( url ) {
				return this._cache[ url ];
			}
		};
	}


	CKEDITOR.plugins.embedBaseOnlyVideo = {
		createWidgetBaseDefinition: createWidgetBaseDefinition,
		_jsonp: Jsonp
	};

} )();
