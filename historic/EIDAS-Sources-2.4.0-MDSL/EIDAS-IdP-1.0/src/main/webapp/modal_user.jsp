<%--
  ~ Copyright (c) 2017 by European Commission
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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.*" %>
<%@ page import="member_country_specific.idp.IDPUtil" %>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    Map<String, String> userMap = new HashMap<String, String>();
    Properties configs = IDPUtil.loadConfigs("user.properties");
    Set<String> keys = configs.stringPropertyNames();
    for (String key : keys) {
        if (!key.contains(".")) {
            userMap.put(key, configs.getProperty(key));
        }
    }
%>
<!-- Start Modal -->
<div class="modal fade" id="loaModal" tabindex="-1" role="dialog" aria-labelledby="ModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            </div>
            <div class="modal-body">
                <h3>Test Users</h3>
                <div>
                    <table style="width:100%">
                        <tr>
                            <th>Username</th>
                            <th>Password</th>
                            <th>Description</th>
                        </tr>
                        <c:forEach items="<%=userMap%>" var="entry">
                            <c:set var="username" value="${entry.key}"/>
                            <tr>
                                <td><c:out value="${entry.key}"/></td>
                                <td><c:out value="${entry.value}"/></td>
                                <%
                                    String username = (String)pageContext.getAttribute("username");
                                    String description  = configs.getProperty(username + ".description");
                                %>
                                <td><c:out value="<%=description%>"/></td>
                            </tr>
                        </c:forEach>
                    </table>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>
<!-- End Modal -->