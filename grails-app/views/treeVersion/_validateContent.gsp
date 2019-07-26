<div class="rest-resource-content tree-gsp">
  <h2>Validate ${treeVersion.tree.name} ${treeVersion.draftName}</h2>

  <g:if test="${payload?.synonymsOfAcceptedNames}">
    <h3>Synonyms that are also accepted names <span
        class="text-muted small">(${payload?.synonymsOfAcceptedNames.size()})</span></h3>

    <table class="table">
      <g:each in="${payload?.synonymsOfAcceptedNames}" var="record">
        <tr class="titleRow">
          <td colspan="2">
            <div class="smaller text-muted"><tree:prettyNamePath path="${record.accepted_name_path}"/></div>
            ${record.description}
          </td>
        </tr>
        <tr>
          <td class="">
            <div>${raw(record.accepted_html)}</div>
          </td>
          <td class="">
            <div class="highlight-name"
                 data-syn-id="${record.synonym_name_id}">${raw(record.synonym_accepted_html)}</div>
          </td>
        </tr>
      </g:each>
    </table>
  </g:if>

  <g:if test="${payload?.commonSynonyms}">
    <h3>Synonyms in common <span class="text-muted small">(${payload?.commonSynonyms.size()})</span></h3>

    <table class="table">
      <tree:commonSynonyms results="${payload?.commonSynonyms}">
        <tr class="titleRow">
          <td colspan="2">
            <div class="smaller text-muted"><tree:prettyNamePath path="${namePath}"/></div>
            ${raw(synonym)} is a synonym in common for:
          </td>
        </tr>
        <tr>
          <td class="">
            <i>${raw(name1.type)}</i>

            <div class="highlight-name" data-syn-id="${name1.syn_name_id}">${raw(name1.html)}</div>
          </td>
          <td class="">
            <i>${raw(name2.type)}</i><br>

            <div class="highlight-name" data-syn-id="${name2.syn_name_id}">${raw(name2.html)}</div>
          </td>
        </tr>
      </tree:commonSynonyms>
    </table>
  </g:if>
  <g:else>
    <p>nothing</p>
  </g:else>
  <script>
    $('div.highlight-name').each(function () {
      var id = $(this).data('syn-id');
      var sel = 'name[data-id="' + id + '"]';
      $(this).find('nom > scientific > ' + sel).addClass('target');
      $(this).find('tax > scientific > ' + sel).addClass('target');
      $(this).find('syn > scientific > ' + sel).addClass('target');
    });
  </script>
</div>