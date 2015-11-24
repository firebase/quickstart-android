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

var Firebase = require('firebase');
var ref = new Firebase('<DATABASE_URL>');

// Makes all new messages ALL UPPERCASE.
exports.makeuppercase = function(context, data) {

  // Read the Firebase database object that triggered the function.
  var messageRef = ref.child(data.path);
  console.log('Reading firebase object at path: ' + messageRef.toString());
  messageRef.once('value', function(messageData) {

    // Retrieved the message and uppercase it.
    console.log('Retrieved message content: ' + JSON.stringify(messageData.val()));
    var uppercased = messageData.val().text.toUpperCase();

    // Saving the uppercased message to DB.
    console.log('Saving uppercased message: ' + uppercased);
    messageRef.update({text: uppercased}, context.done);

  }, context.done);
};
