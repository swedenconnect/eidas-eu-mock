<%--
  ~ Copyright (c) 2015 by European Commission
  ~
  ~ Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
  ~ the European Commission - subsequent versions of the EUPL (the "Licence");
  ~ You may not use this work except in compliance with the Licence.
  ~ You may obtain a copy of the Licence at:
  ~ http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the Licence is distributed on an "AS IS" basis,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the Licence for the specific language governing permissions and
  ~ limitations under the Licence.
  ~
  ~ This product combines work with different licenses. See the "NOTICE" text
  ~ file for details on the various modules and licenses.
  ~ The "NOTICE" text file is part of the distribution. Any derivative works
  ~ that you distribute must include a readable copy of the "NOTICE" text file.
  ~
  --%>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<fmt:setBundle basename="eu.eidas.node.package" var="i18n_eng"/>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>
<%@ taglib prefix="token" uri="https://eidas.europa.eu/" %>

<h1 class="title">
    <c:out value="${e:forHtml(spId)}"/>
    <c:if test="${eidasAttributes}">
                                    <span>
                                        <fmt:message key="presentConsent.levelOfAssurance" bundle="${i18n_eng}"/>
                                        <span>
                                            <c:out value="${e:forHtml(LoA)}"/>
                                            <button type="button" class="btn btn-info" data-toggle="modal" data-target="#loaModal">?</button>
                                        </span>
                                    </span>
    </c:if>
    <c:if test="${!eidasAttributes}">
                                    <span><fmt:message key="presentConsent.qualityOfAssurance" bundle="${i18n_eng}"/>
                                    <c:out value="${e:forHtml(qaaLevel)}"/></span>
    </c:if>
    <fmt:message key="presentConsent.isRequesting" bundle="${i18n_eng}"/>
</h1>