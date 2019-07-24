package au.org.biodiversity.nsl

import grails.gorm.transactions.Transactional

@Transactional
class AdminService {

    private Boolean servicing = false

    Boolean serviceMode() {
        return servicing
    }

    Boolean enableServiceMode(Boolean on) {
        servicing = on
    }

}
