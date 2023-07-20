<%-- 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.slf4j.Logger,org.slf4j.LoggerFactory" %>

<%! private static final Logger LOGGER = LoggerFactory.getLogger("proxyServiceBuildVersion.jsp");%>

<%
    final String SEPARATOR = "-";
    String buildDate = application.getInitParameter("buildDate");
    String server = request.getServerName();
    String projectVersion = application.getInitParameter("projectVersion");

    StringBuilder buildVersion = new StringBuilder();
    buildVersion.append(buildDate).append(SEPARATOR)
            .append(server).append(SEPARATOR)
            .append(projectVersion).append(SEPARATOR);
    LOGGER.info("Build version: {}", buildVersion.toString());
%>

<div hidden="true">Build version: <%= buildVersion.toString()%></div>
