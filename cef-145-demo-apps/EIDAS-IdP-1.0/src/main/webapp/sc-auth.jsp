<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="java.util.Properties"%>
<%@page import="java.net.URL"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.util.Enumeration"%>
<%@page import="eu.eidas.auth.commons.EidasParameterKeys"%>
<%
	String samlToken = request.getParameter(EidasParameterKeys.SAML_REQUEST.toString());
	String signAssertion = request.getParameter("signAssertion");
	String encryptAssertion = request.getParameter("encryptAssertion");
%>
<html lang="en">

<head>
	<jsp:include page="sc-htmlHead.jsp"/>
	<title>eIDAS Authentication Service (IdP)</title>
	<script src="sc-js/sc-auth.js"></script>


</head>
<body>

<jsp:include page="sc-htmlTitle.jsp"/>

<div class="container main">
	<jsp:include page="sc-langMock.jsp"/>

	<div class="row">
		<div class="col-sm-12 content-container">

			<div class="row">
				<div class="col-sm-12 content-heading">
					<h2 class="article-header">Demo SP - CEF eIDAS node version 1.4.5</h2>
					<h5>Identity Provider</h5>
				</div>
			</div>

			<hr class="full-width"/>
			<br>

			<form id="authenticationForm" name="authentication" method="post" action="Login">
				<div class="form-group">

					<label for="username">Username</label>
					<select class="selectpicker show-menu-arrow" data-width="100%" id="username" name="username">
						<s:iterator value="userInfoList" status="userListStatus">
							<option value="<s:property value="id"/>" data-content="<b><s:property value="displayName"/></b><br/><s:property value="description"/>"></option>
						</s:iterator>
					</select>

				</div>
				<c:if test="${param.messageFormat=='eidas'}">
					<div class="form-group" id="eidasDiv">
						<label for="eidasloa">Level of Assurance</label>
						<select class="selectpicker show-menu-arrow" data-width="100%" name="eidasloa" id="eidasloa" >
							<option value="http://eidas.europa.eu/LoA/low">http://eidas.europa.eu/LoA/low</option>
							<option value="http://eidas.europa.eu/LoA/substantial">http://eidas.europa.eu/LoA/substantial</option>
							<option value="http://eidas.europa.eu/LoA/high">http://eidas.europa.eu/LoA/high</option>
						</select>
					</div>
				</c:if>
				<div class="form-group">
							<span>
								<input class="form-horizontal" id="addIPAddress" type="checkbox" name="ipAddress" checked/>&nbsp;
								<label for="addIPAddress">IP Address for SubjectConfirmationData</label>
							</span>
				</div>
				<input type="hidden" name="samlToken" value="<%=samlToken%>"/>
				<input type="hidden" name="signAssertion" value="<%=signAssertion%>"/>
				<input type="hidden" name="encryptAssertion" value="<%=encryptAssertion%>"/>
				<button type="submit" id="idpSubmitbutton" class="btn btn-default btn-lg btn-block">Submit</button>
			</form>

		</div>
	</div>
</div>

</body>
</html>