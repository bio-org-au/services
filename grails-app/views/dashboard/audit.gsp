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
<div class="container-lg">
  <h2>Audit</h2>
  <g:render template="form"/>

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
        <g:set var="diffs" value="${row.fieldDiffs()}"/>
        <g:if test="${ServiceTagLib.shouldDisplay(diffs)}">
        <g:if test="${!row.isUpdateBeforeDelete()}">
          <tr class="editor-${row.sessionUserName}">
            <td>${[U: 'Updated', I: 'Created', D: 'Deleted'].get(row.action)}</td>
            <td>
              <div class="height-${row.sessionUserName}">
                <g:each in="${ServiceTagLib.sortDiffs(row.table, diffs)}" var="diff">
                  <g:if test="${ServiceTagLib.shouldDisplay(row.table, diff.fieldName)}">

                  <div style="overflow: hidden;width: 100%;">
%{--                    <div style="float: left;width:20%;"><b>${diff.fieldName.replaceAll('_id', '').replaceAll('_', ' ')}</b></div>--}%
                    <div style="float: left;width:20%;"><b title="${diff.fieldName}"><st:diffLabel table="${row.table}" field="${diff.fieldName}"/>&nbsp;</b></div>
                    <div class="diffBefore" style="float: left;width: 40%;">
                      <st:diffValue value="${diff.before}"/>
                    </div>
                    <g:if test="${row.action != 'D'}">
                      <div class="diffAfter" style="float: left;width:40%;">
                        <st:diffValue value="${diff.after}"/>
                      </div>
                    </g:if>
                  </div>
                  </g:if>
                </g:each>
              </div>
            </td>
            <td><b>${row.updatedBy()}</b></td>
            <td>${row.when()}</td>
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
          </tr>
        </g:if>
        </g:if>
%{--        <g:else>--}%
%{--          <tr><td colspan="3"> D: ${diffs[0]} :D</td></tr>--}%
%{--        </g:else>--}%
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
