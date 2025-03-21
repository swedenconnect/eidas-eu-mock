/*
 * Copyright (c) 2024 by European Commission
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

function showUrl() {
    document.forms[0].nodeUrl.value = document.forms[0].connector[document.forms[0].connector.selectedIndex].value;
}

function showEidasUrl() {
    var i;
    var idx = 0;
    for (i = 0; i < document.forms.length; i++) {
        if (document.forms[i].eidasconnector != null) {
            idx = i;
            break;
        }
    }
    document.forms[idx].nodeMetadataUrl.value = document.forms[idx].eidasconnector[document.forms[idx].eidasconnector.selectedIndex].value;
}


function showUrl2() {
    document.forms[2].nodeUrl2.value = document.forms[2].connector2[document.forms[2].connector2.selectedIndex].value;
}

function ajaxResignSubmit(formId, postUrl, onSuccessHandler, onErrorHandler) {
    //setSAMRequestMethod();
    var formData = $("#" + formId).serialize();
    $.ajax({
        type: "POST",
        url: postUrl,
        cache: false,
        data: formData,
        success: onSuccessHandler,
        error: onErrorHandler
    });


}

function ajaxChangeHttpMethod(formId, postUrl, onSuccessHandler, onErrorHandler) {
    setSAMRequestMethod();
    changeActionUrl();
    ajaxResignSubmit(formId, postUrl, onSuccessHandler, onErrorHandler);

}

function receiveSignedRequest(result) {
    document.forms['samlRequestXML'].smsspRequestJSON.value = result;
    callEncodeSAMLRequest();
}

function errorAjaxRequest(result) {
    alert("Error performing resign ");
}

function b64EncodeUnicode(str) {
    return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, function (match, p1) {
        return String.fromCharCode(parseInt(p1, 16))
    }))
}

function checkFieldExistence(fieldValue, fieldName) {

    if ((fieldValue == null) || (fieldValue == undefined) || (fieldValue == "null") || (fieldValue == "")) {
        window.alert('authentication_request.' + fieldName + ' does not exist');
        return 1;
    }
    return 0;
}


function setSAMRequestMethod() {

    var getjSonRequestDecoded = document.forms['samlRequestXML'].smsspRequestJSON.value;

    /*    //check existence of json Request fields
    var obj = JSON.parse(getjSonRequestDecoded);
        var errorCount = 0;
        errorCount = errorCount + checkFieldExistence(obj.authentication_request.provider_name, "provider_name");
        errorCount = errorCount + checkFieldExistence(obj.authentication_request.name_id_policy, "name_id_policy");
        errorCount = errorCount + checkFieldExistence(obj.authentication_request.sp_type, "sp_type");


        if (errorCount > 0) {
            return false;
        } else {*/
    document.forms['countrySelector'].method = "POST";
    var base64_encode = b64EncodeUnicode(getjSonRequestDecoded);
    document.forms['countrySelector'].SMSSPRequest.value = base64_encode;
    return true;
    // }
}


function changeActionUrl() {

    locationUrl = document.forms['countrySelector'].postLocationUrl.value;
    document.forms['countrySelector'].action = locationUrl;
    document.forms['samlRequestXML'].samlRequestLocation.value = locationUrl;
    return true;
}

function signAndEncodeSAMLRequest() {
    ajaxResignSubmit('samlRequestXML', 'reSign.action', receiveSignedRequest, errorAjaxRequest);
}

function enableEncoding(enable) {
//	if(document.getElementById("encodeButton")!=null)
//		document.getElementById("encodeButton").disabled= !enable;
}

function callEncodeSAMLRequest() {
    encodeSAMLRequest();
    enableEncoding(false);
}

function samlRequestXMLChanged() {
    enableEncoding(true);
}


function initCustomCombos() {
    try {
        /*Tab 1 load*/
        if ($("#connector").length) {
            var connector = $("#connector").msDropdown().data("dd");
            connector.on("change", showUrl);
            $("#citizen").msDropdown();
        }
        /*Tab 2 load*/
        if ($("#eidasconnector").length) {
            var eidasconnector = $("#eidasconnector").msDropdown().data("dd");
            eidasconnector.on("change", showEidasUrl);
            $("#citizeneidas").msDropdown();
        }
        /*Tab 3 load*/
        if ($("#connector2").length) {
            var connector2 = $("#connector2").msDropdown().data("dd");
            connector2.on("change", showUrl2);
        }
    } catch (e) {
        //console.debug(e);
        alert(e);
    }
}

/*function initTabs() {
    var tabContainers = $('div.tabs > div');

    $('div.tabs ul.tabNavigation a').click(function () {
        tabContainers.hide().filter(this.hash).show();

        $('div.tabs ul.tabNavigation a').removeClass('selected');
        $(this).addClass('selected');

        return false;
    }).filter(function(index) {
		if(index==1){ //eidas by default
			return true;
		}
	}).click();
}

function initPlugin() {

}*/

function checkAll(type) {
    $("[id^=" + type + "_]").each(function (index, el) {
        el.checked = true;
    });
}

/*
 ***************************************************************************************************
 Script executed at start of page
 ***************************************************************************************************
 */
function init() {

    /*window.addEventListener('load', initCustomCombos());
    window.addEventListener('load', initTabs());
    window.addEventListener('load', initPlugin());*/

    if (document.getElementById("connector") != null) {
        document.getElementById("connector").addEventListener("change", showUrl);
    }
    if (document.getElementById("eidasconnector") != null) {
        document.getElementById("eidasconnector").addEventListener("change", showEidasUrl);
    }

    if (document.getElementById("connector2") != null) {
        document.getElementById("connector2").addEventListener("change", showUrl2);
    }

    /** onclick events listeners to check all radio buttons **/
    if (document.getElementById("connector") != null) {
        document.getElementById("check_all_Mandatory").addEventListener("click", function () {
            checkAll("Mandatory");
        });
        document.getElementById("check_all_Optional").addEventListener("click", function () {
            checkAll("Optional");
        });
        document.getElementById("check_all_NoRequest").addEventListener("click", function () {
            checkAll("NoRequest");
        });
    }

    if (document.getElementById("eidasconnector") != null) {
        document.getElementById("check_all_MandatoryEidas").addEventListener("click", function () {
            checkAll("Mandatory");
        });
        document.getElementById("check_all_OptionalEidas").addEventListener("click", function () {
            checkAll("Optional");
        });
        document.getElementById("check_all_NoRequestEidas").addEventListener("click", function () {
            checkAll("NoRequest");
        });
        document.getElementById("check_all_MandatoryRepvEidas").addEventListener("click", function () {
            checkAll("MandatoryRepv");
        });
        document.getElementById("check_all_OptionalRepvEidas").addEventListener("click", function () {
            checkAll("OptionalRepv");
        });
        document.getElementById("check_all_NoRequestRepvEidas").addEventListener("click", function () {
            checkAll("NoRequestRepv");
        });
    }

    if (document.getElementById("connector2") != null) {
        document.getElementById("check_all_Mandatory2").addEventListener("click", function () {
            checkAll("2Mandatory");
        });
        document.getElementById("check_all_Optional2").addEventListener("click", function () {
            checkAll("2Optional");
        });
        document.getElementById("check_all_NoRequest2").addEventListener("click", function () {
            checkAll("2NoRequest");
        });
    }
    /* Submit buttons events*/
    $("#submit_tab1").click(function (event) {
        $('#formTab1').submit();
    });
    $("#submit_tab2").click(function (event) {
        $('#formTab2').submit();
    });
    $("#submit_tab3").click(function (event) {
        $('#formTab3').submit();
    });

    /* hiding all the content parts */
    $(".content").hide();
    /*toggle controls*/
    $("#tab1_toggle1").on("click", function (e) {
        $(this).toggleClass("expanded");
        $("#tab1_toggle1_content").slideToggle();
    });
    $("#tab2_toggle1").on("click", function (e) {
        $(this).toggleClass("expanded");
        $("#tab2_toggle1_content").slideToggle();
    });
    $("#tab2_toggle2").on("click", function (e) {
        $(this).toggleClass("expanded");
        $("#tab2_toggle2_content").slideToggle();
    });
    $("#tab2_toggle3").on("click", function (e) {
        $(this).toggleClass("expanded");
        $("#tab2_toggle3_content").slideToggle();
    });
    $("#tab2_toggle4").on("click", function (e) {
        $(this).toggleClass("expanded");
        $("#tab2_toggle4_content").slideToggle();
    });
    $("#tab2_toggle5").on("click", function (e) {
        $(this).toggleClass("expanded");
        $("#tab2_toggle5_content").slideToggle();
    });
    $("#tab3_toggle1").on("click", function (e) {
        $(this).toggleClass("expanded");
        $("#tab3_toggle1_content").slideToggle();
    });

    if ($("#eidasconnector").length) {
        var eidasconnector = $("#eidasconnector").msDropdown().data("dd");
        eidasconnector.on("change", showEidasUrl);
        $("#citizeneidas").msDropdown();
    }
    if ($("#connector").length) {
        connector.on("change", showUrl);
    }

    if ($("#connector2").length) {
        connector2.on("change", showUrl2);
    }
}

$(document).ready(function () {
    init();
});