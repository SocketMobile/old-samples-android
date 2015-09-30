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

import android.util.Base64;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class WarrantyCheck {

    private static final String TAG = WarrantyCheck.class.getName();

    String bluetoothMac;

    String developerId;

    String applicationId;

    String hostPlatform;

    String osVersion;

    String firmwareVersion;

    String scanApiType;

    private String baseUrl;

    public WarrantyCheck() {
        this.scanApiType = "ScanAPI SDK";
        this.baseUrl = "https://api.socketmobile.com/v1/scanners/";
    }

    public WarrantyCheck(boolean useSandbox) {
        this();
        if (useSandbox) {
            this.baseUrl = "https://api.socketmobile.com/v1/sandbox/scanners/";
        }
    }

    public WarrantyCheck setBluetoothMac(String value) {
        this.bluetoothMac = value;
        return this;
    }

    public WarrantyCheck setDeveloperId(String value) {
        this.developerId = value;
        return this;
    }

    public WarrantyCheck setApplicationId(String value) {
        this.applicationId = value;
        return this;
    }

    public WarrantyCheck setHostPlatform(String value) {
        this.hostPlatform = value;
        return this;
    }

    public WarrantyCheck setOsVersion(String value) {
        this.osVersion = value;
        return this;
    }

    public WarrantyCheck setScannerFirmwareVersion(String value) {
        this.firmwareVersion = value;
        return this;
    }

    public Object submit() {
        String queryString;

        Log.i(TAG, "Checking warranty of " + bluetoothMac);

        String authString = developerId + ":" + applicationId;

        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("hostPlatform", hostPlatform));
            params.add(new BasicNameValuePair("osVersion", osVersion));

            queryString = getQuery(params);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported value passed as a parameter");
            e.printStackTrace();
            return null;
        }
        try {
            Log.d(TAG, queryString);
            return fetchWarranty(bluetoothMac, authString, queryString);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Object fetchWarranty(String id, String authString, String query) throws IOException {
        InputStream is = null;
        RegistrationApiResponse result = null;
        RegistrationApiErrorResponse errorResult = null;
        String authHeader = "Basic " + Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);

        try {
            URL url = new URL(baseUrl + id
                    + "?" + query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(10000 /* milliseconds */);
            //conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", authHeader);
            conn.setDoInput(true);
            conn.setDoOutput(false);

            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "WarrantyCheck query responded: " + response);
            switch (response / 100) {
                case 2:
                    is = conn.getInputStream();
                    RegistrationApiResponse.Reader reader = new RegistrationApiResponse.Reader();
                    result = reader.readJsonStream(is);
                    break;
                case 4:
                case 5:
                    is = conn.getErrorStream();
                    RegistrationApiErrorResponse.Reader errorReader
                            = new RegistrationApiErrorResponse.Reader();
                    errorResult = errorReader.readErrorJsonStream(is);
                    break;
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return (result != null) ? result : errorResult;
    }

    private String getQuery(List<NameValuePair> pairs) throws UnsupportedEncodingException {
        StringBuilder query = new StringBuilder();
        boolean first = true;

        for (NameValuePair nvp : pairs) {
            if (first) {
                first = false;
            } else {
                query.append("&");
            }

            query.append(URLEncoder.encode(nvp.getName(), "UTF-8"));
            query.append("=");
            query.append(URLEncoder.encode(nvp.getValue(), "UTF-8"));

        }

        return query.toString();
    }

}

