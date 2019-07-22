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
        (<a href="https://www.anbg.gov.au/ibis25/display/NSL/Data+Extracts+-+Darwin+Core"
            title="About this export"
            target="_blank">about</a>)</li>
    </g:each>
  </ul>

</div>
</body>
</html>