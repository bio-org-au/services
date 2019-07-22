<%--
  User: pmcneil
  Date: 16/09/14
--%>
<%@ page contentType="text/html;charset=UTF-8" %>

<div class="panel  ${(params.product == 'APC' ? 'panel-success' : 'panel-info')} ">
  <div class="panel-heading">
    <g:if test="${results}">
      <div class="btn-group hideSearch hidden-print">
        <g:form name="search" role="form" controller="search" action="nameCheck" method="POST" class="closable">
          <input type="hidden" name="name" value="${query.name}">
          <input type="hidden" name="csv" value="csv">
          <input type="hidden" name="max" value="${max}">
          <button type="submit" name="nameCheck" value="true" class="btn btn-primary">Download CSV</button>
        </g:form>
      </div>
    </g:if>
  </div>

  <div class="panel-body">
    <div class="results">
      <g:if test="${results}">
        <table class="table">
          <th>Found?</th>
          <th>Search term</th>
          <th>Census</th>
          <th>Matched name(s)</th>
          <th>Family</th>
          <th>Tags</th>
          <g:each in="${results}" var="result">
            <g:if test="${result.found}">
              <g:set var="first" value="${true}"/>
              <g:each in="${result.names}" var="nameData">
                <tr>
                  <g:if test="${first}">
                    <td>
                      <b>Found</b>
                      <g:if test="${result.names.size() > 1}">
                        <span class="text-muted">${result.names.size()}</span>
                      </g:if>
                    </td>
                    <td>
                      &quot;${result.query}&quot;
                      <g:if test="${result.names.size() == max}"><span
                          class="text-info">Limited to ${max} results</span>
                      </g:if>
                    </td>
                  </g:if>
                  <g:else>
                    <td colspan="2"></td>
                  </g:else>
                  <td>
                    <g:if test="${nameData.treeVersionElement}">
                      <a href="${g.createLink(absolute: true, controller: 'apcFormat', action: 'display', id: nameData.name.id)}">
                        <g:if test="${!nameData.treeVersionElement.treeElement.excluded}">
                          <apc><i class="fa fa-check"></i>${treeName}</apc>
                        </g:if>
                        <g:else>
                          <apc title="excluded from ${treeName}"><i class="fa fa-ban"></i>${treeName} ex.</apc>
                        </g:else>
                      </a>
                    </g:if>
                    <g:else>&nbsp;</g:else>
                  </td>
                  <td>
                    <st:preferredLink target="${nameData.name}" api="api/apniFormat">
                      ${raw(nameData.name.fullNameHtml)}</st:preferredLink><name-status
                      class="${nameData.name.nameStatus.name}">, ${nameData.name.nameStatus.name}</name-status><name-type
                      class="${nameData.name.nameType.name}">, ${nameData.name.nameType.name}</name-type>
                  </td>
                  <td>
                    <st:preferredLink target="${nameData.family}" api="api/apniFormat">
                      ${raw(nameData.family.fullNameHtml)}
                    </st:preferredLink>
                  </td>
                  <td>
                    <g:each in="${nameData.name.tags}" var="tag">
                      <name-tag>${tag.tag.name}<i class="fa fa-tag"></i></name-tag>
                    </g:each>
                    <br>
                  </td>
                </tr>
                <g:set var="first" value="${false}"/>
              </g:each>
            </g:if>
            <g:else>
              <tr>
                <td>
                  <b>Not Found</b>
                </td>
                <td>&quot;${result.query}&quot;</td>
                <td>&nbsp;</td>
                <td>not found</td>
                <td>&nbsp;</td>
              </tr>
            </g:else>
          </g:each>
        </table>
      </g:if>
    </div>
  </div>
</div>
