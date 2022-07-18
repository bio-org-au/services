<%--
  User: pmcneil
  Date: 15/09/14
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main">
  <title>NSL Audit</title>
</head>

<body>
<div class="container">
  <h2>Audit</h2>
  <g:form name="search" role="form" controller="dashboard" action="audit" method="GET">
    <div class="form-group">
      <div class="form-row">
        <div class="col-md-3">
          <label>User name
            <input type="text" name="userName" placeholder="Enter a user name" value="${query.userName}"
                  class="form-control" size="30"/>
          </label>
        </div>
        <div class="col-md-2">
          <label>From
            <input type="text" name="fromStr" class="form-control fromDate" value="${query.fromStr}">
          </label>  
        </div>
        <div class="col-md-2">
          <label>To
            <input type="text" name="toStr" class="form-control toDate" value="${query.toStr}">
          </label>  
        </div>

        <div class="col-md-2">
          <label for="filterBy">Show only
          <select name="filterBy" id="filter-by" type="text" name="filterBy" class="form-control filterBy" value="${query.filterBy}">
            <option value="all">All</option>
            <option value="name">Names</option>
            <option value="instance">Instances</option>
            <option value="reference">References</option>
            <option value="author">Authors</option>
          </select>
          </label>
        </div>
        <div class="col-md-2">
          <label for="search">
            <button type="submit" name="search" value="true" class="btn btn-primary audit-search">Search</button>
          </label>
        </div>
        
      </div>
    </div>
  </g:form>

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

  <g:if test="${auditRows == null}">
    <div>
      Your results will be here.
    </div>
  </g:if>
  <g:elseif test="${auditRows.size() > 0}">
    <h2>
      <g:if test="${auditRows.size() < 500}">
        <span class="btn btn-secondary position-relative">
        <strong>Audit log</strong>
        <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-primary">
          ${auditRows.size()}
        </span>
      </g:if>
      <g:else>
        <span class="btn btn-dark position-relative">
        <strong>Audit log</strong>
        <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
          ${auditRows.size()}
        </span>
      </g:else>
    </span>
    </h2>
    <table class="table">
      <g:each in="${auditRows}" var="row">
        <g:if test="${!row.isUpdateBeforeDelete()}">
          <tr class="editor-${row.sessionUserName}">
            <td>${row.when()}</td>
            <td><b>${row.updatedBy()}</b></td>
            <td>${[U: 'Updated', I: 'Created', D: 'Deleted'].get(row.action)}</td>
            <td>
              <div class="height-${row.sessionUserName}">
                <g:if test="${row.auditedObj}">
                  <st:diffValue value="${row.auditedObj}"/>
                </g:if>
                <g:else>
                  ${"$row.table $row.rowData.id ${row.action != 'D' ? '(deleted?)' : ''}"}
                </g:else>
              </div>
            </td>
            <td>
              <div class="height-${row.sessionUserName}">
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
              </div>
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