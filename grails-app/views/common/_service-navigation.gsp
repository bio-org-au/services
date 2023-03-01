<%@ page import="org.apache.shiro.SecurityUtils" %>
<g:set var="configService" bean="configService"/>
<div class="imageAndText">
  <asset:image src="${st.bannerImage().toString()}" class="align-right"/>
  <div class="col">
    <div class="col-sm-12">
    <h1><st:bannerText/>
      <g:if test="${params?.product}">
        <span class="small"><st:productLabel
            product="${params.product}">${label} (${params.product})</st:productLabel></span></h1>
      </g:if>
    </div>
  </div>
</div>

<nav class="navbar navbar-expand-lg navbar-dark bg-dark" role="navigation">
  <a class="navbar-brand" href="${createLink(uri: configService.getHomeURL())}">
    NSL
  </a>
  <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent"
          aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
    <span class="navbar-toggler-icon"></span>
  </button>

  <div class="collapse navbar-collapse" id="navbarSupportedContent">
    <ul class="navbar-nav mr-auto">
      <li class="nav-item ${params.controller == 'dashboard' ? 'active' : ''}">
        <a class="nav-link dashboard" href="${createLink(controller: 'dashboard', action: 'index')}"><i
            class="fa fa-bar-chart-o"></i> Dashboard</a>
      </li>
      <li class="nav-item ${params.action == 'names' ? 'active' : ''}">
        <a class="nav-link search"
           href="${createLink(controller: 'search', action: 'names')}"><i
            class="fa fa-search"></i> Names (<st:nameTree/>)</a>
      </li>
      <li class="nav-item ${params.action == 'taxonomy' ? 'active' : ''}">
        <a class="nav-link search"
           href="${createLink(controller: 'search', action: 'taxonomy')}"><i
            class="fa fa-search"></i> Taxonomy (<st:primaryClassification/>)</a>
      </li>
      <li class="nav-item ${params.action == 'name-check' ? 'active' : ''}">
        <a class="nav-link search"
           href="${createLink(controller: 'search', action: 'nameCheck')}"><i
            class="fa fa-search"></i> Name Check (<st:nameTree/>/<st:primaryClassification/>)</a>
      </li>
      <li class="nav-item ${params.controller == 'tree' ? 'active' : ''}">
        <a class="nav-link" href="${createLink(namespace: 'api', controller: 'tree')}">Classifications</a>
      </li>
      <li class="nav-item ${params.controller == 'export' ? 'active' : ''}">
        <a class="nav-link" href="${createLink(namespace: 'api', controller: 'export', action: 'index')}">Exports</a>
      </li>

    </ul>

    <ul class="navbar-nav">

      <li class="nav-item">
        <st:documentationLink/>
      </li>

      <shiro:isLoggedIn>
        <shiro:hasRole name="QA">
          <li class="nav-item">
            <a class="nav-link home" href="${createLink(controller: 'dashboard', action: 'audit')}">
              <i class="far fa-file-pdf"></i> audit
            </a>
          </li>
        </shiro:hasRole>
        <shiro:hasRole name="admin">
          <li class="nav-item">
            <a class="nav-link home" href="${createLink(controller: 'admin', action: 'index')}">
              <i class="fa fa-gears"></i> admin
            </a>
          </li>
        </shiro:hasRole>
        <li class="nav-item active">
          <a class="nav-link logout" href="${createLink(controller: 'auth', action: 'signOut')}">
            <i class="fa fa-user${shiro.hasRole(name: 'admin') {
              '-plus'
            }}"></i> <span>${SecurityUtils.subject?.principal}</span>
            -
            <i class="fa fa-power-off"></i> Logout
          </a>
        </li>
      </shiro:isLoggedIn>
      <shiro:isNotLoggedIn>
        <li class="nav-item dropdown login-form">
          <a class="nav-link" id="dLabel" data-target="#" href="${createLink(controller: 'auth', action: 'login')}"
             data-toggle="dropdown" aria-haspopup="true" role="button" aria-expanded="false">
            <i class="fa fa-power-off"></i> Login
            <span class="caret"></span>
          </a>
          <ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
            <li>
              <div class="navbar-form">
                <g:form controller="auth" action="signIn">
                  <input type="hidden" name="targetUri" value="${request.forwardURI - request.contextPath}"/>
                  <label>Username:
                    <input class="" type="text" name="username" value=""/>
                  </label>
                  <label>Password:
                    <input class="" type="password" name="password" value=""/>
                  </label>
                  <input class="" type="submit" value="Login"/>
                </g:form>
              </div>
            </li>
          </ul>
        </li>
      </shiro:isNotLoggedIn>
    </ul>

  </div><!--/.nav-collapse -->
</nav>
<g:if test="${flash.message}">
  <div class="alert alert-warning alert-dismissible" role="alert">
    <span class="fa fa-warning" aria-hidden="true"></span>&nbsp;${flash.message}
    <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span>
    </button>
  </div>
</g:if>
