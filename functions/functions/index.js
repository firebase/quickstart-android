/**
 * Copyright 2018 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

const {onCall, HttpsError} = require('firebase-functions/v2/https')
const {initializeApp} = require("firebase-admin/app");
const {getDatabase} = require('firebase-admin/database');
const {getMessaging} = require('firebase-admin/messaging');

const sanitizer = require('./sanitizer');

initializeApp();

// [START allAdd]
// [START addFunctionTrigger]
// Adds two numbers to each other.
exports.addNumbers = onCall((request) => {
// [END addFunctionTrigger]
  // [START readAddData]
  // Numbers passed from the client.
  const firstNumber = request.data?.firstNumber;
  const secondNumber = request.data?.secondNumber;
  // [END readAddData]

  // [START addHttpsError]
  // Checking that attributes are present and are numbers.
  if (!Number.isFinite(firstNumber) || !Number.isFinite(secondNumber)) {
    // Throwing an HttpsError so that the client gets the error details.
    throw new HttpsError('invalid-argument', 'The function must be called with ' +
        'two arguments "firstNumber" and "secondNumber" which must both be numbers.');
  }
  // [END addHttpsError]

  // [START returnAddData]
  // returning result.
  return {
    firstNumber: firstNumber,
    secondNumber: secondNumber,
    operator: '+',
    operationResult: firstNumber + secondNumber,
  };
  // [END returnAddData]
});
// [END allAdd]

// [START messageFunctionTrigger]
// Saves a message to the Firebase Realtime Database but sanitizes the text by removing swearwords.
exports.addMessage = onCall((request) => {
  // [START_EXCLUDE]
  // [START readMessageData]
  // Message text passed from the client.
  const text = request.data?.text;
  // [END readMessageData]
  // [START messageHttpsErrors]
  // Checking attribute.
  if (!(typeof text === 'string') || text.length === 0) {
    // Throwing an HttpsError so that the client gets the error details.
    throw new HttpsError('invalid-argument', 'The function must be called with ' +
        'one arguments "text" containing the message text to add.');
  }
  // Checking that the user is authenticated.
  if (!request.auth) {
    // Throwing an HttpsError so that the client gets the error details.
    throw new HttpsError('failed-precondition', 'The function must be called ' +
        'while authenticated.');
  }
  // [END messageHttpsErrors]

  // [START authIntegration]
  // Authentication / user information is automatically added to the request.
  const uid = request.auth.uid;
  const name = request.auth.token.name || null;
  const picture = request.auth.token.picture || null;
  const email = request.auth.token.email || null;
  // [END authIntegration]

  // [START returnMessageAsync]
  // Saving the new message to the Realtime Database.
  const sanitizedMessage = sanitizer.sanitizeText(text); // Sanitize the message.
  return getDatabase().ref('/messages').push({
    text: sanitizedMessage,
    author: { uid, name, picture, email },
  }).then(() => {
    // Optionally send a push notification with the message.
    if (request.data.push && request.instanceIdToken) {
      return getMessaging().send({
        token: request.instanceIdToken,
        data: { text: sanitizedMessage },
      });
    }
  }).then(() => {
    console.log('New Message written');
    return sanitizedMessage;
  }).catch((error) => {
    // Re-throwing the error as an HttpsError so that the client gets the error details.
    throw new HttpsError('unknown', error.message, error);
  });
  // [END returnMessageAsync]
  // [END_EXCLUDE]
});
// [END messageFunctionTrigger]
