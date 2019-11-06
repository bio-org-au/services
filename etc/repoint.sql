update mapper.host set host_name = 'id.biodiversity.local:7070/broker' where preferred = true;
update tree set host_name = 'http://id.biodiversity.local:7070/broker';
update tree_element set instance_link = regexp_replace(instance_link, 'https://id.biodiversity.org.au', 'http://id.biodiversity.local:7070/broker'), name_link = regexp_replace(name_link, 'https://id.biodiversity.org.au', 'http://id.biodiversity.local:7070/broker');
update tree_element set instance_link = regexp_replace(instance_link, 'https://test-id.biodiversity.org.au', 'http://id.biodiversity.local:7070/broker'), name_link = regexp_replace(name_link, 'https://test-id.biodiversity.org.au', 'http://id.biodiversity.local:7070/broker');
update shard_config set value = 'http://id.biolocal.local:7070/broker/' where name = 'mapper host';
