<%--
  User: pmcneil
  Date: 15/09/14
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main">
  <title>NSL Exports</title>
</head>

<body>
<div class="container">
  <h2>NSL exports</h2>

  <p>
    We provide export files in different formats for different purposes. Please follow the about link next to the export
    for information about that export file.
  </p>

  <h2>Available exports</h2>

  <ul>
    <g:each in="${exports}" var="export">
      <li><a href="${export.url}" title="The export file">export ${export.label}</a>
        </li>
    </g:each>
  </ul>
  <h5>What do these exports mean?</h5>
  <div class="text-muted">Read more about these exports
  <a href="https://ibis-cloud.atlassian.net/wiki/spaces/NP/pages/1154383889/Data+Extracts+-+Darwin+Core"
                                   title="Read NSL Data Extracts Darwin Core Documentation"
                                   target="_blank"><strong>here</strong></a>
  </div>
</div>
</body>
</html>