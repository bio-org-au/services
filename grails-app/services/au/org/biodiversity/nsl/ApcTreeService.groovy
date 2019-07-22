/*
    Copyright 2015 Australian National Botanic Gardens

    This file is part of NSL services project.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy
    of the License at http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package au.org.biodiversity.nsl

import grails.transaction.Transactional
import grails.core.GrailsApplication
import org.hibernate.SessionFactory
import org.hibernate.jdbc.Work

import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

/**
 * This class contains code that is specific to the APC tree in NSL.
 * Code in this class operates at the level of TreeOperationsService. However, it is not in
 * TreeOperationsService because it is specific to the data we have here at ANBG.
 * Created by ibis on 3/06/15.
 */
@Transactional
class ApcTreeService {
    GrailsApplication grailsApplication
    SessionFactory sessionFactory_nsl

    def transferApcProfileData() {
        log.info "applying instance APC comments and distribution text to the APC tree"

        sessionFactory_nsl.getCurrentSession().doWork(new Work() {
            void execute(Connection connection) throws SQLException {
                Closure getN = { String sqlQuery ->
                    ResultSet rs = connection.createStatement().executeQuery(sqlQuery)
                    (++rs)
                    int n = rs.getInt(1)
                    rs.close()
                    return n
                }

                int eventId = getN('''select id from tree_event where note = 'APC import - create empty classification' ''')
                int apcId = getN('''select id from tree_arrangement where label = 'APC' ''')
                int apcNsId = getN('''select id from tree_uri_ns where label = 'apc-voc' ''')
                int xsNsId = getN('''select id from tree_uri_ns where label = 'xs' ''')

                log.debug("APC tree creation event is ${eventId}")
                log.debug("APC tree is ${apcId}")
                log.debug("APC vocabulary is ${apcNsId}")
                log.debug("XML vocabulary is ${xsNsId}")


                log.debug "deleting links"
                connection.createStatement().execute('''
DELETE FROM tree_link l
USING
    tree_node subnode JOIN tree_arrangement sub_a ON subnode.tree_arrangement_id = sub_a.id
WHERE
    l.subnode_id = subnode.id
    AND l.type_uri_id_part IN ('comment', 'distribution')
    AND subnode.internal_type = 'V'
'''
                )

                log.debug "deleting nodes"
                connection.createStatement().execute('''
DELETE FROM tree_node n
WHERE n.internal_type = 'V'
AND NOT exists (
  SELECT l.id FROM tree_link l WHERE l.subnode_id = n.id
)
'''
                )

                /*
                    Ok, time to extract the notes. I will make one value node for each relevant instance_note.
                    They could be combined, but I think it would confuse people.
                 */

                log.debug "creating temp table"
                connection.createStatement().execute('''
DROP TABLE IF  EXISTS tmp_instance_note_nodes
'''
                )

                connection.createStatement().execute('''
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_instance_note_nodes (
instance_note_id BIGINT PRIMARY KEY,
instance_id BIGINT,
note_key CHARACTER VARYING(255) NOT NULL,
node_id BIGINT NOT NULL
)
ON COMMIT DELETE ROWS'''
                )

                log.debug "populating temp table"
                // this is not frequently executed, so I'll just put the ids in the string with groovy
                //noinspection SqlResolve
                connection.createStatement().execute("""
insert into tmp_instance_note_nodes
select instance_note.id,
instance_note.instance_id,
instance_note_key.name,
nextval('nsl_global_seq')
from instance_note join instance_note_key on instance_note.instance_note_key_id = instance_note_key.id
where instance_note_key.name in ('APC Comment', 'APC Dist.')
and exists (
  select tree_node.id
  from tree_node
    join tree_arrangement on tree_node.tree_arrangement_id = tree_arrangement.id
  where tree_node.instance_id = instance_note.instance_id
  and (tree_arrangement.id = ${apcId} or tree_arrangement.base_arrangement_id = ${apcId})
)
"""
                )

                //noinspection SqlResolve
                ResultSet rs = connection.createStatement().executeQuery("SELECT count(*) n FROM tmp_instance_note_nodes ")
                ++rs
                log.debug "${rs.getInt('n')} values found"
                rs.close()

                log.debug "creating value nodes"
                //noinspection SqlResolve
                connection.createStatement().execute("""
insert into tree_node(
  id,
  lock_version,
  checked_in_at_id,
  internal_type,
  is_synthetic,
  literal,
  tree_arrangement_id,
  type_uri_ns_part_id,
  type_uri_id_part
)
select
  nn.node_id,--id,
  1,--lock_version,
  ${eventId},--checked_in_at_id,
  'V',--internal_type,
  'N',
  instance_note.value,--literal,
  ${apcId},--tree_arrangement_id,
  case nn.note_key when 'APC Comment' then ${xsNsId} when 'APC Dist.' then ${apcNsId} end,--type_uri_ns_part_id,
  case nn.note_key when 'APC Comment' then 'string' when 'APC Dist.' then 'distributionstring' end--type_uri_id_part
from tmp_instance_note_nodes nn join instance_note on nn.instance_note_id = instance_note.id
"""
                )

                log.debug "creating links to value nodes"
                //noinspection SqlResolve
                connection.createStatement().execute("""
insert into tree_link(
  id,
  lock_version,
  link_seq,
  supernode_id,
  subnode_id,
  is_synthetic,
  type_uri_ns_part_id,
  type_uri_id_part,
  versioning_method
)
select
  nextval('nsl_global_seq'),--id,
  1,--lock_version,
  currval('nsl_global_seq'),--link_seq,
  n.id,--supernode_id,
  nn.node_id,--subnode_id,
  'N',--is_synthetic,
  ${apcNsId},--type_uri_ns_part_id,
  case nn.note_key when 'APC Comment' then 'comment' when 'APC Dist.' then 'distribution' end,--type_uri_id_part,
  'F'--versioning_method
from tree_arrangement a
join tree_node n on a.id = n.tree_arrangement_id
join tmp_instance_note_nodes nn on n.instance_id = nn.instance_id
where (a.id = ${apcId} or a.base_arrangement_id = ${apcId})
"""
                )


                log.debug "dropping temp table"
                connection.createStatement().execute('''
DROP TABLE IF  EXISTS instance_note_nodes
'''
                )

            }
        })

        return "All comments and distributions reset."
    }

}
