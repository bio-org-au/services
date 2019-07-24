<%--
  User: pmcneil
  Date: 15/09/14
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main">
  <title>${params.product} - ${name.simpleName}</title>
  <meta name="description" content="APNI format of ${params.product} - ${name.fullNameHtml}"/>
  <meta property="og:title" content="${params.product} - ${name.fullNameHtml}"/>
  <meta property="og:description"
        content="This is the APNI format descriptions of the name ${name.fullNameHtml} in ${params.product}"/>
  <meta property="og:url" content="${name.uri}"/>
  <g:if test="${photo}">
    <meta property="og:image" content="${photo}"/>
  </g:if>
</head>

<body>
<div class="${params.product}">
  <g:render template="/search/searchTabs"/>
  <g:set var="panelClass"
         value="${st.panelClass(product: params.product)}"/>

  <div class="">
    <div class="results-header ${panelClass}">
      <strong>Showing ${name.simpleName}</strong>

      <div class="btn-group">
        <button id="fontToggle" class="btn btn-default" title="change font"><i class="fa fa-font"></i></button>
      </div>
    </div>

    <div class="panel-body">
      <div class="results">
        <g:render template="name"
                  model="[name: name, treeVersionElement: treeVersionElement, preferredNameLink: preferredNameLink]"/>
      </div>
    </div>

  </div>
</div>
</body>
</html>
