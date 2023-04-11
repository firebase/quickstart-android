'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

const EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 60; // 60 days

/**
 * Scheduled function that runs once a day. It retrieves all stale tokens then
 * unsubscribes them from 'topic1' then deletes them.
 *
 * Note: weather is an example topic here. It is up to the developer to unsubscribe
 * all topics the token is subscribed to.
 */
// [START remove_stale_tokens]
exports.pruneTokens = functions.pubsub.schedule('every 24 hours').onRun(async (context) => {
  // Get all documents where the timestamp exceeds is not within the past month
  const staleTokensResult = await admin.firestore().collection('fcmTokens')
      .where("timestamp", "<", Date.now() - EXPIRATION_TIME)
      .get();
  // Delete devices with stale tokens
  staleTokensResult.forEach(function(doc) { doc.ref.delete(); });
});
// [END remove_stale_tokens]
