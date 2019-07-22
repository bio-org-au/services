prefix xs: &lt;http://www.w3.org/2001/XMLSchema#&gt;
prefix rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt;
prefix g: &lt;http://biodiversity.org.au/voc/graph/GRAPH#&gt;
prefix boa_name: &lt;http://biodiversity.org.au/voc/boa/Name#&gt;
prefix boa_inst: &lt;http://biodiversity.org.au/voc/boa/Instance#&gt;
prefix boa_ref: &lt;http://biodiversity.org.au/voc/boa/Reference#&gt;
prefix boa_tree: &lt;http://biodiversity.org.au/voc/boa/Tree#&gt;

select ?nameHtml ?nameUri ?in ?instType ?of ?placement
  where {
    graph g:nsl {
      ?name boa_name:simpleName 'Blechnum' .

      ?node boa_tree:name ?name .
      ?node boa_tree:tree &lt;http://biodiversity.org.au/boa/tree/APC&gt; .
      ?node a boa_tree:CurrentNode .

      {
        {
          ?node boa_tree:taxon ?apc_inst .
        }
        union {
          ?link boa_tree:linkSup ?node .
          ?link boa_tree:linkSub/boa_tree:taxon ?apc_inst .
          optional { ?link boa_tree:linkType ?placement . }
        }
      }

      {
        {
          bind(?apc_inst as ?inst) .
        }
        union {
          ?inst boa_inst:citedBy ?apc_inst .
          ?apc_inst boa_inst:name/boa_name:fullNameHtml ?of .
        }
      }

      ?inst boa_inst:type/rdfs:label ?instType .
      ?inst boa_inst:name ?nameUri .
      optional { ?inst boa_inst:cites/boa_inst:reference/boa_ref:citationHtml ?in . }
      ?nameUri boa_name:fullNameHtml ?nameHtml .
      ?nameUri boa_name:simpleName ?simpleName .

    }
  }
ORDER BY ?simpleName
LIMIT 50