// vue.config.js
module.exports = {
    // 基本路径
    publicPath: 'business/static',
    devServer: {
        host: '0.0.0.0',
        port: 8080,
        proxy: {
            '/business': {
                target: 'http://localhost:8088/business',
                changeOrigin: true,
                ws: true,
                pathRewrite: {
                    '^/business': ''
                }
            }
        }
    }
}