<%@ page import="eu.eidas.auth.commons.EidasStringUtil" %>
<%@ page import="eu.eidas.auth.commons.EidasParameterKeys" %>
<%@ page import="member_country_specific.idp.ProcessLogin" %>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>

<%
	String jSonResponseDecoded = request.getParameter("jSonResponseDecoded");
    String jSonResponseEncoded = new String(EidasStringUtil.decodeBytesFromBase64(jSonResponseDecoded));
%>

<html lang="en">

<head>
	<jsp:include page="htmlHead.jsp"/>
	<title>eIDAS Authentication Service (IdP)</title>
</head>
<body>


<form id="redirectForm" name="redirectForm" method="post" action="${e:forHtml(callback)}">
	<input type="hidden" id="SAMLResponse" name="SAMLResponse" value="${e:forHtml(samlToken)}"/>
	<input type="hidden" id="SMSSPResponse" name="SMSSPResponse" value="${e:forHtml(smsspToken)}"/>


	<button type="submit" id="idpSubmitbutton" class="btn btn-default btn-lg btn-block">Submit</button>
	<input type="hidden" id="username" name="username" value="${e:forHtml(username)}"/>
</form>
<noscript>
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
					<h2>
						<s:i18n name="member_country_specific.idp.bundle">
						<s:text name="IdPRedirect.title"/>
						</s:i18n>
					</h2>
				</div>
				<jsp:include page="leftColumn.jsp"/>
				<div class="col-md-6">
					<form id="redirectFormNoJS" name="redirectForm" method="post" action="${e:forHtml(callback)}">
						<input type="hidden" id="SAMLResponseNoJS" name="SAMLResponse" value="${e:forHtml(samlToken)}"/>
						<input type="text" id="SMSSPResponseNoJS" name="SMSSPResponse" value="${e:forHtml(smsspToken)}"/>
						<input type="hidden" id="usernameNoJS" name="username" value="${e:forHtml(username)}"/>
						<button class="btn btn-default btn-lg btn-block" type="submit">Redirect</button>
					</form>
				</div>
			</div>
		</div>
	</div>
</div>
<jsp:include page="footer.jsp"/>
</noscript>
</body>
<script type="text/javascript" src="resources/js/redirectCallbackOnload.js"></script>
</html>