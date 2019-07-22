<%@ page import="au.org.biodiversity.nsl.InstanceNote" %>
<!DOCTYPE html>
<html>
<head>
  <meta name="layout" content="main">
  <title>Instance Note</title>
</head>

<body>
<h2>Instance Note <help>
  <i class="fa fa-info-circle"></i>

  <div>
    An note used on an Instance of a name.
    <ul>
      <li>At the bottom of this page are the citable links to this Instance object or just use the <i
          class="fa fa-link"></i> icon.
      You can "right click" in most browsers to copy it or open it in a new browser tab.</li>
    </ul>
  </div>
</help>
</h2>

<div class="rest-resource-content">

  <instance-note>
    <h1>Instance Note</h1>

    <dl class="dl-horizontal">

      <g:if test="${instanceNote?.instanceNoteKey}">
        <dt>Instance Note Key</dt><dd>${instanceNote?.instanceNoteKey?.name?.encodeAsHTML()}</dd>
      </g:if>

      <g:if test="${instanceNote?.value}">
        <dt>Value</dt><dd>${instanceNote.value}</dd>
      </g:if>

      <g:if test="${instanceNote?.instance}">
        <dt>Instance</dt>
        <dd>
          <st:preferredLink target="${instanceNote?.instance}">${link}</st:preferredLink>
        </dd>
      </g:if>

    </dl>

    <g:render template="links"/>
  </instance-note>
</div>
</body>
</html>
