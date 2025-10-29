<%@ page import="au.org.biodiversity.nsl.TveDiff" %>
%{--NSL-5608--}%
%{--We will not remove the code BUT I would like it clear that the functionality is on hold until--}%
%{--we can find a use case for it. At this stage it can stay in the code base.--}%
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

    </g:form>
  </g:else>
</div>
