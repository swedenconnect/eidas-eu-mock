<%@ page import="eu.eidas.auth.commons.EidasParameterKeys" %>
<%@ page import="eu.eidas.auth.commons.EidasStringUtil" %>
<%@ page import="member_country_specific.idp.ApplicationSpecificIDPException" %>
<%@ page import="member_country_specific.idp.ProcessLogin" %>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page errorPage="/errorPage.jsp" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>

<%
    boolean checkBoxIpAddress = "on".equalsIgnoreCase(request.getParameter("checkBoxIpAddress"));
    String eidasnameid = null;
    if ("on".equalsIgnoreCase(request.getParameter("checkBoxNameId")))
        eidasnameid = request.getParameter("eidasnameid");

    String ipAddress = request.getRemoteAddr();
    if (request.getHeader(EidasParameterKeys.HTTP_X_FORWARDED_FOR.toString()) != null) {
        ipAddress = request.getHeader(EidasParameterKeys.HTTP_X_FORWARDED_FOR.toString());
    } else {
        if (request.getHeader(EidasParameterKeys.X_FORWARDED_FOR.toString()) != null)
            ipAddress = request.getHeader(EidasParameterKeys.X_FORWARDED_FOR.toString());
    }

    String doNotmodifyTheResponse = request.getParameter("doNotmodifyTheResponse");
    String jSonRequestDecoded = request.getParameter("jSonRequestDecoded");
    String smsspToken = request.getParameter("smsspToken");
    String eidasloa = request.getParameter("eidasloa");
    String jSonResponseDecoded = null;
    String username = null;
    String password = null;
    String callback = request.getParameter("callback");
    String jSonResponseEncoded = null;
    String errorMessageTitle = null;
    String errorMessage = null;
    try {

        if (jSonRequestDecoded != null && !jSonRequestDecoded.equals("")) {
            username = request.getParameter("username");
            password = request.getParameter("password");
            ProcessLogin processLogin = new ProcessLogin();

            jSonResponseEncoded = processLogin.checkAuthentication(username, password, smsspToken);
            if (jSonResponseEncoded == null)
                jSonResponseEncoded = processLogin.generateDummyJsonResponse(jSonRequestDecoded, username, eidasloa, checkBoxIpAddress ? ipAddress : null, eidasnameid);
            jSonResponseDecoded = new String(EidasStringUtil.decodeBytesFromBase64(jSonResponseEncoded));
        }
    } catch (ApplicationSpecificIDPException ex) {
        errorMessageTitle = ex.getTitle();
        errorMessage = ex.getMessage();
    }
%>
<script language="Javascript" type="text/javascript">

    function submitForm() {

        var doNotmodifyTheResponse = document.getElementById('doNotmodifyTheResponse').value;
        var errorMessage = document.getElementById('errorMessage').value;
        if ((errorMessage == null)||(errorMessage == undefined)||(errorMessage == "null")||(errorMessage == "")) {
            var callback = document.getElementById('callback').value;
            if (doNotmodifyTheResponse == "on") {
                base64_encode(callback);
            }

        } else {
            document.forms[0].action = "Error";
            document.forms[0].method = "post";
            document.forms[0].submit();
        }
    }

    window.onload = submitForm;
    window.onload();

    function b64EncodeUnicode(str) {
        return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, function (match, p1) {
            return String.fromCharCode(parseInt(p1, 16))
        }))
    }

    function base64_encode(callback) {
        var getjSonResponseDecoded = document.getElementById('jSonResponseDecoded').value;
        var base64_encode = b64EncodeUnicode(getjSonResponseDecoded);
        document.getElementById('SMSSPResponse').value = base64_encode;
        document.forms[0].action = callback;
        document.forms[0].method = "post";
        document.forms[0].submit();
    }
</script>
<html lang="en">

<head>
    <jsp:include page="htmlHead.jsp"/>
    <title>eIDAS Authentication Service (IdP)</title>
</head>
<body>
<!--START HEADER-->
<header class="header">
	<div class="container">
		<h1>eIDAS Authentication Service (IdP)</h1>
	</div>
</header>
<!--END HEADER-->
<div class="container">
	<div class="row">
		<div class="tab-content">
			<div role="tabpanel" class="tab-pane fade in active" id="tab-02">
				<div class="col-md-12">
					<h2>Response
					</h2>
				</div>
				<jsp:include page="leftColumn.jsp"/>
				<div class="col-md-6">
					<form id="genericForm" name="genericForm">
						<input type="hidden" id="SMSSPResponse" name="SMSSPResponse"/>
						<input type="hidden" id="errorMessage" name="errorMessage" value="<%=errorMessage%>"/>
						<input type="hidden" id="errorMessageTitle" name="errorMessageTitle" value="<%=errorMessageTitle%>"/>
					</form>

					<form id="redirectForm" name="redirectForm">
						<div class="form-group">
							<input type="hidden" id="username" name="username" value="<%=username%>"/>
							<label for="jSonResponseDecoded">SmsspToken Response</label>
							<textarea name="jSonResponseDecoded" id="jSonResponseDecoded" class="form-control" rows="10"><%=jSonResponseDecoded%></textarea>
							<input type="hidden" id="callback" name="callback" value="<%=callback%>"/>
							<input type="hidden" id="doNotmodifyTheResponse" name="doNotmodifyTheResponse" value="<%=doNotmodifyTheResponse%>"/>
							<input type="hidden" id="jSonResponseEncoded" name="jSonResponseEncoded" value="<%=jSonResponseEncoded%>"/>
						</div>
						<item type="button" id="idpSubmitbutton" class="btn btn-default btn-lg btn-block"
							  onclick="return base64_encode('<%=callback%>');">Submit
						</item>
					</form>
				</div>
			</div>
		</div>
	</div>
</div>

<noscript>
    <!--START HEADER-->
    <header class="header">
        <div class="container">
            <h1>eIDAS Authentication Service (IdP)</h1>
        </div>
    </header>
    <!--END HEADER-->

    <jsp:include page="footer.jsp"/>
</noscript>
</body>
</html>