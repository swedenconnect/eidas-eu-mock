window.addEventListener('load', initMoaAnmeldung());
document.getElementById('bkukarte').addEventListener('click', function () {
    bkuOnlineClicked();
});
document.getElementById('bkuhandy').addEventListener('click', function () {
    bkuHandyClicked();
});

function initMoaAnmeldung(){
    document.getElementById("localBKU").style.display = "none";
    document.getElementById("installJava").style.display = "none";
    document.getElementById("BrowserNOK").style.display = "none";
}

function areCookiesEnabled() {
    var cookiesEnabled = (navigator.cookieEnabled) ? true : false;
    if (typeof navigator.cookieEnabled == "undefined" && !cookieEnabled) {
        document.cookie = "testDummyCookie";
        cookiesEnabled = (document.cookie.indexOf("testDummyCookie") != -1) ? true : false;
    }
    return (cookiesEnabled);
}

function bkuOnlineClicked() {
    var cookieAllowed = areCookiesEnabled();
    if (cookieAllowed) {
        document.getElementById("localBKU").style.display = "block";
    }
    document.getElementById("installJava").style.display = "none";
    document.getElementById("BrowserNOK").style.display = "none";

    var el = document.getElementById("bkulogin");
    var parent = el.parentNode;

    if (cookieAllowed && navigator.javaEnabled() && ( deployJava.versionCheck("1.6.0_11+")
        || (  BrowserDetect.browser.toLowerCase() == "safari"
        && navigator.platform.toLowerCase().indexOf('win') == 0 ))) {

        if (BrowserDetect.browser.toLowerCase() != "explorer" && BrowserDetect.browser.toLowerCase()
            != "firefox" && BrowserDetect.browser.toLowerCase() != "mozilla") {
            alert("Hinweis! \nUm die Online BKU verwenden zu koennen benoetigen Sie Java 1.6 oder hoeher.");
        }

        var iframe = document.createElement("iframe");
        var callURL = "ColleagueResponse?country=AT&bku=online";
        iframe.setAttribute("src", callURL);
        iframe.setAttribute("width", "210");
        iframe.setAttribute("height", "165");
        iframe.setAttribute("frameborder", "0");
        iframe.setAttribute("scrolling", "no");
        iframe.setAttribute("title", "Login");


        parent.replaceChild(iframe, el);
    } else {
        okbrowser = true;
        if (BrowserDetect.browser.toLowerCase() == "explorer" || BrowserDetect.browser.toLowerCase()
            == "firefox" || BrowserDetect.browser.toLowerCase() == "safari"
            || BrowserDetect.browser.toLowerCase() == "mozilla") {
            okbrowser = true;
        } else {
            okbrowser = false;
        }
        if (!cookieAllowed) {
            parent.removeChild(el);
            document.getElementById("sessionCookiesNotAvailable").style.display = "block";
        } else {
            if (okbrowser == true) {
                parent.removeChild(el);
                document.getElementById("installJava").style.display = "block";
            } else {
                parent.removeChild(el);
                document.getElementById("BrowserNOK").style.display = "block";
            }
        }
    }
}

function bkuHandyClicked() {
    document.getElementById("localBKU").style.display = "none";
    document.getElementById("installJava").style.display = "none";
    document.getElementById("BrowserNOK").style.display = "none";
    document.getElementById("sessionCookiesNotAvailable").style.display = "none";

    var el = document.getElementById("bkulogin");
    var parent = el.parentNode;

    var iframe = document.createElement("iframe");
    var callURL = "Bku-anmeldung?bku=mobile";

    iframe.setAttribute("src", callURL);
    iframe.setAttribute("width", "210");
    iframe.setAttribute("height", "165");
    iframe.setAttribute("frameborder", "0");
    iframe.setAttribute("scrolling", "no");
    iframe.setAttribute("title", "Login");

    parent.replaceChild(iframe, el);
}

function getBaseURL() {
    var pathArray = document.location.pathname.split('/');
    newURL = window.location.protocol + "//" + window.location.host + "/" + pathArray[1];
    return newURL;
}
function fillLocalBKU() {
    var baseURL = getBaseURL();
    document.getElementById("moaLocal").action = document.getElementById("MoaIdConnectionURLGlobal").value;
    document.getElementById("OALocal").value = document.getElementById("OAGlobal").value;
    document.getElementById("bkuURILocal").value = document.getElementById("bkuURILocalGlobal").value;

    if (document.getElementById("TargetGlobal") == null) {
        document.getElementById("moaLocal").removeChild(document.getElementById("TargetLocal"));
    } else {
        document.getElementById("TargetLocal").value = document.getElementById("TargetGlobal").value;
    }

    if (document.getElementById("TemplateGlobal") == null) {
        document.getElementById("TemplateLocal").value = baseURL + "/template_localBKU.html";
    } else {
        document.getElementById("TemplateLocal").value = document.getElementById("TemplateGlobal").value;
    }

}