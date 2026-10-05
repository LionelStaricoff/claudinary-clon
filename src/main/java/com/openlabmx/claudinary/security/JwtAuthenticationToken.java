package com.openlabmx.claudinary.security;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

public class JwtAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private String token;
    public JwtAuthenticationToken(Object principal, String token, Collection<? extends GrantedAuthority> authorities) {
        super(principal, null, authorities);
        this.token = token;
    }
    public String getToken() { return token; }
    @Override public Object getCredentials() { return null; }
    @Override public Object getPrincipal() { return super.getPrincipal(); }
}
