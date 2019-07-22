prefix g: &lt;http://biodiversity.org.au/voc/graph/GRAPH#&gt;
prefix xs: &lt;http://www.w3.org/2001/XMLSchema#&gt;

select *
  where {
    {
      { bind('Doodia aspera' as ?match) }
      union { bind('MONOTREMATA' as ?match) }
      union { bind('Doodia aspera'^^xs:string as ?match) }
      union { bind('MONOTREMATA'^^xs:string as ?match) }
    }

    graph g:all {
      ?item ?predicate ?match
    }
  }
ORDER BY ?item ?predicate
LIMIT 50