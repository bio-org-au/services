prefix rdf: &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#&gt;
prefix rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt;
prefix dcterms: &lt;http://purl.org/dc/terms/&gt;
prefix g: &lt;http://biodiversity.org.au/voc/graph/GRAPH#&gt;

select *
  where {
    ?uri rdf:type g:GraphURI .
    OPTIONAL { ?uri rdfs:label ?label  } .
    OPTIONAL { ?uri dcterms:title ?title  } .
    OPTIONAL { ?uri dcterms:description ?desc  } .
  }
ORDER BY ?uri
LIMIT 50