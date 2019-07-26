<%--
  User: pmcneil
  Date: 15/09/14
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main">
  <title>APC Format - ${name.simpleName}</title>
</head>

<body>
<g:render template="/search/nameSearchTabs"/>
<g:set var="panelClass"
       value="panel ${st.panelClass(product: params.product)}"/>

<div>
  <div class="results-header ${panelClass}">
    <strong>Showing ${name.simpleName}</strong>

    <div class="btn-group">
      <button id="fontToggle" class="btn btn-default" title="change font"><i class="fa fa-font"></i></button>
    </div>
  </div>

  <div class="panel-body">
    <div class="results">
      <g:render template="name" model="[name: name, apc: apc]"/>
    </div>
  </div>
</div>
</body>
</html>