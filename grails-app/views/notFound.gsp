<!doctype html>
<html>
    <head>
        <title>Page Not Found</title>
        <meta name="layout" content="main">
        <g:if env="development"><asset:stylesheet src="errors.css"/></g:if>
    </head>
    <body>
        <ul class="errors">
            <li>Error: Resource Not Found (404)</li>
            <li>Path: ${request.forwardURI}</li>
            <g:if test="${message}">
                <li>${message}</li>
            </g:if>
        </ul>
    </body>
</html>
