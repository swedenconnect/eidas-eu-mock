<%@ page import="eu.eidas.auth.commons.EidasParameterKeys" %>
<%@ page import="eu.eidas.auth.commons.EidasStringUtil" %>
<%@ page import="member_country_specific.idp.ApplicationSpecificIDPException" %>
<%@ page import="member_country_specific.idp.ProcessLogin" %>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page errorPage="/errorPage.jsp" %>
<%
    boolean checkBoxIpAddress = "on".equalsIgnoreCase(request.getParameter("checkBoxIpAddress"));
    String eidasnameid = null;
    if ("on".equalsIgnoreCase(request.getParameter("checkBoxNameId")))
        eidasnameid = request.getParameter("eidasnameid");

    String ipAddress = request.getRemoteAddr();
    if (request.getHeader(EidasParameterKeys.X_FORWARDED_FOR.toString()) != null) {
        ipAddress = request.getHeader(EidasParameterKeys.X_FORWARDED_FOR.toString());
    }

    String doNotmodifyTheResponse = request.getParameter("doNotmodifyTheResponse");
    String jSonRequestDecoded = request.getParameter("jSonRequestDecoded");
    String smsspToken = request.getParameter("smsspToken");
    String eidasloa = request.getParameter("eidasloa");
    String jSonResponseDecoded = null;
    String username = null;
    String password = null;
    String callback = request.getParameter("callback");
    String jSonResponseEncoded = null;
    String errorMessageTitle = null;
    String errorMessage = null;
    try {

        if (jSonRequestDecoded != null && !jSonRequestDecoded.equals("")) {
            username = request.getParameter("username");
            password = request.getParameter("password");
            ProcessLogin processLogin = new ProcessLogin();

            jSonResponseEncoded = processLogin.checkAuthentication(username, password, smsspToken);
            if (jSonResponseEncoded == null)
                jSonResponseEncoded = processLogin.generateDummyJsonResponse(jSonRequestDecoded, username, eidasloa, checkBoxIpAddress ? ipAddress : null, eidasnameid);
            jSonResponseDecoded = new String(EidasStringUtil.decodeBytesFromBase64(jSonResponseEncoded));
        }
    } catch (ApplicationSpecificIDPException ex) {
        errorMessageTitle = ex.getTitle();
        errorMessage = ex.getMessage();
    }
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
<main>
<div class="container">
    <div class="row">
        <div class="tab-content">
            <div role="tabpanel" class="tab-pane fade in active" id="tab-02">
                <div class="col-md-12">
                    <h2>Response
                    </h2>
                </div>
                <jsp:include page="leftColumn.jsp"/>
                <div class="col-md-6">
                    <form id="genericForm" name="genericForm">

                        <input type="hidden" id="errorMessage" name="errorMessage" value="<%=errorMessage%>"/>
                        <input type="hidden" id="errorMessageTitle" name="errorMessageTitle"
                               value="<%=errorMessageTitle%>"/>
                    </form>

                    <form id="redirectForm" name="redirectForm" action="<%=callback%>" method="post">
                        <div class="form-group">
                            <label for="jSonResponseDecoded">SmsspToken Response</label>
                            <textarea name="jSonResponseDecoded" id="jSonResponseDecoded" class="form-control"
                                      rows="10"><%=jSonResponseDecoded%></textarea>
                            <input type="hidden" id="doNotmodifyTheResponse" name="doNotmodifyTheResponse"
                                   value="<%=doNotmodifyTheResponse%>"/>

                            <input type="hidden" id="SMSSPResponse" name="SMSSPResponse"/>
                        </div>
                        <item type="button" style='display:none;' id="idpSubmitbutton"
                              class="btn btn-default btn-lg btn-block" onclick="return base64_encode();">Submit
                        </item>
                    </form>
                    <noscript>
                        <form id="noJavaScriptForm" name="noJavaScriptRedirectForm" action="<%=callback%>"
                              method="post">
                            <input type="hidden" id="SMSSPResponseNoJS" name="SMSSPResponse"
                                   value="<%=jSonResponseEncoded%>"/>
                            <input type="submit" id="submitButton1" class="btn btn-next" value="Submit"/>
                        </form>
                    </noscript>
                </div>
            </div>
        </div>
    </div>
</div>
</main>
<script type="text/javascript" src="resources/js/redirectCallbackOnload.js"></script>
</body>
</html>