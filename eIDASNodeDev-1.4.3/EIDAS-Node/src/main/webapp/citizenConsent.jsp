<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<fmt:setBundle basename="eu.eidas.node.package" var="i18n_eng"/>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>
<%@ taglib prefix="token" uri="https://eidas.europa.eu/" %>

<html lang="en">

<head>
	<jsp:include page="internal/htmlHead.jsp"/>
	<title><fmt:message key="consent.page.title" bundle="${i18n_eng}"/></title>
	<script type="text/javascript" src="js/base64.js"></script>
</head>
<% /* Page displayed when going back from IDP-AP, after user's consent, will redirect to Connector.ColleagueResponse */%>
<body>
<main>
	<div class="wrapper">
		<jsp:include page="internal/centralSlider.jsp"/>
		<jsp:include page="internal/leftColumn.jsp"/>
		<div class="col-right">
			<div class="col-right-inner">
				<div class="clearfix">
					<div class="menu-top"> <a class="item text-minus" href="#"></a> <a class="item text-plus" href="#"></a> <a class="item contrast" href="#"></a> </div>
				</div>
				<div class="col-right-content">
					<jsp:include page="internal/content-security-header-deactivated.jsp"/>
					<form id="consentSelector" name="consentSelector" method="post" action="${e:forHtml(redirectUrl)}">
						<jsp:include page="internal/titleWithAssurance.jsp"/>
						<p id="stepstatusjs" name="stepstatusjs" class="step-status"><fmt:message key="common.step" bundle="${i18n_eng}"/> <span>3</span> | 3</p>
						<h2 class="sub-title"><fmt:message key="citizenConsent.resume" bundle="${i18n_eng}"/></h2>
						<div class="row"><% /** Mandatory attributes are here */ %>
							<% /** EIDAS */ %>
							<c:if test="${eidasAttributes}">
								<div class="col-sm-6"> <% /** Natural person */ %>
									<c:set var="categoryIsDisplayed" value="false"/>
									<c:forEach items="${pal}" var="palItemVar">
										<c:set var="personType" value="${palItemVar.key.personType}" />
										<c:set var="friendlyName" value="${palItemVar.key.friendlyName}" />
										<c:set var="attrValue" value="${palItemVar.value}" />
										<c:if test="${!fn:startsWith(fn:toLowerCase(displayAttr), 'false') && fn:startsWith(fn:toLowerCase(personType), 'natural_person')}">
											<c:if test="${categoryIsDisplayed=='false'}">
												<h3><fmt:message key="citizenConsent.natural" bundle="${i18n_eng}"/>
													<span><fmt:message key="citizenConsent.person" bundle="${i18n_eng}"/></span>
												</h3>
												<c:set var="categoryIsDisplayed" value="true"/>
												<ul class="resume list-unstyled">
											</c:if>
											<li>
												<fmt:message key="${friendlyName}" bundle="${i18n_eng}"/>
												<strong>
													<c:forEach items="${attrValue}" var="aVal">
														${e:forHtml(aVal)}
													</c:forEach>
												</strong>
											</li>
										 </c:if>
									</c:forEach>
									<c:if test="${categoryIsDisplayed=='true'}"></ul></c:if>
								</div>
								<div class="col-sm-6"> <% /** Legal person */ %>
									<c:set var="categoryIsDisplayed" value="false"/>
									<c:forEach items="${pal}" var="palItemVar">
										<c:set var="personType" value="${palItemVar.key.personType}" />
										<c:set var="friendlyName" value="${palItemVar.key.friendlyName}" />
										<c:set var="attrValue" value="${palItemVar.value}" />
									<c:if test="${!fn:startsWith(fn:toLowerCase(displayAttr), 'false') && fn:startsWith(fn:toLowerCase(personType), 'legal_person')}">
											<c:if test="${categoryIsDisplayed=='false'}">
												<h3><fmt:message key="citizenConsent.legal" bundle="${i18n_eng}"/>
													<span><fmt:message key="citizenConsent.person" bundle="${i18n_eng}"/></span>
												</h3>
												<c:set var="categoryIsDisplayed" value="true"/>
												<ul class="resume list-unstyled">
											</c:if>
											<li>
												<fmt:message key="${friendlyName}" bundle="${i18n_eng}"/>
												<strong>
													<c:forEach items="${attrValue}" var="aVal">
														${e:forHtml(aVal)}
													</c:forEach>
												</strong>
											</li>
										</c:if>
									</c:forEach>
									<c:if test="${categoryIsDisplayed=='true'}"></ul></c:if>
								</div>
								<div class="col-sm-6"> <% /** Representative person */ %>
									<c:set var="categoryIsDisplayed" value="false"/>
									<c:forEach items="${pal}" var="palItemVar">
									<c:set var="personType" value="${palItemVar.key.personType}" />
									<c:set var="friendlyName" value="${palItemVar.key.friendlyName}" />
									<c:set var="attrValue" value="${palItemVar.value}" />
									<c:if test="${!fn:startsWith(fn:toLowerCase(displayAttr), 'false') && fn:startsWith(fn:toLowerCase(personType), 'repv')}">
									<c:if test="${categoryIsDisplayed=='false'}">
									<h3><fmt:message key="citizenConsent.representative" bundle="${i18n_eng}"/>
										<span><fmt:message key="citizenConsent.person" bundle="${i18n_eng}"/></span>
									</h3>
									<c:set var="categoryIsDisplayed" value="true"/>
									<ul class="resume list-unstyled">
										</c:if>
										<li>
											<fmt:message key="${fn:replace(friendlyName,'Representative','')}" bundle="${i18n_eng}"/>
											<strong>
												<c:forEach items="${attrValue}" var="aVal">
													${e:forHtml(aVal)}
												</c:forEach>
											</strong>
										</li>
										</c:if>
										</c:forEach>
										<c:if test="${categoryIsDisplayed=='true'}"></ul></c:if>
								</div>
							</c:if>
						</div>
						<input type="hidden" id="SAMLResponse" name="SAMLResponse" value="${e:forHtml(samlToken)}"/>
						<input type="hidden" id="relayState" name="RelayState" value="${e:forHtml(RelayState)}"/>
						<noscript>
							<p class="step-status"><fmt:message key="common.step" bundle="${i18n_eng}"/> <span>3</span> | 3</p>
							<p class="box-btn">
								<button type="submit" id="buttonNextNoScript" class="btn btn-next btn-submit"><span><fmt:message key="common.submit" bundle="${i18n_eng}"/></span></button>
							</p>
						</noscript>
					</form>
					<form id="cancelForm" name="cancelForm" method="post" action="${e:forHtml(redirectUrl)}">
						<input type="hidden" id="SAMLResponse" name="SAMLResponse" value="${e:forHtml(samlTokenFail)}"/>
						<c:if test="RelayState!=null">
							<input type="hidden" id="relayState" name="RelayState" value="${e:forHtml(RelayState)}"/>
						</c:if>
						<noscript>
							<p class="box-btn">
								<button type="submit" id="buttonCancelNoScript" class="btn btn-cancel"><span><fmt:message key="common.cancel" bundle="${i18n_eng}"/></span></button>
							</p>
						</noscript>
					</form>
					<p id="buttongroupjsjs" name="buttongroupjsjs" class="box-btn jsOK">
						<button type="button" id="buttonCancel" class="btn btn-opposite"><span><fmt:message key="common.cancel" bundle="${i18n_eng}"/></span></button>
						<button type="button" id="buttonNext" class="btn btn-next btn-submit"><span><fmt:message key="common.submit" bundle="${i18n_eng}"/></span></button>
					</p>
					<jsp:include page="internal/footer-img.jsp"/>
				</div>
			</div>
		</div>
	</div>
</main>
<jsp:include page="helpPages/modal_loa.jsp"/>
<jsp:include page="internal/footerScripts.jsp"/>
<script type="text/javascript" src="js/citizenConsent.js"></script>
<script type="text/javascript" src="js/autocompleteOff.js"></script>
</body>
</html>
