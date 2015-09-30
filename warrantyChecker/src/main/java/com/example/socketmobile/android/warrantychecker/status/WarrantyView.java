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
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.socketmobile.android.scanapi.SingleEntryApplication;
import com.example.socketmobile.android.warrantychecker.R;
import com.example.socketmobile.android.warrantychecker.form.RegistrationForm;
import com.example.socketmobile.android.warrantychecker.network.RegistrationApiResponse;

public class WarrantyView extends Activity
        implements WarrantyViewStateFragment.StateChangeListener {

    private static final String TAG = WarrantyView.class.getSimpleName();

    private static final int REGISTRATION_REQUEST = 1 << 3;

    // Private variables
    private WarrantyViewStateFragment state;

    BroadcastReceiver deviceArrivalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SingleEntryApplication.NOTIFY_SCANNER_ARRIVAL)) {
                if (intent.hasExtra(SingleEntryApplication.EXTRA_BDADDRESS)) {
                    state.setScanner(
                            intent.getStringExtra(SingleEntryApplication.EXTRA_BDADDRESS)
                    );
                }
            }
        }
    };

    // BroadcastReceivers
    BroadcastReceiver bluetoothStateChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        bluetoothDisabled();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        bluetoothEnabled();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };

    private DialogFragment mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warranty_view);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        state = (WarrantyViewStateFragment) fm.findFragmentByTag(WarrantyViewStateFragment.TAG);

        if (state == null) {
            state = new WarrantyViewStateFragment();
            ft.add(state, WarrantyViewStateFragment.TAG);
        }

        if (savedInstanceState == null) {
            ft.add(R.id.container, new WarrantyViewFragment(), WarrantyViewFragment.TAG);
        }

        ft.commit();

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (BluetoothAdapter.getDefaultAdapter() != null &&
                BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            bluetoothEnabled();
        } else {
            bluetoothDisabled();
        }

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateChanged, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(bluetoothStateChanged);
        stopScanApi(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.warranty_view, menu);

        if (state != null &&
                state.getWarranty() != null &&
                !state.getWarranty().isRegistered) {
            menu.findItem(R.id.action_register)
                    .setEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_register:
                Intent intent = state.getRegistrationIntent();
                if (intent != null) {
                    startActivityForResult(intent, REGISTRATION_REQUEST);
                }
                return true;
            case R.id.action_reset:
                state.reset();
                return true;
            default:
                String bda = item.getTitle().toString();
                if (state != null && bda.length() == 12) {
                    state.setScanner(bda, true);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REGISTRATION_REQUEST:
                if (resultCode == RESULT_OK) {
                    if (data.hasExtra(RegistrationForm.OUT_WARRANTY_EXPIRATION_DATE)) {
                        state.updateWarrantyExpiration(
                                data.getLongExtra(RegistrationForm.OUT_WARRANTY_EXPIRATION_DATE, 0)
                        );
                    }
                }
                break;
        }
    }

    private void bluetoothDisabled() {
        state.setBluetoothEnabled(false);
        stopScanApi();
    }

    private void bluetoothEnabled() {
        state.setBluetoothEnabled(true);
        startScanApi();
    }

    private void startScanApi() {
        state.setScanApiRunning(true);
        SingleEntryApplication.getApplicationInstance().increaseViewCount();
        IntentFilter filter = new IntentFilter(SingleEntryApplication.NOTIFY_SCANNER_ARRIVAL);
        registerReceiver(deviceArrivalReceiver, filter);
    }

    @Override
    public void stateUpdated() {
        updateUI();
    }

    private void stopScanApi() {
        stopScanApi(false);
    }

    private void stopScanApi(boolean isStopping) {
        if (!isStopping) {
            state.setScanApiRunning(false);
        }
        SingleEntryApplication.getApplicationInstance().decreaseViewCount();
        try {
            unregisterReceiver(deviceArrivalReceiver);
        } catch (IllegalArgumentException e) {
            Log.d(TAG,
                    "deviceArrivalReceiver not registered. Bluetooth was not enabled or receiver was already unregistered");
        }
    }

    private void updateUI() {

        FragmentTransaction ft = getFragmentManager().beginTransaction();

        if (mDialog != null) {
            Log.v(TAG, "Removing dialog fragment");
            mDialog.dismiss();
        }

        if (state.isCheckingWarranty()) {
            Log.v(TAG, "Showing fetching warranty dialog");
            mDialog = LoadingDialogFragment.newInstance(getString(R.string.warranty_loading));
            mDialog.show(ft, "dialog");
            // Dialog show commits the open transaction
        } else {
            ft.commit();
            WarrantyViewFragment view = (WarrantyViewFragment) getFragmentManager()
                    .findFragmentByTag(WarrantyViewFragment.TAG);
            view.updateWarranty(state.getWarranty());
            view.updateFlashes(state.getFlashes());
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class WarrantyViewFragment extends Fragment {

        static final String TAG = WarrantyViewFragment.class.getName();

        private LinearLayout warrantyInfo;

        private ImageView headerImage;

        private TextView warrantyDescription;

        private TextView expirationDate;

        private TextView regStatus;

        private TextView flashes;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_warranty_view, container, false);

            headerImage = (ImageView) rootView.findViewById(R.id.imageView);

            flashes = (TextView) rootView.findViewById(R.id.flash);

            warrantyInfo = (LinearLayout) rootView.findViewById(R.id.warranty_info);
            warrantyDescription = (TextView) rootView.findViewById(R.id.warranty_description);
            expirationDate = (TextView) rootView.findViewById(R.id.warranty_end);
            regStatus = (TextView) rootView.findViewById(R.id.registration_status);

            // Display current values
            WarrantyViewStateFragment state = (WarrantyViewStateFragment) getFragmentManager()
                    .findFragmentByTag(WarrantyViewStateFragment.TAG);
            updateWarranty(state.getWarranty());
            updateFlashes(state.getFlashes());

            return rootView;
        }

        public void updateWarranty(RegistrationApiResponse response) {

            if (response == null) {
                headerImage.setImageDrawable(getResources().getDrawable(R.drawable.seven_ci_bw));
                warrantyInfo.setVisibility(View.INVISIBLE);
                return;
            }

            headerImage.setImageDrawable(getResources().getDrawable(R.drawable.seven_ci));
            warrantyInfo.setVisibility(View.VISIBLE);
            warrantyDescription.setText(response.getWarrantyDescription());
            expirationDate.setText(String.format(
                            getString(R.string.warranty_end),
                            response.getWarrantyExpiration()
                    )
            );
            if (response.isRegistered) {
                regStatus.setText(getString(R.string.registration_status_registered));
                regStatus.setTextAppearance(getActivity(), R.style.ScannerRegistered);
            } else {
                regStatus.setText(getString(R.string.registration_status_unregistered));
                regStatus.setTextAppearance(getActivity(), R.style.ScannerUnregistered);
            }

            getActivity().invalidateOptionsMenu();
        }

        public void updateFlashes(String flashString) {
            flashes.setText((flashString != null) ? flashString : "");
        }
    }

}
