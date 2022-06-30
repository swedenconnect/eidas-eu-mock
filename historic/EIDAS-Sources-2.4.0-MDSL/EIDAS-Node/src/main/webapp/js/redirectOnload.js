// Automatic Redirect of the form
function submitRedirectFormAction() {

    if (document.getElementById('redirectLabel') !== null)
        document.getElementById('redirectLabel').innerHTML = document.getElementById('redirectingMessageId').value;

    if (document.getElementById('tokenRedirectLabel') !== null)
        document.getElementById('tokenRedirectLabel').innerHTML = document.getElementById('redirectingMessageId').value;

    if (document.getElementById('connectorRedirectLabel') !== null)
        document.getElementById('connectorRedirectLabel').innerHTML = document.getElementById('redirectingMessageId').value;

    if (document.getElementById('tokenConnectorRedirectLabel') !== null)
        document.getElementById('tokenConnectorRedirectLabel').innerHTML = document.getElementById('redirectingMessageId').value;

    document.getElementsByName('redirectForm')[0].submit();
}
window.addEventListener('load', submitRedirectFormAction());