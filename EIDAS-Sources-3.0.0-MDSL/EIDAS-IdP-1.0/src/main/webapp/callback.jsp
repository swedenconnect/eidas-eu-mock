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
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="e" uri="owasp.encoder.jakarta" %>

<html lang="en">

<head>
    <jsp:include page="htmlHead.jsp"/>
    <title>eIDAS Authentication Service (IdP)</title>
</head>
<body>


<form id="redirectForm" name="redirectForm" method="post" action="${e:forHtml(callback)}">
    <input type="hidden" id="SAMLResponse" name="SAMLResponse" value="${e:forHtml(samlToken)}"/>
    <input type="hidden" id="SMSSPResponse" name="SMSSPResponse" value="${e:forHtml(smsspToken)}"/>


    <button type="submit" id="idpSubmitbutton" class="btn btn-default btn-lg btn-block">Submit</button>
    <input type="hidden" id="username" name="username" value="${e:forHtml(username)}"/>
</form>
<noscript>
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
                        <h2>
                            <s:i18n name="member_country_specific.idp.bundle">
                                <s:text name="IdPRedirect.title"/>
                            </s:i18n>
                        </h2>
                    </div>
                    <jsp:include page="leftColumn.jsp"/>
                    <div class="col-md-6">
                        <form id="redirectFormNoJS" name="redirectForm" method="post" action="${e:forHtml(callback)}">
                            <input type="hidden" id="SAMLResponseNoJS" name="SAMLResponse"
                                   value="${e:forHtml(samlToken)}"/>
                            <input type="text" id="SMSSPResponseNoJS" name="SMSSPResponse"
                                   value="${e:forHtml(smsspToken)}"/>
                            <input type="hidden" id="usernameNoJS" name="username" value="${e:forHtml(username)}"/>
                            <button class="btn btn-default btn-lg btn-block" type="submit">Redirect</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <jsp:include page="footer.jsp"/>
</noscript>
</body>
<script type="text/javascript" src="resource/js/redirectCallbackOnload.js"></script>
</html>