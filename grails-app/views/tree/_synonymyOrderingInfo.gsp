<g:if test="${!nomenclatural.empty}">
  <table class="table table-striped">
    <thead>
    <g:each in="${nomenclatural.first()?.keySet()}" var="key">
      <th><st:camelToLabel camel="${key}"/></th>
    </g:each>
    </thead>
    <tbody>
    <g:each in="${nomenclatural}" var="name">
      <tr>
        <g:each in="${name.values()}" var="v">
          <td>${v}</td>
        </g:each>
      </tr>
    </g:each>
    </tbody>
  </table>
  <hr>
</g:if>
<g:if test="${!taxanomic.empty}">
  <table class="table table-striped">
    <thead>
    <g:each in="${taxanomic.first()?.keySet()}" var="key">
      <th><st:camelToLabel camel="${key}"/></th>
    </g:each>
    </thead>
    <tbody>
    <g:each in="${taxanomic}" var="name">
      <tr>
        <g:each in="${name.values()}" var="v">
          <td>${v}</td>
        </g:each>
      </tr>
    </g:each>
    </tbody>
  </table>
</g:if>