if (config.devServer) {
    config.devServer.proxy = {
        '/ws': {
            target: 'https://sicenet.itsur.edu.mx',
            changeOrigin: true,
            secure: false
        }
    };
}
