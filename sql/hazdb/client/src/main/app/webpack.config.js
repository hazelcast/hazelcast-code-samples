const path = require('path');

module.exports = {
    entry: './src/index.js',
        mode: 'production',
    output: {
        path: __dirname,
        filename: './static/built/bundle.js'
    },
        module: {
                rules: [
                        {
                                test: path.join(__dirname, '.'),
                                exclude: /(node_modules)/,
                                use: [{
                                        loader: 'babel-loader',
                                        options: {
                                                presets: ["@babel/preset-env", "@babel/preset-react"]
                                        }
                                }]
                        }
                ]
        }
};
