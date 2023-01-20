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
                <th>Last Modified By</th>
                <g:set var="totals" value="${new LinkedHashMap<String,HashMap<String,Long>>()}"/>
                <g:each in="${stats[stats.keySet()[0]]?.keySet()}" var="recType">
                    <% totals[recType] = [created: 0L, updated: 0L, deleted: 0L] %>
                    <th colspan="3">${recType}</th>
                </g:each>
            </tr>
            <tr class="stats-h2">
                <th></th>
                <g:each in="${stats[stats.keySet()[0]]?.keySet()}" var="thing">
                    <th><i class="fa fa-plus" title="Created"></i></th>
                    <th><i class="fa fa-edit" title="Updated"></i></th>
                    <th><i class="fa fa-minus" title="Deleted"></i></th>
                </g:each>
            </tr>
            <g:each in="${stats.keySet().sort()}" var="user">
                <tr>
                    <td>${user}</td>
                    <g:each in="${stats[user]}" var="thing">
                        <% totals[thing.key]['created'] = totals[thing.key]['created'] + (thing.value.created ?: 0L) %>
                        <% totals[thing.key]['updated'] = totals[thing.key]['updated'] + (thing.value.updated ?: 0L) %>
                        <% totals[thing.key]['deleted'] = totals[thing.key]['deleted'] + (thing.value.deleted ?: 0L) %>
                        <td class="data">
                            ${thing.value.created ?: ''}
                        </td>
                        <td class="data">
                            ${thing.value.updated ?: ''}
                        </td>
                        <td class="data">
                            ${thing.value.deleted ?: ''}
                        </td>
                    </g:each>
                </tr>
            </g:each>
            <tr>
                <td>TOTAL</td>
                <g:each in="${totals.keySet()}" var="recType">
                    <td>
                        ${totals[recType]['created']}
                    </td>
                    <td>
                        ${totals[recType]['updated']}
                    </td>
                    <td>
                        ${totals[recType]['deleted']}
                    </td>
                </g:each>
            </tr>
        </table>
    </g:if>
    <g:form>
    <div class="col-md-1">
        <label for="exportStats">
            <g:actionSubmit type="submit" name="exportStats" value="Export" class="btn btn-primary audit-search" action="exportStats"/>
        </label>
    </div>
    </g:form>
</div>
</body>
</html>
