<!DOCTYPE html>
<html>
<head>
  <meta name="layout" content="main">
  <title>Tree - merged</title>
  <asset:stylesheet src="tree.css"/>

</head>

<body>

<h1>Merge <g:if test="${!payload.complete}">in</g:if>complete. </h1>

<p>${payload.message}</p>

<ul>
  <g:each in="${payload.report}" var="msg">
    <li>${msg}</li>
  </g:each>
</ul>

</body>
</html>