import { Amplify } from 'aws-amplify';
import { fetchAuthSession, signOut } from 'aws-amplify/auth';
import { cognitoUserPoolsTokenProvider } from 'aws-amplify/auth/cognito';
import { CookieStorage } from 'aws-amplify/utils';

var config = window._config || {};

if (config.cognito) {
    Amplify.configure({
        Auth: {
            Cognito: {
                userPoolId: config.cognito.userPoolId,
                userPoolClientId: config.cognito.userPoolClientId,
                identityPoolId: config.cognito.identityPoolId,
            }
        }
    });

    cognitoUserPoolsTokenProvider.setKeyValueStorage(new CookieStorage({
        domain: '.cowtool.com',
        secure: true,
        sameSite: 'lax',
    }));
}

window.SqcCalculator = window.SqcCalculator || {};

window.SqcCalculator.signOut = async function signOutUser() {
    try {
        await signOut();
    } catch (err) {
        console.error('Error signing out:', err);
    }
};

const authPromise = fetchAuthSession()
    .then(session => {
        const idToken = session.tokens?.idToken?.toString();
        return idToken || null;
    })
    .catch(err => {
        console.warn('Could not fetch Cognito auth session:', err);
        return null;
    });

if (typeof window.SqcCalculator._resolveAuthToken === 'function') {
    authPromise.then(token => window.SqcCalculator._resolveAuthToken(token));
} else {
    window.SqcCalculator.authToken = authPromise;
}