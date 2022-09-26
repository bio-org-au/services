<%@ page import="au.org.biodiversity.nsl.Instance" %>
<!DOCTYPE html>
<html>
<head>
  <meta name="layout" content="main">
  <title>${instance.name.simpleName}, ${instance.reference.citation}</title>
</head>

<body>
<h1>Usage of a name <span class="small">(Instance)</span> <help>
  <i class="fa fa-info-circle"></i>

  <div>
    An instance of a name in a reference, or usage of a name.
    <ul>
      <li>At the bottom of this page are the citable links to this Instance object or just use the <i
          class="fa fa-link"></i> icon.
      You can "right click" in most browsers to copy it or open it in a new browser tab.</li>
    </ul>
  </div>
</help>
</h1>

<div class="rest-resource-content">

  <div data-nameId="${instance.name.id}">
    %{--do not reformat the next line it inserts a space between the comma and the fullName--}%
    <b><st:preferredLink target="${instance.name}">${raw(instance.name.fullNameHtml)}</st:preferredLink></b><name-status
      class="${instance.name.nameStatus.name}">, ${instance.name.nameStatus.name}</name-status><name-type
      class="${instance.name.nameType.name}">, ${instance.name.nameType.name}</name-type>
    <span class="text-muted small">${instance?.instanceType?.name}</span>
    <editor>
      <st:editorLink nameId="${instance.name.id}"><i class="fa fa-edit" title="Edit"></i></st:editorLink>
    </editor>

    <af:apniLink name="${instance.name}"/>
    <span class="vertbar">
      <st:preferredLink target="${instance}"><i class="fa fa-link"></i></st:preferredLink>
    </span>
  </div>

  <reference data-referenceId="${instance.reference.id}">
    <ref-citation>
      %{--don't reformat the citationHtml line--}%
      <st:preferredLink target="${instance.reference}">${raw(instance.reference?.citationHtml)}</st:preferredLink>:
    </ref-citation>

    <page><af:page instance="${instance}"/></page>

    <af:onTree element="${treeVersionElement}" instance="${instance}"/>
    <instance-type class="${instance?.instanceType?.name}">[${instance?.instanceType?.name}]</instance-type>

    <st:preferredLink target="${instance.reference}"><i class="fa fa-book"></i></st:preferredLink>
    <g:if test="${instance.bhlUrl}">
      <bhl-link>
        <a href="${instance.bhlUrl}" title="BHL link" target="_blank"><asset:image src="BHL.svg" alt="BHL" height="12"/></a>
      </bhl-link>
    </g:if>
    <span class="vertbar" title="Search for names in this reference.">
      <a href="${af.refNameTreeSearchLink(citation: instance.reference?.citation, product: params.product)}"
         title="Search for names in this reference.">
        <i class="fa fa-search"></i></a>
    </span>

    <instance data-instanceId="${instance.id}">

      <ul class="instance-notes list-unstyled">
        <af:getTypeNotes instance="${instance}" var="instanceNote">
          <li>
            <instance-note-key
                class="${instanceNote.instanceNoteKey.name}">${instanceNote.instanceNoteKey.name}:</instance-note-key>
            <instance-note>${instanceNote.value}</instance-note>
          </li>
        </af:getTypeNotes>
      </ul>

      <g:if test="${instance.instanceType.synonym || instance.instanceType.unsourced}">
        <g:render template="/apniFormat/synonymOf" model="[instance: instance]"/>
      </g:if>

      <g:if test="${instance.cites}">
        <has-synonym>
          Cites <synonym-type
            class="${instance.cites.instanceType.name}">${instance.cites.instanceType.name}:</synonym-type>
          <st:preferredLink target="${instance.cites}">${raw(instance.cites.name.fullNameHtml)}</st:preferredLink>
          <name-status
              class="${instance.cites.name.nameStatus.name}">${instance.cites.name.nameStatus.name}</name-status>

          <af:apniLink name="${instance.cites.name}"/>

        </has-synonym>
      </g:if>

      <g:if test="${instance.instancesForCitedBy}">
        <g:render template="/apniFormat/hasSynonym"
                  model="[instances: instance.instancesForCitedBy.findAll { it.instanceType.synonym }]"/>
        <g:render template="/apniFormat/missapplication"
                  model="[instances: instance.instancesForCitedBy.findAll {
                    it.instanceType.misapplied && !it.instanceType.unsourced
                  }]"/>
      %{--other synonyms--}%
        <g:render template="/apniFormat/hasSynonym" model="[instances: instance.instancesForCitedBy.findAll {
          (!it.instanceType.synonym && !it.instanceType.misapplied)
        }]"/>
      </g:if>

      <g:if test="${instance.instanceType.misapplied}">
        <g:render template="/apniFormat/missappliedTo" model="[instance: instance]"/>
      </g:if>


      <ul class="instance-notes list-unstyled">
        <af:getDisplayableNonTypeNotes instance="${instance}" var="instanceNote">
          <li>
            <instance-note-key
                class="${instanceNote.instanceNoteKey.name}">${instanceNote.instanceNoteKey.name}:</instance-note-key>
            <instance-note>${instanceNote.value}</instance-note>
          </li>
        </af:getDisplayableNonTypeNotes>
      </ul>

      <g:if test="${instance.instanceType.secondaryInstance}">
        <h4>Secondary reference of</h4>
        <st:primaryInstance var="primaryInstance" name="${instance.name}">
          <st:preferredLink target="${primaryInstance}">${raw(primaryInstance.name.fullNameHtml)}</st:preferredLink>
        </st:primaryInstance>
      </g:if>

      <g:if test="${instance.instancesForCites}">
        <h4>Nomenclatural links</h4>
        <af:sortedInstances instances="${instance.instancesForCites.findAll { it.instanceType.synonym }}" var="synonym">
          <g:render template="/apniFormat/synonymOf" model="[instance: synonym]"/>
        </af:sortedInstances>

        <g:render template="/apniFormat/missapplication"
                  model="[instances: instance.instancesForCites.findAll {
                    it.instanceType.misapplied && !it.instanceType.unsourced
                  }]"/>
      </g:if>

    </instance>
  </reference>

  <div id="foaToggle" class="toggleNext" style="display:none;">
    Flora of Australia
    <i class="fa fa-caret-right"></i>
    <i class="fa fa-caret-down" style="display: none;"></i>
  </div>

  <g:render template="links"/>
</div>
</body>
</html>

