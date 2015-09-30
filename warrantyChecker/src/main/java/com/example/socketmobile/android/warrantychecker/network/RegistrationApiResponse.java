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
package com.example.socketmobile.android.warrantychecker.network;

import android.util.JsonReader;

import com.example.socketmobile.android.warrantychecker.models.Warranty;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class RegistrationApiResponse {

    private static final String TAG = RegistrationApiResponse.class.getName();

    public Boolean isRegistered;

    public Warranty warranty;

    public RegistrationApiResponse(Boolean isRegistered, Warranty warranty) {
        this.isRegistered = isRegistered;
        this.warranty = warranty;
    }

    public String getWarrantyDescription() {
        int i = warranty.description.indexOf("(");
        if (i > 0) {
            return warranty.description.substring(0, i);
        } else {
            return warranty.description;
        }
    }

    public String getWarrantyExpiration() {
        DateFormat format = SimpleDateFormat
                .getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        return format.format(warranty.expiration);
    }

    static class Reader {

        RegistrationApiResponse readJsonStream(InputStream stream) throws IOException {
            JsonReader reader;
            reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
            try {
                return readMessage(reader);
            } finally {
                reader.close();
            }
        }

        private RegistrationApiResponse readMessage(JsonReader reader) throws IOException {
            boolean isRegistered = false;
            Warranty warranty = null;

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("IsRegistered")) {
                    isRegistered = reader.nextBoolean();
                } else if (name.equals("Warranty")) {
                    warranty = readWarranty(reader);
                } else {
                    reader.skipValue();
                }
            }
            return new RegistrationApiResponse(isRegistered, warranty);
        }

        private Warranty readWarranty(JsonReader reader) throws IOException {
            Boolean extensionEligible = false;
            String description = null;
            String expiration = null;

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("ExtensionEligible")) {
                    extensionEligible = reader.nextBoolean();
                } else if (name.equals("Description")) {
                    description = reader.nextString();
                } else if (name.equals("EndDate")) {
                    expiration = reader.nextString();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();

            return new Warranty(extensionEligible, description, expiration);
        }
    }
}
