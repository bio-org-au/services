prefix xs: &lt;http://www.w3.org/2001/XMLSchema#&gt;
prefix rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt;
prefix g: &lt;http://biodiversity.org.au/voc/graph/GRAPH#&gt;
prefix boa_name: &lt;http://biodiversity.org.au/voc/boa/Name#&gt;
prefix boa_inst: &lt;http://biodiversity.org.au/voc/boa/Instance#&gt;
prefix boa_ref: &lt;http://biodiversity.org.au/voc/boa/Reference#&gt;
prefix boa_tree: &lt;http://biodiversity.org.au/voc/boa/Tree#&gt;

select ?match ?matchedName ?matchedIn ?type ?acceptedName ?acceptedRef
  where {
    {
      { bind('Onoclea nuda' as ?match) }
      union { bind('Salpichlaena orientalis' as ?match) }
      union { bind('Stegania lanceolata' as ?match) }
      union { bind('Blechnum' as ?match) }
    }

    graph g:nsl {
      ?nameURI boa_name:simpleName ?match .
      OPTIONAL {
        ?taxonURI boa_inst:name ?nameURI .
        {
          {
            ?taxonURI boa_inst:citedBy ?acceptedTaxonURI .
            ?nameURI boa_name:fullNameHtml ?matchedName .
            optional { ?taxonURI boa_inst:cites/boa_inst:reference/boa_ref:citationHtml ?matchedIn . }
          }
          UNION
          {
            BIND ( ?taxonURI as ?acceptedTaxonURI ) .
          }
        }
        ?node boa_tree:taxon ?acceptedTaxonURI .
        ?node a boa_tree:CurrentNode .
        ?node boa_tree:tree &lt;http://biodiversity.org.au/boa/tree/APC&gt; .
        ?taxonURI boa_inst:type/rdfs:label ?type .
        OPTIONAL { ?node boa_tree:name/boa_name:fullNameHtml ?acceptedName }
        OPTIONAL { ?acceptedTaxonURI boa_inst:reference/boa_ref:citationHtml ?acceptedRef . }
      }
    }
  }
LIMIT 50
