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
//IE placeholder;
$(function (){
    if (/MSIE 9|MSIE 8|MSIE 7|MSIE 6/g.test(navigator.userAgent)) {
        function resetPlaceholder() {
            if ($(this).val() === '') {
                $(this).val($(this).attr('placeholder'))
                        .attr('data-placeholder', true)
                        .addClass('ie-placeholder');
                if ($(this).is(':password')) {
                    var field = $('<input />');
                    $.each(this.attributes, function (i, attr) {
                        if (attr.name !== 'type') {
                            field.attr(attr.name, attr.value);
                        }
                    });
                    field.attr({
                        'type': 'text',
                        'data-input-password': true,
                        'value': $(this).val()
                    });
                    $(this).replaceWith(field);
                }
            }
        }

        $('[placeholder]').each(function () {
            //ie user refresh don't reset input values workaround
            if ($(this).attr('placeholder') !== '' && $(this).attr('placeholder') === $(this).val()){
                $(this).val('');
            }
            resetPlaceholder.call(this);
        });
        $(document).on('focus', '[placeholder]', function () {
            // add test  if ($(this).val !== 'root') to not reset value for "root"
            if ($(this).val() !== 'root') {
                if ($(this).attr('data-placeholder')) {
                    $(this).val('').removeAttr('data-placeholder').removeClass('ie-placeholder');
                }
            }
        }).on('blur', '[placeholder]', function () { resetPlaceholder.call(this); });
        $(document).on('focus', '[data-input-password]', function () {
            var field = $('<input />');
            $.each(this.attributes, function (i, attr) {
                if (['type','data-placeholder','data-input-password','value'].indexOf(attr.name) === -1) {
                    field.attr(attr.name, attr.value);
                }
            });
            field.attr('type', 'password').on('focus', function () { this.select(); });
            $(this).replaceWith(field);
            field.trigger('focus');
        });
    }
});