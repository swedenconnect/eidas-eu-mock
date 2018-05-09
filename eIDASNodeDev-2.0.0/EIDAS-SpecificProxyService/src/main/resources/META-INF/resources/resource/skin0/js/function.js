/*
 * Copyright (c) 2017 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

/* Switchery = Checkbox Switch Toogle */
var elems = Array.prototype.slice.call(document.querySelectorAll('.js-switch'));
elems.forEach(function (html) {
    new Switchery(html, {size: 'small', color: '#467a39', jackColor: '#fff', secondaryColor: '#b3b3b3'});
});


jQuery(document).ready(function () {

    /*Plus-minus-text*/
    $('.text-plus').click(function () {
        $('.col-right-content').stop().animate({fontSize: '+=0.1em'}, 300);
    });
    $('.text-minus').click(function () {
        $('.col-right-content').stop().animate({fontSize: '-=0.1em'}, 300);
    });

    /*Contrast*/
    $(".contrast").click(function () {
        $("body").toggleClass("contrast");
    });

});
