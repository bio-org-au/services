package au.org.biodiversity.nsl.config

/**
 * User: pmcneil
 * Date: 22/07/19
 *
 */
class ApplicationUser {
    String key
    String application
    String host
    List<String> roles
    List<String> permissions

    ApplicationUser(String key, Map params) {
        this.key = key
        application = params.application
        host = params.host
        roles = params.roles as List<String> ?: []
        permissions = params.permissions as List<String> ?: []
    }
}
