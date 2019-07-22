prefix xs: &lt;http://www.w3.org/2001/XMLSchema#&gt;
prefix rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt;
prefix g: &lt;http://biodiversity.org.au/voc/graph/GRAPH#&gt;
prefix boa_name: &lt;http://biodiversity.org.au/voc/boa/Name#&gt;
prefix boa_inst: &lt;http://biodiversity.org.au/voc/boa/Instance#&gt;
prefix boa_tree: &lt;http://biodiversity.org.au/voc/boa/Tree#&gt;

select ?fullName ?node ?from_ts ?to_ts
  where {
    graph g:nsl {
      ?name boa_name:simpleName 'Malvaceae' .
      optional { ?name boa_name:fullNameHtml ?fullName . }

      ?node boa_tree:name ?name .
      ?node boa_tree:tree &lt;http://biodiversity.org.au/boa/tree/APC&gt; .
      optional { ?node boa_tree:checkedInAt/boa_tree:eventTimeStamp ?from_ts . }
      optional { ?node boa_tree:replacedAt/boa_tree:eventTimeStamp ?to_ts . }
    }
  }
ORDER BY ?name ?from_ts
LIMIT 50