<label>No. of results <help><i class="fa fa-info-circle"></i>

  <div>
    Select the number of results to display.<br>
    The total number of results available will also be displayed.
  </div>
</help>
  <g:select from="${[1, 10, 50, 100, 500, 1000]}" name="max" value="${max}" class="form-control"
            title="Select the number of results to display."/>
</label>

<input type="hidden" name="display" value="${params.display}"/>

<button type="submit" name="${formName}" value="true" class="btn btn-primary">Search</button>
