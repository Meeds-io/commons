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
CKEDITOR.plugins.add( 'simpleImage',
{
    icons: 'simpleImage', 
    init : function( editor ) {
        editor.addCommand( 'simpleImage', new CKEDITOR.dialogCommand( 'simpleImageDialog' ) );
        editor.ui.addButton( 'simpleImage', {
            label: 'image',
            command: 'simpleImage',
        });
        
        if ( editor.contextMenu ) {
            editor.addMenuGroup( 'imageGroup' );
            editor.addMenuItem( 'imageItem', {
                label: 'Image',
                icon: this.path + 'icons/simpleImage.png',
                command: 'simpleImage',
                group: 'imageGroup'
            });

            editor.contextMenu.addListener( function( element ) {
                if ( element) {
                    return { imageItem: CKEDITOR.TRISTATE_OFF };
                }
            });
        };

        CKEDITOR.dialog.add( 'simpleImageDialog', this.path + 'dialogs/simpleImage.js' );
        
        editor.on( 'doubleclick', function( evt ) {
            var element = evt.data.element;

            if ( element.is( 'img' ) && !element.data( 'cke-realelement' ) && !element.isReadOnly() )
                evt.data.dialog = 'simpleImageDialog';
        } );
    }
});
