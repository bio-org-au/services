<div class="apc name" id="${name.id}">
  <st:primaryInstance var="primaryInstance" name="${name}">
    <g:if test="${apcInstance}">

      <div data-nameId="${name.id}">
        <g:if test="${!excluded}">
          <accepted-name title='Accepted name'><a href="${preferredNameLink + '/api/apni-format'}">
            ${raw(name.fullNameHtml)}</a></accepted-name>
        </g:if>
        <g:else>
          <excluded-name title='excluded name'><a href="${preferredNameLink + '/api/apni-format'}">
            ${raw(name.fullNameHtml)}</a>
            <af:onTree element="${treeVersionElement}"/></excluded-name>
        </g:else>
        <name-status class="${name.nameStatus.name}">${name.nameStatus.name}</name-status>
        <af:branch name="${name}"><i class="fa fa-code-fork"></i></af:branch>
        | sensu
        <st:preferredLink
            target="${apcInstance?.reference}">${raw(apcInstance?.reference?.citationHtml)}</st:preferredLink>
        <a href="${af.refAPCSearchLink(citation: apcInstance?.reference?.citation, product: params.product)}"
           title="Search for names in this reference in APC">
          <i class="fa fa-search"></i>
        </a>

        <span class="vertbar">
          <a href="${preferredNameLink}"><i title="Link to Name" class="fa fa-link"></i></a>
        </span>

        <span class="toggleNext vertbar">
          <i class="fa fa-caret-up"></i><i class="fa fa-caret-down" style="display: none"></i>
        </span>

        <div class="well instances">
          <g:if test="${instances}">
            <g:render template="hasSynonym" model="[instances: instances]"/>
          </g:if>
          <af:ifOnTree instance="${apcInstance}" tve="${treeVersionElement}">
            <ul class="instance-notes list-unstyled">
              <af:treeComment tve="${treeVersionElement}">
                <li>
                  <tree-note>${raw(note.value)}</tree-note>
                </li>
              </af:treeComment>
              <af:treeDistribution tve="${treeVersionElement}">
                <li>
                  <tree-note>${raw(note.value)}</tree-note>
                </li>
              </af:treeDistribution>
            </ul>
          </af:ifOnTree>

        </div>
      </div>

      <g:if test="${misapplied}">
        <af:sortedReferences instances="${misapplied}" var="synonym" sortOn="cites">
          <div data-nameId="${name.id}">
            <g:if test="${synonym.instanceType.misapplied}">
              <st:preferredLink target="${name}" api="api/apni-format">${raw(name.simpleNameHtml)}</st:preferredLink>
              <st:preferredLink target="${primaryInstance}"><i title="Link to use in reference"
                                                               class="fa fa-book"></i></st:preferredLink>
              auct. non <af:author name="${synonym.name}"/>: <af:harvard reference="${synonym.cites.reference}"/>
              [fide <af:harvard reference="${synonym.citedBy.reference}"/>]
            </g:if>
            <g:else>
              <st:preferredLink target="${name}" api="api/apni-format">${raw(name.simpleNameHtml)}</st:preferredLink>
              <st:preferredLink target="${primaryInstance}"><i title="Link to use in reference"
                                                               class="fa fa-book"></i></st:preferredLink>
              <name-status class="${name.nameStatus.name}">${name.nameStatus.name}</name-status>
            </g:else>
            <g:if test="${synonym.instanceType.proParte}">, p.p.</g:if>
            =
            <accepted-name title='Accepted name'>
              <st:preferredLink target="${synonym.citedBy.name}"
                                api="api/apni-format">${raw(synonym.citedBy.name.fullNameHtml)}</st:preferredLink>
              <st:preferredLink target="${synonym.citedBy}"><i title="Link to use in reference"
                                                               class="fa fa-book"></i></st:preferredLink>
            </accepted-name>
            <span class="vertbar">
              <af:branch name="${synonym.citedBy.name}" tree="APC"><i class="fa fa-code-fork"></i></af:branch>
            </span>
            <span class="vertbar">
              <st:preferredLink target="${name}"><i title="citable link to name"
                                                    class="fa fa-link"></i></st:preferredLink>
            </span>

          </div>
        </af:sortedReferences>
      </g:if>

    </g:if>
    <g:elseif test="${synonymOf}">
      <af:sortedReferences instances="${synonymOf}" var="synonym" sortOn="cites">
        <div data-nameId="${name.id}">
          <g:if test="${synonym.instanceType.doubtful}">?</g:if>
          <g:if test="${synonym.instanceType.misapplied}">
            <span class="misapplied">
              <st:preferredLink target="${name}" api="api/apni-format">
                ${raw(name.fullNameHtml)}
              </st:preferredLink>
              <st:preferredLink target="${primaryInstance ?: name}"><i title="Link to use in reference"
                                                                       class="fa fa-book"></i></st:preferredLink>
              auct. non <af:author name="${synonym.name}"/>:

              <g:if
                  test="${!synonym.instanceType.unsourced}">sensu ${raw(synonym.cites?.reference?.citationHtml)}: ${synonym.cites?.page ?: '-'}</g:if>
            </span>
          </g:if>
          <g:else>
            <st:preferredLink target="${name}" api="api/apni-format">${raw(name.fullNameHtml)}</st:preferredLink>
            <st:preferredLink target="${primaryInstance ?: name}"><i title="Link to use in reference"
                                                                     class="fa fa-book"></i></st:preferredLink>
            <name-status class="${name.nameStatus.name}">${name.nameStatus.name}</name-status>
          </g:else>
          <g:if test="${synonym.instanceType.proParte}">
            <g:if test="${synonym.instanceType.unsourced}">p.p.</g:if><g:else>, p.p.</g:else>
          </g:if>
          =
          <accepted-name title='Accepted name'>
            <st:preferredLink target="${synonym.citedBy.name}"
                              api="api/apni-format">${raw(synonym.citedBy.name.fullNameHtml)}</st:preferredLink>
            <st:preferredLink target="${synonym.citedBy}"><i title="Link to use in reference"
                                                             class="fa fa-book"></i></st:preferredLink>
          </accepted-name>
          <span class="vertbar">
            <af:branch name="${synonym.citedBy.name}" tree="APC"><i class="fa fa-code-fork"></i></af:branch>
          </span>
          <span class="vertbar">
            <st:preferredLink target="${name}"><i title="citable link to name"
                                                  class="fa fa-link"></i></st:preferredLink>
          </span>

        </div>
      </af:sortedReferences>
    </g:elseif>
    <g:else>
      <span class="text-muted">${raw(name.fullNameHtml)} not in APC. (Perhaps restrict your search to APC?)</span>
    </g:else>
  </st:primaryInstance>
</div>
