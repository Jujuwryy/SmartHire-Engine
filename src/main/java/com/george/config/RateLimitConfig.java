package com.george.config;

import com.bucket4j.Bandwidth;
import com.bucket4j.Bucket;
import com.bucket4j.Refill;
import com.github.ben-manes.caffeine.cache.Cache;
import com.github.ben-manes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Value("${app.security.rate-limit.generate.requests:5}")
    private int generateRequests;

    @Value("${app.security.rate-limit.generate.window-minutes:60}")
    private int generateWindowMinutes;

    @Value("${app.security.rate-limit.match.requests:100}")
    private int matchRequests;

    @Value("${app.security.rate-limit.match.window-minutes:1}")
    private int matchWindowMinutes;

    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter(generateRequests, generateWindowMinutes, matchRequests, matchWindowMinutes);
    }

    public static class RateLimitFilter extends OncePerRequestFilter {
        private final Cache<String, Bucket> cache;
        private final Bandwidth generateBandwidth;
        private final Bandwidth matchBandwidth;

        public RateLimitFilter(int generateRequests, int generateWindowMinutes, 
                              int matchRequests, int matchWindowMinutes) {
            this.cache = Caffeine.newBuilder()
                    .maximumSize(10_000)
                    .expireAfterAccess(Duration.ofHours(1))
                    .build();

            this.generateBandwidth = Bandwidth.classic(
                    generateRequests,
                    Refill.intervally(generateRequests, Duration.ofMinutes(generateWindowMinutes))
            );

            this.matchBandwidth = Bandwidth.classic(
                    matchRequests,
                    Refill.intervally(matchRequests, Duration.ofMinutes(matchWindowMinutes))
            );
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, 
                                       HttpServletResponse response, 
                                       FilterChain filterChain) throws ServletException, IOException {
            String path = request.getRequestURI();
            String clientId = getClientId(request);

            if (path.contains("/generate")) {
                Bucket bucket = cache.get(clientId, k -> Bucket.builder().addLimit(generateBandwidth).build());
                if (!bucket.tryConsume(1)) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\"}");
                    return;
                }
            } else if (path.contains("/jobs/match")) {
                Bucket bucket = cache.get(clientId, k -> Bucket.builder().addLimit(matchBandwidth).build());
                if (!bucket.tryConsume(1)) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\"}");
                    return;
                }
            }

            filterChain.doFilter(request, response);
        }

        private String getClientId(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }
            return request.getRemoteAddr();
        }
    }
}

