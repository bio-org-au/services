<div>
  <g:if test="${tve}">
  <div class="text-muted">
    Updated by ${tve.updatedBy} <date>${tve.updatedAt}</date>
  </div>

  <div class="tr ${tve.treeElement.excluded ? 'excluded' : ''} level${tve.depth}">
    <div class="wrap">
      <a href="${tve.fullElementLink()}" title="link to tree element">${raw(tve.treeElement.displayHtml)}</a>
      <a href="${tve.treeElement.nameLink}/api/apni-format?versionId=${tve.treeVersionId}&draft=true"
         title="View name in APNI format.">
        <i class="fa fa-list-alt see-through"></i>
      </a>
      ${raw(syn)}
    </div>
  </div>
  </g:if>
  <g:else>
    [This page left intentionally blank.]
  </g:else>
</div>