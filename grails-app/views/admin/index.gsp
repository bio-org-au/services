<%--
  User: pmcneil
  Date: 27/03/15
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main">
  <title>Service administration</title>
</head>

<body>

<div class="">

  <h1>Admin dashboard</h1>

  <g:if test="${servicing}">
    <h2>Servicing Mode On.</h2>
  </g:if>

  <ul>
    <li>
      <a class=""
         href="${g.createLink(controller: "admin", action: "setAdminModeOn")}">
        Turn on servicing
      </a>
    </li>
    <li>
      <a class=""
         href="${g.createLink(controller: "admin", action: "setAdminModeOff")}">
        Turn off servicing
      </a>
    </li>
  </ul>

  <div role="tabpanel">
    <ul class="nav nav-tabs" role="tablist">
      <li role="presentation" class="active">
        <a href="#names" aria-controls="names" role="tab" data-toggle="tab">
          Names
        </a>
      </li>
      <li role="presentation" class="">
        <a href="#references" aria-controls="references" role="tab" data-toggle="tab">
          References
        </a>
      </li>
      <li role="presentation" class="">
        <a href="#instances" aria-controls="instances" role="tab" data-toggle="tab">
          Instances
        </a>
      </li>
      <li role="presentation" class="">
        <a href="#views" aria-controls="views" role="tab" data-toggle="tab">
          Views
        </a>
      </li>
      <li role="presentation" class="">
        <a href="#database" aria-controls="database" role="tab" data-toggle="tab">
          Database
        </a>
      </li>
    </ul>

    <div class="tab-content">

      <div role="tabpanel"
           class="tab-pane active" id="names">
        <div class="panel panel-info">

          <div class="panel-heading">
            Admin tasks for names
          </div>

          <div class="panel-body">
            <div>
              <label>Name updater ${pollingNames}
                <div class="btn-toolbar">
                  <div class="btn-group">
                    <a class="btn btn-success" href="${g.createLink(controller: 'admin', action: 'resumeUpdates')}">
                      <i class="fa fa-play"></i>
                    </a>
                    <a class="btn btn-warning" href="${g.createLink(controller: 'admin', action: 'pauseUpdates')}">
                      <i class="fa fa-pause"></i>
                    </a>
                  </div>
                </div>
              </label>
            </div>

            <ul>
              <li>
                <a class=""
                   href="${g.createLink(controller: "admin", action: "checkNames")}">
                  Check name strings
                </a>
              </li>
              <li>
                <a class=""
                   href="${g.createLink(controller: "admin", action: "reconstructNames")}">
                  Reconstruct name strings
                </a>
              </li>
              <li>
                <a class=""
                   href="${g.createLink(controller: "admin", action: "reconstructSortNames")}">
                  Reconstruct sort name strings
                </a>
              </li>
              <li>
                <a class=""
                   href="${g.createLink(controller: "admin", action: "constructMissingNames")}">
                  Construct missing name strings (${stats.namesNeedingConstruction})
                </a>
              </li>
              <li>
                <a class=""
                   href="${g.createLink(controller: "admin", action: "deduplicateMarkedNames")}">
                  Deduplicate marked names
                </a>
              </li>
              <li>
                Deleted names:
                <ul>
                  <g:each in="${stats.deletedNames}" var="name">
                    <li>${name}</li>
                  </g:each>
                </ul>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <div role="tabpanel"
           class="tab-pane" id="references">
        <div class="panel panel-info">

          <div class="panel-heading">
            Admin tasks for References
          </div>

          <div class="panel-body">

            <ul>
              <li>
                <a class=""
                   href="${g.createLink(controller: "admin", action: "reconstructCitations")}">
                  Reconstruct reference citations
                </a>
              </li>
              <li>
                <a class=""
                   href="${g.createLink(controller: "admin", action: "autoDedupeAuthors")}">
                  Deduplicate Authors
                </a>
              </li>
              <li>
                <a class=""
                   href="${g.createLink(controller: "admin", action: "deduplicateMarkedReferences")}">
                  Deduplicate marked references
                </a>
              </li>
              <li>
                <a class=""
                   href="${g.createLink(controller: "admin", action: "replaceReferenceTitleXics")}">
                  Replace XICs in reference titles
                </a>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <div role="tabpanel"
           class="tab-pane" id="instances">
        <div class="panel panel-info">

          <div class="panel-heading">
            Admin tasks for Instances
          </div>

          <div class="panel-body">
            <ul>
              <li>
                <a class=""
                   href="${g.createLink(controller: "admin", action: "replaceInstanceNoteXics")}">
                  Replace XICs in Instance Notes
                </a>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <div role="tabpanel"
           class="tab-pane" id="views">
        <div class="panel panel-info">

          <div class="panel-heading">
            Admin tasks for Views
          </div>

          <div class="panel-body">

            <ul>
              <li>
                <a class=""
                   href="${g.createLink(controller: "admin", action: "refreshViews")}">
                  Refresh all views
                </a>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <div role="tabpanel"
           class="tab-pane" id="database">
        <div class="panel panel-info">

          <div class="panel-heading">
            Configuration
          </div>

          <div class="panel-body">
            <h2>Database</h2>

            <div>
              ${dbInfo}
            </div>
            <h2>Config</h2>
            <pre>
              ${appConfig.replaceAll('(,)', ',\n')}
            </pre>
          </div>
        </div>
      </div>
    </div>

  </div>

  <div id='logs' class="row">

  </div>

</div>

</body>
</html>