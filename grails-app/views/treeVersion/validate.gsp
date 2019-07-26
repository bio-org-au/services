<g:if test="${embed}">
  <g:render template="validateContent"/>
</g:if>
<g:else>
  <!DOCTYPE html>
  <html>
  <head>
    <meta name="layout" content="main">
    <title>Validate Tree</title>
    <asset:stylesheet src="tree.css"/>
  </head>

  <body>
  <g:render template="validateContent"/>
  </body>
  </html>
</g:else>