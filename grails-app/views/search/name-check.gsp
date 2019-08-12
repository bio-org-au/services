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
    Name Check
  </title>
</head>

<body>

<div>
  <g:render template="/search/name-check-form"/>
</div>
</body>
</html>