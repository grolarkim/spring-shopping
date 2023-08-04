package shopping.learn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Disabled
@DisplayName("JWT 학습 테스트")
class JwtTest {

    Logger log = LoggerFactory.getLogger(JwtTest.class);

    @Test
    void CreateAndParse() {
        Map<String, Object> map = new HashMap<>();
        String mail = "test@example.com";
        map.put("email", mail);
        Date now = new Date();
        String key = "asdfasdfasdfsdf";
        String token = Jwts.builder()
            .setClaims(map)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + 3600000))
            .signWith(SignatureAlgorithm.HS256, key.getBytes())
            .compact();
        log.debug(token);

        Claims claims = Jwts.parser()
            .setSigningKey(key.getBytes())
            .parseClaimsJws(token)
            .getBody();
        String tokenMail = claims.get("email", String.class);

        log.debug("..");
        log.debug(tokenMail);

        assertThat(tokenMail).isEqualTo(mail);
    }

    @Test
    void ExpiredToken() {
        Map<String, Object> map = new HashMap<>();
        String mail = "test@example.com";
        map.put("email", mail);
        Date now = new Date();
        String key = "asdfasdfasdfsdf";
        String token = Jwts.builder()
            .setClaims(map)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime()))
            .signWith(SignatureAlgorithm.HS256, key.getBytes())
            .compact();

        assertThatCode(() -> Jwts.parser()
            .setSigningKey(key.getBytes())
            .parseClaimsJws(token)
            .getBody())
            .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void SignatureNotMatch() {
        Map<String, Object> map = new HashMap<>();
        String mail = "test@example.com";
        map.put("email", mail);
        Date now = new Date();
        String key = "asdfasdfasdfsdf";
        String token = Jwts.builder()
            .setClaims(map)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + 3600000))
            .signWith(SignatureAlgorithm.HS256, key.getBytes())
            .compact();

        assertThatCode(() -> Jwts.parser()
            .setSigningKey(("1" + key).getBytes())
            .parseClaimsJws(token)
            .getBody())
            .isInstanceOf(SignatureException.class);
    }

}
