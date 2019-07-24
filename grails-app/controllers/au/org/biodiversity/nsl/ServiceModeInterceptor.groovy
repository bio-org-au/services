package au.org.biodiversity.nsl

/**
 * If we're in service mode then only allow admin to access pages
 */
class ServiceModeInterceptor {

    int order = HIGHEST_PRECEDENCE+99

    def adminService

    ServiceModeInterceptor() {
        matchAll()
    }

    boolean before() {
        if (adminService.serviceMode()) {
            return accessControl() {
                hasRole('admin')
            }
        }
        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
