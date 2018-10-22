<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<fmt:setBundle basename="eu.eidas.node.package" var="i18n_eng"/>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>
<%@ taglib prefix="token" uri="https://eidas.europa.eu/" %>


<html>

<head>
	<jsp:include page="internal/htmlHead.jsp"/>
	<title><fmt:message key="eidas.title" bundle="${i18n_eng}"/></title>
</head>
<body>
<main>
	<div class="wrapper">
		<jsp:include page="internal/centralSliderNoAnim.jsp"/>
		<jsp:include page="internal/leftColumnNoAnim.jsp"/>
		<div class="col-right">
			<div class="col-right-inner">
				<div class="clearfix">
					<div class="menu-top"> <a class="item text-minus" href="#"></a> <a class="item text-plus" href="#"></a> <a class="item contrast" href="#"></a> </div>
				</div>
				<div class="col-right-content">
					<jsp:include page="internal/content-security-header-deactivated.jsp"/>
					<h1 class="title">
						<span><fmt:message key="eidas.title" bundle="${i18n_eng}"/></span>
					</h1>
					<h2><fmt:message key="country.framing.inactive" bundle="${i18n_eng}"/></h2>
				</div>
			</div>
		</div>
	</div>
</main>
<script type="text/javascript" src="js/autocompleteOff.js"></script>
<script type="text/javascript" src="js/redirectOnload.js"></script>
<jsp:include page="internal/footerScripts.jsp"/>
</body>
</html>