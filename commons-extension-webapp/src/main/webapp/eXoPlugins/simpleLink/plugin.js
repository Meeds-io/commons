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
CKEDITOR.plugins.add( 'simpleLink',
{
    icons: 'simpleLink', 
    init : function( editor ) {
        editor.addCommand( 'simpleLink', new CKEDITOR.dialogCommand( 'simpleLinkDialog' ) );
        editor.ui.addButton( 'simpleLink', {
            label: 'Link',
            command: 'simpleLink',
        });
        
        if ( editor.contextMenu ) {
            editor.addMenuGroup( 'linkGroup' );
            editor.addMenuItem( 'linkItem', {
                label: 'Link',
                icon: this.path + 'icons/simpleLink.png',
                command: 'simpleLink',
                group: 'linkGroup'
            });

            editor.contextMenu.addListener( function( element ) {
                if ( element) {
                    return { linkItem: CKEDITOR.TRISTATE_OFF };
                }
            });
        };

        CKEDITOR.dialog.add( 'simpleLinkDialog', this.path + 'dialogs/simpleLink.js' );
    }
});
