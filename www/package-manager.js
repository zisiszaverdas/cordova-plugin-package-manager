var exec = require('cordova/exec');

var packageManager = {
    install:function(params, success, fail) {
        exec(function(args) {success(args);}, function(args) {fail(args);}, "PackageManagerPlugin", "install", [params]);
    }
};

module.exports = packageManager;
