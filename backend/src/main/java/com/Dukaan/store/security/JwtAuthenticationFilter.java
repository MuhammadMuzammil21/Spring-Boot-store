package com.Dukaan.store.security;

import com.Dukaan.store.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    // Use the same secret key as in JwtUtil
    private final String SECRET_KEY = "mySecretKeyForJWTTokenGenerationThatIsLongEnoughForHS256Algorithm";

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String token = null;
        String username = null;
        
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                username = claims.getSubject();
            } catch (Exception e) {
                // Invalid token - log for debugging
                logger.debug("Invalid JWT token: " + e.getMessage());
            }
        }
        
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private boolean validateToken(String token, UserDetails userDetails) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject().equals(userDetails.getUsername());
        } catch (Exception e) {
            logger.debug("Token validation failed: " + e.getMessage());
            return false;
        }
    }
}
