package au.org.biodiversity.nsl

/**
 * User: pmcneil
 * Date: 2/05/18
 *
 */
class EventRecordTypes {

    static final String SYNONYMY_UPDATED = 'Synonymy Updated'
    static final String ACCEPTED_INSTANCE_DELETED = 'Accepted Instance Deleted'
    static final String CREATE_DRAFT_TREE = 'Creating draft tree'

    static final types = [
            SYNONYMY_UPDATED,
            ACCEPTED_INSTANCE_DELETED,
            CREATE_DRAFT_TREE
    ]
}
