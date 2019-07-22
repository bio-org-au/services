<%@ page import="au.org.biodiversity.nsl.TveDiff" %>
<div class="rest-resource-content tree-gsp">
  <h2>Merge changes from ${data.payload.from.tree.name} in to ${data.payload.to.draftName}</h2>

  <g:if test="${data.payload.upToDate == true}">
    <h3>Nothing to see here.</h3>

    <p>Draft ${data.payload.to.draftName} is up to date.</p>
  </g:if>
  <g:else>
    <g:form controller="treeVersion" action="merge">
      <textarea name="changeset" class="hidden">
        ${data.payload as grails.converters.JSON}
      </textarea>
      <div class="nonConflicting">
        <h3 class="sep">${data.payload.nonConflicts.size()} Non conflicting changes</h3>

        <g:render template="tveDiff"
                  model="[diffs: data.payload.nonConflicts, before: data.payload.to.draftName, after: data.payload.from.tree.name]"/>

        <input type="hidden" name="draftVersion" value="${data.payload.to.id}"/>
      </div>

      <div class="conflicting">
        <h3 class="sep">${data.payload.conflicts.size()} Conflicting changes</h3>

        <g:render template="tveDiff"
                  model="[diffs: data.payload.conflicts, before: data.payload.to.draftName, after: data.payload.from.tree.name]"/>
      </div>

      <shiro:hasRole name="treebuilder">
        <g:submitButton class="btn btn-primary"
                        name="Merge selected in to draft '${data.payload.to.draftName}'"/>
      </shiro:hasRole>
    </g:form>
  </g:else>
</div>