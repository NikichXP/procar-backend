const webpack = require("webpack");

config.plugins.push(
    new webpack.DefinePlugin({
        GATEWAY_BASE_URL: JSON.stringify(process.env.GATEWAY_BASE_URL || "http://localhost:8080")
    })
);
