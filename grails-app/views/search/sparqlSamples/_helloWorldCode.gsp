select *
  where {
    bind('Hello, World!' as ?greeting)
  }
LIMIT 50
