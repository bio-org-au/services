<%--
  User: pmcneil
  Date: 15/09/14
--%>
<%@ page import="au.org.biodiversity.nsl.ConfigService" contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main">
  <title>${ConfigService.shardGroupName} Dashboard</title>
</head>

<body>
<div class="container">

  <h2>APNI ${ConfigService.shardGroupName} statistics</h2>
  <ul>
    <li>Services Version: <g:meta name="app.version"/></li>
    <g:each in="${stats}" var="info">
      <li><st:camelToLabel camel="${info.key}"/>: <span class="text-success"><st:linkedData val="${info.value}"/></span></li>
    </g:each>
  </ul>

  <h2>${ConfigService.shardGroupName} Vocabulary</h2>
  <a href="${createLink(action: 'downloadVocabularyTerms')}">Download ${ConfigService.shardGroupName} RDF Vocabulary</a>

  <h2>Audit</h2>
  <ul>
    <g:each in="${auditRows}" var="row">
      <li>${row.toString()}</li>
    </g:each>
  </ul>

</div>
</body>
</html>