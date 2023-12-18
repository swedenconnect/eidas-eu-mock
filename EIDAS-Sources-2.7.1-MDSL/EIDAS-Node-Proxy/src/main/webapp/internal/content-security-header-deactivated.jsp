<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
  ~ Copyright (c) 2023 by European Commission
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

<fmt:setBundle basename="eu.eidas.node.package" var="i18n_eng"/>
<%/**
 * This message is displayed to the user when the CSP is not active, the content-security-policy directive when supported will indicates the browser to not execute
 * embedded javascript.
 */%>

<c:if test="configurationSecurityBean.isCspFallbackCheck()">
    <h1 id="cspMessage" class="title text-error"></h1>
    <script type="text/javascript" src="js/testCSP.js" nonce="${cspScriptNonce}"></script>
</c:if>