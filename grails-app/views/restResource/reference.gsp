<%@ page import="au.org.biodiversity.nsl.Instance; au.org.biodiversity.nsl.Reference" %>
<!DOCTYPE html>
<html>
<head>
  <meta name="layout" content="main">
  <title>${reference.citation}</title>
</head>

<body>
<h2>Reference <help>
  <i class="fa fa-info-circle"></i>

  <div>
    A reference work with citation and author referred to by instances.
    <ul>
      <li>At the bottom of this page are the citable links to this Instance object or just use the <i
          class="fa fa-link"></i> icon.
      You can "right click" in most browsers to copy it or open it in a new browser tab.</li>
    </ul>
  </div>
</help>
</h2>

<div class="rest-resource-content">

  <reference data-referenceId="${reference.id}">
    <ref-citation>
      %{--don't reformat the citationHtml line--}%
      <b>${raw(reference?.citationHtml)}</b> <st:preferredLink target="${reference}"><i
        class="fa fa-link"></i></st:preferredLink>
    </ref-citation>
    <g:if test="${reference.pages}">
      : ${reference.pages}
    </g:if>
    <g:if test="${reference.bhlUrl}">
      <bhl-link>
        <a href="${reference.bhlUrl}" title="BHL link"><asset:image src="BHL.svg" alt="BHL" height="12"/></a>
      </bhl-link>
    </g:if>
    <g:if test="${reference.doi}">
      <doi-link>
        <a href="${reference.doi}" title="DOI link"><asset:image src="doi.png" alt="DOI"/></a>
      </doi-link>
    </g:if>
    (<reference-type>${reference.refType.name}</reference-type>)

    <reference-author data-authorId="${reference.author?.id}"><i
        class="fa fa-user"></i> <st:preferredLink
        target="${reference.author}">${reference.author?.name}</st:preferredLink> <reference-author-role
        class="${reference.refAuthorRole.name}">${reference.refAuthorRole.name}</reference-author-role>
    </reference-author>

    <div>
      Names in this reference: <a
        href="${g.createLink(controller: 'search', params: [publication: reference?.citation, search: true, advanced: true, display: 'apni'])}"
        title="search for (${reference.instances.size()}) name usages in this reference">
      <i class="fa fa-search"></i></a>
      <ol>
        <g:each
            in="${(au.org.biodiversity.nsl.Instance.executeQuery('select distinct(i.name) from Instance i where i.reference = :ref', [ref: reference])).sort {it.simpleName}}"
            var="name">
          <li>${raw(name.fullNameHtml)}</li>
        </g:each>
      </ol>

    </div>
  </reference>


</ol>

  <g:render template="links"/>
</div>
</body>
</html>
