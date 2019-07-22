<%@ page contentType="text/html;charset=UTF-8" %>
<g:if test="${params.unwrapped}">
  <st:displayMap map="${data}"/>
</g:if>
<g:else>
<html>
<head>
  <meta name="layout" content="main">
  <title>${params.action}</title>
</head>

<body>
<div class="container">
  <st:displayMap map="${data}"/>
</div>
</body>
</html>
</g:else>