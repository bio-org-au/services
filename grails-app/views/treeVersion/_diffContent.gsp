<div class="rest-resource-content tree-gsp">
  <h2>Changes to ${payload.v1.tree.name} in ${payload.v2.draftName}</h2>

  <g:if test="${payload.changed == false}">
    <h3>Nothing to see here.</h3>

    <p>We have no changes, nothing, zip.</p>
  </g:if>
  <g:elseif test="${payload.overflow}">
    <h1>Too many changes</h1>

    <p>We have changes, so many changes.</p>
  </g:elseif>
  <g:else>
    <h3>Added <span class="text-muted small">(${payload?.added?.size()})</span></h3>

    <g:if test="${payload?.added}">
      <table class="table">
        <g:each in="${payload?.added}" var="tve">
          <tr>
            <td>
              <g:render template="treeElement" model="[tve: tve, syn: tve.treeElement.synonymsHtml]"/>
              <tree:profile profile="${tve.treeElement.profile}"/>
            </td>
          </tr>
        </g:each>
      </table>
    </g:if>
    <g:else>
      <p>nothing</p>
    </g:else>

    <h3>Removed <span class="text-muted small">(${payload?.removed?.size()})</span></h3>

    <g:if test="${payload?.removed}">
      <table class="table">
        <g:each in="${payload?.removed}" var="tve">
          <tr>
            <td>
              <g:render template="treeElement" model="[tve: tve, syn: tve.treeElement.synonymsHtml]"/>
              <tree:profile profile="${tve.treeElement.profile}"/>
            </td>
          </tr>
        </g:each>
      </table>
    </g:if>
    <g:else>
      <p>nothing</p>
    </g:else>

    <h3>Modified <span class="text-muted small">(${payload?.modified?.size()})</span></h3>

    <g:if test="${payload?.modified}">

      <table class="table">
        <thead>
        <tr>
          <th>Before (${payload.v1.publishedAt?.format('dd/MM/yyyy hh:mm a')})</th>
          <th>After (${payload.v2.publishedAt?.format('dd/MM/yyyy hh:mm a')})</th>
        </tr>
        </thead>
        <g:each in="${payload?.modified}" var="mod">
          <tree:diffPath a="${mod[1].namePath}"
                         b="${mod[0].namePath}">
            <tr class="noBottom sep">
              <td class="diffBefore">
                ${raw(pathA)}
              </td>
              <td class="diffAfter">
                ${raw(pathB)}
              </td>
            </tr>
          </tree:diffPath>
          <tree:diffSynonyms a="${mod[1].treeElement.synonymsHtml}"
                             b="${mod[0].treeElement.synonymsHtml}">
            <tr class="noTop noBottom">
              <td class="diffBefore">
                <g:render template="treeElement" model="[tve: mod[1], syn: diffA]"/>
              </td>
              <td class="diffAfter">
                <g:render template="treeElement" model="[tve: mod[0], syn: diffB]"/>
              </td>
            </tr>
          </tree:diffSynonyms>
          <tree:diffProfiles a="${mod[1].treeElement.profile}"
                             b="${mod[0].treeElement.profile}">
            <tr class="noTop">
              <td class="diffBefore">
                <dl class="dl-horizontal">
                  <dt>${key}</dt><dd>${raw(diffProfileA)}</dd>
                </dl>
              </td>
              <td class="diffAfter">
                <dl class="dl-horizontal">
                  <dt>${key}</dt><dd>${raw(diffProfileB)}</dd>
                </dl>
              </td>
            </tr>
          </tree:diffProfiles>
        </g:each>
      </table>
    </g:if>
    <g:else>
      <p>nothing</p>
    </g:else>
  </g:else>
</div>