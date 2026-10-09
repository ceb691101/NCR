module.exports = {
  webpack: {
    configure: {
      resolve: {
        fallback: {
          http: false,
          https: false,
          zlib: false,
          stream: false,
          crypto: false,
          url: false,
          assert: false,
          util: false,
        },
      },
    },
  },
  devServer: {
    proxy: [
      // local
      {
        context: ["/api"],
        target: "http://localhost:8080",
        changeOrigin: true,
        pathRewrite: {
          "^/api": "/HSB/api",
        },
      },
      // live
      // {
      //   context: ["/api"],
      //   target: "http://10.128.1.59:8080",
      //   changeOrigin: true,
      //   pathRewrite: {
      //     "^/api": "/HSB/api",
      //   },
      // },
      // dot net
      {
        context: ["/dotnetapi"],
        target: "http://10.128.1.59:5005",
        changeOrigin: true,
        pathRewrite: {
          "^/dotnetapi": "/api",
        },
      },
    ],
  },
};
