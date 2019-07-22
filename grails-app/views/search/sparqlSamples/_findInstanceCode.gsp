prefix xs: &lt;http://www.w3.org/2001/XMLSchema#&gt;
prefix rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt;
prefix g: &lt;http://biodiversity.org.au/voc/graph/GRAPH#&gt;
prefix boa_name: &lt;http://biodiversity.org.au/voc/boa/Name#&gt;
prefix boa_inst: &lt;http://biodiversity.org.au/voc/boa/Instance#&gt;
prefix boa_ref: &lt;http://biodiversity.org.au/voc/boa/Reference#&gt;

select ?inst ?fullName ?ref ?page ?inst_type ?of
  where {
    graph g:nsl {
      ?name boa_name:simpleName 'Doodia aspera' .
      optional { ?name boa_name:fullNameHtml ?fullName }
      ?inst boa_inst:name ?name .
      optional { ?inst boa_inst:type/rdfs:label ?inst_type }
      optional { ?inst boa_inst:page ?page }
      optional { ?inst boa_inst:reference/boa_ref:citationHtml ?ref . }
      optional { ?inst boa_inst:citedBy/boa_inst:name/boa_name:fullNameHtml ?of . }
    }
  }
ORDER BY ?inst ?ref
LIMIT 50
