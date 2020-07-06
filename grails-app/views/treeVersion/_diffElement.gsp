<tree:diffPath a="${mod[1].namePath}"
               b="${mod[0].namePath}">
  <tree:diffSynonyms a="${mod[1].treeElement.synonymsHtml}"
                     b="${mod[0].treeElement.synonymsHtml}">
    <div class="diffBefore">
      <div>Before (${v1.publishedAt?.format('dd/MM/yyyy hh:mm a')})</div>

      <div>${raw(pathA)}</div>
      <g:render template="treeElement" model="[tve: mod[1], syn: diffA]"/>
      <tree:diffProfiles a="${mod[1].treeElement.profile}"
                         b="${mod[0].treeElement.profile}">
        <dl class="dl-horizontal">
          <dt>${key}</dt><dd>${raw(diffProfileA)}</dd>
        </dl>
      </tree:diffProfiles>
    </div>

    <div class="diffAfter">
      <div>After (${v2.publishedAt?.format('dd/MM/yyyy hh:mm a')})</div>

      <div>${raw(pathB)}</div>
      <g:render template="treeElement" model="[tve: mod[0], syn: diffB]"/>
      <tree:diffProfiles a="${mod[1].treeElement.profile}"
                         b="${mod[0].treeElement.profile}">
        <dl class="dl-horizontal">
          <dt>${key}</dt><dd>${raw(diffProfileB)}</dd>
        </dl>
      </tree:diffProfiles>
    </div>
    <hr>
  </tree:diffSynonyms>
</tree:diffPath>
