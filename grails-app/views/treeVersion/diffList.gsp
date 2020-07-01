<!DOCTYPE html>
<html>
<head>
  <meta name="layout" content="main">
  <title>Tree - diff</title>
  <asset:stylesheet src="tree.css"/>

</head>

<body>

<div class="rest-resource-content tree-gsp">
  <h2>List Changes to ${payload.v1.tree.name} in ${payload.v2.draftName}</h2>

  <g:if test="${payload.changed == false}">
    <h3>Nothing to see here.</h3>

    <p>We have no changes, nothing, zip.</p>
  </g:if>
  <g:elseif test="${payload.overflow}">
    <h1>Too many changes</h1>

    <p>We have changes, so many changes.</p>
  </g:elseif>
  <g:else>

    <div class="indented indent0">
      <tree:withDiffList changeSet="${payload.changeSet}">
        <g:if test="${family != null}">
          <div class="family">
          ${raw(family?.treeElement?.displayHtml)}
          </div>
        </g:if>
        <g:if test="${higherRank}">
          <div>${tve.namePath}</div>
        </g:if>
        <div class="tr ${tve.treeElement.excluded ? 'excluded' : ''} level${depth} ${action}"
        title="${action}">
          <div class="wrap">
            <a href="${g.createLink(action: 'diffElement', params: [e1: tve.elementLink, e2: prev?.elementLink])}">${raw(tve.treeElement.displayHtml)}</a>
            <a href="${tve.treeElement.nameLink}/api/apni-format" title="View name in APNI format.">
              <i class="fa fa-list-alt see-through"></i>
            </a>
          </div>
        </div>
      </tree:withDiffList>
    </div>

  </g:else>
</div>
</body>
</html>