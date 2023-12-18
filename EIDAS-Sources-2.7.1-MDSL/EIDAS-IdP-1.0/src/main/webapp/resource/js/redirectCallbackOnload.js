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
// Automatic Redirect of the form
function b64EncodeUnicode(str) {
    return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, function (match, p1) {
        return String.fromCharCode(parseInt(p1, 16))
    }))
}

function base64_encode() {
    var getjSonResponseDecoded = document.getElementById('jSonResponseDecoded').value;
    var jSonResponseEncoded = b64EncodeUnicode(getjSonResponseDecoded);
    document.getElementById('SMSSPResponse').value = jSonResponseEncoded;
    document.getElementsByName('redirectForm')[0].submit();
}

function submitRedirectFormAction() {
    document.getElementById('idpSubmitbutton').style.display = 'block';
    var doNotmodifyTheResponse = document.getElementById('doNotmodifyTheResponse').value;
    var errorMessage = document.getElementById('errorMessage').value;
    if ((errorMessage == null) || (errorMessage == undefined) || (errorMessage == "null") || (errorMessage == "")) {
        if (doNotmodifyTheResponse == "on") {
            base64_encode();
        }
    } else {
        document.forms[0].action = "Error";
        document.forms[0].method = "post";
        document.forms[0].submit();
    }
}
window.addEventListener('load', submitRedirectFormAction());
