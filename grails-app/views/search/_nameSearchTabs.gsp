<g:set var="panelClass"
       value="${st.panelClass(product: params.product)}"/>
<g:render template="/search/common-search-heading"/>

<div role="tabpanel">
  <ul class="nav nav-tabs ${panelClass}" role="tablist">
    <li role="presentation"
        class="nav-item ">
      <a href="#name" id="name-tab" aria-controls="name" role="tab" data-toggle="tab"
         class="nav-link ${!(query.advanced || query.nameCheck || query.sparql) ? 'active' : ''}">
        Name search
      </a>
    </li>
    <li role="presentation" class="nav-item">
      <a href="#advanced" id="advanced-tab" aria-controls="advanced" role="tab" data-toggle="tab"
         class="nav-link ${query.advanced ? 'active' : ''}">
        Advanced search
      </a>
    </li>
    <li role="presentation" class="nav-item">
      <a href="#nameCheck" id="nameCheck-tab" aria-controls="nameCheck" role="tab" data-toggle="tab"
         class="nav-link ${query.nameCheck ? 'active' : ''}">
        Name check
      </a>
    </li>
  </ul>

  <div class="tab-content" id="searchTabContent">
    <div role="tabpanel"
         class="tab-pane fade ${!(query.advanced || query.nameCheck || query.sparql) ? 'show active' : ''}"
         id="name">
      <g:render template="/search/simple-search-form"/>
    </div>

    <div role="tabpanel" class="tab-pane fade ${query.advanced ? 'show active' : ''}" id="advanced">
        <g:render template="/search/advanced-name-search-form"/>
    </div>

    <div role="tabpanel" class="tab-pane fade ${query.nameCheck ? 'show active' : ''}" id="nameCheck">
      <g:render template="/search/name-check-form"/>
    </div>

  </div>
</div>