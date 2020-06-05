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
					<h2>AUTHENTICATION
					</h2>
				</div>
				<jsp:include page="leftColumn.jsp"/>
				<div class="col-md-6">
					<form id="authenticationForm" name="authentication" method="post" action="Login">
						<div class="form-group">
							<label for="username">Username</label>
							<span>
								<button type="button" class="btn btn-info" data-toggle="modal" data-target="#loaModal">Test user</button>
							</span>
							<input type="text" class="form-control" id="username" name="username"/>
						</div>
						<div class="form-group">
							<label for="password">Password</label>
							<input type="password" class="form-control" name="password" id="password" placeholder="Password"/>
						</div>
						<c:if test="${param.messageFormat=='eidas'}">
							<div class="form-group" id="eidasDiv">
								<label for="eidasloa">Level of Assurance</label>
								<select class="form-control" name="eidasloa" id="eidasloa" >
									<option value="http://eidas.europa.eu/LoA/low">
										http://eidas.europa.eu/LoA/low</option>
									<option value="http://eidas.europa.eu/LoA/substantial">
										http://eidas.europa.eu/LoA/substantial</option>
									<option value="http://eidas.europa.eu/LoA/high">
										http://eidas.europa.eu/LoA/high</option>
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
	</div>
</div>
<jsp:include page="footer.jsp"/>
<jsp:include page="modal_user.jsp"/>
</body>
</html>