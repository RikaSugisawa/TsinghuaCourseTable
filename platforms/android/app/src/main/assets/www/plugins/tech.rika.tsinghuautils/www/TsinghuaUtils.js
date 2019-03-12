cordova.define("tech.rika.tsinghuautils.TsinghuaUtils", function (require, exports, module) {
    var exec = require('cordova/exec');

    exports.login = function (username, password, success, error) {
     return   exec(success, error, 'TsinghuaUtils', 'login', [username, password])
//        return new Promise(function (resolve, reject) {
//            exec(resolve, (_) => {
//                reject(new Error(_))
//            }, 'TsinghuaUtils', 'login', [username, password])
//        })
    };
    exports.getCalendar = function (start, end, success, error) {
    return    exec(success,error,'TsinghuaUtils', 'getCalendar',[start, end])
    };

});
