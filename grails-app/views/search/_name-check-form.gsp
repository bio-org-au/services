<g:form name="search" role="form" controller="search" action="nameCheck" method="POST" class="closable">

  <div class="form-group">
    <div class="col-md-6">
      <label>Names <span class="text-muted small">Enter each name to check on a new line.</span>
        <help><i class="fa fa-info-circle"></i>

          <div>
            <ul>
              <li>This search uses the simple name without author.</li>
              <li>The names must be an exact match, including an 'x' in hybrid names</li>
              <li>You can use a wild card but only the first match will be returned.</li>
            </ul>
          </div>
        </help>

        <textarea name="name" placeholder="Enter each name to check on a new line."
                  class="form-control" rows="20">${query.name}</textarea>
      </label>
    </div>
  <div class="col-md-6">
    <div>
      <h2>Hints</h2>
      <ul>
        <li>The Name Check facility checks a list of names for correct spelling and currency i.e. whether the taxon is
        accepted by the Australian Plant Census.</li>
        <li>It checks only for valid and legitimate scientific, phrase and cultivar names with the correct orthography.
        You can use names with or without author.</li>
        <li>The names must be an exact match, including an 'x' in hybrid names.</li>
        <li>You can use a wild card but it may not find the match you are looking for. Wildcards might help for abbreviations like "subsp." e.g. s% </li>
        <li>Duplicated names and blanks will be removed from your input.</li>
        <li>You can copy and paste from word and excel, including across multiple excel columns.</li>
      </ul>
      <h3>Example search</h3>
      <pre>
        Eucalyptus citriodora
        Grevillea 'Canberra Gem'
        Eucalyptus coolibah
        Eucalyptus coolabah
        Banksia spinulosa ‘Sixteen Candles’
        Banksia spinulosa ‘Coastal Cushion’
        Banksia spinulosa var. collina
        Callistemon pachyphyllus ‘Smoked Salmon’
        Callistemon subulatus
        Correa ‘Just a Touch’
        Correa alba
        Doodia aspera
        Epacris impressa
        Abelia × grandiflora
        Acacia adoxa var. adoxa x Acacia spondylophylla
      </pre>
    </div>
  </div>
  </div>

  <g:set var="formName" value="nameCheck"/>
  <g:render template="/search/submit"/>

</g:form>