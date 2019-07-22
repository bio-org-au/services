<%--
  User: pmcneil
  Date: 16/09/14
--%>

<%@ page import="au.org.biodiversity.nsl.NameRank; au.org.biodiversity.nsl.NameType" contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main">
  <title>
    <st:pageTitle/>
    <g:if test="${params.product}">
      ${params.product}
    </g:if>
    <g:else>Search</g:else>
    <g:if test="${query.name}">- ${query.name}?</g:if>
    <g:if test="${query.sparql}">- SPARQL</g:if>
  </title>
</head>

<body>
<div class="${params.product}">

  <g:render template="/search/searchTabs"/>
  <g:if test="${params.search || params.advanced}">
    <g:if test="${names}">
      <div class="panel  ${st.panelClass(product: params.product)} ">
      <div class="panel-heading">
        <g:if test="${names}">
          <strong>Found ${total} names
          %{--<g:each in="${count}" var="rankCount">--}%
          %{--${rankCount.value} ${rankCount.key},--}%
          %{--</g:each>--}%
          </strong>
          <span class="text-muted">in ${queryTime}ms. Limited to ${max} result<g:if test="${max == 0 || max > 1}">s</g:if>. </span>
        </g:if>
        <div class="btn-group hideSearch hidden-print">
          <button id="expandAll" class="btn btn-success"><i class="fa fa-caret-down"></i> view detail</button>
          <button id="collapseAll" class="btn btn-primary"><i class="fa fa-caret-up"></i> hide detail</button>
        </div>

        <div class="btn-group hidden-print">
          <button id="fontToggle" class="btn btn-default" title="change font"><i class="fa fa-font"></i></button>
        </div>

        <div class="text text-info">
          <g:message code="product.search.tip.${params.product}" default=""/>
        </div>
      </div>

      <div class="panel-body">
        <div class="results">
          <g:each in="${names}" var="name">
            <div>
              <div class='unfetched name' id="${name.id}" data-format="${params.display}-format"
                   data-nameId="${name.id}" data-product="${params.product ?: ''}">

                <div class="name" id="${name.id}">
                  <family>
                    <g:if test="${familyName}">
                      ${raw(familyName.fullNameHtml)} <af:branch name="${name}" tree="APC"><i
                        class="fa fa-code-fork"></i></af:branch>
                    </g:if>
                  </family>

                  <div data-nameId="${name.id}">
                    %{--do not reformat the next line it inserts a space between the comma and the fullName--}%
                    <accepted-name>${raw(name.fullNameHtml)}
                    </accepted-name><name-status
                      class="${name.nameStatus.name}">, ${name.nameStatus.name}</name-status><name-type
                      class="${name.nameType.name}">, ${name.nameType.name}</name-type>
                    <editor class="hidden-print">
                      <st:editorLink nameId="${name.id}"><i class="fa fa-edit" title="Edit"></i></st:editorLink>
                    </editor>

                    <a class="loadFormat"
                       href="${g.createLink(controller: (params.display + 'Format'), action: 'name', id: name.id)}">
                      <i class="fa fa-caret-down"></i>
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </g:each>
        </div>
      </div>
    </g:if>
    <g:elseif test="${total == 0}">
      <div class="panel  ${(params.product == 'apc' ? 'panel-success' : 'panel-info')} ">
        <div class="panel-heading">
          <strong>No results found</strong>

          <div class="text text-info">
            <g:message code="product.search.tip.${params.product}" default=""/>
          </div>
        </div>

        <div class="panel-body">
          <div class="results">
            <h2>No results found <g:if test="${query.name}">for &quot;${query.name}&quot;</g:if>.</h2>

            <p>Try searching for a different name, e.g. "<st:randomName q="${query.name}"/>"</p>
          </div>
        </div>
      </div>
    </g:elseif>
  </g:if>
  <g:elseif test="${query.sparql}">
  <g:render template="/search/sparql-results"/>
  </g:elseif>
  <g:elseif test="${query.nameCheck}">
  <g:render template="/search/name-check-results"/>
  </g:elseif>
  <g:else>
    <div class="panel  ${(params.product == 'apc' ? 'panel-success' : 'panel-info')} ">
      <div class="panel-heading">
        <strong>No results yet</strong>

        <div class="text text-info">
          <g:message code="product.search.tip.${params.product}" default=""/>
        </div>
      </div>

      <div class="panel-body">
        <div class="results">
          <h2>Your results will be here.</h2>

          <p>Type a name into the search form above.</p>

          <div>
            <ul>
              <li>You will get suggestions as you type in your query, they tell you what your query will return, and you can select one for an exact match.</li>
              <li>The query is <b>not</b> case sensitive.</li>
              <li>This search uses an automatic wild card at the end of the query to match all endings (unless the query is in double quotes).</li>
              <li>The query is an ordered set of search terms, so viola l. will match "Viola L." and "Viola L. sect Viola."</li>
              <li>Putting double quotes around your entire query will cause it to be matched exactly (except case). e.g. "Viola L." will match just Viola L.</li>
              <li>You can use a % as a wild card inside the search query e.g. hakea elon% be or "hakea % var. elon% benth." to find "Hakea ceratophylla var. elongata Benth."</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </g:else>

</div>
</body>
</html>