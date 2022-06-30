/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */

function submitCancelFormAction() {
    document.getElementsByName('cancelForm')[0].submit();
}

function submitSendFormAction() {
    document.getElementsByName('consentSelector')[0].submit();
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
