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
    <g:if test="${query?.name}">- ${query.name}?</g:if>
  </title>
</head>

<body>
<g:set var="panelClass"
       value="${st.panelClass(product: params.product)}"/>

<div class="${params.product}">

  <g:render template="/search/nameSearchTabs"/>

  <div>
    <div class="results-header ${panelClass}">
      <strong>Found ${total} names</strong>
      <span class="text-muted">in ${queryTime}ms. Limited to ${max} result<g:if
          test="${max == 0 || max > 1}">s</g:if>.</span>

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

    <div class="results">
      <g:each in="${names}" var="name">
        <div>
          <div class='unfetched name' id="${name.id}" data-format="../${params.display}-format"
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
</body>
</html>