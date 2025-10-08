<!DOCTYPE html>
<html>
<head>
  <meta name="layout" content="main">
  <title>Tree Element ${treeVersionElement.elementLink}</title>
  <asset:stylesheet src="tree.css"/>

</head>

<body>

<g:set var="currentTreeVersion" value="${treeVersionElement.treeVersion.tree.currentTreeVersion}"/>

<div>
  <h1>
    <a href="${treeVersionElement.fullElementLink()}"><i class="fa fa-link"></i></a>
    <g:if test="${treeVersionElement.treeElement.excluded}">
      <apc title="excluded from ${treeVersionElement.treeVersion.tree.name}"><i
          class="fa fa-ban"></i> ${treeVersionElement.treeVersion.tree.name}</apc>: ${treeVersionElement.treeElement.simpleName}
    </g:if>
    <g:else>
      ${treeVersionElement.treeVersion.tree.name}: ${treeVersionElement.treeElement.simpleName}
    </g:else>

    <help>
      <i class="fa fa-info-circle"></i>

      <div>
        A tree element is an element of a classification tree structure (also known as a Node). The element holds the position
        of a Taxon Concept in an arrangement of taxon that we refer to generically as a tree.
        <ul>
          <li>At the bottom of this page are the citable links to this object or just use the <i
              class="fa fa-link"></i> icon.
          You can "right click" in most browsers to copy it or open it in a new browser tab.</li>
        </ul>
      </div>
    </help>

    <span class="small text-secondary">(<tree:versionStatus version="${treeVersionElement.treeVersion}"/>)</span>

    <tree:findCurrentVersion element="${treeVersionElement}">
      <span class="small  text-secondary">
        <g:if test="${synonym}">
          currently
          <g:each in="${elements}" var="currentElement">
            <i class="fa fa-long-arrow-right"></i>
            <a href='${currentElement.fullElementLink()}'>${currentElement.treeElement.simpleName}</a>
          </g:each>
        </g:if>
        <g:else>
          <i class="fa fa-long-arrow-right"></i>
          <a href='${currentElement.fullElementLink()}'>
            current version.
          </a>
        </g:else>
      </span>
    </tree:findCurrentVersion>

  </h1>

  <st:preferredLink target="${treeVersionElement.treeVersion}"
                    title="Go to tree version ${treeVersionElement.treeVersion.id}"
                    useButton="${true}">
    ${treeVersionElement.treeVersion.tree.name} (version ${treeVersionElement.treeVersion.id})
    <g:if test="${treeVersionElement.treeVersion.published}">
      published ${treeVersionElement.treeVersion.publishedAt.dateString} by ${treeVersionElement.treeVersion.publishedBy}
    </g:if>
    <g:else>DRAFT</g:else>
  </st:preferredLink>
  <div class="timeline">
    <b>Changes:</b>
    <tree:history element="${treeVersionElement}">
      <g:if test="${index == 0}">
        <span class="timeline ${currentPos ? 'active' : ''}">
          <a href="${historyElement.fullElementLink()}" title="taxon ID: ${historyElement.taxonLink}">
            <g:if test="${currentTreeVersion == historyElement.treeVersion}">Now</g:if>
            <g:else><date>${historyElement.treeVersion.publishedAt}</date></g:else>
            <i class="fas fa-long-arrow-alt-left"></i> <date>${historyElement.updatedAt}</date>
          </a>
        </span>
      </g:if>
      <g:else>&nbsp;<i class="fas fa-long-arrow-alt-left"></i>
        <span class="timeline ${currentPos ? 'active' : ''}">
          <a href="${historyElement.fullElementLink()}" title="taxon ID: ${historyElement.taxonLink}">
            <date>${historyElement.updatedAt}</date>
          </a>
        </span>
      </g:else>
    </tree:history>
  </div>
  <hr>
</div>


<div class="rest-resource-content">

  <div>
    <tree:profile profile="${treeVersionElement.treeElement.profile}"/>
    <tree:elementPath element="${treeVersionElement}" var="pathElement" separator="/" excludeThis="false">
      <a href="${pathElement.fullElementLink()}">${pathElement.treeElement.simpleName}</a>
    </tree:elementPath>
    <div class="text-info">${children.size() - 1} sub taxa</div>
  </div>

  <div class="indented indent${treeVersionElement.depth}">
    <g:each in="${children}" var="childElement">
      <div class="tr ${childElement.excluded ? 'excluded' : ''} level${childElement.depth}">
        <div class="wrap">
          <a href="${childElement.elementLink}">${raw(childElement.displayHtml)}</a>
          <a href="${childElement.nameLink}/api/apni-format" title="View name in APNI format.">
            <i class="fa fa-list-alt see-through"></i>
          </a>
          ${raw(childElement.synonymsHtml)}
        </div>
      </div>
    </g:each>
  </div>
</div>

<h4>link to here <help><i class="fa fa-info-circle"></i>

  <div>
    <ul>
      <li>To cite this object in a database or publication please use the following preferred link.</li>
      <li>The preferred link is the most specific of the permalinks to here and makes later comparisons of linked
      resources easier.</li>
      <li>Note you can access JSON and XML versions of this object by setting the
      correct mime type in the ACCEPTS header of your HTTP request or by appending &quot;.json&quot; or &quot;.xml&quot;
      to the end of the URL.</li>
    </ul>
  </div>
</help>
</h4>
Please cite using: <a href="${treeVersionElement.fullElementLink()}">
  ${treeVersionElement.fullElementLink()}
</a> <i class="fa fa-star green"></i>

</div>
</body>
</html>

