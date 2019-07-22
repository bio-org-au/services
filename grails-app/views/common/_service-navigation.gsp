<%@ page import="org.apache.shiro.SecurityUtils" %>

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

<div class="navbar navbar-inverse" role="navigation">
  <div class="">
    <div class="navbar-header">
      <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
        <span class="sr-only">Toggle navigation</span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
      <a class="navbar-brand" href="${createLink(uri: '/')}">
        NSL
      </a>
    </div>

    <div class="collapse navbar-collapse">
      <ul class="nav navbar-nav">
        <li class="${params.controller == 'dashboard' ? 'active' : ''}">
          <a class="dashboard" href="${createLink(controller: 'dashboard', action: 'index')}"><i
              class="fa fa-bar-chart-o"></i> Dashboard</a>
        </li>
        <g:if test="${SecurityUtils.subject?.principal}">
          <li class="${params.controller == 'search' ? 'active' : ''}">
            <a class="search" href="${createLink(controller: 'search', action: 'search')}"><i
                class="fa fa-search"></i> Search</a>
          </li>
        </g:if>
        <li class="${params.product == st.nameTree().toString() ? 'active' : ''}">
          <a class="search"
             href="${createLink(controller: 'search', action: 'search', params: [product: st.nameTree().toString()])}"><i
              class="fa fa-search"></i> Names (<st:nameTree/>)</a>
        </li>
        <li class="${params.product == st.primaryClassification().toString() ? 'active' : ''}">
          <a class="search"
             href="${createLink(controller: 'search', action: 'search', params: [product: st.primaryClassification().toString()])}"><i
              class="fa fa-search"></i> Taxonomy (<st:primaryClassification/>)</a>
        </li>
        <li class="${params.controller == 'tree' ? 'active' : ''}">
          <a href="${createLink(namespace: 'api', controller: 'tree')}">Classifications</a>
        </li>
        <li class="${params.controller == 'export' ? 'active' : ''}">
          <a href="${createLink(namespace: 'api', controller: 'export', action: 'index')}">Exports</a>
        </li>

      </ul>

      <ul class="nav navbar-nav navbar-right">

        <li>
          <st:documentationLink/>
        </li>

        <shiro:isLoggedIn>
          <shiro:hasRole name="QA">
            <li>
              <a class="home" href="${createLink(controller: 'dashboard', action: 'audit')}">
                <i class="fa fa-file-text-o"></i> audit
              </a>
            </li>
          </shiro:hasRole>
          <shiro:hasRole name="admin">
            <li>
              <a class="home" href="${createLink(controller: 'admin', action: 'index')}">
                <i class="fa fa-gears"></i> admin
              </a>
            </li>
          </shiro:hasRole>
          <li class="active">
            <a class="logout" href="${createLink(controller: 'auth', action: 'signOut')}">
              <i class="fa fa-user${shiro.hasRole(name: 'admin') {
                '-plus'
              }}"></i> <span>${SecurityUtils.subject?.principal}</span>
              -
              <i class="fa fa-power-off"></i> Logout
            </a>
          </li>
        </shiro:isLoggedIn>
        <shiro:isNotLoggedIn>
          <li class="dropdown">
            <a id="dLabel" data-target="#" href="${createLink(controller: 'auth', action: 'login')}"
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
                    <input class="form-control" type="text" name="username" value=""/>
                  </label>
                  <label>Password:
                    <input class="form-control" type="password" name="password" value=""/>
                  </label>
                  <input class="btn btn-default" type="submit" value="Login"/>
                </g:form>
                </div>
              </li>
            </ul>
          </li>
        </shiro:isNotLoggedIn>
      </ul>

    </div><!--/.nav-collapse -->
  </div>
</div>
<g:if test="${flash.message}">
  <div class="alert alert-warning" role="alert">
    <span class="fa fa-warning" aria-hidden="true"></span>&nbsp;${flash.message}</div>
</g:if>
