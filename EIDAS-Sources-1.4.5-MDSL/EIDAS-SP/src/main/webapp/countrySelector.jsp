<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html lang="en">

<head>
	<jsp:include page="htmlHead.jsp"/>
	<title><s:property value="%{getText('tituloId')}" /></title>
</head>

<body>

<!--START HEADER-->
<header class="header">
	<div class="container">
		<h1><s:property value="%{getText('tituloCabeceraId')}"/></h1>
	</div>
</header>
<!--END HEADER-->

<div class="container">
	<div class="row">
		<div class="tab-content">
			<div role="tabpanel" class="tab-pane fade in active" id="tab-02">
				<div class="col-md-12">
					<h2><s:property value="%{providerName}"/></h2>
				</div>
				<jsp:include page="leftColumn.jsp"/>
				<div class="col-md-6">

					<%

						String providerName = (String) request.getAttribute("providerName");
						String spId = (String) request.getAttribute("spId");
						String spUrl= (String) request.getAttribute("spUrl");
						String spQaaLevel = (String) request.getAttribute("spQaaLevel");
						String nodeCountryForm = (String) request.getAttribute("nodeCountryForm");
						String attrList = (String) request.getAttribute("attrList");
						String spSector = (String) request.getAttribute("spSector");
						String spInstitution = (String) request.getAttribute("spInstitution");
						String spApplication = (String) request.getAttribute("spApplication");
						String spCountry = (String) request.getAttribute("serviceProviderCountryCode");
						String spMetadataUrl = (String) request.getAttribute("spmetadataurl");
						String enc="UTF-8";

					%>
					<div class="form-group">
						<label><s:property value="%{getText('selectCountryId')}" /></label>
					</div>
					<iframe
							src="<%=nodeCountryForm%>?spId=<%=spId%>&providerName=<%=providerName%>&spUrl=<%=spUrl%>&spQaaLevel=<%=spQaaLevel%>&attrList=<%=java.net.URLEncoder.encode(attrList, enc)%>&spSector=<%=spSector%>&spInstitution=<%=spInstitution%>&spApplication=<%=spApplication%>&spCountry=<%=spCountry%>&spmetadataurl=<%=spMetadataUrl%>"
							width="100%" height="250px" style="border: 0px;"></iframe>
				</div>
			</div>
		</div>
	</div>
</div>

<jsp:include page="footer.jsp"/>
</body>
</html>
