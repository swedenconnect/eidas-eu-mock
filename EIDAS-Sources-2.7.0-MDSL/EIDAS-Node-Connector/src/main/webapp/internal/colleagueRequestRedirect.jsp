<%--
  ~ Copyright (c) 2019 by European Commission
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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>

<html>

<head>
    <jsp:include page="htmlHead.jsp"/>
    <title><fmt:message key="eidas.title" bundle="${i18n_eng}"/></title>
</head>
<body>
<main>
    <div class="wrapper">
        <jsp:include page="centralSliderNoAnim.jsp"/>
        <jsp:include page="leftColumnNoAnim.jsp"/>
        <div class="col-right">
            <div class="col-right-inner">
                <div class="col-right-content">
                    <jsp:include page="content-security-header-deactivated.jsp"/>
                    <h1 class="title">
                        <span><fmt:message key="eidas.title" bundle="${i18n_eng}"/></span>
                    </h1>

                    <fmt:message var="redirectingValue" key="colleagueRequestRedirect.text" bundle="${i18n_eng}"/>
                    <input type="hidden" id="redirectingMessageId" value="${e:forHtml(redirectingValue)}"/>
                    <h2 class="display-none" id="redirectLabel" class="sub-title"></h2>

                    <form name="redirectForm" method="${e:forHtml(binding)}" action="${e:forHtml(serviceUrl)}">
                        <input type="hidden" id="SAMLRequest" name="SAMLRequest" value="${e:forHtml(SAMLRequest)}"/>
                        <input type="hidden" id="relayState" name="RelayState" value="${e:forHtml(RelayState)}"/>
                    </form>
                    <noscript>
                        <h2 class="sub-title"><fmt:message key="ConnectorRedirect.text" bundle="${i18n_eng}"/></h2>
                        <form id="redirectFormNoJs" name="redirectFormNoJs" method="post" action="${e:forHtml(serviceUrl)}">
                            <input type="hidden" id="noScriptSAMLRequest" name="SAMLRequest" value="${e:forHtml(SAMLRequest)}"/>
                            <input type="hidden" id="noScriptrelayState" name="RelayState" value="${e:forHtml(RelayState)}"/>
                            <p class="box-btn">
                                <input type="submit" id="noScriptButton" class="btn btn-next" value="<fmt:message key='accept.button' bundle="${i18n_eng}"/>"/>
                            </p>
                        </form>
                    </noscript>
                </div>
            </div>
        </div>
    </div>
</main>
<script type="text/javascript" src="js/autocompleteOff.js"></script>
<script type="text/javascript" src="js/redirectOnload.js"></script>
</body>
</html>
