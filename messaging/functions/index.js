'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

const EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 30; // 30 days

/**
 * Scheduled function that runs once a month. It updates the last refresh date for
 * tokens so that a client can refresh the token if the last time it did so was
 * before the refresh date.
 */
// [START refresh_date_scheduled_function]
exports.scheduledFunction = functions.pubsub.schedule('0 0 1 * *').onRun((context) => { 
    admin.firestore().doc('refresh/refreshDate').set({ lastRefreshDate : Date.now() });
});
// [END refresh_date_scheduled_function]

/**
 * Scheduled function that runs once a day. It retrieves all stale tokens then
 * unsubscribes them from 'topic1' then deletes them.
 *
 * Note: weather is an example topic here. It is up to the developer to unsubscribe
 * all topics the token is subscribed to.
 */
// [START remove_stale_tokens]
exports.pruneTokens = functions.pubsub.schedule('every 24 hours').onRun(async (context) => {
  const staleTokensResult = await admin.firestore().collection('fcmTokens')
      .where("timestamp", "<", Date.now() - EXPIRATION_TIME)
      .get();

  const staleTokens = staleTokensResult.docs.map(staleTokenDoc => staleTokenDoc.id);

  await admin.messaging().unsubscribeFromTopic(staleTokens, 'weather');

  const deletePromises = [];
  for (const staleTokenDoc of staleTokensResult.docs) {
      deletePromises.push(staleTokenDoc.ref.delete());
  }
  await Promise.all(deletePromises);
});
// [END remove_stale_tokens]
