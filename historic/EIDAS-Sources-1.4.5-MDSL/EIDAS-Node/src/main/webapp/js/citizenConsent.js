/*
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

function submitCancelFormAction() {
    document.getElementsByName('cancelForm')[0].submit();
}

function submitSendFormAction() {
    document.getElementsByName('consentSelector')[0].submit();
    /*if ($("#checkbox_confirmation").is(':checked')){
        document.getElementsByName('consentSelector')[0].submit();
    } else {
        blink("#checkbox_Confirmation_div");
    }*/
}

function initJSEnabledContainers() {
    $('.jsOK').prop('disabled', false);
    $('.jsOK').show();
}

function init () {
    initJSEnabledContainers();
    document.getElementById("buttonNext").addEventListener("click", function () {submitSendFormAction()});
    document.getElementById("buttonCancel").addEventListener("click", function () {submitCancelFormAction()});
    $("#currentAddressBase64Out").text(decodeCurrentAddress());
    document.getElementById("stepstatusjs").style.display = "block" ;
    document.getElementById("buttongroupjsjs").style.display = "block" ;
}

init();