prefix g: &lt;http://biodiversity.org.au/voc/graph/GRAPH#&gt;
prefix xs: &lt;http://www.w3.org/2001/XMLSchema#&gt;
prefix boa_name: &lt;http://biodiversity.org.au/voc/boa/Name#&gt;

select *
where {
  graph g:nsl {
    ?name boa_name:simpleName 'Doodia aspera' .
    ?name ?p ?o .
  }
}
ORDER BY ?name ?p ?o
LIMIT 50