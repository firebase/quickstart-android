'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();
exports.scheduledFunction = functions.pubsub.schedule('0 0 1 * *').onRun((context) => { 
    admin.firestore().doc('refresh/refreshDate').set({ lastRefreshDate : Date.now() });
});
