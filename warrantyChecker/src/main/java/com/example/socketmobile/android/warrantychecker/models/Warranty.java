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

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Warranty {

    private static final String TAG = Warranty.class.getName();

    public Boolean extensionEligible;

    public String description;

    public Date expiration;

    public Warranty(Boolean extensionEligible, String description, String expiration) {
        this.extensionEligible = extensionEligible;
        this.description = description;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            this.expiration = format.parse(expiration);
        } catch (ParseException e) {
            Log.e(TAG, "Unable to parse date: " + expiration);
            this.expiration = new Date(0);
        }
    }

}