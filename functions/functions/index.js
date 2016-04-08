/**
 * Copyright 2015 Google Inc. All Rights Reserved.
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

var functions = require('firebase-functions');

// Makes all new messages ALL UPPERCASE.
exports.makeUppercase = functions.database()
    .path('/messages/{messageId}').on('write', function(event) {
      // Reference to the database object that triggered the function.
      // This reference is authorized as the user who initiated the write that triggered the function.
      var messageRef = event.data.ref();
      console.log('Reading firebase object at path: ' + messageRef.toString());

      // The Firebase database object that triggered the function.
      var messageDataValue = event.data.val();
      console.log('Message content: ' + JSON.stringify(messageDataValue));

      // Uppercase the message.
      var uppercased = messageDataValue.text.toUpperCase();

      // Saving the uppercased message to DB.
      console.log('Saving uppercased message: ' + uppercased);
      return messageRef.update({text: uppercased});
    });
