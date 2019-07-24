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

  <g:render template="name-check-results"/>
</body>
</html>