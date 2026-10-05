package com.openlabmx.claudinary.security;
import com.openlabmx.claudinary.entity.User;
import com.openlabmx.claudinary.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong123456789}")
    private String secretKey;

    @Value("${jwt.access.expiration:3600}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh.expiration:86400}")
    private long refreshTokenExpiration;

    private final UserRepository userRepository;

    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpiration);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpiration);
    }

    private String generateToken(User user, long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("email", user.getEmail());
        Set<String> roles = user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet());
        claims.put("roles", roles);
        claims.put("storageUsed", user.getStorageUsed());
        claims.put("storageLimit", user.getStorageLimit());

        return Jwts.builder().claims(claims).subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getSigningKey(), Jwts.SIG.HS512).compact();
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    public UUID getUserIdFromToken(String token) {
        String userIdStr = getAllClaimsFromToken(token).get("userId", String.class);
        return userIdStr != null ? UUID.fromString(userIdStr) : null;
    }

    public Set<String> getRolesFromToken(String token) {
        Object rolesObj = getAllClaimsFromToken(token).get("roles");
        if (rolesObj instanceof List) {
            return ((List<?>) rolesObj).stream().filter(String.class::isInstance).map(String.class::cast).collect(Collectors.toSet());
        } else if (rolesObj instanceof Set) {
            return ((Set<?>) rolesObj).stream().filter(String.class::isInstance).map(String.class::cast).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    public Boolean isTokenExpired(String token) {
        return getAllClaimsFromToken(token).getExpiration().before(new Date());
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = loadUserByToken(token);
        return new JwtAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }

    public UserDetails loadUserByToken(String token) {
        String username = getUsernameFromToken(token);
        if (username == null) return null;
        try {
            User user = userRepository.findByUsername(username)
                .orElse(null);
            if (user == null) return null;
            return new JwtUserDetails(user);
        } catch (Exception e) {
            return null;
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("jwtToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return request.getParameter("token");
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public long getRemainingTime(String token) {
        Date expiration = getAllClaimsFromToken(token).getExpiration();
        return TimeUnit.MILLISECONDS.toSeconds(expiration.getTime() - System.currentTimeMillis());
    }

    public boolean validateRefreshToken(String refreshToken, User user) {
        try {
            String usernameFromToken = getUsernameFromToken(refreshToken);
            return usernameFromToken != null && usernameFromToken.equals(user.getUsername()) && !isTokenExpired(refreshToken);
        } catch (Exception e) {
            return false;
        }
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpirationSeconds() {
        return refreshTokenExpiration;
    }
}
