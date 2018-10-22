<%@ page pageEncoding="utf-8" contentType="text/html; charset=utf-8"%>
<%@taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <meta http-equiv='pragma' content='no-cache'/>
    <meta http-equiv='cache-control' content='no-cache, no-store, must-revalidate'/>
	<meta http-equiv="Expires" content="-1"/>
</head>
<body>
<form action="${e:forHtml(moaConfigData.moaAuthURI)}" id="moa" name="moa" method="post">
    <input type="hidden" name="show" value="false" />
    <input type="hidden" name="OA" value="${e:forHtml(moaConfigData.oaURI)}" id="OAOnline"/>
    <c:if test="${bku == 'online'}">
        <input type="hidden" value="${e:forHtml(moaConfigData.bkuOnlineURI)}" id="bkuURI" name="bkuURI" />
        <input type="hidden" value="${e:forHtml(moaConfigData.templateOnlineURI)}" id="Template" name="Template" />
    </c:if>
    <c:if test="${bku != 'online'}">
        <input type="hidden" value="${e:forHtml(moaConfigData.bkuMobileURI)}" id="bkuURI" name="bkuURI" />
        <input type="hidden" value="${e:forHtml(moaConfigData.templateMobileURI)}" id="Template" name="Template" />
    </c:if>

    <input size="400" name="bkuButton" value="SUBMIT" class="sendButton" type="submit">
</form>
</body>
<script src="js/redirectMoaOnLoad.js"></script>
</html>