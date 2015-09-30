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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.socketmobile.scanapi.ISktScanProperty;
import com.socketmobile.scanapi.SktScanErrors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleEntryActivity extends Activity {

    private EditText _decodedData;

    private TextView _status;

    private Spinner _confirmationSoundConfigSpinner;

    private Spinner _softscanSpinner;

    private Button _triggerBtn;

    private Context _context;

    private boolean _soundConfigReadyForChange;

    private int _previousSoftScanStatus = -1;

    /**
     * handler for receiving the notifications coming from SingleEntryApplication. Since
     * ScanApiHelper is "attached" to the main application object that is persistent across screen
     * rotation, the application object sends notifications coming from ScanAPI by broadcasting
     * intent.
     *
     * Update the UI accordingly when we receive a notification
     */
    private final BroadcastReceiver _newItemsReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            // ScanAPI is initialized
            if (intent.getAction()
                    .equalsIgnoreCase(SingleEntryApplication.NOTIFY_SCANPI_INITIALIZED)) {
                _status.setText("Waiting for scanner...");
                Button btn = (Button) findViewById(R.id.buttonEzPair);
                if (btn != null) {
                    btn.setVisibility(View.VISIBLE);
                }
                _softscanSpinner.setEnabled(true);

                // activate this if you want to see all the traces
                // don't leave the traces in the final application as it will
                // slow down  the overall application
//	            SingleEntryApplication.getApplicationInstance().setTraces(true);
                // asking for the SoftScan status
                SingleEntryApplication.getApplicationInstance().getSoftScanStatus();
            }

            // a Scanner has connected
            else if (intent.getAction()
                    .equalsIgnoreCase(SingleEntryApplication.NOTIFY_SCANNER_ARRIVAL)) {
                boolean softScan = intent
                        .getBooleanExtra(SingleEntryApplication.EXTRA_ISSOFTSCAN, false);
                String text = intent.getStringExtra(SingleEntryApplication.EXTRA_DEVICENAME);
                _status.setText(text);
                Button btn = (Button) findViewById(R.id.buttonEzPair);
                if (btn != null) {
                    btn.setVisibility(View.INVISIBLE);
                }

                btn = (Button) findViewById(R.id.buttonConfirm);
                if (btn != null) {
                    btn.setVisibility(View.VISIBLE);
                }
                if (softScan) {
                    _triggerBtn.setVisibility(View.VISIBLE);
                    // before triggering the softscanner, the overlay view must be set
                    // with the context of this app.
                    Map<String, Object> overlay = new HashMap<>();
                    overlay.put(ISktScanProperty.values.softScanContext.kSktScanSoftScanContext,
                            _context);
                    SingleEntryApplication.getApplicationInstance().setOverlayView(overlay);
                } else {
                    // ask for the sound confirmation config of the connected scanner
                    Intent newIntent = new Intent(SingleEntryApplication.GET_SOUND_CONFIG);
                    _context.sendBroadcast(newIntent);
                }
            }

            // a Scanner has disconnected
            else if (intent.getAction()
                    .equalsIgnoreCase(SingleEntryApplication.NOTIFY_SCANNER_REMOVAL)) {
                boolean softScan = intent
                        .getBooleanExtra(SingleEntryApplication.EXTRA_ISSOFTSCAN, false);
                _status.setText("Waiting for scanner...");
                Button btn = (Button) findViewById(R.id.buttonEzPair);
                if (btn != null) {
                    btn.setVisibility(View.VISIBLE);
                }
                btn = (Button) findViewById(R.id.buttonConfirm);
                if (btn != null) {
                    btn.setVisibility(View.INVISIBLE);
                }
                if (softScan) {
                    _triggerBtn.setVisibility(View.INVISIBLE);
                }
                _soundConfigReadyForChange = false;
                _confirmationSoundConfigSpinner.setVisibility(View.INVISIBLE);
            }

            // decoded Data received from a scanner
            else if (intent.getAction()
                    .equalsIgnoreCase(SingleEntryApplication.NOTIFY_DECODED_DATA)) {
                char[] data = intent.getCharArrayExtra(SingleEntryApplication.EXTRA_DECODEDDATA);
                _decodedData.setText(new String(data));
            }

            // an error has occurred
            else if (intent.getAction()
                    .equalsIgnoreCase(SingleEntryApplication.NOTIFY_ERROR_MESSAGE)) {
                String text = intent.getStringExtra(SingleEntryApplication.EXTRA_ERROR_MESSAGE);
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }

            // get sound config complete received
            else if (intent.getAction()
                    .equalsIgnoreCase(SingleEntryApplication.GET_SOUND_CONFIG_COMPLETE)) {
                String text = intent
                        .getStringExtra(SingleEntryApplication.EXTRA_SOUND_CONFIG_FREQUENCY);
                if (text.contains(SingleEntryApplication.SOUND_CONFIG_FREQUENCY_HIGH)) {
                    _confirmationSoundConfigSpinner.setSelection(0);
                } else if (text.contains(SingleEntryApplication.SOUND_CONFIG_FREQUENCY_MEDIUM)) {
                    _confirmationSoundConfigSpinner.setSelection(1);
                } else if (text.contains(SingleEntryApplication.SOUND_CONFIG_FREQUENCY_LOW)) {
                    _confirmationSoundConfigSpinner.setSelection(2);
                }
                _confirmationSoundConfigSpinner.setVisibility(View.VISIBLE);
                _soundConfigReadyForChange = true;
            }

            // get softscan status
            else if (intent.getAction()
                    .equalsIgnoreCase(SingleEntryApplication.GET_SOFTSCAN_COMPLETE)) {
                long result = intent.getLongExtra(SingleEntryApplication.EXTRA_ERROR,
                        SktScanErrors.ESKT_NOERROR);
                if (SktScanErrors.SKTSUCCESS(result)) {
                    int status = intent.getCharExtra(SingleEntryApplication.EXTRA_SOFTSCAN_STATUS,
                            ISktScanProperty.values.enableordisableSoftScan.kSktScanSoftScanNotSupported);
                    _previousSoftScanStatus = status;
                    int position = getSoftScanSpinnerPositionFromStatus(status);
                    _softscanSpinner.setSelection(position);
                }
            } else if (intent.getAction()
                    .equalsIgnoreCase(SingleEntryApplication.SET_SOFTSCAN_COMPLETE)) {
                long result = intent.getLongExtra(SingleEntryApplication.EXTRA_ERROR,
                        SktScanErrors.ESKT_NOERROR);
                // restore the previous softscan setting in case of error
                if (!SktScanErrors.SKTSUCCESS(result)) {
                    int position = _softscanSpinner.getSelectedItemPosition();
                    int status = getSoftScanStatusFromSpinnerPosition(position);
                    // the status cannot move from enable to not supported without being first disabled
                    if (status
                            == ISktScanProperty.values.enableordisableSoftScan.kSktScanSoftScanNotSupported) {
                        Toast.makeText(context, R.string.pleasedisablefirst, Toast.LENGTH_LONG)
                                .show();
                    }
                    position = getSoftScanSpinnerPositionFromStatus(_previousSoftScanStatus);
                    _softscanSpinner.setSelection(position);
                } else {
                    int position = _softscanSpinner.getSelectedItemPosition();
                    _previousSoftScanStatus = getSoftScanStatusFromSpinnerPosition(position);
                }
            } else if (intent.getAction()
                    .equalsIgnoreCase(SingleEntryApplication.SET_OVERLAYVIEW_COMPLETE)) {
                long result = intent.getLongExtra(SingleEntryApplication.EXTRA_ERROR,
                        SktScanErrors.ESKT_NOERROR);
                if (SktScanErrors.SKTSUCCESS(result)) {

                }
            } else if (intent.getAction()
                    .equalsIgnoreCase(SingleEntryApplication.SET_TRIGGER_COMPLETE)) {
                long result = intent.getLongExtra(SingleEntryApplication.EXTRA_ERROR,
                        SktScanErrors.ESKT_NOERROR);
                if (!SktScanErrors.SKTSUCCESS(result)) {
                    String text = getString(R.string.formaterrorwhiletriggering);
                    String msg = String.format(text, result);
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private OnClickListener _onPairToScanner = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // retrieve the scanner name and start the EZ Pair Process
            Intent intent = new Intent(_context, EzPairActivity.class);
            startActivity(intent);
        }
    };

    // handler for the 'Confirm' button that 'sends' the beeps
    private OnClickListener _onConfirm = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // send the confirmation beep to the scanner
            Intent intent = new Intent(SingleEntryApplication.SET_DATA_CONFIRMATION);
            sendBroadcast(intent);
        }
    };

    // handle for the Sound Config Spinner selection
    private OnItemSelectedListener _onChangeSoundConfig = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position,
                long id) {
            if ((view.getVisibility() == View.VISIBLE) && (_soundConfigReadyForChange)) {
                Intent newIntent = new Intent(SingleEntryApplication.SET_SOUND_CONFIG);
                String value = SingleEntryApplication.SOUND_CONFIG_FREQUENCY_HIGH;
                if (position == 0) {
                    value = SingleEntryApplication.SOUND_CONFIG_FREQUENCY_LOW;
                } else if (position == 1) {
                    value = SingleEntryApplication.SOUND_CONFIG_FREQUENCY_MEDIUM;
                }
                newIntent.putExtra(SingleEntryApplication.EXTRA_SOUND_CONFIG_FREQUENCY, value);
                sendBroadcast(newIntent);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private OnItemSelectedListener _onChangeSoftscan = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position,
                long id) {
            if (_previousSoftScanStatus != -1) {
                // make sure the required application is installed on the device
                if (isRequiredAppInstalled()) {
                    int status
                            = ISktScanProperty.values.enableordisableSoftScan.kSktScanSoftScanNotSupported;
                    status = getSoftScanStatusFromSpinnerPosition(position);
                    if (_previousSoftScanStatus != status) {
                        // first if the current status was unsupported then make it supported first
                        if (_previousSoftScanStatus
                                == ISktScanProperty.values.enableordisableSoftScan.kSktScanSoftScanNotSupported) {
                            SingleEntryApplication.getApplicationInstance().
                                    setSoftScanStatus(
                                            ISktScanProperty.values.enableordisableSoftScan.kSktScanSoftScanSupported);
                        }
                        SingleEntryApplication.getApplicationInstance().setSoftScanStatus(status);
                    }
                } else {
                    Toast.makeText(_context, R.string.pleaseinstallrequiredapplication,
                            Toast.LENGTH_LONG).show();
                    // change the spinner to not supported
                    _softscanSpinner.setSelection(0);
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub

        }
    };

    private OnClickListener _onTrigger = new OnClickListener() {

        @Override
        public void onClick(View v) {
            SingleEntryApplication.getApplicationInstance()
                    .setSoftScanTrigger(ISktScanProperty.values.trigger.kSktScanTriggerStart);
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        _context = this;

        // get the different UI fields
        _decodedData = (EditText) findViewById(R.id.editText1);
        _status = (TextView) findViewById(R.id.textViewStatus);
        _soundConfigReadyForChange
                = false;// wait for a Get Sound Config Complete before being ready
        _confirmationSoundConfigSpinner = (Spinner) findViewById(R.id.spinner_confirmation);
        _confirmationSoundConfigSpinner.setVisibility(View.INVISIBLE);
        _confirmationSoundConfigSpinner.setOnItemSelectedListener(_onChangeSoundConfig);

        _softscanSpinner = (Spinner) findViewById(R.id.spinner_softscan);
        _softscanSpinner.setOnItemSelectedListener(_onChangeSoftscan);
        _softscanSpinner.setEnabled(false);

        _triggerBtn = (Button) findViewById(R.id.buttonTrigger);
        _triggerBtn.setOnClickListener(_onTrigger);
        _triggerBtn.setVisibility(View.INVISIBLE);

        Button btn = (Button) findViewById(R.id.buttonEzPair);
        if (btn != null) {
            btn.setOnClickListener(_onPairToScanner);
            btn.setVisibility(View.INVISIBLE);
        }

        btn = (Button) findViewById(R.id.buttonConfirm);
        if (btn != null) {
            btn.setOnClickListener(_onConfirm);
            btn.setVisibility(View.INVISIBLE);
        }

        // register to receive notifications from SingleEntryApplication
        // these notifications originate from ScanAPI 
        IntentFilter filter;
        filter = new IntentFilter(SingleEntryApplication.NOTIFY_SCANPI_INITIALIZED);
        registerReceiver(this._newItemsReceiver, filter);

        filter = new IntentFilter(SingleEntryApplication.NOTIFY_SCANNER_ARRIVAL);
        registerReceiver(this._newItemsReceiver, filter);

        filter = new IntentFilter(SingleEntryApplication.NOTIFY_SCANNER_REMOVAL);
        registerReceiver(this._newItemsReceiver, filter);

        filter = new IntentFilter(SingleEntryApplication.NOTIFY_DECODED_DATA);
        registerReceiver(this._newItemsReceiver, filter);

        filter = new IntentFilter(SingleEntryApplication.NOTIFY_ERROR_MESSAGE);
        registerReceiver(this._newItemsReceiver, filter);

        filter = new IntentFilter(SingleEntryApplication.NOTIFY_CLOSE_ACTIVITY);
        registerReceiver(this._newItemsReceiver, filter);

        filter = new IntentFilter(SingleEntryApplication.SET_SOUND_CONFIG_COMPLETE);
        registerReceiver(this._newItemsReceiver, filter);

        filter = new IntentFilter(SingleEntryApplication.GET_SOUND_CONFIG_COMPLETE);
        registerReceiver(this._newItemsReceiver, filter);

        filter = new IntentFilter(SingleEntryApplication.GET_SOFTSCAN_COMPLETE);
        registerReceiver(this._newItemsReceiver, filter);

        filter = new IntentFilter(SingleEntryApplication.SET_SOFTSCAN_COMPLETE);
        registerReceiver(this._newItemsReceiver, filter);

        filter = new IntentFilter(SingleEntryApplication.SET_TRIGGER_COMPLETE);
        registerReceiver(this._newItemsReceiver, filter);

        filter = new IntentFilter(SingleEntryApplication.SET_OVERLAYVIEW_COMPLETE);
        registerReceiver(this._newItemsReceiver, filter);

        // increasing the Application View count from 0 to 1 will
        // cause the application to open and initialize ScanAPI
        SingleEntryApplication.getApplicationInstance().increaseViewCount();


    }

    /**
     * check if the required application for SoftScan is installed on the device
     */
    protected boolean isRequiredAppInstalled() {
        boolean isPresent = false;
        Intent intent1 = new Intent("com.google.zxing.client.android.SCAN");
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent1,
                PackageManager.MATCH_DEFAULT_ONLY);
        // Zxing is available 
        if (list.size() > 0) {
            isPresent = true;
        }
        return isPresent;
    }

    /**
     * utility method to convert a SoftScan status into a Spinner position
     */
    protected int getSoftScanSpinnerPositionFromStatus(int status) {
        int position = 0;
        if (status
                == ISktScanProperty.values.enableordisableSoftScan.kSktScanSoftScanNotSupported) {
            position = 0;
        } else if (status
                == ISktScanProperty.values.enableordisableSoftScan.kSktScanSoftScanSupported) {
            position = 1;
        } else if (status
                == ISktScanProperty.values.enableordisableSoftScan.kSktScanDisableSoftScan) {
            position = 1;
        } else if (status
                == ISktScanProperty.values.enableordisableSoftScan.kSktScanEnableSoftScan) {
            position = 2;
        }
        return position;
    }

    /**
     * utility method to convert a Spinner position into a SoftScan status
     */
    protected int getSoftScanStatusFromSpinnerPosition(int position) {
        int status = ISktScanProperty.values.enableordisableSoftScan.kSktScanSoftScanNotSupported;
        if (position == 0) {
            status = ISktScanProperty.values.enableordisableSoftScan.kSktScanSoftScanNotSupported;
        } else if (position == 1) {
            status = ISktScanProperty.values.enableordisableSoftScan.kSktScanDisableSoftScan;
        } else if (position == 2) {
            status = ISktScanProperty.values.enableordisableSoftScan.kSktScanEnableSoftScan;
        }
        return status;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // unregister the notifications
        unregisterReceiver(_newItemsReceiver);

        // indicate this view has been destroyed
        // if the reference count becomes 0 ScanAPI can
        // be closed if this is not a screen rotation scenario
        SingleEntryApplication.getApplicationInstance().decreaseViewCount();
    }


}
