// vue.config.js
module.exports = {
    devServer: {
        host: '0.0.0.0',
        port: 8080,
        proxy: {
            '/rest': {
                target: 'http://localhost:8088/rest', 
                changeOrigin: true,
                ws: true,
                pathRewrite: {
                    '^/rest': ''
                }
            }
        }
    }
}