<g:if test="${embed}">
  <g:render template="diffElement"/>
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
  <div class="rest-resource-content tree-gsp">
    <g:render template="diffElement"/>
  </div>
  </body>
  </html>
</g:else>