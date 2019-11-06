
grails.serverURL = "http://biodiversity.local:8080/nsl/services"

shard {
    system.message.file = "/etc/nsl/broadcast.txt"
    temp.file.directory = "/tmp"
    colourScheme = "local"
    webUser = "webapni"
}

services {

    mapper {
        username = 'TEST-services'
        password = 'buy-me-a-pony'
    }

    link {
        mapperURL = "http://biodiversity.local:7070/broker"
        internalMapperURL = 'http://biodiversity.local:7070'
        editor = "http://biodiversity.local:3000/nsl/editor"
    }

    photoService {
        url = "http://www.anbg.gov.au/cgi-bin/apiiDigital?name=%&FORMAT=CSV"
        search = { String name ->
            "http://staging.anbg.gov.au/cgi-bin/apiiName2?name=${name.encodeAsURL()}"
        }
    }
    scriptAddOns = """Google analytics goes here
<script type="text/javascript" src="https://www.anbg.gov.au/25jira/s/babfb0d409e2906e93e4a4009daf1384-T/en_US-eg0fpt/71013/b6b48b2829824b869586ac216d119363/2.0.14/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?locale=en-US&collectorId=30647d85"></script>"""

//scriptAddOns = "Nothing to see here..."
}

security {
    shiro {
        realm {
            ldap {
                server.urls = 'ldap://biodiversity.local:10389'
                userName.attribute = 'uid'

                search {
                    base = 'ou=users,o=shards'
                    user = 'uid=admin,ou=system'
                    pass = 'secret'

                    group {
                        name = "ou=groups,ou=vasc,o=shards"
                        member {
                            element = 'uniqueMember'
                            prefix = 'uid='
                        }
                    }
                    permission {
                        commonName = 'cn=permission'
                        member {
                            element = 'uniqueMember'
                            prefix = 'uid='
                        }
                    }
                }
            }
        }
    }
}

dataSource {
    username = 'nsl'
    password = 'nsl'
    url = "jdbc:postgresql://localhost:5432/nsl"
}

api.auth = [
        'dev-apni-editor': [
                application: 'apni-editor',
                roles      : ['admin', 'APC', 'treebuilder']
        ],
        'IAM-A-TEST-KEY' : [
                application: 'tester',
                roles      : ['admin', 'APC', 'treebuilder']
        ]
]

nslServices.jwt.secret = 'poobarbee'
