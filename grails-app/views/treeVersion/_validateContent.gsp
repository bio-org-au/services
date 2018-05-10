<div class="rest-resource-content tree-gsp">
  <h2>Validate ${data.treeVersion.tree.name} ${data.treeVersion.draftName}</h2>

  <g:if test="${data.payload?.synonymsOfAcceptedNames}">
    <h3>Synonyms of accepted names</h3>
  </g:if>
  <g:if test="${data.payload?.commonSynonyms}">
    <h3>Common synonyms</h3>

    <table class="table">
      <tree:commonSynonyms results="${data.payload?.commonSynonyms}">
        <tr class="titleRow">
          <td colspan="2">
            ${synonym} is a common synonym in:
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