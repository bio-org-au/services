<reference data-referenceId="${reference.id}">

  <af:sortedInstances instances="${instances}" var="instance">
    <g:if test="${drafts || !(instance.draft || instance.citedBy?.draft)}">
      <g:if test="${newPage}">
        <span class="${(instance.draft || instance.citedBy?.draft) ? 'draft' : ''}">
          <ref-citation>
            %{--don't reformat the citationHtml line--}%
            <st:preferredLink target="${instance}">${raw(reference?.citationHtml)}</st:preferredLink>:
          </ref-citation>
          <page><af:page instance="${instance}"/></page>
        </span>
        <g:if test="${instance.sourceId && instance.sourceSystem == 'PLANT_NAME_REFERENCE'}">
          <protologue-pdf
              data-id="https://biodiversity.org.au/images/pnrid-pdf/${instance.sourceId}.pdf">
          </protologue-pdf>
        </g:if>
        <g:if test="${instance.bhlUrl}">
          <bhl-link>
            <a href="${instance.bhlUrl}" title="BHL link" target="_blank"><asset:image src="BHL.svg" alt="BHL" height="12"/></a>
          </bhl-link>
        </g:if>
        <span title="Reference link">
          <st:preferredLink target="${reference}"><i class="fa fa-book"></i></st:preferredLink>
        </span>
      </g:if>
        <a href="${af.refNameTreeSearchLink(citation: reference?.citation, product: params.product)}"
           class="hidden-print" title="Search for names in this reference."><i
            class="fa fa-search"></i></a>

      <af:onTree element="${treeVersionElement}" instance="${instance}"/>
      <af:rangeOnAcceptedTree instance="${instance}">
        <g:if test="${current}">
          <a href="${last.fullElementLink()}" class="small text-info"
             title="in current tree since ${first.treeVersion.publishedAt.format('dd/MM/yyyy')}">
            <i class="fa fa-tree"></i>
          </a>
        </g:if>
        <g:else>
          <a href="${last.fullElementLink()}" class="small text-info"
             title="previously ${last.treeElement.excluded ? 'excluded' : 'accepted'} till ${last.treeVersion.publishedAt.format('dd/MM/yyyy')}">
            <i class="fa fa-tree"></i>
          </a>
        </g:else>
        <g:render template="edit_profile" model="[tve: last]"/>
      </af:rangeOnAcceptedTree>
      <af:legacyAPCInstanceNotes instance="${instance}">
        <g:render template="edit_note" model="[notes: notes]"/>
      </af:legacyAPCInstanceNotes>


      <instance-type class="${instance?.instanceType?.name}">[${instance?.instanceType?.name}]</instance-type>
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

        <g:if test="${instance.instancesForCitedBy}">
          <g:render template="/apniFormat/hasSynonym"
                    model="[instances: instance.instancesForCitedBy.findAll {
                      it.instanceType.synonym
                    }]"/>
          <g:render template="/apniFormat/missapplication"
                    model="[instances: instance.instancesForCitedBy.findAll {
                      it.instanceType.misapplied && !it.instanceType.unsourced
                    }]"/>
          <g:render template="/apniFormat/hasSynonym"
                    model="[instances: instance.instancesForCitedBy.findAll {
                      it.instanceType.unsourced
                    }]"/>
        %{--other synonyms--}%
          <g:render template="/apniFormat/hasSynonym" model="[instances: instance.instancesForCitedBy.findAll {
            (!it.instanceType.synonym && !it.instanceType.misapplied && !it.instanceType.unsourced)
          }]"/>
        </g:if>

        <g:if test="${instance.instanceType.misapplied && !instance.instanceType.unsourced}">
          <g:render template="/apniFormat/missappliedTo" model="[instance: instance]"/>
        </g:if>

        <af:ifEverOnAcceptedTree instance="${instance}" exclude="${treeVersionElement}">
          <ul class="instance-notes list-unstyled">
            <af:treeComment tve="${tve}">
              <li>
                <tree-note class="${tve.treeVersion.tree.name} key">${note.name}:</tree-note>
                <tree-note>${raw(note.value)}</tree-note>
              </li>
            </af:treeComment>
            <af:treeDistribution tve="${tve}">
              <li>
                <tree-note class="${tve.treeVersion.tree.name} key">${note.name}:</tree-note>
                <tree-note>${raw(note.value)}</tree-note>
              </li>
            </af:treeDistribution>
          </ul>
        </af:ifEverOnAcceptedTree>

        <g:if test="${!versionId}">
          <af:ifOnTree instance="${instance}" tve="${treeVersionElement}">
            <ul class="instance-notes list-unstyled">
              <af:treeComment tve="${treeVersionElement}">
                <li>
                  <tree-note class="${treeVersionElement.treeVersion.tree.name} key">${note.name}:</tree-note>
                  <tree-note>${raw(note.value)}</tree-note>
                </li>
              </af:treeComment>
              <af:treeDistribution tve="${treeVersionElement}">
                <li>
                  <tree-note class="${treeVersionElement.treeVersion.tree.name} key">${note.name}:</tree-note>
                  <tree-note>${raw(note.value)}</tree-note>
                </li>
              </af:treeDistribution>
            </ul>
          </af:ifOnTree>
        </g:if>

        <af:ifNeverOnAcceptedTreeSet instance="${instance}" var="incApcNotes">
          <ul class="instance-notes list-unstyled">
            <af:getDisplayableNonTypeNotes instance="${instance}" var="instanceNote" incApc="${incApcNotes}">
              <li>
                <instance-note-key
                    class="${instanceNote.instanceNoteKey.name}">${instanceNote.instanceNoteKey.name}:</instance-note-key>
                <instance-note>${instanceNote.value}</instance-note>
              </li>
            </af:getDisplayableNonTypeNotes>
          </ul>
        </af:ifNeverOnAcceptedTreeSet>
      </instance>
    </g:if>
  </af:sortedInstances>
</reference>
