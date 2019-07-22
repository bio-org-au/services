package au.org.biodiversity.nsl

import org.joda.time.DateTime

import java.sql.Timestamp

/**
 * User: pmcneil
 * Date: 4/04/18
 *
 */
class ProfileValue {

    String value
    String createdAt
    String createdBy
    String updatedAt
    String updatedBy
    String errataReason
    String sourceLink
    ProfileValue previous

    ProfileValue(String value, String userName) {
        this.value = value
        createdAt = updatedAt = new Timestamp(System.currentTimeMillis())
        createdBy = updatedBy = userName
    }

    ProfileValue(String value, String userName, Map previousVal, String reason) {
        this.value = value
        DateTime now = new DateTime()
        String nowStr = now.toDateTimeISO()
        createdAt = updatedAt = nowStr
        createdBy = updatedBy = userName
        errataReason = reason
        if (previousVal) {
            previous = new ProfileValue(previousVal)
            createdBy = this.previous.createdBy
            createdAt = this.previous.createdAt
        }
    }

    ProfileValue(Map profileValue) {
        value = profileValue.value
        createdAt = profileValue.created_at
        createdBy = profileValue.created_by
        updatedAt = profileValue.updated_at
        updatedBy = profileValue.updated_by
        errataReason = profileValue.errata_reason
        sourceLink = profileValue.source_link
        if (profileValue.previous) {
            previous = new ProfileValue(profileValue.previous as Map)
        }
    }

    Map toMap() {
        // redundant assignment due to https://youtrack.jetbrains.com/issue/IDEA-190205
        Map m = [
                value        : value,
                created_at   : createdAt,
                created_by   : createdBy,
                updated_at   : updatedAt,
                updated_by   : updatedBy,
                errata_reason: errataReason,
                source_link  : sourceLink,
                previous     : previous?.toMap()
        ]
        return m
    }

}
