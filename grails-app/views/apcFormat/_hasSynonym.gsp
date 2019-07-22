<div>
  <af:apcSortedInstances instances="${instances.findAll { !it.instanceType.misapplied && it.instanceType.synonym }}" var="synonym">
    <has-synonym>
      <g:if test="${synonym.instanceType.doubtful}">?</g:if>
      <st:preferredLink target="${synonym.name}" api="api/apni-format">${raw(synonym.name.fullNameHtml)}</st:preferredLink>
      <st:preferredLink target="${synonym}"><i title="Use in reference" class="fa fa-book"></i></st:preferredLink>
      <name-status class="${synonym.name.nameStatus.name}">${synonym.name.nameStatus.name}</name-status>

      <g:if test="${synonym.instanceType.proParte}">p.p.</g:if>
    </has-synonym>
  </af:apcSortedInstances>
</div>

<div>
  <af:apcSortedInstances instances="${instances.findAll { it.instanceType.misapplied }}" var="synonym">
    <has-synonym class="${synonym.instanceType.misapplied ? 'misapplied' : ''}">
      <g:if test="${synonym.instanceType.doubtful}">?</g:if>
      <st:preferredLink target="${synonym.name}" api="api/apni-format">
        ${raw(synonym.name.fullNameHtml)}
      </st:preferredLink>
      <st:preferredLink target="${synonym}"><i title="Use in reference" class="fa fa-book"></i></st:preferredLink>

      <g:if test="${synonym.instanceType.misapplied}">
        auct. non <af:author name="${synonym.name}"/>:
      </g:if>
      <g:else>
        <name-status class="${synonym.name.nameStatus.name}">${synonym.name.nameStatus.name}</name-status>
        sensu
      </g:else>

      <g:if test="${synonym.cites}">
        ${raw(synonym.cites.reference.citationHtml)}: ${synonym?.cites?.page ?: '-'}<g:if
          test="${synonym.instanceType.proParte}">, p.p.</g:if>
      </g:if>
      <g:else>
      %{--unsourced reference--}%
        <g:if test="${synonym.instanceType.proParte}">p.p.</g:if>
      </g:else>

    </has-synonym>
  </af:apcSortedInstances>
</div>
