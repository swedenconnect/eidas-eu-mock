/**
 * Copyright (c) 2015 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */
/** @author vanegdi */

/*function blink(mySelector){
    $(mySelector).animate({opacity:0},200,"linear",function(){
        $(this).animate({opacity:1},200);
    });
}*/

// Automatic Redirect of the form
function submitSendFormAction() {
    document.getElementsByName('consentSelector')[0].submit();
    /*if ($("#checkbox_confirmation").is(':checked')){
        document.getElementsByName('consentSelector')[0].submit();
    } else {
        blink("#checkbox_Confirmation_div");
    }*/
}

function submitCancelFormAction() {
    document.getElementsByName('cancelForm')[0].submit();
}

function nextEventSlide1() {
    $("#slider1").slideUp();
    $("#slider2").slideDown();
}
function backEventSlide2() {
    $("#slider1").slideDown();
    $("#slider2").slideUp();
    //$("#checkbox_Confirmation_div").css('color','white');
}

function initJSEnabledContainers() {
    $('.jsOK').prop('disabled', false);
    $('.jsOK').show();
}

$("#attributeModal").on('show.bs.modal', function (e) {
    var attributeModalName = $(e.relatedTarget).data('attribute-name');
    var attributeModalDesc = $(e.relatedTarget).data('attribute-desc');
    $(e.currentTarget).find('label[id="attributeModalNameDisp"]').text(attributeModalName);
    $(e.currentTarget).find('div[id="attributeModalDescriptionDisp"]').text(attributeModalDesc);
});

function init () {

    initJSEnabledContainers();

    document.getElementById("slider1").style.display = "block" ;
    document.getElementById("slider2").style.display = "none" ;

    document.getElementById("buttonNextSlide1").addEventListener("click", function () {nextEventSlide1()});

    document.getElementById("buttonNextSlide2").addEventListener("click", function () {submitSendFormAction()});
    document.getElementById("buttonBackSlide2").addEventListener("click", function () {backEventSlide2()});
    document.getElementById("buttonCancelSlide1").addEventListener("click", function () {submitCancelFormAction()});
    document.getElementById("buttonCancelSlide2").addEventListener("click", function () {submitCancelFormAction()});

    if (($(".attr_stork_li_slider1").length)>0){
        $("#noDataDiv_slider1").hide();
    } else{
        $("#noDataDiv_slider1").show();
    }

    if (($(".attr_stork_li_slider2").length)>0){
        $("#noDataDiv_slider2").hide();
    } else{
        $("#noDataDiv_slider2").show();
    }
}

init();
