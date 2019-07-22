<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <title>Login</title>
</head>

<body>
<g:if test="${flash.message}">
  <div class="message">${flash.message}</div>
</g:if>
<h1>login...<div class="hint small">Please login to continue</div> </h1>

<g:form controller="auth" action="signIn">
  <input type="hidden" name="targetUri" value="${targetUri}"/>

  <div class="form-group">
    <label>Username:
      <input class="form-control" type="text" name="username" value="${username}"/>
    </label>
  </div>

  <div class="form-group">
    <label>Password:
      <input class="form-control" type="password" name="password" value=""/>
    </label>
  </div>
  <input class="btn btn-default" type="submit" value="Login"/>
</g:form>

</body>
</html>
