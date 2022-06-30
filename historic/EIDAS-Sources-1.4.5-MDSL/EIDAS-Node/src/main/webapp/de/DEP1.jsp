<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv='pragma' content='no-cache'/>
    <meta http-equiv='cache-control' content='no-cache, no-store, must-revalidate'/>
	<meta http-equiv="Expires" content="-1"/>
</head>
<c:if test="${empty deSessionId}">
    <script language="JavaScript">
        alert("null deSessionId");
    </script>
</c:if>

<body onload="document.forms[0].submit()">
<form action="${deTargetURL}" method="post">

    <!--input type="hidden" name="deTargetURL" value="${deTargetURL}" /-->
    <input type="hidden" name="SAMLRequest" value="${deSamlRequest}" />
    <c:if test="${not empty deSessionId}">
        <input type="hidden" name="RelayState" value="${deSessionId}" />
    </c:if>
    <noscript>
        <p>
            <strong>Note:</strong> Since your browser does not support
            JavaScript, you must press the Continue button once to proceed.
        </p>
        <input type="submit" value="Continue" />
    </noscript>

</form>
</body>
</html>