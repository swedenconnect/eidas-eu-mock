<%--
  ~ Copyright (c) 2024 by European Commission
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
                    <fmt:message var="redirectingValue" key="eidas.redirecting" bundle="${i18n_eng}"/>
                    <input type="hidden" id="redirectingMessageId" value="${e:forHtml(redirectingValue)}"/>
                    <h2 id="connectorRedirectLabel" class="sub-title"></h2>
                    <form id="ColleagueResponse" name="redirectForm" action="${e:forHtml(redirectUrl)}" method="post">
                        <input type="hidden" name="SAMLResponse" value="${e:forHtml(samlToken)}" id="ColleagueResponse_SAMLResponse"/>
                        <input type="hidden" name="RelayState" value="${e:forHtml(RelayState)}" id="relayState"/>
                    </form>
                    <noscript>
                        <h2><fmt:message key="APRedirect.text" bundle="${i18n_eng}"/></h2>
                        <br/>
                        <form id="ColleagueResponse_NoJs" name="redirectForm" action="${e:forHtml(redirectUrl)}" method="post">
                            <input type="hidden" name="SAMLResponse" value="${e:forHtml(samlToken)}" id="ColleagueResponse_SAMLResponse"/>
                            <fmt:message var="btnMsg" key="accept.button" bundle="${i18n_eng}"/>
                            <input type="submit" id="ColleagueResponse_0" class="btn btn-next" value="${e:forHtml(btnMsg)}"/>
                            <input type="hidden" name="RelayState" value="${e:forHtml(RelayState)}" id="relayState"/>
                        </form>
                    </noscript>
                </div>
            </div>
        </div>
    </div>
</main>
<script type="text/javascript" src="js/autocompleteOff.js" nonce="${cspScriptNonce}"></script>
<script type="text/javascript" src="js/redirectOnload.js" nonce="${cspScriptNonce}"></script>
</body>
</html>