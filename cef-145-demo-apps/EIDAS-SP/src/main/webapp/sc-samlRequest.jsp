<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html lang="en">

<head>
    <script src="webjars/jquery/3.3.1-1/jquery.min.js"></script>
    <title><s:property value="%{getText('tituloId')}"/></title>
    <script>
        $(document).ready(function () {
            $('#countrySelector').submit();
        });
    </script>
</head>
<body>

<form id="countrySelector" name="countrySelector" action="<s:property value="defaultActionUrl" />" target="_parent" method="post">
    <input type="hidden" id="postLocationUrl" name="postLocationUrl" value="<s:property value="postActionUrl" />">
    <input type="hidden" id="redirectLocationUrl" name="redirectLocationUrl" value="<s:property value="redirectActionUrl" />">
    <input type="hidden" name="country" value="<s:property value="citizen"/>" id="country" class="form-control"/>
    <input type="hidden" id="relayState" name="RelayState" value="MyRelayState" class="form-control"/>
    <textarea class="form-control" rows="8" id="SAMLRequest" name="SAMLRequest" style="display: none"><s:property value="samlRequest"/></textarea>
</form>
</body>
</html>
