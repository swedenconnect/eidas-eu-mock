// Testing the CSP is active
function testCSP() {
    try {
        eval("1=1");
        document.getElementById("cspMessage").innerHTML="Please upgrade your browser to support the latest security features.";
    } catch (e) {
        // CSP is enabled
    }
}
window.addEventListener('load', testCSP());