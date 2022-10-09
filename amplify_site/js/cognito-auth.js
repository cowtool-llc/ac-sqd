/*global SqdCalculator _config AmazonCognitoIdentity AWSCognito*/

var SqdCalculator = window.SqdCalculator || {};

(function scopeWrapper($) {
    var signinUrl = 'https://www.cowtool.com/';

    var poolData = {
        UserPoolId: _config.cognito.userPoolId,
        ClientId: _config.cognito.userPoolClientId,
        Storage: new AmazonCognitoIdentity.CookieStorage({secure: true, domain: '.cowtool.com'})
    };

    var userPool = new AmazonCognitoIdentity.CognitoUserPool(poolData);

    if (typeof AWSCognito !== 'undefined') {
        AWSCognito.config.region = _config.cognito.region;
    }

    SqdCalculator.signOut = function signOut() {
        userPool.getCurrentUser().signOut();
    };

    SqdCalculator.authToken = new Promise(function fetchCurrentAuthToken(resolve, reject) {
        var cognitoUser = userPool.getCurrentUser();

        if (cognitoUser) {
            cognitoUser.getSession(function sessionCallback(err, session) {
                if (err) {
                    reject(err);
                } else if (!session.isValid()) {
                    resolve(null);
                } else {
                    resolve(session.getIdToken().getJwtToken());
                }
            });
        } else {
            resolve(null);
        }
    });
}(jQuery));
