<%--
  User: pmcneil
  Date: 27/03/15
--%>

<%@ page import="ch.qos.logback.core.FileAppender" contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main">
  <title>Service administration</title>
</head>

<body>

<div class="">

  <h1>Admin dashboard</h1>

  <div class="row">
    <div class="col-3">
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

    <div class="col-3">
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
    </div>
  </div>

  <ul class="nav nav-tabs" role="tablist">
    <li role="presentation" class="nav-item">
      <a href="#names" id="names-tab" aria-controls="names" role="tab" data-toggle="tab" aria-selected="true"
         class="nav-link active">
        Names
      </a>
    </li>
    <li role="presentation" class="nav-item">
      <a href="#references" id="references-tab" aria-controls="references" role="tab" aria-selected="false"
         data-toggle="tab"
         class="nav-link">
        References
      </a>
    </li>
    <li role="presentation" class="nav-item">
      <a href="#instances" id="instamces-tab" aria-controls="instances" role="tab" data-toggle="tab"
         aria-selected="false"
         class="nav-link">
        Instances
      </a>
    </li>
    <li role="presentation" class="nav-item">
      <a href="#views" id="views-tab" aria-controls="views" role="tab" data-toggle="tab" aria-selected="false"
         class="nav-link">
        Views
      </a>
    </li>
    <li role="presentation" class="nav-item">
      <a href="#tree" id="tree-tab" aria-controls="tree" role="tab" data-toggle="tab" aria-selected="false"
         class="nav-link">
        Tree
      </a>
    </li>
    <li role="presentation" class="nav-item">
      <a href="#configuration" id="configuration-tab" aria-controls="configuration" role="tab" aria-selected="false"
         data-toggle="tab"
         class="nav-link">
        Configuration
      </a>
    </li>
  </ul>

  <div class="tab-content" id="adminTabContent">

    <div role="tabpanel" class="tab-pane fade show active" id="names">

      <h2>
        Admin tasks for names
      </h2>

      <div>

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
               href="${g.createLink(controller: "admin", action: "fixNamePaths")}">
              Fix broken name paths (${stats.incorrectNamePaths})
            </a>
            <span class="text text-muted">This may need to be run a couple of times as fixed name paths may break others.</span>
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
          <li>
            <a class=""
               href="${g.createLink(controller: "admin", action: "addMissingDistEntries")}">
              Add missing distribution entries.
            </a>
          </li>
        </ul>
      </div>
    </div>

    <div role="tabpanel" class="tab-pane fade show" id="references">

      <h2>
        Admin tasks for References
      </h2>

      <div>
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

    <div role="tabpanel" class="tab-pane fade show" id="instances">

      <h2>
        Admin tasks for Instances
      </h2>

      <div>
        <ul>
        <%-- Replace XICs in Instance Notes --%>
          <li>
            <a class=""
               href="${g.createLink(controller: "admin", action: "replaceInstanceNoteXics")}">
              Replace XICs in Instance Notes
            </a>
          </li>
          <%-- Run Synonomy Update On Instances --%>
          <li>
            <a class=""
               href="${g.createLink(controller: "admin", action: "runSynonomyUpdateOnInstances")}">
              Run Synonomy Update On Instances
            </a> <span class="text text-muted">(Browser Hangs - But Navigate Away)</span>
          </li>
          <%-- update Tree Elements For Name --%>
          <li class="padded">
            <g:form action="updateTreeElementsForName">
                Update Tree Synonymy For  NameID: <input name="nameId" type="number" />
                <g:actionSubmit class="btn btn-success btn-sm" value="Start Update" action="updateTreeElementsForName"/>
            </g:form>
          </li>
          <%-- update Tree Elements For Author, his refs and name --%>
          <li class="padded">
            <g:form action="updateTreeElementsForAuthor">
              Update Tree Synonymy For AuthorID: <input name="authorId" type="number" />
              <g:actionSubmit class="btn btn-success btn-sm" value="Start Update" action="updateTreeElementsForAuthor"/>
              (Takes longer)
            </g:form>
          </li>
          <%-- update Tree Elements For a Reference and names that use that reference --%>
          <li class="padded">
            <g:form action="updateTreeElementsForReference">
              Update Tree Synonymy For RefereID: <input name="referenceId" type="number" />
              <g:actionSubmit class="btn btn-success btn-sm" value="Start Update" action="updateTreeElementsForReference"/>
            </g:form>
          </li>
        </ul>
      </div>
    </div>

    

    <div role="tabpanel" class="tab-pane fade show" id="views">

      <h2>
        Admin tasks for Views
      </h2>

      <div>

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

    <div role="tabpanel" class="tab-pane fade show" id="tree">

      <h2>
        Admin tasks for Tree
      </h2>

      <div>

        <ul>
          <li>
            <a class=""
               href="${g.createLink(controller: "admin", action: "updateInvalidTreePaths")}">
              Update Invalid Tree Paths
            </a> <span class="text text-muted">(Browser Hangs - But Navigate Away)</span>
          </li>
          <li>
            <a class=""
               href="${g.createLink(controller: "admin", action: "refreshDisplayHtml")}">
              Update Tree Version Element Display
            </a> <span class="text text-muted">(Browser Hangs - But Navigate Away)</span>
          </li>
        </ul>
      </div>
    </div>

    <div role="tabpanel" class="tab-pane fade show" id="configuration">

      <h2>
        Configuration
      </h2>

      <div>
        <h3>Database</h3>

        <p>
          ${dbInfo}
        </p>

        <h3>Log files</h3>
        <table class="table table-striped">
          <thead>
          <th>Appender name</th>
          <th>Path</th>
          </thead>
          <tbody>
          <g:each in="${logFiles}" var="f">
            <tr>
              <td><b>${f.name}</b></td><td>${f.file}</td>
            </tr>
          </g:each>
          </tbody>
        </table>

        <h3>Other</h3>

        <table class="table table-striped">
          <thead>
          <th>Property</th>
          <th>Value</th>
          </thead>
          <st:appConfig>
            <tr>
              <td valign="top"><b>${key}</b></td>
              <td><div class="lines-6">${value}</div></td>
            </tr>
          </st:appConfig>
        </table>
      </div>
    </div>
  </div>

</div>

<h2>Log</h2>

<div id='logs' class="container log-output">

</div>

</body>
</html>