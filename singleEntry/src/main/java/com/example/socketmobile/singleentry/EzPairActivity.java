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

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Set;

/**
 * EzPairActivity
 *
 * This activity displays a list of already paired Bluetooth devices. In order to pair a Bluetooth
 * devices, go to the Bluetooth Settings and discover the Bluetooth devices to pair with it. Then
 * going back to this activity to select the scanner you would like to connect to and click the
 * "pair to scanner" button. This will start the EZ Pair process.
 *
 * This Activity doesn't have any particular code for the Screen rotation so it is safe to be
 * recreated at each rotation. The Application object is the one maintaining states.
 *
 * @author EricG
 */
public class EzPairActivity extends Activity {

    private final int PROGRESS_DIALOG = 1;

    private ArrayAdapter<String> _adapterDevices;

    private String _deviceSelectedToPairWith;

    private String _hostBluetoothAddress;

    private CheckedTextView _previousSelection;

    private Context _context;

    /**
     * Progress is a Progress Dialog used to display some UI while EZ Pair is processing
     *
     * @author EricG
     */
    private class Progress extends ProgressDialog {

        public Progress(Context context) {
            super(context);
        }

        /**
         * @see android.app.ProgressDialog#onStart()
         *
         * Start the EZ Pair process.
         */
        @Override
        public void onStart() {
            super.onStart();

            // THIS IS THE STARTING POINT OF EZ PAIR PROCESS
            Intent intent = new Intent(SingleEntryApplication.START_EZ_PAIR);
            // remove the bluetooth address and keep only the device friendly name
            if (_deviceSelectedToPairWith != null) {
                if (_deviceSelectedToPairWith.length() > 18) {
                    _deviceSelectedToPairWith = _deviceSelectedToPairWith
                            .substring(0, _deviceSelectedToPairWith.length() - 18);
                }
                intent.putExtra(SingleEntryApplication.EXTRA_EZ_PAIR_DEVICE,
                        _deviceSelectedToPairWith);
                intent.putExtra(SingleEntryApplication.EXTRA_EZ_PAIR_HOST_ADDRESS,
                        _hostBluetoothAddress);
                sendBroadcast(intent);
            }
        }

        /**
         * @see android.app.Activity#onStop()
         *
         * Stop the EZ Pair process. This will restore ScanAPI Configuration to its original
         * settings
         */
        @Override
        protected void onStop() {
            super.onStop();

            // THIS WILL STOP THE EZ PAIR PROCESS
            Intent intent = new Intent(SingleEntryApplication.STOP_EZ_PAIR);
            sendBroadcast(intent);
        }

    }

    ;

    private Progress _progress;

    /**
     * handler to receive the broadcast of ERROR MESSAGE or EZ PAIR COMPLETED from the application
     * object. In both cases, the progress dialog is dismissed.
     */
    private BroadcastReceiver _broadcastReveiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().contains(SingleEntryApplication.NOTIFY_ERROR_MESSAGE)) {
                dismissDialog(PROGRESS_DIALOG);
                String text = intent.getStringExtra(SingleEntryApplication.EXTRA_ERROR_MESSAGE);
                Toast.makeText(context, text, Toast.LENGTH_LONG);
            } else if (intent.getAction()
                    .contains(SingleEntryApplication.NOTIFY_EZ_PAIR_COMPLETED)) {
                dismissDialog(PROGRESS_DIALOG);
                Toast.makeText(context, "Pairing Completed", Toast.LENGTH_LONG);
                finish();
            }
        }
    };


    /**
     * Handler of the Pair to scanner button. If a scanner has been previously selected this will
     * display the Progress Dialog that will start the EZ Pair process
     */
    private OnClickListener _onStartPairing = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (_deviceSelectedToPairWith != null) {
                showDialog(PROGRESS_DIALOG);
            }
        }
    };

    /**
     * Handler of the Bluetooth Paired device list onItemClick. This selects the scanner to EZ Pair
     * with.
     */
    private OnItemClickListener _onPairedDeviceSelected = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                long arg3) {
            CheckedTextView ctv = (CheckedTextView) arg1;
            if (ctv != null) {
                _deviceSelectedToPairWith = ctv.getText().toString();
                ctv.setChecked(true);
                if (_previousSelection != null) {
                    _previousSelection.setChecked(false);
                }
                _previousSelection = ctv;
            }
        }
    };


    /**
     * Entry point of this EZ Pair activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ezpair);

        _context = this;

        // select the broadcast this activity should receive
        // from the Application
        IntentFilter filter;
        filter = new IntentFilter(SingleEntryApplication.NOTIFY_ERROR_MESSAGE);
        registerReceiver(_broadcastReveiver, filter);
        filter = new IntentFilter(SingleEntryApplication.NOTIFY_EZ_PAIR_COMPLETED);
        registerReceiver(_broadcastReveiver, filter);

        // create an adapter for the ListView of the Paired Bluetooth device
        // in this particular case we would like a single choice line
        _adapterDevices = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_single_choice);

        // install the handler for the "Pair to scanner" button
        Button btn = (Button) findViewById(R.id.buttonPairToScanner);
        if (btn != null) {
            btn.setOnClickListener(_onStartPairing);
        }

        // install the Adapter and the handler for
        // the Bluetooth Paired device ListView
        ListView lv = (ListView) findViewById(R.id.listViewScanners);
        if (lv != null) {
            lv.setAdapter(_adapterDevices);
            lv.setOnItemClickListener(_onPairedDeviceSelected);
        }

        // retrieve the host Bluetooth address and the list of
        // paired device for which the Bluetooth address starts
        // by the Socket identifier
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            _hostBluetoothAddress = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(), "bluetooth_address").replace(":", "");
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            // If there are paired devices, add each one to the ArrayAdapter
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
//	            	if((device.getAddress().toLowerCase().contains("00:c0:1b")||
//	            			(device.getAddress().toLowerCase().contains("00:06:66"))))
                    _adapterDevices.add(device.getName() + "\n" + device.getAddress());
                }
            } else {
                String noDevices = getResources().getText(R.string.none_paired).toString();
                _adapterDevices.add(noDevices);
                if (btn != null) {
                    btn.setEnabled(false);
                }
            }
        } else {
            String noBluetooth = getResources().getText(R.string.no_bluetooth).toString();
            _adapterDevices.add(noBluetooth);
            if (btn != null) {
                btn.setEnabled(false);
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(_broadcastReveiver);
    }


    /**
     * used for showing the Progress Dialog
     *
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        if (id == PROGRESS_DIALOG) {
            _progress = new Progress(_context);
            _progress.setTitle("EZ Pair");
            _progress.setMessage("Please wait while configuring the scanner");
            _progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            //_progress.show();
            dialog = _progress;
        } else {
            dialog = super.onCreateDialog(id);
        }
        return dialog;
    }

}
