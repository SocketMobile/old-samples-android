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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class UserRegistration {

    private static final String TAG = UserRegistration.class.getName();

    String bluetoothMac;

    String developerId;

    String applicationId;

    String userName;

    String userEmail;

    String userCompany;

    String userAddress;

    String userCity;

    String userState;

    String userZipcode;

    String userCountry;

    String userIndustry;

    boolean isPurchaser;

    String whrPurchased;

    boolean useSoftscan;

    private String baseUrl;

    public UserRegistration() {
        this(false);
    }

    public UserRegistration(boolean useSandbox) {
        if (useSandbox) {
            this.baseUrl = "https://api.socketmobile.com/v1/sandbox/scanners/";
        } else {
            this.baseUrl = "https://api.socketmobile.com/v1/scanners/";
        }

    }

    public UserRegistration setBluetoothMac(String bluetoothMac) {
        this.bluetoothMac = bluetoothMac;
        return this;
    }

    public UserRegistration setDeveloperId(String developerId) {
        this.developerId = developerId;
        return this;
    }

    public UserRegistration setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public UserRegistration setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public UserRegistration setUserEmail(String userEmail) {
        this.userEmail = userEmail;
        return this;
    }

    public UserRegistration setUserCompany(String userCompany) {
        this.userCompany = userCompany;
        return this;
    }

    public UserRegistration setUserAddress(String userAddress) {
        this.userAddress = userAddress;
        return this;
    }

    public UserRegistration setUserCity(String userCity) {
        this.userCity = userCity;
        return this;
    }

    public UserRegistration setUserState(String userState) {
        this.userState = userState;
        return this;
    }

    public UserRegistration setUserZipcode(String userZipcode) {
        this.userZipcode = userZipcode;
        return this;
    }

    public UserRegistration setUserCountry(String userCountry) {
        this.userCountry = userCountry;
        return this;
    }

    public UserRegistration setUserIndustry(String userIndustry) {
        this.userIndustry = userIndustry;
        return this;
    }

    public UserRegistration setPurchaser(boolean isPurchaser) {
        this.isPurchaser = isPurchaser;
        return this;
    }

    public UserRegistration setWhrPurchased(String whrPurchased) {
        this.whrPurchased = whrPurchased;
        return this;
    }

    public UserRegistration setUseSoftscan(boolean useSoftscan) {
        this.useSoftscan = useSoftscan;
        return this;
    }

    public Object submit() {
        String queryString;
        String authString = developerId + ":" + applicationId;

        Log.i(TAG, "Submitting registration for " + bluetoothMac);

        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("userName", userName));
            params.add(new BasicNameValuePair("userEmail", userEmail));
            params.add(new BasicNameValuePair("userCity", userCity));
            params.add(new BasicNameValuePair("userState", userState));
            params.add(new BasicNameValuePair("userZipcode", userZipcode));
            params.add(new BasicNameValuePair("userCountry", userCountry));
            params.add(new BasicNameValuePair("userIndustry", userIndustry));
            if (isPurchaser) {
                params.add(new BasicNameValuePair("isPurchaser", ""));
            }
            params.add(new BasicNameValuePair("whrPurchased", whrPurchased));
            if (useSoftscan) {
                params.add(new BasicNameValuePair("useSoftscan", ""));
            }

            queryString = getQuery(params);

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported value passed as a parameter");
            e.printStackTrace();
            return null;
        }
        try {
            Log.i(TAG, "Attempting to register " + bluetoothMac);
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
        String authHeader = "Basic " +
                Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);

        try {
            URL url = new URL(baseUrl + id + "/registrations");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(10000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", authHeader);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8")
            );
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            conn.connect();
            int response = conn.getResponseCode();

            Log.d(TAG, "Warranty query responded: " + response);
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

        } catch (IOException e) {
            e.printStackTrace();
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
            if (nvp.getValue() != null) {
                query.append(URLEncoder.encode(nvp.getValue(), "UTF-8"));
            }

        }

        return query.toString();
    }
}

