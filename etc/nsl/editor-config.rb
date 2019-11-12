#hosts
external_services_host = "http://#{ENV['EXT_HOST']}:8080/nsl/services"
internal_services_host = "http://#{ENV['EXT_HOST']}:8080/nsl/services"
internal_mapper_host = "http://#{ENV['EXT_HOST']}:7070"
external_mapper_host = "http://#{ENV['EXT_MAPPER_HOST']}"

#environment
Rails.configuration.action_controller.relative_url_root = "/nsl/editor"
Rails.configuration.environment = 'production'
Rails.configuration.session_key_tag = 'prod'
Rails.configuration.draft_instances = 'true'

#Services
Rails.configuration.services_clientside_root_url = "#{external_services_host}/"
Rails.configuration.services = "#{internal_services_host}/"
#TODO deprecate these
Rails.configuration.name_services = "#{internal_services_host}/rest/name/apni/"
Rails.configuration.reference_services = "#{internal_services_host}/rest/reference/apni/"

#used to create external facing links to the services
Rails.configuration.nsl_links = "#{external_services_host}/"

#API key for the services
Rails.configuration.api_key = 'dev-apni-editor'

#mapper                                                        
Rails.configuration.x.mapper_api.version = 2
Rails.configuration.x.mapper_api.url = "#{internal_mapper_host}/api/"
Rails.configuration.x.mapper_api.username = 'TEST-services'
Rails.configuration.x.mapper_api.password = 'buy-me-a-pony'
Rails.configuration.x.mapper_external.url = "#{external_mapper_host}/"

#LDAP
Rails.configuration.ldap_admin_username = "uid=admin,ou=system"
Rails.configuration.ldap_admin_password = "secret"
Rails.configuration.ldap_base = "ou=users,o=shards"
Rails.configuration.ldap_host = "#{ENV['EXT_HOST']}"
Rails.configuration.ldap_port = 10389
Rails.configuration.ldap_users = "ou=users,o=shards"
Rails.configuration.ldap_groups = "ou=groups,ou=#{ENV['SHARD']},o=shards"
