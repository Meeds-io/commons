/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
CKEDITOR.dialog.add( 'simpleLinkDialog', function( editor ) {
    return {
        title: 'Link',
        minWidth: 400,
        minHeight: 200,
        resizable: CKEDITOR.DIALOG_RESIZE_NONE,
        contents: [
            {
                id: 'tab',
                label: 'Link',
                elements: [
                    {
                        type: 'textarea',
                        id: 'text',
                        label: 'Text',
                        validate: CKEDITOR.dialog.validate.notEmpty( "Text field cannot be empty." ),
                        setup: function(element) {
                            this.setValue(element.getText());
                        },
                        commit: function(element) {
                            element.setText(this.getValue());
                        },
                        onLoad : function () {
                            this.getInputElement().$.className = ''; 
                        }
                    },
                    {
                        type: 'text',
                        id: 'link',
                        label: 'Link',
                        validate: CKEDITOR.dialog.validate.notEmpty( "Link field cannot be empty." ),
                        setup: function(element) {
                            this.setValue(element.getAttribute("href") );
                        },
                        commit: function(element) {
                            var url = this.getValue();
                            if (url && !url.match(/^(\/|((https?|ftp|file):\/\/))/ig)) {
                                url = "http://" + url;
                            }
                            element.setAttribute("href", url);
                        },
                        onLoad : function () {
                            this.getInputElement().$.className = ''; 
                        }
                    }
                ]
            }
        ],
        
        onShow: function() {
            var selection = editor.getSelection();
            var element = selection.getStartElement();

            if ( element )
                element = element.getAscendant( 'a', true );

            if ( !element || element.getName() != 'a' ) {
                element = editor.document.createElement( 'a' );
                element.setText(selection.getSelectedText());
                this.insertMode = true;
            }
            else
                this.insertMode = false;

            this.element = element;
//            if (!this.insertMode )
                this.setupContent( this.element );
        },
        
        onOk: function() {
            var dialog = this;
            var a = this.element;
            this.commitContent(a);

            if ( this.insertMode )
                editor.insertElement(a);
        },

        onLoad : function () {
            var dialog = this.getElement();
            var dialogCover = document.getElementsByClassName('cke_dialog_background_cover')[0];

            dialog.removeClass('cke_reset_all').$.className += ' uiPopup cke_dialog simpleLinkDialog';
            dialog.findOne('.cke_dialog_ui_button_ok').$.className = 'btn btn-primary';
            dialog.findOne('.cke_dialog_ui_button_cancel').$.className = 'btn';
            dialog.findOne('.cke_dialog_footer').$.className = 'uiActionBorder';
            dialog.findOne('.cke_dialog_title').$.className = 'popupHeader';
            dialog.findOne('.cke_dialog_close_button').$.className = 'uiIconClose cke_dialog_close_button';         	
            dialogCover.className = 'cke_dialog_background_cover uiPopupWrapper';
            dialogCover.style.backgroundColor = '';
            dialogCover.style.opacity = '';
        }
    };
});

