// FILE: src/setupProxy.js
const { createProxyMiddleware } = require("http-proxy-middleware");

module.exports = function (app) {
  // Handle CORS preflight (OPTIONS) requests first
  app.use((req, res, next) => {
    if (req.method === "OPTIONS") {
      console.log("Handling OPTIONS preflight request for:", req.path);
      res.header("Access-Control-Allow-Origin", "*");
      res.header(
        "Access-Control-Allow-Methods",
        "GET, POST, PUT, DELETE, PATCH, OPTIONS",
      );
      res.header(
        "Access-Control-Allow-Headers",
        "Content-Type, Authorization, X-Requested-With",
      );
      res.header("Access-Control-Max-Age", "86400"); // 24 hours
      return res.status(200).end();
    }
    next();
  });

  // Proxy for MonthlyCharge external API - Enhanced with CORS handling
  app.use(
    "/api/MonthlyCharge",
    createProxyMiddleware({
      target: "http://10.128.1.59:5005",
      changeOrigin: true,
      secure: false,
      logLevel: "debug",
      pathRewrite: {
        "^/api/MonthlyCharge": "/api/MonthlyCharge",
      },
      onProxyReq: (proxyReq, req, res) => {
        console.log(`Proxying ${req.method} request to: ${req.originalUrl}`);

        // Add CORS headers to the proxied request
        proxyReq.setHeader("Access-Control-Allow-Origin", "*");
        proxyReq.setHeader(
          "Access-Control-Allow-Methods",
          "GET, POST, PUT, DELETE, PATCH, OPTIONS",
        );
        proxyReq.setHeader(
          "Access-Control-Allow-Headers",
          "Content-Type, Authorization, X-Requested-With",
        );

        // Log request details for debugging
        if (req.method === "POST" && req.body) {
          console.log("Request body:", JSON.stringify(req.body, null, 2));
        }
      },
      onProxyRes: (proxyRes, req, res) => {
        console.log(
          `Received response with status: ${proxyRes.statusCode} for ${req.originalUrl}`,
        );

        // Ensure CORS headers are in response
        proxyRes.headers["Access-Control-Allow-Origin"] = "*";
        proxyRes.headers["Access-Control-Allow-Methods"] =
          "GET, POST, PUT, DELETE, PATCH, OPTIONS";
        proxyRes.headers["Access-Control-Allow-Headers"] =
          "Content-Type, Authorization, X-Requested-With";
        proxyRes.headers["Access-Control-Allow-Credentials"] = "true";
        proxyRes.headers["Access-Control-Expose-Headers"] = "*";

        // Handle 500 errors specifically
        if (proxyRes.statusCode === 500) {
          console.error(`API returned 500 error for: ${req.originalUrl}`);
        }
      },
      onError: (err, req, res) => {
        console.error("Proxy error:", err);
        console.error("Error details:", {
          code: err.code,
          message: err.message,
          url: req.originalUrl,
          method: req.method,
        });

        // Return a JSON error response instead of HTML
        res.status(502).json({
          error: "Proxy error",
          message: "Cannot connect to API server",
          details: err.message,
          timestamp: new Date().toISOString(),
        });
      },
    }),
  );

  // Proxy for internal API (localhost:8080) - Enhanced with CORS handling
  app.use(
    "/api",
    createProxyMiddleware({
      target: "http://localhost:8080",
      changeOrigin: true,
      secure: false,
      logLevel: "debug",
      pathRewrite: {
        "^/api": "/api",
      },
      onProxyReq: (proxyReq, req, res) => {
        console.log(
          `Proxying ${req.method} request to internal API: ${req.originalUrl}`,
        );

        // Add CORS headers to the proxied request
        proxyReq.setHeader("Access-Control-Allow-Origin", "*");
        proxyReq.setHeader(
          "Access-Control-Allow-Methods",
          "GET, POST, PUT, DELETE, PATCH, OPTIONS",
        );
        proxyReq.setHeader(
          "Access-Control-Allow-Headers",
          "Content-Type, Authorization, X-Requested-With",
        );

        // Add Basic Auth header if needed
        if (req.headers.authorization) {
          proxyReq.setHeader("Authorization", req.headers.authorization);
        }
      },
      onProxyRes: (proxyRes, req, res) => {
        console.log(
          `Internal API response: ${proxyRes.statusCode} for ${req.originalUrl}`,
        );

        // Ensure CORS headers are in response
        proxyRes.headers["Access-Control-Allow-Origin"] = "*";
        proxyRes.headers["Access-Control-Allow-Methods"] =
          "GET, POST, PUT, DELETE, PATCH, OPTIONS";
        proxyRes.headers["Access-Control-Allow-Headers"] =
          "Content-Type, Authorization, X-Requested-With";
        proxyRes.headers["Access-Control-Allow-Credentials"] = "true";
      },
      onError: (err, req, res) => {
        console.error("Internal API proxy error:", err);
        console.error("Error details:", {
          code: err.code,
          message: err.message,
          url: req.originalUrl,
          method: req.method,
        });

        // Return a JSON error response
        res.status(502).json({
          error: "Internal API proxy error",
          message: "Cannot connect to internal API server",
          details: err.message,
          timestamp: new Date().toISOString(),
        });
      },
    }),
  );

  // Additional proxy for any other external APIs if needed
  app.use(
    "/external-api",
    createProxyMiddleware({
      target: "http://10.128.1.59:5005",
      changeOrigin: true,
      secure: false,
      logLevel: "debug",
      pathRewrite: {
        "^/external-api": "",
      },
      onProxyReq: (proxyReq, req, res) => {
        proxyReq.setHeader("Access-Control-Allow-Origin", "*");
        proxyReq.setHeader(
          "Access-Control-Allow-Methods",
          "GET, POST, PUT, DELETE, PATCH, OPTIONS",
        );
        proxyReq.setHeader(
          "Access-Control-Allow-Headers",
          "Content-Type, Authorization, X-Requested-With",
        );
      },
      onProxyRes: (proxyRes, req, res) => {
        proxyRes.headers["Access-Control-Allow-Origin"] = "*";
        proxyRes.headers["Access-Control-Allow-Methods"] =
          "GET, POST, PUT, DELETE, PATCH, OPTIONS";
        proxyRes.headers["Access-Control-Allow-Headers"] =
          "Content-Type, Authorization, X-Requested-With";
        proxyRes.headers["Access-Control-Allow-Credentials"] = "true";
      },
    }),
  );

  // Proxy for Centralized HR Authentication API
  app.use(
    "/CBRSAPI",
    createProxyMiddleware({
      target: "http://10.128.1.126",
      changeOrigin: true,
      secure: false,
      logLevel: "debug",
      onProxyRes: (proxyRes) => {
        proxyRes.headers["Access-Control-Allow-Origin"] = "*";
      },
    }),
  );

  // Proxy for Centralized AD Authentication API
  app.use(
    "/SMART_API",
    createProxyMiddleware({
      target: "http://smartceb.ceb:81",
      changeOrigin: true,
      secure: false,
      logLevel: "debug",
      onProxyRes: (proxyRes) => {
        proxyRes.headers["Access-Control-Allow-Origin"] = "*";
      },
    }),
  );

  // Health check endpoint
  app.use("/health", (req, res) => {
    res.json({
      status: "ok",
      timestamp: new Date().toISOString(),
      proxies: [
        {
          path: "/api/MonthlyCharge",
          target: "http://10.128.1.59:5005",
          status: "active",
        },
        {
          path: "/api",
          target: "http://localhost:8080",
          status: "active",
        },
      ],
    });
  });

  console.log("Proxy middleware configured with CORS support");
  console.log("Routes:");
  console.log("  /api/MonthlyCharge/* -> http://10.128.1.59:5005");
  console.log("  /api/* -> http://localhost:8080");
  console.log("  /external-api/* -> http://10.128.1.59:5005");
  console.log("  /health -> health check endpoint");
};
