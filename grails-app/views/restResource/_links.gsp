<h4>link to here <help><i class="fa fa-info-circle"></i>

  <div>
    <ul>
      <li>To cite this object in a database or publication please use the following preferred link.</li>
      <li>The preferred link is the most specific of the permalinks to here and makes later comparisons of linked
      resources easier.</li>
      <li>Note you can access JSON and XML versions of this object by setting the
      correct mime type in the ACCEPTS header of your HTTP request or by appending &quot;.json&quot; or &quot;.xml&quot;
      to the end of the URL.</li>
    </ul>
  </div>
</help>
</h4>
Please cite using: <a href="${links.find { Map link -> link.preferred }?.link}">
  ${links.find { Map link -> link.preferred }?.link}
</a> <i class="fa fa-star green"></i>
<h5>Also known as <help><i class="fa fa-info-circle"></i>

  <div>
    <ul>
      <li>
        These are all the non deprecated permalinks to this object. The link with a <i
          class="fa fa-star green"></i> is the
      preferred link.
      </li>
      <li>
        Deprecated (old, no longer used) links will not appear here, but will still resolve. You will get a 301, moved
        permanently, redirect if you use a deprecated link.
      </li>
      <li>
        You may link to this resource with any of the specific links, but we would prefer you used the preferred link as
        this makes later comparisons of linked resources easier.
      </li>
    </ul>
  </div>
</help>
</h5>
<ul>
  <g:each in="${links}" var="link">
    <g:if test="${!link.preferred && link.resourceCount == 1}">
      <li><a href="${link.link}">${link.link}</a>
        <span class="text-muted">specific.</span>
      </li>
    </g:if>
  </g:each>
</ul>
