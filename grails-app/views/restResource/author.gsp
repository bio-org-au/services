<%@ page import="au.org.biodiversity.nsl.Author" %>
<!DOCTYPE html>
<html>
<head>
  <meta name="layout" content="main">
  <title>${author.abbrev ?: author.name}</title>
</head>

<body>
<div class="rest-resource-content">

  <h2>Author <help>
    <i class="fa fa-info-circle"></i>

    <div>
      The author of a name or a reference referred to by instances.
      <ul>
        <li>At the bottom of this page are the citable links to this Author object or just use the <i
            class="fa fa-link"></i> icon.
        You can "right click" in most browsers to copy it or open it in a new browser tab.</li>
      </ul>
    </div>
  </help>
  </h2>
  <person>

    <g:if test="${author?.abbrev}">
      <b>${author.abbrev}</b> <st:preferredLink target="${author}"><i class="fa fa-link"></i></st:preferredLink>
      <span class="text-muted">Name author</span>
    </g:if>

    <g:if test="${author?.name}">
      <b>${author.name}</b> <st:preferredLink target="${author}"><i class="fa fa-link"></i></st:preferredLink>
      <span class="text-muted">Reference author</span>
    </g:if>

    <dl class="dl-horizontal">

      <g:if test="${author?.fullName}">
        <dt>Full name (discriminator):</dt> <dd>${author.fullName}</dd>
      </g:if>

      <g:if test="${author?.dateRange}">
        <dt>Date range:</dt><dd>${author.dateRange}</dd>
      </g:if>

      <g:if test="${author?.notes}">
        <dt>Notes:</dt> <dd>${author.notes}</dd>
      </g:if>

      <g:if test="${author?.ipniId}">
        <dt>IPNI id</dt> <dd>${author.ipniId}</dd>
      </g:if>

      <g:if test="${author?.namesForAuthor}">
        <dt>Author for:</dt> <dd>${author.namesForAuthor.size()} names</dd>
      </g:if>

      <g:if test="${author?.namesForBaseAuthor}">
        <dt>Base author for:</dt> <dd>${author.namesForBaseAuthor.size()} names</dd>
      </g:if>

      <g:if test="${author?.namesForExAuthor}">
        <dt>Ex author for:</dt> <dd>${author.namesForExAuthor.size()} names</dd>
      </g:if>

      <g:if test="${author?.namesForExBaseAuthor}">
        <dt>Ex base author for:</dt> <dd>${author.namesForExBaseAuthor.size()} names</dd>
      </g:if>

      <g:if test="${author?.namesForSanctioningAuthor}">
        <dt>Sactioning author for:</dt> <dd>${author.namesForSanctioningAuthor.size()} names</dd>
      </g:if>

      ${author.references.size()} References:
      <ol>
        <g:each in="${author.references.sort{it.year}}" var="ref">
          <li>${raw(ref.citationHtml)}</li>
        </g:each>
      </ol>

    </dl>

  </person>

  <g:render template="links"/>
</div>
</body>
</html>
