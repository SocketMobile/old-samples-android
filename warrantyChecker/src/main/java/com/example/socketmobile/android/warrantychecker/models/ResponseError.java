/*
 * Copyright 2015 Socket Mobile, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.socketmobile.android.warrantychecker.models;

import java.util.ArrayList;
import java.util.List;

public class ResponseError {

    public String field;

    public List<String> messages;

    public ResponseError(String field) {
        this.field = field;
        messages = new ArrayList<>();
    }

    public void add(String message) {
        messages.add(message);
    }

    public String getMessages() {
        if (messages.size() == 1) {
            return messages.get(0);
        } else {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (String msg : messages) {
                if (!first) {
                    sb.append("\n");
                }
                sb.append(msg);
            }
            return sb.toString();
        }
    }

}