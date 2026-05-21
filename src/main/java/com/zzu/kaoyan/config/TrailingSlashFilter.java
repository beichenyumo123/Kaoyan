package com.zzu.kaoyan.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TrailingSlashFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri.length() > 1 && uri.endsWith("/")) {
            String trimmedUri = uri.substring(0, uri.length() - 1);
            request = new HttpServletRequestWrapper(request) {
                @Override
                public String getRequestURI() {
                    return trimmedUri;
                }

                @Override
                public String getServletPath() {
                    String path = super.getServletPath();
                    return path.length() > 1 && path.endsWith("/")
                            ? path.substring(0, path.length() - 1)
                            : path;
                }
            };
        }
        filterChain.doFilter(request, response);
    }
}
