package id.ac.ui.cs.advprog.b13.hiringgo.log.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtTokenProvider.validateToken(token)) {
                Claims claims = jwtTokenProvider.getAllClaimsFromToken(token);
                // String username = claims.getSubject(); // usually email
                Long userId = claims.get("userId", Integer.class).longValue();
                Object rolesClaim = claims.get("roles");

                List<SimpleGrantedAuthority> authorities;

                if (rolesClaim instanceof List) {
                    authorities = ((List<?>) rolesClaim).stream()
                            .map(role -> new SimpleGrantedAuthority(String.valueOf(role)))
                            .collect(Collectors.toList());
                } else if (rolesClaim instanceof String) {
                     authorities = Collections.singletonList(new SimpleGrantedAuthority((String) rolesClaim));
                }
                else {
                    authorities = Collections.emptyList(); // No usable roles
                }

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
