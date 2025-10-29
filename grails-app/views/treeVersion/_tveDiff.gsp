%{--NSL-5608--}%
%{--We will not remove the code BUT I would like it clear that the functionality is on hold until--}%
%{--we can find a use case for it. At this stage it can stay in the code base.--}%
<table class="table bottom">
  <thead>
  <tr>
    <th>#</th>
    <th>${before ?: 'Before'}</th>
    <th>${after ?: 'After'}</th>
  </tr>
  </thead>
  <g:each in="${diffs}" var="tveDiff">
    <tree:diffPath a="${tveDiff.from?.namePath}"
                   b="${tveDiff.to?.namePath}">
      <tr class="noBottom sep">
        <td>
          ${tveDiff.id}
          <br>
          <g:if test="${tveDiff.parentId}">
            &#x2193;<br>
          ${tveDiff.parentId}
          </g:if>
        </td>
        <td class="${tveDiff.fromTypeString}">
          <span class="change-description">${tveDiff.fromTypeString}</span>
          <br>
          ${raw(pathA)}
        </td>
        <td class="${tveDiff.toTypeString}">
          <span class="change-description">${tveDiff.toTypeString}</span>
          <span class="small text-muted">(pub. <date>${tveDiff.to?.treeVersion?.publishedAt}</date>)</span>
          <br>
          ${raw(pathB)}
        </td>
      </tr>
    </tree:diffPath>
    <tree:diffSynonyms a="${tveDiff.from?.treeElement?.synonymsHtml}"
                       b="${tveDiff.to?.treeElement?.synonymsHtml}">
      <tr class="noTop noBottom">
        <td></td>
        <td class="${tveDiff.fromTypeString}">
          <g:render template="treeElement" model="[tve: tveDiff.from, syn: diffA]"/>
        </td>
        <td class="${tveDiff.toTypeString}">
          <g:render template="treeElement" model="[tve: tveDiff.to, syn: diffB]"/>
        </td>
      </tr>
    </tree:diffSynonyms>
    <tree:diffProfiles a="${tveDiff.from?.treeElement?.profile}"
                       b="${tveDiff.to?.treeElement?.profile}">
      <tr class="noTop noBottom">
        <td></td>
        <td class="${tveDiff.fromTypeString}">
          <dl class="dl-horizontal">
            <dt>${key}</dt><dd>${raw(diffProfileA)}</dd>
          </dl>
        </td>
        <td class="${tveDiff.toTypeString}">
          <dl class="dl-horizontal">
            <dt>${key}</dt><dd>${raw(diffProfileB)}</dd>
          </dl>
        </td>
      </tr>
    </tree:diffProfiles>
    <tr class="noTop">
      <td></td>
      <td class="${tveDiff.fromTypeString}">
        <div class="form-inline tve-diff-from" style="float:right">
          <label data-parentid="${tveDiff.parentId}">Keep ${before ?: 'Before'}
            <input type="radio" data-diffid="${tveDiff.id}" id="from-${tveDiff.id}" name="diff-${tveDiff.id}" class="form-control" value="from"
                   checked="checked"/>
          </label>
        </div>
      </td>
      <td class="${tveDiff.toTypeString}">
        <div class="form-inline tve-diff-to" style="float:right">
          <label data-parentid="${tveDiff.parentId}">Accept ${after ?: 'After'}
            <input type="radio" data-diffid="${tveDiff.id}" id="to-${tveDiff.id}" name="diff-${tveDiff.id}" class="form-control" value="to"
                   checked="checked"/>
          </label>
        </div>
      </td>
    </tr>
  </g:each>
</table>
