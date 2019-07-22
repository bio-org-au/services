prefix rdf: &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#&gt;
prefix rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt;
prefix dcterms: &lt;http://purl.org/dc/terms/&gt;
prefix g: &lt;http://biodiversity.org.au/voc/graph/GRAPH#&gt;
prefix owl: &lt;http://www.w3.org/2002/07/owl#&gt;

select *
  where {
    graph g:ibis_voc {
      {
        { ?class a rdfs:Class . }
        union
        { ?class a owl:Class . }
      }
      optional { ?class rdfs:label ?label }
      optional { ?class dcterms:description ?description }
      optional { ?class rdfs:isDefinedBy ?definedBy }
    }
  }
ORDER BY ?class
LIMIT 500