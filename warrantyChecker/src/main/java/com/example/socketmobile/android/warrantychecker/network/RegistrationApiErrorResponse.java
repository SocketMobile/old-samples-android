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

import com.example.socketmobile.android.warrantychecker.models.ResponseError;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RegistrationApiErrorResponse {

    private static final String TAG = RegistrationApiErrorResponse.class.getName();

    public String message;

    public String errorDetail;

    public List<ResponseError> modelStateErrors;

    public RegistrationApiErrorResponse(String message, String errorDetail,
            List<ResponseError> modelStateErrors) {
        this.message = message;
        this.errorDetail = errorDetail;
        this.modelStateErrors = modelStateErrors;
    }

    static class Reader {

        RegistrationApiErrorResponse readErrorJsonStream(InputStream stream) throws IOException {
            JsonReader reader;
            reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));

            try {
                return readErrorMessage(reader);
            } finally {
                reader.close();
            }
        }

        private RegistrationApiErrorResponse readErrorMessage(JsonReader reader)
                throws IOException {
            String message = "";
            String errorDetail = "";
            List<ResponseError> modelStateErrors = null;

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("Message")) {
                    message = reader.nextString();
                } else if (name.equals("ErrorMessage")) {
                    errorDetail = reader.nextString();
                } else if (name.equals("ModelState")) {
                    modelStateErrors = readModelState(reader);
                }
            }
            reader.endObject();

            return new RegistrationApiErrorResponse(message, errorDetail, modelStateErrors);
        }

        private List<ResponseError> readModelState(JsonReader reader) throws IOException {
            List<ResponseError> errors = new ArrayList<>();

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                errors.add(readError(name.substring(name.indexOf(".") + 1), reader));
            }
            reader.endObject();

            return errors;
        }

        private ResponseError readError(String field, JsonReader reader) throws IOException {
            ResponseError error = new ResponseError(field);

            reader.beginArray();
            while (reader.hasNext()) {
                error.add(reader.nextString());
            }
            reader.endArray();

            return error;
        }
    }
}
