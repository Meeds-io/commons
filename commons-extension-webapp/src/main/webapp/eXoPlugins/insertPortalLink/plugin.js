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
CKEDITOR.plugins.add( 'insertPortalLink',
{
	requires : [ 'dialog' ],
	lang : ['en','fr','vi'],
	init : function( editor )
	{
		editor.ui.addButton( 'insertPortalLink.btn',
		{
			label : editor.lang.insertPortalLink.WCMInsertPortalLinkPlugins,
			command : 'insertPortalLink.cmd',
			icon : this.path + 'images/insertPortalLink.png'
		});

		var dialog = {
			canUndo : 'false',
			dialogName : 'insertPortalLink.dlg',
			editorFocus : 'false',
			exec : function(editor) {
				editor.openDialog('insertPortalLink.dlg');
				editor.titleLink = editor.getSelection().getSelectedText();
			}
		}
		
		var command = editor.addCommand( 'insertPortalLink.cmd', dialog);
		command.modes = { wysiwyg:1, source:1 };
		command.canUndo = false;

		CKEDITOR.dialog.add( 'insertPortalLink.dlg', this.path + 'js/portalLink.js' );
	}
});

