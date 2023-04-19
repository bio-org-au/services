<div class="rest-resource-content tree-gsp">

  <g:if test="${data.ok == false}">
    <h2>Check All Synonyms Report</h2>

    <h3>Problem.</h3>

    <p>${data.status}: ${data.error}</p>
  </g:if>
  <g:else>
    <h2>Check All Synonyms Report for ${treeVersion.tree.name} ${treeVersion.draftName}</h2>

    <h3>Changed Synonymy <span class="text-muted small">(${data.payload.count})</span></h3>

    <p>Showing ${data.payload.results.size()} results.</p>

    <g:if test="${data.payload.results}">
      <g:form controller="treeElement" action="updateSynonymyByInstance">
        <input type="hidden" name="versionId" value="${treeVersion.id}"/>
        <table class="table">
          <thead>
          <tr>
            <th>Previous synonymy</th>
            <th>Accepted current instance synonymy</th>
          </tr>
          </thead>
          <g:each in="${data.payload.results}" var="report">
            <tree:diffSynonyms a="${report.treeVersionElement.treeElement.synonymsHtml}"
                               b="${report.synonymsHtml}">
              <tr>
                <td class="diffBefore">
                  <div class="text-muted">
                    Updated by ${report.treeVersionElement.updatedBy} <date>${report.treeVersionElement.updatedAt}</date>
                  </div>

                  <div
                      class="tr ${report.treeVersionElement.treeElement.excluded ? 'excluded' : ''} level${report.treeVersionElement.depth}">
                    <div class="wrap">
                      <a href="${report.treeVersionElement.fullElementLink()}"
                         title="link to tree element">${raw(report.treeVersionElement.treeElement.displayHtml)}</a>
                      <a href="${report.treeVersionElement.treeElement.nameLink}/api/apni-format?versionId=${report.treeVersionElement.treeVersionId}&draft=true"
                         title="View name in APNI format.">
                        <i class="fa fa-list-alt see-through"></i>
                      </a>
                      ${raw(diffA)}
                    </div>
                  </div>
                </td>
                <td class="diffAfter">
                  <div class="text-muted">
                    Updated by ?  <a
                      href="${createLink(absolute: true, controller: 'tree', action: 'synonymyOrderingInfo', id: report.treeVersionElement.treeElement.instanceId)}"
                      target="_order" title="get ordering information."><i class="fa fa-sort"></i></a>
                  </div>

                  <div
                      class="tr ${report.treeVersionElement.treeElement.excluded ? 'excluded' : ''} level${report.treeVersionElement.depth}">
                    <div class="wrap">
                      <a href="${report.treeVersionElement.fullElementLink()}"
                         title="link to tree element">${raw(report.treeVersionElement.treeElement.displayHtml)}</a>
                      <a href="${report.treeVersionElement.treeElement.nameLink}/api/apni-format?versionId=${report.treeVersionElement.treeVersionId}&draft=true"
                         title="View name in APNI format.">
                        <i class="fa fa-list-alt see-through"></i>
                      </a>
                      ${raw(diffB)}
                    </div>
                  </div>

                  <div class="form-inline" style="float:right">
                    <label>Select
                      <input type="checkbox" name="instances" class="form-control" value="${report.instanceId}"
                             checked="checked"/>
                    </label>
                  </div>
                </td>
              </tr>
            </tree:diffSynonyms>
          </g:each>
        </table>
        <shiro:hasRole name="treebuilder">
          <g:if test="${treeVersion}">
            <g:submitButton class="btn btn-primary"
                            name="Update selected in draft '${treeVersion.draftName}'"/>
          </g:if>
          <g:else>
            <h2 class="text-warning"><i class="fa fa-exclamation-triangle"></i> Please create a draft tree to update.
            </h2>
          </g:else>
        </shiro:hasRole>
      </g:form>
    </g:if>
    <g:else>
      <p>No changes to synonymy found.</p>
    </g:else>
  </g:else>
</div>
