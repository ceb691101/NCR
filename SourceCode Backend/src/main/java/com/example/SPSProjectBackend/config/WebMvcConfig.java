package com.example.SPSProjectBackend.config;

import com.example.SPSProjectBackend.security.AuthorizationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthorizationInterceptor authorizationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorizationInterceptor)
                .addPathPatterns("/api/**")
                // These patterns are matched against the path WITHIN the application,
                // so they must NOT include the servlet context path ("/NCRE" when the
                // app is deployed as NCRE.war). Keep this list in sync with
                // AuthorizationInterceptor.PUBLIC_ENDPOINTS.
                .excludePathPatterns(
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
    }
}
