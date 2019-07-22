package au.org.biodiversity.nsl

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.crypto.MacProvider

import java.security.Key

class JsonWebTokenRealm {
    static authTokenClass = JsonWebToken
    def configService
    private static final long FIVE_MINUTES = 500000
    private static final long THIRTY_MINUTES = 1800000

    @SuppressWarnings("GroovyUnusedDeclaration")
    List<String> authenticate(JsonWebToken authToken) {
        //if they created an authToken, then it's already parsed the JWT and verified the signature
        return [(String) authToken.principal, 'jwt']
    }

    static String makeJWT(String principal, Key key) {
        Jwts.builder()
            .setSubject(principal)
            .setIssuer('nsl-services')
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + FIVE_MINUTES))
            .signWith(SignatureAlgorithm.HS512, key)
            .compact()
    }

    static String makeRefreshJWT(String principal, Key key) {
        Jwts.builder()
            .setSubject(principal)
            .setIssuer('nsl-services')
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + THIRTY_MINUTES))
            .signWith(SignatureAlgorithm.HS512, key)
            .compact()
    }

    static Key makeASecretKey() {
        MacProvider.generateKey()
    }
}
