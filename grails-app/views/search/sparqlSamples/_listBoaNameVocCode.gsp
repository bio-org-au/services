prefix rdf: &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#&gt;
prefix rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt;
prefix dcterms: &lt;http://purl.org/dc/terms/&gt;
prefix g: &lt;http://biodiversity.org.au/voc/graph/GRAPH#&gt;
prefix owl: &lt;http://www.w3.org/2002/07/owl#&gt;

select *
  where {
    graph g:ibis_voc {
      ?item rdfs:isDefinedBy &lt;http://biodiversity.org.au/voc/boa/Name#ONTOLOGY&gt;
      optional { ?item rdfs:label ?label }
      optional { ?item dcterms:title ?title }
      optional { ?item dcterms:description ?description }
      optional { ?item a ?class }
    }
  }
ORDER BY ?class ?item
LIMIT 500