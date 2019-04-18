<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html lang="en">

<head>
	<jsp:include page="sc-htmlHead.jsp"/>
	<title><s:property value="%{getText('tituloId')}" /></title>
</head>

<body>

<nav class="navbar navbar-expand-lg fixed-top navbar-light">
	<a class="navbar-brand" style="margin-right: 50px" href="https://swedenconnect.se/">
		<img class="logo" src="sc-img/sc-logo.svg" alt="sc_logo">
	</a>

	<button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent"
			aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
		<span class="navbar-toggler-icon"></span>
	</button>

	<div class="collapse navbar-collapse" id="navbarSupportedContent">
		<ul class="navbar-nav ml-auto">
			<li class="nav-item active">
				<a class="nav-link" href="https://swedenconnect.se">Sweden Connect</a>
			</li>
			<li class="nav-item">
				<a class="nav-link" href="https://sandbox.swedenconnect.se/home/index.html">Sandbox</a>
			</li>
		</ul>
	</div>

</nav>
<div class="content-push"></div>

<div class="container">
	<div class="row">
		<br />
		<h1>This is the SP application - Select Attribute</h1>
	</div>
</div>

</body>
</html>
