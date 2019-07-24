<g:form name="search" role="form" controller="search" action="${params.action}" method="GET">
  <div class="row">
    <input type="hidden" name="product" value="${query.product}">
    <input type="hidden" name="tree.id" value="${query.tree?.id}">
    <div class="col">
      <label>name
        <help><i class="fa fa-info-circle"></i>

          <div>
            <ul>
              <li>You will get suggestions as you type in your query, they tell you what your query will return, and you can select one for an exact match.</li>
              <li>The query is <b>not</b> case sensitive.</li>
              <li>This search uses an automatic wild card at the end of the query to match all endings (unless the query is in double quotes).</li>
              <li>The query is an ordered set of search terms, so viola l. will match "Viola L." and "Viola L. sect Viola."</li>
              <li>Putting double quotes around your entire query will cause it to be matched exactly (except case). e.g. "Viola L." will match just Viola L.</li>
              <li>You can use a % as a wild card inside the search query e.g. hakea elon% be or "hakea % var. elon% benth." to find "Hakea ceratophylla var. elongata Benth."</li>
            </ul>
          </div>
        </help> <span class="text-muted small">click the <i class="fa fa-info-circle"></i> for help.</span>

        <input type="text" name="name" placeholder="Enter a name" value="${query.name}"
               class="suggest form-control "
               data-subject="${params.display}-search" size="30"/>
      </label>
    </div>

    <div class="col">
      <h4>Include
        <help>
          <i class="fa fa-info-circle"></i>

          <div>
            Pick the types of name to include. If none are checked, then Scientific and cultivated plant names will be included.
          </div>
        </help>
      </h4>

      <div class="form-group">
        <div class="checkbox">
          <label><g:checkBox name="inc.scientific"
                             value="${query.inc?.scientific}"/>Scientific names</label>
        </div>

        <div class="checkbox">
          <label><g:checkBox name="inc.cultivar" value="${query.inc?.cultivar}"/>Names of cultivated plants
          </label>
        </div>
        <g:if test="${params.product != st.primaryClassification().toString()}">
          <div class="checkbox">
            <label><g:checkBox name="inc.other" value="${query.inc?.other}"/>Other names, e.g. common</label>
          </div>
        </g:if>
      </div>
    </div>

    <g:set var="formName" value="search"/>
    <div class="col">
      <g:render template="/search/submit"/>
    </div>

  </div>
</g:form>
