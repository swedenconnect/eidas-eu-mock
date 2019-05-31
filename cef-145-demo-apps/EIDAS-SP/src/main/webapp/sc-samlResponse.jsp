<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html lang="en">

<head>
	<script src="webjars/jquery/3.3.1-1/jquery.min.js"></script>
	<title><s:property value="%{getText('tituloId')}"/></title>
	<script>
		$(document).ready(function () {
			$('#responseForm').submit();
		});
	</script>
</head>
<body>


<form id="responseForm" name="countrySelector" action="populateReturnPage" target="_parent" method="post">
	<textarea class="form-control" name="SAMLResponse" id="SAMLResponse" rows="8" style="display: none"><s:property value="SAMLResponse"/></textarea>
</form>


</body>
</html>
