<g:set var="panelClass"
       value="panel ${st.panelClass(product: params.product)}"/>
<g:render template="/search/common-search-heading"/>

<div role="tabpanel">
  <ul class="nav nav-tabs" role="tablist">
    <li role="presentation"
        class="${!(query.advanced || query.nameCheck || query.sparql) ? 'active' : ''}">
      <a href="#name" aria-controls="name" role="tab" data-toggle="tab">
        Name search
      </a>
    </li>
    <li role="presentation" class="${query.advanced ? 'active' : ''}">
      <a href="#advanced" aria-controls="advanced" role="tab" data-toggle="tab">
        Advanced search
      </a>
    </li>
    <li role="presentation" class="${query.nameCheck ? 'active' : ''}">
      <a href="#nameCheck" aria-controls="nameCheck" role="tab" data-toggle="tab">
        Name check
      </a>
    </li>
    <g:if test="${!params.product}">
      <li role="presentation" class="${query.sparql ? 'active' : ''}">
        <a href="#sparql" role="tab" data-toggle="tab">
          Sparql
        </a>
      </li>
    </g:if>
  </ul>

  <div class="tab-content">
    <div role="tabpanel"
         class="tab-pane ${!(query.advanced || query.nameCheck || query.sparql) ? 'active' : ''}"
         id="name">
      <div class="${panelClass}">
        <div class="panel-heading">
          %{--<g:render template="/search/common-search-heading"/>--}%
        </div>

        <div class="panel-body">
          <g:render template="/search/simple-search-form"/>
        </div>
      </div>

    </div>

    <div role="tabpanel" class="tab-pane ${query.advanced ? 'active' : ''}" id="advanced">

      <div class="${panelClass}">
        <div class="panel-heading">
          %{--<g:render template="/search/common-search-heading"/>--}%
          <g:render template="/search/hide-show"/>
        </div>

        <div class="panel-body">
          <g:if test="${treeSearch}">
            <g:render template="/search/advanced-search-form"/>
          </g:if>
          <g:else>
            <g:render template="/search/advanced-name-search-form"/>
          </g:else>
        </div>
      </div>
    </div>

    <div role="tabpanel" class="tab-pane ${query.nameCheck ? 'active' : ''}" id="nameCheck">

      <div class="${panelClass}">
        <div class="panel-heading">
          %{--<g:render template="/search/common-search-heading"/>--}%
          <g:render template="/search/hide-show"/>
        </div>

        <div class="panel-body">
          <g:render template="/search/name-check-form"/>
        </div>
      </div>
    </div>

    <g:if test="${!params.product}">
      <div role="tabpanel" class="tab-pane ${query.sparql ? 'active' : ''}" id="sparql">
        <g:render template="/search/sparql-panel"/>
      </div>
    </g:if>
  </div>
</div>