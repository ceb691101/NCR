package com.example.SPSProjectBackend.security;

import com.example.SPSProjectBackend.service.NcreAuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

@Component
public class AuthorizationInterceptor implements HandlerInterceptor {

    @Autowired
    private NcreAuthorizationService authorizationService;

    // Endpoints that do not require session authorization checks
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/v1/secinfo/login",
            "/api/v1/secinfo/logout",
            "/api/v1/secinfo/validate-session",
            "/api/v1/secinfo/health",
            "/api/v1/secinfo/get-user-location",
            "/api/v1/secinfo/permitted-areas",
            "/api/v1/secinfo/select-area",
            "/api/v1/auth/login",
            "/api/v1/auth/permissions",
            "/api/v1/auth/check-access",
            "/api/journals/health"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Always permit OPTIONS requests for CORS preflight
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        // getRequestURI() includes the servlet context path (e.g. "/NCRE" when the app
        // is deployed as NCRE.war), so it must be stripped before comparing against the
        // context-path-free patterns in PUBLIC_ENDPOINTS.
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String pathWithinApp = (contextPath != null && !contextPath.isEmpty()
                && requestURI.startsWith(contextPath))
                        ? requestURI.substring(contextPath.length())
                        : requestURI;

        // Allow public auth endpoints
        for (String publicPath : PUBLIC_ENDPOINTS) {
            if (pathWithinApp.equals(publicPath) || pathWithinApp.startsWith(publicPath + "/")) {
                return true;
            }
        }

        // Extract session ID from headers or params
        String sessionId = request.getHeader("X-Session-Id");
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = request.getHeader("session_id");
        }
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = request.getHeader("Session-Id");
        }
        if (sessionId == null || sessionId.trim().isEmpty()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                sessionId = authHeader.substring(7).trim();
            }
        }
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = request.getParameter("session_id");
        }
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = request.getParameter("sessionId");
        }

        if (sessionId == null || sessionId.trim().isEmpty()) {
            sendForbiddenResponse(response, "Authorization session ID missing. Please log in.");
            return false;
        }

        Optional<String> userIdOpt = authorizationService.getUserIdFromSession(sessionId);
        if (!userIdOpt.isPresent()) {
            sendForbiddenResponse(response, "Session invalid or expired. Access denied.");
            return false;
        }

        String userId = userIdOpt.get();
        request.setAttribute("authenticated_user_id", userId);
        request.setAttribute("session_id", sessionId);

        // Check if user has active permissions in NCRE
        var activePerms = authorizationService.getUserActivePermissions(userId, NcreAuthorizationService.DEFAULT_APPL_ID);
        if (activePerms == null || activePerms.isEmpty()) {
            sendForbiddenResponse(response, "User has no active permissions in NCRE system.");
            return false;
        }

        return true;
    }

    private void sendForbiddenResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(String.format("{\"success\":false,\"message\":\"%s\",\"error\":\"FORBIDDEN\"}", message));
        writer.flush();
    }
}
