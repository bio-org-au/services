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
    Name Check results
  </title>
</head>

<body>
<g:set var="panelClass"
       value="${st.panelClass(product: params.product)}"/>

<div class="${params.product}">
  <a class="btn btn-dark" data-toggle="collapse" href="#search-again" role="button" aria-expanded="false" aria-controls="collapseExample">
    Toggle Search Form
  </a>
  <div class="collapse" id="search-again">
    <h1 class="display-4">Search Again?</h1>
    <g:render template="name-check-form"/>
    <hr>
  </div>
  <g:render template="name-check-results"/>
</body>
</html>