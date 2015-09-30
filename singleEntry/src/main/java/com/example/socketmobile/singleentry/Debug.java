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
package com.example.socketmobile.singleentry;

import android.util.Log;

public final class Debug {

    public static final int kLevelTrace = 1;

    public static final int kLevelWarning = 2;

    public static final int kLevelError = 3;

    private static String kTag = "SingleEntry";

    public static void MSG(int level, String expression) {
        if (level == Debug.kLevelTrace) {
            Log.d(kTag, expression);
        } else if (level == Debug.kLevelWarning) {
            Log.w(kTag, expression);
        } else if (level == Debug.kLevelError) {
            Log.e(kTag, expression);
        } else {
            Log.v(kTag, expression);
        }
    }
}
