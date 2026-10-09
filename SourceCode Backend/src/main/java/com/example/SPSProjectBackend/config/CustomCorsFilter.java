package com.example.SPSProjectBackend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

// @Component
// public class CustomCorsFilter implements Filter {
//
////     @Override
////     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
////             throws IOException, ServletException {
////
////         HttpServletRequest req = (HttpServletRequest) request;
////         HttpServletResponse res = (HttpServletResponse) response;
////
////         System.out.println("cors filter applied for request: " + req.getMethod() + " " + req.getRequestURI());
////
////         res.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
////         res.setHeader("Access-Control-Allow-Credentials", "true");
////         res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
////         res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
////
////         if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
////             res.setStatus(HttpServletResponse.SC_OK);
////             return;
////         }
////
////         chain.doFilter(request, response);
////     }
//// }

@Component
public class CustomCorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        System.out.println("cors filter applied for request: " + req.getMethod() + " " + req.getRequestURI());

        // Allow both origins, or use just 8095
        String origin = req.getHeader("Origin");
        if ("http://localhost:8095".equals(origin) || "http://localhost:3000".equals(origin) || "http://10.128.1.59:8097".equals(origin) || "http://127.0.0.1:8095".equals(origin) || "http://10.128.28.29:8095".equals(origin)) {
            res.setHeader("Access-Control-Allow-Origin", origin);
        }

        res.setHeader("Access-Control-Allow-Credentials", "true");
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Content-Length, X-Requested-With");

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(request, response);
    }
}

