package com.j32bit.inviso.security;


import com.j32bit.inviso.service.InvisoUserDetailsService;
import com.j32bit.inviso.utils.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Authentication filter
 */

@Log4j2
@Component
@RequiredArgsConstructor
public class JwtSecurityFilter extends OncePerRequestFilter {

    private final InvisoUserDetailsService invisoUserDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info("JwtSecurityFilter started filtering...");

        String authorizationToken = request.getHeader("Authorization");

        if (authorizationToken == null || !authorizationToken.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        authorizationToken = authorizationToken.split(" ")[1].trim();
        String username = jwtTokenUtil.getUsernameFromToken(authorizationToken);

        log.debug("Username was extracted from authorization token: {}", username);

        if (!jwtTokenUtil.validateToken(authorizationToken)) {
            log.debug("Authorization token was not validated for {}", username);
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!username.isEmpty() && (authentication == null || !authentication.getName().equals(username))) {

            UserDetails storedUserDetails = invisoUserDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            storedUserDetails,
                            null,
                            storedUserDetails.getAuthorities()
                    );

            usernamePasswordAuthenticationToken
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            //set authentication with token
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

            log.debug("Roles are: " + storedUserDetails.getAuthorities());
            log.debug("Authentication is set for username: {}", username);
        }

        if (!authorizationToken.isEmpty()) {
            response.setHeader("Token", jwtTokenUtil.refresh(authorizationToken));
        }

        log.info("Proceeding to next filter on filter chain...");

        filterChain.doFilter(request, response);

    }

}
