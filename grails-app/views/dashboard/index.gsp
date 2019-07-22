<%--
  User: pmcneil
  Date: 15/09/14
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main">
  <title>NSL Dashboard</title>
</head>

<body>
<div class="container">

  <h2>APNI NSL statistics</h2>
  <ul>
    <li>Services Version: <g:meta name="app.version"/></li>
    <g:each in="${stats}" var="info">
      <li><st:camelToLabel camel="${info.key}"/>: <span class="text-success"><st:linkedData val="${info.value}"/></span></li>
    </g:each>
  </ul>

  <h2>NSL Vocabulary</h2>
  <a href="${createLink(action: 'downloadVocabularyTerms')}">Download NSL RDF Vocabulary</a>

  <h2>Audit</h2>
  <ul>
    <g:each in="${auditRows}" var="row">
      <li>${row.toString()}</li>
    </g:each>
  </ul>

</div>
</body>
</html>