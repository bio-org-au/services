<%--
  User: pmcneil
  Date: 16/09/14
--%>

<%@ page import="au.org.biodiversity.nsl.NameRank; au.org.biodiversity.nsl.NameType" contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main">
  <title>
    <st:pageTitle/>
    <g:if test="${params.product}">
      ${params.product}
    </g:if>
    <g:else>Search</g:else>
    <g:if test="${query?.name}">- ${query.name}?</g:if>
  </title>
</head>

<body>
<g:set var="panelClass"
       value="${st.panelClass(product: params.product)}"/>

<div class="${params.product}">

  <g:render template="/search/nameSearchTabs"/>

  <div>
    <div class="results-header ${panelClass}">
      <strong>No results yet</strong>

      <div class="text text-info">
        <g:message code="product.search.tip.${params.product}" default=""/>
      </div>
    </div>

    <div class="results">
      <h2>Your results will be here.</h2>

      <p>Type a name into the search form above.</p>

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
    </div>
  </div>

</div>
</body>
</html>