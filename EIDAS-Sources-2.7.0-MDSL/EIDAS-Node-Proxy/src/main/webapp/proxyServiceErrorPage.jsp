<%--
  ~ Copyright (c) 2022 by European Commission
  ~
  ~ Licensed under the EUPL, Version 1.2 or - as soon they will be
  ~ approved by the European Commission - subsequent versions of the
  ~ EUPL (the "Licence");
  ~ You may not use this work except in compliance with the Licence.
  ~ You may obtain a copy of the Licence at:
  ~ https://joinup.ec.europa.eu/page/eupl-text-11-12
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the Licence is distributed on an "AS IS" basis,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  ~ implied.
  ~ See the Licence for the specific language governing permissions and
  ~ limitations under the Licence.
  --%>

<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<fmt:setBundle basename="eu.eidas.node.package" var="i18n_eng"/>
<fmt:setBundle basename="errors" var="i18n_error"/>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>

<html lang="en">

<head>
    <jsp:include page="internal/htmlHead.jsp"/>
    <title><fmt:message key="consent.page.title" bundle="${i18n_eng}"/></title>
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
                        <fmt:message key="eidas.title" bundle="${i18n_eng}"/>
                        <span class="text-error"><fmt:message key="service.error.title" bundle="${i18n_eng}"/></span>
                    </h1>
                    <c:if test="${!empty errorMessage}">
                        <p class="text-center" id="errorMessage">${e:forHtml(errorMessage)}</p>
                    </c:if>
                    <c:if test="${empty errorMessage}">
                        <h2 class="sub-title">
                            <span id="unexpectedError"><fmt:message key="unexpected.error" bundle="${i18n_error}"/></span>
                        </h2>
                        <h3><fmt:message key="report.error" bundle="${i18n_error}" /></h3>
                    </c:if>
                    <c:if test="${!empty errorId}">
                        <h3 id="errorUserMessage">
                            <fmt:message key="error.user.message" bundle="${i18n_error}">
                                <fmt:param value="${e:forHtml(contactSupportEmail)}"/>
                                <fmt:param value="${errorId}"/>
                            </fmt:message>
                        </h3>
                    </c:if>
                    <c:if test="${empty errorId}">
                        <h2 class="sub-title">
                            <span id="genericErrorMessage"><fmt:message key="unexpected.error" bundle="${i18n_error}"/></span>
                        </h2>
                        <h3><fmt:message key="report.error" bundle="${i18n_error}" /></h3>
                    </c:if>
                    <h2 class="sub-title text-highlight"><fmt:message key="eidas.cooperation" bundle="${i18n_eng}" /></h2>
                </div>
            </div>
        </div>
    </div>
</main>
<script type="text/javascript" src="js/autocompleteOff.js"></script>
</body>
</html>