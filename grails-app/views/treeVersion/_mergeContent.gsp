<%@ page import="au.org.biodiversity.nsl.TveDiff" %>
<div class="rest-resource-content tree-gsp">
  <h2>Merge changes from ${payload.from.tree.name} in to ${payload.to.draftName}</h2>

  <g:if test="${payload.upToDate == true}">
    <h3>Nothing to see here.</h3>

    <p>Draft ${payload.to.draftName} is up to date.</p>
  </g:if>
  <g:else>
    <g:form controller="treeVersion" action="merge">
      <textarea name="changeset" class="hidden">
        ${payload as grails.converters.JSON}
      </textarea>
      <div class="nonConflicting">
        <h3 class="sep">${payload.nonConflicts.size()} Non conflicting changes</h3>

        <g:render template="tveDiff"
                  model="[diffs: payload.nonConflicts, before: payload.to.draftName, after: payload.from.tree.name]"/>

        <input type="hidden" name="draftVersion" value="${payload.to.id}"/>
      </div>

      <div class="conflicting">
        <h3 class="sep">${payload.conflicts.size()} Conflicting changes</h3>

        <g:render template="tveDiff"
                  model="[diffs: payload.conflicts, before: payload.to.draftName, after: payload.from.tree.name]"/>
      </div>

      <shiro:hasRole name="treebuilder">
        <g:submitButton class="btn btn-primary"
                        name="Merge selected in to draft '${payload.to.draftName}'"/>
      </shiro:hasRole>
    </g:form>
  </g:else>
</div>