

shard {
    system.message.file = "/etc/nsl/broadcast.txt"
    temp.file.directory = "/tmp"
    colourScheme = "local"
    webUser = "webapni"
}

services {

    mapper {
        username = 'TEST-apni-services'
        password = 'buy-me-a-pony'
    }

    link {
        mapperURL = "http://biodiversity.local:7070"
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

//security {
//    shiro {
//        realm {
//            ldap {
//                server.urls = 'ldap://172.31.128.116:389'
//                username.attribute = 'samAccountName'
//
//                search {
//                    base = 'ou=users,ou=nsl,dc=cloud,dc=biodiversity,dc=org,dc=au'
//                    user = 'cn=NSL Admin,ou=users,ou=nsl,dc=cloud,dc=biodiversity,dc=org,dc=au'
//                    pass = 'Somelongstring789+'
//
//                    group {
//                        name = 'ou=users,ou=nsl,dc=cloud,dc=biodiversity,dc=org,dc=au'
//                        member {
//                            element = 'samAccountName'
//                            memberAttribute = 'memberof'
//                            groupPattern = 'CN=([^,]*),OU=apni,OU=test,OU=nsl'
//                            prefix = ''
//                            postfix = ''
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

security {
    shiro {
        realm {
            ldap {
                server.urls = 'ldap://appstst1-ibis.cloud.biodiversity.org.au:10389'
                userName.attribute = 'uid'

                search {
                    base = 'ou=users,dc=nsl,dc=bio,dc=org,dc=au'
                    user = 'uid=admin,ou=system'
                    pass = 'secret'

                    group {
                        name = 'ou=groups,dc=apni,dc=nsl,dc=bio,dc=org,dc=au'
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
//    password = 'nsl'
    password = "pvq0;yv!t4s3=lld602!"
//    url = "jdbc:postgresql://localhost:5432/nsl"
    url = "jdbc:postgresql://pgsql-test1-ibis.cloud.biodiversity.org.au:5432/apni"
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
