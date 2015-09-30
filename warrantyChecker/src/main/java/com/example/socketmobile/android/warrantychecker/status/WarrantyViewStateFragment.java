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
package com.example.socketmobile.android.warrantychecker.status;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.socketmobile.android.warrantychecker.R;
import com.example.socketmobile.android.warrantychecker.form.RegistrationForm;
import com.example.socketmobile.android.warrantychecker.network.RegistrationApiErrorResponse;
import com.example.socketmobile.android.warrantychecker.network.RegistrationApiResponse;
import com.example.socketmobile.android.warrantychecker.network.WarrantyCheck;

import java.util.Date;

/**
 * Viewless Fragment containing activity state
 */
public class WarrantyViewStateFragment extends Fragment {

    class FetchWarrantyTask extends AsyncTask<WarrantyCheck, Void, Object> {

        @Override
        protected Object doInBackground(WarrantyCheck... params) {
            return params[0].submit();
        }

        @Override
        protected void onCancelled() {
            warrantyTask = null;
            notifyListener();
        }

        @Override
        protected void onPostExecute(Object response) {
            warrantyTask = null;

            if (response == null) {
                Log.i(TAG, "Warranty check failed for unknown reason");
                flashes = "Oops! We are unable to check your warranty at this time.";
                notifyListener();
                return;
            }

            Log.i(TAG, "Warranty check completed.");
            if (response.getClass() == RegistrationApiResponse.class) {
                warrantyData = (RegistrationApiResponse) response;
            } else if (response.getClass() == RegistrationApiErrorResponse.class) {
                flashes = getString(
                        R.string.warranty_check_failed); // "Oops! We are unable to check your warranty at this time. Please contact Socket Mobile support for further assistance."
            }
            notifyListener();
        }
    }

    public interface StateChangeListener {

        void stateUpdated();
    }

    public static final String TAG = WarrantyView.class.getName();

    private static final String PREFERENCES = "WarrantyInfo";

    private static final String BLUETOOTH_MAC = "bt_mac";

    StateChangeListener mListener;

    // Tasks
    FetchWarrantyTask warrantyTask;

    // private
    private boolean bluetoothEnabled;

    private boolean scanApiRunning;

    private String bluetoothMac;

    private final static String developerId = "ed0587a9-d1ed-4638-bb4c-34e88780f047";

    private String applicationId;

    private RegistrationApiResponse warrantyData;

    private String flashes;

    private void checkWarranty(boolean useSandbox) {

        warrantyData = null;
        flashes = null;

        Log.i(TAG, "Checking warranty for " + bluetoothMac);
        warrantyTask = new FetchWarrantyTask();
        warrantyTask.execute(new WarrantyCheck(useSandbox)
                .setBluetoothMac(bluetoothMac).setDeveloperId(developerId)
                .setApplicationId(applicationId).setHostPlatform("Android")
                .setOsVersion(Build.VERSION.RELEASE));
        notifyListener();
    }

    public String getFlashes() {
        int resId = 0;
        if (!bluetoothEnabled && warrantyData == null) {
            resId = R.string.enable_bluetooth;
        } else if (bluetoothMac == null) {
            resId = R.string.waiting_for_device;
        }
        return (resId != 0) ? getString(resId) : flashes;
    }

    public Intent getRegistrationIntent() {
        if (bluetoothMac != null && developerId != null) {
            Intent i = new Intent(getActivity(), RegistrationForm.class);
            i.putExtra(RegistrationForm.EXTRA_BLUETOOTH_MAC, bluetoothMac);
            i.putExtra(RegistrationForm.EXTRA_DEVELOPER_ID, developerId);
            i.putExtra(RegistrationForm.EXTRA_APPLICATION_ID,
                    WarrantyView.class.getPackage().getName());
            return i;
        }
        return null;
    }

    public RegistrationApiResponse getWarranty() {
        return warrantyData;
    }

    public boolean isCheckingWarranty() {
        return warrantyTask != null;
    }

    private void notifyListener() {
        if (mListener != null) {
            mListener.stateUpdated();
        } else {
            Log.i(TAG, "notifyChange called, but nobody is listening");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (StateChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement WarrantyViewStateFragment.StateChangedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        applicationId = getClass().getPackage().getName();

        // Default to last known scanner
        try {
            SharedPreferences prefs = getActivity().getSharedPreferences(
                    PREFERENCES, Context.MODE_PRIVATE);
            bluetoothMac = prefs.getString(BLUETOOTH_MAC, null);
        } catch (NullPointerException e) {
            Log.d(TAG, "No sharedprefs");
        }

        if (bluetoothMac != null) {
            boolean useSandbox = bluetoothMac.startsWith("000555");
            checkWarranty(useSandbox);
        } else {
            Log.i(TAG, "No cached registration found.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (bluetoothMac != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences(
                    PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(BLUETOOTH_MAC, bluetoothMac);
            editor.apply();
        }
    }

    public void reset() {
        if (warrantyTask != null) {
            warrantyTask.cancel(true);
        }
        this.bluetoothMac = null;
        this.warrantyData = null;
        this.flashes = "";
        getActivity().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .edit().clear().commit();

        notifyListener();
    }

    public void setBluetoothEnabled(boolean status) {
        this.bluetoothEnabled = status;
        notifyListener();
    }

    public void setScanApiRunning(boolean status) {
        this.scanApiRunning = status;
        notifyListener();
    }

    public void setScanner(String bluetoothMac) {
        setScanner(bluetoothMac, false);
    }

    public void setScanner(String bluetoothMac, boolean useSandbox) {
        this.bluetoothMac = bluetoothMac;
        checkWarranty(useSandbox);

        // checkWarranty will notifyListener
    }

    public void updateWarrantyExpiration(long ms) {
        if (ms > warrantyData.warranty.expiration.getTime()) {
            warrantyData.warranty.extensionEligible = false;
        }
        warrantyData.warranty.expiration = new Date(ms);
        warrantyData.isRegistered = true;
        notifyListener();
    }
}