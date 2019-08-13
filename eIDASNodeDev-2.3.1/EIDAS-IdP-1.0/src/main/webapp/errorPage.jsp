<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isErrorPage="true" %>

<%
    String errorMessageTitle = request.getParameter("errorMessageTitle");
    String errorMessage = request.getParameter("errorMessage");
%>

<html lang="en">

<head>
    <jsp:include page="htmlHead.jsp"/>
    <title>Demo Identity Provider: Secure Identity Across Borders Linked</title>
</head>
<body>

<!--START HEADER-->
<header class="header">
    <div class="container">
        <h1>Demo Identity Provider</h1>
    </div>
</header>
<!--END HEADER-->
<div class="container">
    <div class="row">
        <div class="tab-content">
            <div role="tabpanel" class="tab-pane fade in active" id="tab-02">
                <div class="col-md-12">
                    <h2><%=errorMessageTitle%>
                    </h2>
                    <br/>
                    <h3><%=errorMessage%>
                    </h3>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
