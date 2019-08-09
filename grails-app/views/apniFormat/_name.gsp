<div class="name" id="${name.id}">
  <family>
    <g:if test="${familyName}">
      ${raw(familyName.fullNameHtml)} <af:branch name="${name}"><i class="fa fa-code-fork"></i></af:branch>
    </g:if>
  </family>

  <div data-nameId="${name.id}">
    <st:primaryInstance name="${name}" var="primaryInstance">
    %{--do not reformat the next line it inserts a space between the comma and the fullName--}%
      <accepted-name><a href="${preferredNameLink + '/api/apni-format'}">${raw(name.fullNameHtml)}</a>
      </accepted-name><name-status class="${name.nameStatus.name}">, ${name.nameStatus.name}</name-status><name-type
        class="${name.nameType.name}">, ${name.nameType.name}</name-type>
      <g:if test="${!familyName}">
        <af:branch name="${name}" tree="${st.primaryClassification()}"><i class="fa fa-code-fork"></i></af:branch>
      </g:if>
      <g:each in="${name.tags}" var="tag">
        <name-tag>${tag.tag.name}<i class="fa fa-tag"></i></name-tag>
      </g:each>
      <editor class="hidden-print">
        <st:editorLink nameId="${name.id}"><i class="fa fa-edit" title="Edit"></i></st:editorLink>
      </editor>

      <span class="vertbar hidden-print">
        <a href="${preferredNameLink}"><i title="Link to Name" class="fa fa-link"></i></a>
      </span>

      <af:nameResources name="${name}">
        <span class="vertbar hidden-print">
          <a href="${res.site.url}/${res.path}">
            <af:resourceIcon resource="${res}"/> ${res.resourceType.name}</a>
        </span>
      </af:nameResources>

      <span class="toggleNext vertbar  hidden-print">
        <i class="fa fa-caret-up"></i><i class="fa fa-caret-down" style="display: none"></i>
      </span>

      <g:if test="${name.nameType.cultivar && name.nameType.hybrid}">
        <br><span class="small text-muted">${name.parent?.simpleName} x ${name.secondParent?.simpleName}</span>
      </g:if>

      <div class="instances">
        <g:if test="${!references}">No references.</g:if>
        <g:each in="${references}" var="reference">
          <g:render template="/apniFormat/instance"
                    model="[reference: reference, instances: instancesByRef[reference], treeVersionElement: treeVersionElement]"/>
        </g:each>
        <div class="btn-group hidden-print">
          <g:if test="${photo}">
            <span class="small" title="photos in APII">
              <a href="${photo}">
                <span class="fa-stack">
                  <i class="fa fa-picture-o fa-stack-2x"></i>
                </span>
              </a>
            </span>
          </g:if>
        </div>
        <hr>
      </div>
    </st:primaryInstance>
  </div>

</div>