This is the NSL (National Species List) Services Application developed at the Australian National Botanic Gardens in
association with CHAH, the ALA and the IBIS team.

The NSL services provide a relatively generic set of services for searching Names, Refererences and Authors as well as
accessing classification trees of taxons.

The NSL is (and probably always will be) a work in progress, we'll be adding a roadmap and more doco here as we go.

This code is Licensed under the Apache 2 License and we welcome contributions from the taxonomic community.

Please use Github issues for any bug reports.

Shard ID ranges
====

For Australian NSL infrastructure we are splitting the ID ranges used across different known shards. This is *not* essential
but it does mean we have a means to discriminate data sources on IDs if somehow we manage to loose track.

The current allocated ranges are:

* Vascular (APNI) 1000 - 10,000,000 + 50,000,001 - 60,000,000
* Moss 10,000,001 - 20,000,000
* Algae 20,000,001 - 30,000,000
* Lichen 30,000,001 - 40,000,000
* DAWR 40,000,001 - 50,000,000 (Aust. Dept. of Agriculture and Water Resources)
* Fungi 60,000,001 - 70,000,000
* AFD 70,000,001 - 80,000,000

There is a confluence page for the NSL infrastructure as well at https://www.anbg.gov.au/ibis25/pages/viewpage.action?spaceKey=NSL&title=NSL+Project+2.0
