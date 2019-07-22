<%@ page import="au.org.biodiversity.nsl.NameRank; au.org.biodiversity.nsl.NameTag" %>
<g:form name="search" role="form" controller="search" action="search" method="GET" class="closable checkbig">
  <div class="row">
      <div class="col-md-2">
        <g:render template="/search/using-tree"/>

        <div class="checkbox" title="Search only for names on the selected tree. i.e. Accepted names on APC.">
          <label>
            <g:checkBox name="exclSynonym" value="${query.exclSynonym}"/> Not synonyms
          </label>
        </div>
      </div>

    <div class="col-md-6">
      <label>Names
        <help><i class="fa fa-info-circle"></i>

          <div>
            <ul>
              <li>This works the same as the simple search on name, except you can search multiple names by putting one on each line.</li>
              <li>For searches of more than 100 names you need to put in the exact full name, and can not use wild cards.</li>
            </ul>
          </div>
        </help><span class="text-muted small">click the <i class="fa fa-info-circle"></i>'s for help.</span>
        <textarea name="name" placeholder="Enter each name to check on a new line."
                  class="form-control suggest" rows="2" data-subject="${params.display}-search">${query.name}</textarea>
      </label>
    </div>

    <div class="col-md-4">
      <label>by Author
        <input type="text" name="author" placeholder="Author (abbrev.)" value="${query.author}"
               data-subject="author"
               class="form-control suggest"/>
      </label>
    </div>
  </div>

  <div class="form-group">

    <label>In
    <g:select from="${NameRank.list().findAll { it.major }}"
              noSelection="${['': 'any']}"
              id="inRank"
              name="inRank.id"
              value="${query?.inRank?.id}"
              optionKey="id"
              optionValue="abbrev"
              class="form-control"/>
    </label>
    <label>Named
      <input type="text"
             id="rankName"
             name="rankName"
             placeholder="name"
             value="${query.rankName}"
             data-subject="apni-search"
             data-context="inRank"
             class="form-control suggest"/>
    </label>
  </div>


  <div class="row">
    <div class="col-md-2">
      <label>of Rank
      <g:select from="${NameRank.list().collect {
        if (it.name != it.abbrev) {
          [id: it.id, name: "$it.name ($it.abbrev)"]
        } else {
          [id: it.id, name: it.name]
        }
      }}"
                noSelection="${['': 'any']}"
                name="ofRank.id"
                value="${query?.ofRank?.id}"
                optionKey="id"
                optionValue="name"
                class="form-control"/>
      </label>
    </div>

    <div class="col-md-1">

      <div class="radio">
        <label>
          <input type="radio" name="matchRank"
                 value="match" ${['above', 'below'].contains(query?.matchRank) ? '' : 'checked'}/>
          match
        </label>
        <label>
          <input type="radio" name="matchRank"
                 value="above" ${query?.matchRank == 'above' ? 'checked' : ''} disabled/>
          above
        </label>
        <label>
          <input type="radio" name="matchRank"
                 value="below" ${query?.matchRank == 'below' ? 'checked' : ''} disabled/>
          below
        </label>
      </div>
    </div>
  </div>

  <div class="form-group">
    <label>Reference citation <help>
      <i class="fa fa-info-circle"></i>

      <div>
        <ul>
          <li>This is a search on the full reference citation, e.g. 'Adams, L.G., George, A.S., (1987) Phylidraceae. Flora of Australia. 45'</li>
          <li>You will get suggestions as you type in your query, they tell you what your query will return, and you can select one for an exact match.</li>
          <li>The query is <b>not</b> case sensitive.</li>
          <li>This search uses an automatic wild card at the begining and end of the query to match all authors and
          volumes, unless you use a quoted search.</li>
          <li>The query is an ordered set of search terms, so 'flora australia' will match
          'Flora of Australia', 'Flora of south-east Australia' and and others.</li>
          <li>Putting double quotes around your entire query will cause it to be matched exactly (except case).
          e.g. "%Flora of South Australia" will match everything that ends in 'Flora of South Australia' (note the % wild card at the begining).</li>
          <li>You can use a % as a wild card inside the search query e.g.flor%  to match flore, flora, florae, florida etc.</li>
          <li>You can use a + in place of a space to make the space match exactly. e.g. 'flora+of+australia.+28' will match
          everything in Flora of Australia volume 28 (1996)</li>
        </ul>
      </div>
    </help>
      <input type="text" name="publication" placeholder="Citation" value="${query.publication}"
             class="form-control suggest" data-subject="publication" data-quoted="yes"/>
    </label>
    <label>Year
      <input type="number" name="year" placeholder="Year" value="${query.year}" class="form-control"
             maxlength="4"
             pattern="[1-2][0-9][0-9][0-9]"/>
    </label>
  </div>

  <div class="row">
    <div class="col-lg-6 col-md-6 col-sm-6 col-xs-12">
      <h4>Include
        <help>
          <i class="fa fa-info-circle"></i>

          <div>
            <p>
              Pick the name types to include. If none are checked, then Scientific and Cultigen types types will be included.
            </p>

            <p>
              Categories other than Scientific and Cultigen are over lapping, so just selecting "Autonym" will give just
              the Autonyms (which are also scientific names by definition).
            </p>

            <p>
              "Other" names include mainly common names. Common names are <b>not</b> part of a classification such as APC,
            so selecting other will expand your search beyond the classification.
            </p>
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

        <div class="checkbox">
          <label><g:checkBox name="inc.hybrid" value="${query.inc?.hybrid}"/>Hybrids</label>
        </div>

        <div class="checkbox">
          <label><g:checkBox name="inc.formula" value="${query.inc?.formula}"/>Formula names</label>
        </div>

        <div class="checkbox">
          <label><g:checkBox name="inc.autonym" value="${query.inc?.autonym}"/>Autonyms</label>
        </div>

        <g:if test="${params.product != st.primaryClassification().toString()}">
          <div class="checkbox">
            <label><g:checkBox name="inc.other" value="${query.inc?.other}"/>Other names, e.g. common</label>
          </div>
        </g:if>
      </div>
    </div>

    <div class="col-lg-6 col-md-6 col-sm-6 col-xs-12">
      <h4>Exclude
        <help>
          <i class="fa fa-info-circle"></i>

          <div>Pick the name types to exclude. If none are checked, then no name types will be excluded.</div>
        </help>
      </h4>

      <div class="form-group">
        <div class="checkbox">
          <label><g:checkBox name="ex.scientific" value="${query.ex?.scientific}"/>Scientific names</label>
        </div>

        <div class="checkbox">
          <label><g:checkBox name="ex.cultivar" value="${query.ex?.cultivar}"/>Names of cultivated plants</label>
        </div>

        <div class="checkbox">
          <label><g:checkBox name="ex.hybrid" value="${query.ex?.hybrid}"/>Hybrids</label>
        </div>

        <div class="checkbox">
          <label><g:checkBox name="ex.formula" value="${query.ex?.formula}"/>Formula names</label>
        </div>

        <div class="checkbox">
          <label><g:checkBox name="ex.autonym" value="${query.ex?.autonym}"/>Autonyms</label>
        </div>
      </div>
    </div>
  </div>

  <div class="form-group">
    <h4>Filter by tag
      <help>
        <i class="fa fa-info-circle"></i>

        <div>Filter the results to include only a specific name tags, e.g. ACRA, PBR or Trade.</div>
      </help>
    </h4>
    <label>Name tag
    <g:select from="${NameTag.list().collect { it.name }}"
              noSelection="['': 'any']"
              name="nameTag"
              value="${params?.nameTag}"
              class="form-control"/>
    </label>
  </div>
  <g:set var="formName" value="advanced"/>
  <g:render template="/search/submit"/>

</g:form>