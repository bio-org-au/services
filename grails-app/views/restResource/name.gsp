<!DOCTYPE html>
<html>
<head>
  <meta name="layout" content="main">
  <title>${name.simpleName}</title>
</head>

<body>
<h2>Name
  <help>
    <i class="fa fa-info-circle"></i>

    <div>
      The unique identifying name (text) referred to in references.
      <ul>
        <li>Below is the Name and protologue.</li>
        <li>At the bottom of this page are the citable links to this Name or just use the <i
            class="fa fa-link"></i> icon.
        You can "right click" in most browsers to copy it or open it in a new browser tab.</li>
      </ul>
    </div>
  </help>
</h2>

<div class="rest-resource-content">

  <div class="name" id="${name.id}">
    <family>
      <g:if test="${familyName}">
        ${raw(familyName.fullNameHtml)} <af:branch name="${name}" tree="APC"><i class="fa fa-code-fork"></i></af:branch>
      </g:if>
    </family>

    <div data-nameId="${name.id}">
      %{--do not reformat the next line it inserts a space between the comma and the fullName--}%
      <accepted-name><st:preferredLink target="${name}">${raw(name.fullNameHtml)}</st:preferredLink>
      </accepted-name><name-status class="${name.nameStatus.name}">, ${name.nameStatus.name}</name-status><name-type
        class="show-always">${name.nameType.name}</name-type>
      <g:if test="${!familyName}">
        <af:branch name="${name}" tree="APC"><i class="fa fa-code-fork"></i></af:branch>
      </g:if>
      <g:each in="${name.tags}" var="tag">
        <name-tag>${tag.tag.name}<i class="fa fa-tag"></i></name-tag>
      </g:each>
      <editor>
        <st:editorLink nameId="${name.id}"><i class="fa fa-edit" title="Edit"></i></st:editorLink>
      </editor>

      <af:apniLink name="${name}"/>

      <span class="vertbar">
        <st:preferredLink target="${name}"><i title="citable link to name" class="fa fa-link"></i></st:preferredLink>
      </span>
      <g:if test="${treeVersionElement}">
        <span class="vertbar">
          <af:onTree element="${treeVersionElement}"/>
        </span>
      </g:if>

      <g:if test="${name.nameType.cultivar && name.nameType.hybrid}">
        <br><span class="small text-muted">${name.parent?.simpleName}  x ${name.secondParent?.simpleName}</span>
      </g:if>

      <st:primaryInstance name="${name}" var="instance">
        <g:if test="${!instance && references}">
          <g:set var="instance" value="${instancesByRef[references[0]][0]}"/>
        </g:if>

        <g:if test="${instance}">
          <reference data-referenceId="${instance.reference.id}">
            <ref-citation>
              %{--don't reformat the citationHtml line--}%
              <st:preferredLink target="${instance}">${raw(instance.reference?.citationHtml)}</st:preferredLink>:
            </ref-citation>

            <page><af:page instance="${instance}"/></page>

            <instance-type class="${instance?.instanceType?.name}">[${instance?.instanceType?.name}]</instance-type>
            <span title="Reference link">
              <st:preferredLink target="${instance.reference}"><i class="fa fa-book"></i></st:preferredLink>
            </span>
            <span class="vertbar">
              <a href="${g.createLink(controller: 'search', action: 'search', params: [publication: instance.reference?.citation, search: true, advanced: true, display: 'apni'])}"
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
                          model="[instances: instance.instancesForCitedBy.findAll { it.instanceType.nomenclatural }]"/>
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

              <g:if test="${instance.instancesForCites}">
                <h4>Nomenclatural links</h4>
                <af:sortedInstances instances="${instance.instancesForCites.findAll { it.instanceType.nomenclatural }}"
                                    var="synonym">
                  <g:render template="/apniFormat/synonymOf" model="[instance: synonym]"/>
                </af:sortedInstances>

                <g:render template="/apniFormat/missapplication"
                          model="[instances: instance.instancesForCites.findAll {
                            it.instanceType.misapplied && !it.instanceType.unsourced
                          }]"/>
              </g:if>

            </instance>
          </reference>
        </g:if>

      </st:primaryInstance>
    </div>

  </div>

  <g:render template="links"/>

</div>

</body>
</html>

