<g:if test="${query.product}">
  <input type="hidden" name="product" value="${query.product}">
  <input type="hidden" name="tree.id" value="${query.tree?.id}">
</g:if>
<g:else>
  <label>Using <help><i class="fa fa-info-circle"></i>

    <div>
      This selects the classification you wish to limit the search results to. <br>
      The classification also provides the hierarchy of names to use for output ordering and adavanced search options
      such as "In family Proteacea"<br>
      The "everything" option ignores all classifications and just searches names. You can use this to find common names.
    </div>
  </help>
    <g:select from="${trees}"
              noSelection="${['': 'everything']}"
              name="tree.id"
              value="${query?.tree?.id}"
              optionKey="id"
              optionValue="name"
              class="form-control"
              title="Select the classification to use, or everything."
  />
  </label>
</g:else>
