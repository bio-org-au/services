<g:if test="${embed}">
  <g:render template="diffContent"/>
</g:if>
<g:else>
  <!DOCTYPE html>
  <html>
  <head>
    <meta name="layout" content="main">
    <title>Tree - diff</title>
    <asset:stylesheet src="tree.css"/>

  </head>

  <body>
  <g:render template="diffContent"/>
  </body>
  </html>
</g:else>