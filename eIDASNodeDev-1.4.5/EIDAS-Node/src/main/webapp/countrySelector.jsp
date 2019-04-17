<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<fmt:setBundle basename="eu.eidas.node.package" var="i18n_eng"/>
<%@taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>
    <meta http-equiv='pragma' content='no-cache'/>
    <meta http-equiv='cache-control' content='no-cache, no-store, must-revalidate'/>
	<meta http-equiv="Expires" content="-1"/>

<form id="countrySelector" method="post" action="${e:forHtml(eidasAuth)}" target="_parent">
    <input type="hidden" id="SAMLRequest" name="SAMLRequest" value="<c:out value='${e:forHtml(SAMLRequest)}'/>"/>
	<input type="hidden" id="spmetadataurl" name="spmetadataurl" value="<c:out value='${e:forHtml(spmetadataurl)}'/>"/>
    <select name="country">
        <c:forEach items="${countries}" var="country">
            <option value="${e:forHtml(country.countryId)}">${e:forHtml(country.countryName)}</option>
        </c:forEach>
    </select>
    <input type="submit" id="submit_button" value="<fmt:message key='submit.button' bundle="${i18n_eng}"/>"/>
</form>

<jsp:include page="internal/content-security-header-deactivated.jsp"/>
<script type="text/javascript" src="js/autocompleteOff.js"></script>