<%--
  User: pmcneil
  Date: 15/09/14
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main">
  <title>NSL Dashboard</title>
</head>

<body>
<div class="container">

  <g:form name="search" role="form" controller="dashboard" action="audit" method="GET">
    <div class="form-group">
      <h2>Audit</h2>
      <label>User name
        <input type="text" name="userName" placeholder="Enter a user name" value="${query.userName}"
               class="form-control" size="30"/>
      </label>

      <label>From
        <input type="text" name="fromStr" class="form-control fromDate" value="${query.fromStr}">
      </label>
      <label>To
        <input type="text" name="toStr" class="form-control toDate" value="${query.toStr}">
      </label>
      <button type="submit" name="search" value="true" class="btn btn-primary">Search</button>
    </div>
  </g:form>

  <g:if test="${stats && !stats.isEmpty()}">
    <table class="table audit-report">
      <tr>
        <th>Editors</th>
        <g:each in="${stats[stats.keySet()[0]]?.keySet()}" var="thing">
          <th colspan="3">${thing}</th>
        </g:each>
      </tr>
      <tr>
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
               ${thing.value.created}
            </td>
            <td class="data">
              ${thing.value.deleted}
            </td>
            <td class="data">
               ${thing.value.updated}
            </td>
          </g:each>
        </tr>
      </g:each>
    </table>
  </g:if>

  <g:if test="${auditRows == null}">
    <div>
      Your results will be here.
    </div>
  </g:if>
  <g:elseif test="${auditRows.size() > 0}">
    <h2>Audit log
      <span class="small hint" style="display: inline-block">
      <g:if test="${auditRows.size() == 500}">
        <i class="fa fa-circle-thin fa-2x"></i><span style="position: relative; left: -27px; top: -9px; font-size: 8px">500</span>
      </g:if>
      <g:else>
        ${auditRows.size()}
      </g:else>
    </span>
    </h2>
    <table class="table">
      <g:each in="${auditRows}" var="row">
        <g:if test="${!row.isUpdateBeforeDelete()}">
          <tr>
            <td>${row.when()}</td>
            <td><b>${row.updatedBy()}</b></td>
            <td>${[U: 'Updated', I: 'Created', D: 'Deleted'].get(row.action)}</td>
            <td>
              <g:if test="${row.auditedObj}">
                <st:diffValue value="${row.auditedObj}"/>
              </g:if>
              <g:else>
                ${"$row.table $row.rowData.id ${row.action != 'D' ? '(deleted?)' : ''}"}
              </g:else>
            </td>
            <td>
              <g:each in="${row.fieldDiffs()}" var="diff">
                <div>
                  <div><b>${diff.fieldName.replaceAll('_id', '').replaceAll('_', ' ')}</b></div>

                  <div class="diffBefore">
                    <st:diffValue value="${diff.before}"/>
                  </div>
                  <g:if test="${row.action != 'D'}">
                    <div class="diffAfter">
                      <st:diffValue value="${diff.after}"/>
                    </div>
                  </g:if>
                </div>
              </g:each>
            </td>
          </tr>
        </g:if>
      </g:each>
    </table>
  </g:elseif>
  <g:else>
    <div>
      No Results found.
    </div>
  </g:else>

</div>
</body>
</html>