package au.org.biodiversity.nsl.api

import javax.servlet.http.HttpServletRequest

/**
 * User: pmcneil
 * Date: 13/09/16
 *
 */
trait RequestUtil {

    private
    final ArrayList<String> headers = ["X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"]

    def remoteAddress(HttpServletRequest request) {
        String ip = null
        String headerFound = headers.find { String header ->
            ip = request.getHeader(header)
            return ip && !"unknown".equalsIgnoreCase(ip)
        }
        if (!headerFound) {
            ip = request.getRemoteAddr()
        }
        return ip
    }
}