<%--
  User: pmcneil
  Date: 15/09/14
--%>
<%@ page import="services.ServiceTagLib" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main">
    <title>NSL Audit</title>
</head>

<body>
<div class="container">
    <h2>Audit</h2>
    <g:render template="form"/>

    <g:if test="${stats && !stats.isEmpty()}">
        <table class="table audit-report">
            <tr class="stats-h1">
                <th>Last Modifed By</th>
                <g:each in="${stats[stats.keySet()[0]]?.keySet()}" var="thing">
                    <th colspan="3">${thing}</th>
                </g:each>
            </tr>
            <tr class="stats-h2">
                <th></th>
                <g:each in="${stats[stats.keySet()[0]]?.keySet()}" var="thing">
                    <th><i class="fa fa-plus"></i></th>
                    <th><i class="fa fa-minus"></i></th>
                    <th><i class="fa fa-edit"></i></th>
                </g:each>
            </tr>
            <g:each in="${stats.keySet().sort()}" var="user">
                <tr>
                    <td>${user}</td>
                    <g:each in="${stats[user]}" var="thing">
                        <td class="data">
                            ${thing.value.created ?: ''}
                        </td>
                        <td class="data">
                            ${thing.value.deleted ?: ''}
                        </td>
                        <td class="data">
                            ${thing.value.updated ?: ''}
                        </td>
                    </g:each>
                </tr>
            </g:each>
        </table>
    </g:if>
    <g:form>
    <div class="col-md-1">
        <label for="exportStats">
            <g:actionSubmit type="submit" name="exportStats" value="Export" class="btn btn-primary audit-search" action="stats"/>
        </label>
    </div>
    </g:form>
</div>
</body>
</html>
