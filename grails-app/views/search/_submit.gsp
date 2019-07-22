<label>No. of results <help><i class="fa fa-info-circle"></i>

  <div>
    Select the number of results to display.<br>
    The total number of results available will also be displayed.
  </div>
</help>
  <g:select from="${[1, 10, 100, 1000, 2000, 5000]}" name="max" value="${max}" class="form-control"
            title="Select the number of results to display."/>
</label>
<g:if test="${query.product}">
  <input type="hidden" name="display" value="${params.display}"/>
</g:if>
<g:else>
  <label>Format <help><i class="fa fa-info-circle"></i>

    <div>
      Select the output format to display your results in.
    </div>
  </help>
  <g:select from="${displayFormats}"
            name="display"
            value="${params?.display}"
            class="form-control"
    title="Select the output format to display"
  />
  </label>
</g:else>
<button type="submit" name="${formName}" value="true" class="btn btn-primary">Search</button>
