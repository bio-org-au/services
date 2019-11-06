import au.org.biodiversity.mapper.Identifier

mapper {
    String base = System.getenv('BASE_DOMAIN')
    String prefix = System.getenv('DOMAIN_PREFIX')
    String prefixDash = System.getenv('DOMAIN_PREFIX_DASH')

    resolverURL = "http://${prefixDash}id.${base}"
    contextExtension = '' //extension to the context path (after nsl-mapper). todo is this used?
    defaultProtocol = 'http'

    Closure defaultResolver = { Identifier ident ->
        Map serviceHosts = [
                apni   : "http://${prefix}${base}:8080",
                ausmoss: "http://${prefixDash}moss.${base}:8080",
                algae  : "http://${prefixDash}algae.${base}:8080",
                fungi  : "http://${prefixDash}fungi.${base}:8080",
                lichen : "http://${prefixDash}lichen.${base}:8080",
                foa    : 'https://test.biodiversity.org.au/'
        ]
        String host = serviceHosts[ident.nameSpace]
        if (ident.objectType == 'treeElement') {
            return "${host}/nsl/services/rest/${ident.objectType}/${ident.versionNumber}/${ident.idNumber}"
        }
        if (ident.nameSpace == "foa") {
            return "${host}foa/taxa/${ident.idNumber}/summary"
        }
        return "${host}/nsl/services/rest/${ident.objectType}/${ident.nameSpace}/${ident.idNumber}"
    }

    format {
        html = defaultResolver
        json = defaultResolver
        xml = defaultResolver
        rdf = { Identifier ident -> return null }
    }

    auth = [
            'TEST-services': [
                    secret     : 'buy-me-a-pony',
                    application: 'services',
                    roles      : ['admin'],
            ],
            'TEST-editor'  : [
                    secret     : 'I-am-a-pony',
                    application: 'editor',
                    roles      : ['admin'],
            ]
    ]

    db {
        username = System.getenv('DATABASE_USER')
        password = System.getenv('DATABASE_PASSWORD')
        url = "jdbc:${System.getenv('DATABASE_URL')}"
    }
}

