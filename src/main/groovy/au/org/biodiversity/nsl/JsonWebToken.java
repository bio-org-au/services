package au.org.biodiversity.nsl;

import io.jsonwebtoken.*;
import org.apache.shiro.authc.AuthenticationToken;

import java.security.Key;

public class JsonWebToken implements AuthenticationToken {
    private final String credentials;
    private final String principal;
    private final Jws<Claims> claims;

    public JsonWebToken(String jwt, Key key) throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException, IllegalArgumentException {
            this.credentials = jwt;
            this.claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(credentials);
            this.principal = claims.getBody().getSubject();
    }

    @SuppressWarnings("unused")
    public Jws<Claims> getClaims() {
        return claims;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }
}
