<div class="panel-title">Sample Queries</div>


<div class="panel-group" id="sample-queries-accordion">
  <div class="panel panel-default">
    <div class="panel-heading panel-title">
      <a data-toggle="collapse" data-parent="#sample-queries-accordion" href="#q-ping">"Hello, World!"</a>
    </div>

    <div id="q-ping" class="panel-collapse collapse panel-body">
      <p>
        A simple test query. This query should return one row, with one column named 'greeting' containing the text 'Hello, World!'.
      </p>
      <pre>
<g:render template="/search/sparqlSamples/helloWorldCode"/>
      </pre>
      <button class="btn btn-default pull-right">Copy</button>
    </div>
  </div>

  <div class="panel panel-default">
    <div class="panel-heading panel-title">
      <a data-toggle="collapse" data-parent="#sample-queries-accordion"
         href="#query-find-string">Simple string search</a>
    </div>

    <div id="query-find-string" class="panel-collapse collapse panel-body">
      <p>
        This query finds 'Doodia aspera' or 'MONOTREMATA' anywhere in the data, and displays the subject and predicate. This type of
        query can be a useful starting point for exploring the data.
      </p>

      <p>
        The <code>?match</code> parameter is bound both as a typed and as an untyped literal in order to find the data however it appears.
      Additionally, this type of search is case-sensitive. Higher taxa in AFD have ALLCAPS names.
      </p>
      <pre >
        <g:render template="/search/sparqlSamples/stringSearchCode"/>
      </pre>
      <button class="btn btn-default pull-right">Copy</button>
    </div>
  </div>


  <div class="panel panel-default">
    <div class="panel-heading panel-title">
      <a data-toggle="collapse" data-parent="#sample-queries-accordion"
         href="#MetadataSampleQueries">Service Metadata</a>
    </div>

    <div id="MetadataSampleQueries" class="panel-collapse collapse panel-body container-fluid panel-group"
         id="metadata-queries-accordion">
      <div class="panel panel-default">
        <div class="panel-heading panel-title">
          <a data-toggle="collapse" data-parent="#metadata-queries-accordion"
             href="#sampleQueryListAllGraphs">List all graphs</a>
        </div>

        <div id="sampleQueryListAllGraphs" class="panel-collapse collapse panel-body container-fluid">
          <p>
            Data on our SPARQL service is organised into a set of named RDF graphs. This query asks the service
            for the list of graphs that it contains.
          </p>

          <p>
            This query does not itself specify a graph, and so takes data from the default graph. On our server,
            the default graph contains only this list of graphs.
          </p>
          <pre>
<g:render template="/search/sparqlSamples/listGraphsCode"/>
          </pre>
          <button class="btn btn-default pull-right">Copy</button>
        </div>
      </div>

      <div class="panel panel-default">
        <div class="panel-heading panel-title">
          <a data-toggle="collapse" data-parent="#metadata-queries-accordion"
             href="#sampleQueryAllVoc">List all vocabularies</a>
        </div>

        <div id="sampleQueryAllVoc" class="panel-collapse collapse panel-body container-fluid">
          <p>
            All of our vocabularies are available in the <code>ibis_voc</code> graph. This query fetches their URIs.
          </p>
          <pre>
            <g:render template="/search/sparqlSamples/listVocCode"/>
          </pre>
          <button class="btn btn-default pull-right">Copy</button>
        </div>
      </div>


      <div class="panel panel-default">
        <div class="panel-heading panel-title">
          <a data-toggle="collapse" data-parent="#metadata-queries-accordion"
             href="#sampleQueryListAllClasses">List all classes</a>
        </div>

        <div id="sampleQueryListAllClasses" class="panel-collapse collapse panel-body container-fluid">
          <p>
            This query fetches all classes defined in any vocabulary. Some vocabularies use <code>rdfs:Class</code>,
          and some use <code>owl:Class</code>, so we take a union.
          </p>
          <pre>
<g:render template="/search/sparqlSamples/listClassesCode"/>
          </pre>
          <button class="btn btn-default pull-right">Copy</button>
        </div>
      </div>


      <div class="panel panel-default">
        <div class="panel-heading panel-title">
          <a data-toggle="collapse" data-parent="#metadata-queries-accordion"
             href="#sampleQueryNameVoc">List BOA Name vocabulary</a>
        </div>

        <div id="sampleQueryNameVoc" class="panel-collapse collapse panel-body container-fluid">
          <p>
            This query fetches all vocabulary items defined as part of the BOA 'name' vocabulary and shows their types.
          </p>
          <pre>
            <g:render template="/search/sparqlSamples/listBoaNameVocCode"/>
          </pre>
          <button class="btn btn-default pull-right">Copy</button>
        </div>
      </div>
    </div>
  </div>


  <div class="panel panel-default">
    <div class="panel-heading panel-title">
      <a data-toggle="collapse" data-parent="#sample-queries-accordion" href="#NSLSampleQueries">NSL Sample queries</a>
    </div>

    <div id="NSLSampleQueries" class="panel-collapse collapse panel-body container-fluid panel-group"
         id="nsl-queries-accordion">
      <div class="panel panel-default">
        <div class="panel-heading panel-title">
          <a data-toggle="collapse" data-parent="#nsl-queries-accordion"
             href="#sampleQueryNSLName">Find a name in NSL</a>
        </div>

        <div id="sampleQueryNSLName" class="panel-collapse collapse panel-body container-fluid">
          <p>
            Find 'Doodia aspera' in NSL using the boa 'nameComplete' property, and display all properties.
          </p>
          <pre>
<g:render template="/search/sparqlSamples/findNameCode"/>
          </pre>
          <button class="btn btn-default pull-right">Copy</button>
        </div>
      </div>


      <div class="panel panel-default">
        <div class="panel-heading panel-title">
          <a data-toggle="collapse" data-parent="#nsl-queries-accordion"
             href="#sampleQueryNSLInstance">Find name instances in NSL</a>
        </div>

        <div id="sampleQueryNSLInstance" class="panel-collapse collapse panel-body container-fluid">
          <p>
            Find 'Doodia aspera' in NSL using the boa 'simpleName' property. Then find all instances, and display the reference and instance type, and the related name if any.
          </p>

          <p>
            This example uses optional clauses to ensure that the identifiers are fetched.
          </p>
          <pre>
<g:render template="/search/sparqlSamples/findInstanceCode"/>
          </pre>
          <button class="btn btn-default pull-right">Copy</button>
        </div>
      </div>

      <div class="panel panel-default">
        <div class="panel-heading panel-title">
          <a data-toggle="collapse" data-parent="#nsl-queries-accordion"
             href="#sampleQueryAPCAccepted">APC Accepted Name</a>
        </div>

        <div id="sampleQueryAPCAccepted" class="panel-collapse collapse panel-body container-fluid">
          <p>
            Find the APC accepted name for a series of simple names.
          </p>

          <p>
            Notice that we use a URI to specify the APC tree. This URI is the persistent semantic web identifier for the APC tree at biodiversity.org.au .
          </p>

          <p>
            This is a complex query that can take several seconds to execute. The <a
              href="http://biodiversity.org.au/dataexport/html/tnrs.html">taxon name matching service</a> executes more quickly
          by taking advantage of certain predicates specific to the National Species List application. This query uses only the BOA vocabulary.
          </p>
          <pre>
<g:render template="/search/sparqlSamples/apcAcceptedNameCode"/>
          </pre>
          <button class="btn btn-default pull-right">Copy</button>
        </div>
      </div>


      <div class="panel panel-default">
        <div class="panel-heading panel-title">
          <a data-toggle="collapse" data-parent="#nsl-queries-accordion"
             href="#sampleQueryAPCPlacement">APC Name Placement</a>
        </div>

        <div id="sampleQueryAPCPlacement" class="panel-collapse collapse panel-body container-fluid">
          <p>
            Find all placements of 'Malvaceae' in APC and sort them by date.
          </p>

          <p>
            Notice that we use a URI to specify the APC tree. This URI is the persistent semantic web identifier for the APC tree at biodiversity.org.au .
          </p>
          <pre>
<g:render template="/search/sparqlSamples/apcNamePlacementCode"/>
          </pre>
          <button class="btn btn-default pull-right">Copy</button>
        </div>
      </div>


      <div class="panel panel-default">
        <div class="panel-heading panel-title">
          <a data-toggle="collapse" data-parent="#nsl-queries-accordion"
             href="#sampleQueryAPCPlacementSynonyms">Synonyms and APC Subnames</a>
        </div>

        <div id="sampleQueryAPCPlacementSynonyms" class="panel-collapse collapse panel-body container-fluid">
          <p>
            Find 'Blechnum' in APC, find its immediate child nodes, then get all synonymns for all of these names.
          </p>

          <p>
            Notice that we use a URI to specify the APC tree. This URI is the persistent semantic web identifier for the APC tree at biodiversity.org.au .
          </p>

          <p>
            This is a complex query and can take several seconds to execute.
          </p>
          <pre>
<g:render template="/search/sparqlSamples/sysnonymApcSubnameCode"/>
          </pre>
          <button class="btn btn-default pull-right">Copy</button>
        </div>
      </div>

    </div>
  </div>
</div>



