// Automatic Redirect of the form
function submitSendFormAction() {
    document.getElementsByName('consentSelector')[0].submit();
}
function submitCancelFormAction() {
    document.getElementsByName('cancelForm')[0].submit();
}
function init () {
    var buttonSend = document.getElementById("buttonBar.send");
    var buttonCancel = document.getElementById("buttonBar.cancel");

    buttonSend.addEventListener ("click", function () {submitSendFormAction()});
	if(buttonCancel==null)
		submitSendFormAction();
	else
		buttonCancel.addEventListener ("click", function () {submitCancelFormAction()});
}

init();

