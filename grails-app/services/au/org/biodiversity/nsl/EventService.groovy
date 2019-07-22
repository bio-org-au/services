package au.org.biodiversity.nsl

import grails.transaction.Transactional

import java.sql.Timestamp

@Transactional
class EventService {

    EventRecord createSynonymyUpdatedEvent(Map data, String user) {
        createEvent(EventRecordTypes.SYNONYMY_UPDATED, data, user)
    }

    EventRecord createAcceptedInstanceDeletedEvent(Map data, String user) {
        createEvent(EventRecordTypes.ACCEPTED_INSTANCE_DELETED, data, user)
    }

    EventRecord createDraftTreeEvent(Map data, String user) {
        createEvent(EventRecordTypes.CREATE_DRAFT_TREE, data, user)
    }

    EventRecord createEvent(String type, Map data, String userName) {
        Timestamp now = new Timestamp(System.currentTimeMillis())
        EventRecord event = new EventRecord(
                type: type,
                dealtWith: false,
                data: data,
                createdAt: now,
                createdBy: userName,
                updatedAt: now,
                updatedBy: userName
        )
        event.save(flush: true)
    }

    EventRecord dealWith(EventRecord event) {
        event.dealtWith = true
        event.save()
    }
}
