<%@ page import="au.org.biodiversity.nsl.ConfigService" %>
<g:if test="${params.product}">
  <g:set var="alertClass"
         value="${((params.product == ConfigService.classificationTreeName) ? 'alert-success' : 'alert-info')}"/>

  <div id="productDescription" data-product="${params.product}" class="alert ${alertClass} alert-dismissible text-default" role="alert">
    <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span>
    </button>
    <st:productDescription product="${params.product}"/>
  </div>
</g:if>
<g:else>
  <h3 class="text-default">
    Cross classification search
  </h3>
  <div id="productDescription" data-product="cross" class="alert alert-warning alert-dismissible text-default" role="alert">
    <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span>
    </button>
    <p>
    This search works across all classifications and output formats. If you choose 'everything' the classifications are
    ignored and you simply search the names and references with no hierarchy. This allows you to search for names not in
    a classification, such as common names.
    </p>
    <p>
      The SPARQL search works across all datasets in the ${ConfigService.shardsForSPARQL}.
    </p>
  </div>
</g:else>
<g:if test="${count == 0}">
  <span class="text text-info">- No results found.</span>
</g:if>
<g:if test="${flash.message}">
  <div class="text text-warning"><i class="fa fa-warning"></i> ${flash.message}</div>
</g:if>
