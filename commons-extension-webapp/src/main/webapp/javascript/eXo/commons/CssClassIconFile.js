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
(function() {
  var ClassIcon = {
    datas : null,
    init : function() {
      ClassIcon.datas = window.eXo.fileIconsJsonData;
    },
    hasDatas : function() {
      if (ClassIcon.datas == null) {
        ClassIcon.init();
      }
      return (ClassIcon.datas !== undefined && ClassIcon.datas !== null);
    },

    getFileExtension : function(fileName) {
      if (fileName !== undefined && fileName.trim().length > 0) {
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
      }
      return "default";
    },

    getFileType : function(fileType) {
      var fileType_ = fileType.replace(/\./g, '');
      return fileType_.substring((fileType_.indexOf("/") + 1)).toLowerCase();
    },

    getCssClassByType : function(fileType, size) {
      if (fileType == null || fileType === undefined) {
        return ClassIcon.buildCssClass(null, size);
      }
      if (ClassIcon.hasDatas()) {
        for ( var i = 0; i < ClassIcon.datas.length; ++i) {
          var gFileTypes = ClassIcon.datas[i].groupFileTypes.split(',');
          for ( var j = 0; j < gFileTypes.length; ++j) {
            if (gFileTypes[j].indexOf(fileType) >= 0 || gFileTypes[j].indexOf(ClassIcon.getFileType(fileType)) >= 0) {
              return ClassIcon.buildCssClass(ClassIcon.datas[i].cssClass, size)
            }
          }
        }
      }
      fileType = fileType.replace(/\//g, "").replace(/:/g, "_").replace(/ /g, "_");
      return ClassIcon.buildCssClass(fileType, size);
    },

    getCssClassByName : function(fileName, size) {
      if (ClassIcon.hasDatas() && fileName !== undefined) {
        var clazz = null;
        var fileExtension = ClassIcon.getFileExtension(fileName);
        for ( var i = 0; i < ClassIcon.datas.length; ++i) {
          if (fileExtension === ClassIcon.datas[i].type) {
            clazz = ClassIcon.datas[i].cssClass;
          }
        }
        return ClassIcon.buildCssClass(clazz, size);
      }
      return ClassIcon.buildCssClass(null, size);
    },

    getCssClassByNameType : function(nameType, size) {
      var fileExtension = ClassIcon.getFileExtension(nameType);
      var fileType = 'File' + fileExtension.substring(0, 1).toUpperCase() + fileExtension.substring(1).toLowerCase();
      return ClassIcon.getCssClassByType(fileType, size);
    },

    getCssClassByNameAndType : function(fileName, fileType, size) {
      var clazz = ClassIcon.getCssClassByName(fileName, size);
      if (clazz.indexOf('FileDefault') > 0) {
        clazz = ClassIcon.getCssClassByType(fileType, size);
        if (clazz.indexOf('FileDefault') > 0) {
          clazz = ClassIcon.getCssClassByNameType(fileName);
        }
      }
      return clazz;
    },

    buildCssClass : function(clazz, size) {
      if (clazz == null || clazz === undefined) {
        clazz = 'FileDefault';
      }
      if (size == null || size === undefined) {
        size = '16x16';
      }
      clazz = 'uiIcon' + size + clazz + ' uiIcon' + size + 'nt_file';
      return clazz;
    }
  };
  return ClassIcon;
})();
