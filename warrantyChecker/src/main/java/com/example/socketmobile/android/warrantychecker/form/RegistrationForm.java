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
package com.example.socketmobile.android.warrantychecker.form;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.socketmobile.android.warrantychecker.R;
import com.example.socketmobile.android.warrantychecker.models.ResponseError;
import com.example.socketmobile.android.warrantychecker.network.RegistrationApiErrorResponse;
import com.example.socketmobile.android.warrantychecker.network.RegistrationApiResponse;
import com.example.socketmobile.android.warrantychecker.network.UserRegistration;
import com.example.socketmobile.android.warrantychecker.spinners.Countries;
import com.example.socketmobile.android.warrantychecker.spinners.SpinnerPair;
import com.example.socketmobile.android.warrantychecker.status.LoadingDialogFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class RegistrationForm extends Activity {

    /**
     * Extras for populating hidden fields in the form.
     * Bluetooth Mac and Developer Id are both required.
     */
    public static final String EXTRA_BLUETOOTH_MAC = RegistrationForm.class.getPackage()
            + ".extras.BLUETOOTH_MAC";

    public static final String EXTRA_DEVELOPER_ID = RegistrationForm.class.getPackage()
            + ".extras.DEVELOPER_ID";

    public static final String EXTRA_APPLICATION_ID = RegistrationForm.class.getPackage()
            + ".extras.APP_ID";

    /**
     * Return value
     */
    public static final String OUT_WARRANTY_EXPIRATION_DATE = RegistrationForm.class.getName()
            + ".out.WARRANTY_EXPIRATION_DATE";

    private static final String TAG = RegistrationForm.class.getSimpleName();

    // Values for email and password at the time of the login attempt.
    private String mBluetoothMac;

    private String mDeveloperId;

    private String mApplicationId;

    private String mCountry;

    private String mIndustry;

    // UI references
    private DialogFragment loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registration);

        // Set up the login form.
        mBluetoothMac = getIntent().getStringExtra(EXTRA_BLUETOOTH_MAC);
        mDeveloperId = getIntent().getStringExtra(EXTRA_DEVELOPER_ID);
        mApplicationId = getIntent().getStringExtra(EXTRA_APPLICATION_ID);

        // Set up country spinner
        Spinner mCountrySpinner = (Spinner) findViewById(R.id.country);
        ArrayList<SpinnerPair> countries = SpinnerPair.getArrayList(Countries.getList());
        Log.d(TAG, "Adding " + countries.size() + " countries to the spinner");
        Collections.sort(countries);
        ArrayAdapter<SpinnerPair> mCountryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                countries);
        mCountrySpinner.setAdapter(mCountryAdapter);

        // Attempt to set selection to user's country
        SpinnerPair userCountry = new SpinnerPair(Locale.getDefault().getCountry(),
                Locale.getDefault().getDisplayCountry());
        mCountrySpinner.setSelection(mCountryAdapter.getPosition(userCountry));

        // Listen for changes
        mCountrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerPair item = (SpinnerPair) parent.getItemAtPosition(position);
                mCountry = item.value;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCountry = null;
            }
        });

        // Set up Industry spinner
        Spinner mIndustrySpinner = (Spinner) findViewById(R.id.industry);
        ArrayList<SpinnerPair> industries = SpinnerPair.getArrayList(
                getResources().getStringArray(R.array.industry_array));
        ArrayAdapter<SpinnerPair> mIndustryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                industries);
        mIndustrySpinner.setAdapter(mIndustryAdapter);

        mIndustrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerPair item = (SpinnerPair) parent.getItemAtPosition(position);
                mIndustry = item.value;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mIndustry = null;
            }
        });

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegistration();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.registration_form, menu);
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * Does not perform local validation to demonstrate handling fail response.
     */
    public void attemptRegistration() {

        // Get user input from form
        UserRegistration registration = collectData();

        // Submit form
        RegistrationTask mTask = new RegistrationTask();
        mTask.execute(registration);

        // Show loading
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        loading = LoadingDialogFragment.newInstance(getString(R.string.login_progress_registering));
        loading.show(ft, null);
    }

    private UserRegistration collectData() {
        // Get references to form fields
        boolean useSandbox = mBluetoothMac.startsWith("000555");
        return new UserRegistration(useSandbox)
                .setBluetoothMac(mBluetoothMac)
                .setDeveloperId(mDeveloperId)
                .setApplicationId(mApplicationId)
                .setUserEmail(((EditText) findViewById(R.id.email)).getText().toString())
                .setUserName(((EditText) findViewById(R.id.first_name)).getText().toString())
                .setUserCompany(((EditText) findViewById(R.id.company)).getText().toString())
                .setUserAddress(((EditText) findViewById(R.id.address1)).getText().toString())
                .setUserCity(((EditText) findViewById(R.id.city)).getText().toString())
                .setUserState(((EditText) findViewById(R.id.state)).getText().toString())
                .setUserZipcode(((EditText) findViewById(R.id.zipcode)).getText().toString())
                .setUserCountry(mCountry)
                .setUserIndustry(mIndustry)
                .setPurchaser(((CheckBox) findViewById(R.id.is_purchaser)).isChecked())
                .setWhrPurchased(
                        ((EditText) findViewById(R.id.where_purchased)).getText().toString())
                .setUseSoftscan(((CheckBox) findViewById(R.id.use_softscan)).isChecked());
    }

    private void onError(final RegistrationApiErrorResponse response) {

        loading.dismiss();

        View focus = null;

        for (ResponseError error : response.modelStateErrors) {

            if ("UserEmail".equals(error.field)) {
                EditText mEmailView = (EditText) findViewById(R.id.email);
                mEmailView.setError(error.getMessages());
                focus = mEmailView;

            } else if ("UserZipcode".equals(error.field)) {
                EditText mZipcodeView = (EditText) findViewById(R.id.zipcode);
                mZipcodeView.setError(error.getMessages());
                focus = (focus == null) ? mZipcodeView : focus;

            } else if ("UserName".equals(error.field)) {
                EditText mUserNameView = (EditText) findViewById(R.id.first_name);
                mUserNameView.setError(error.getMessages());
                focus = (focus == null) ? mUserNameView : focus;

            } else if ("UserCompany".equals(error.field)) {
                EditText mCompanyView = (EditText) findViewById(R.id.company);
                mCompanyView.setError(error.getMessages());
                focus = (focus == null) ? mCompanyView : focus;

            } else if ("UserAddress".equals(error.field)) {
                EditText mAddressView = (EditText) findViewById(R.id.address1);
                mAddressView.setError(error.getMessages());
                focus = (focus == null) ? mAddressView : focus;

            } else if ("UserCity".equals(error.field)) {
                EditText mCityView = (EditText) findViewById(R.id.city);
                mCityView.setError(error.getMessages());
                focus = (focus == null) ? mCityView : focus;

            } else if ("UserState".equals(error.field)) {
                EditText mStateView = (EditText) findViewById(R.id.state);
                mStateView.setError(error.getMessages());
                focus = (focus == null) ? mStateView : focus;

            } else if ("IsPurchaser".equals(error.field)) {
                CheckBox mIsPurchaserView = (CheckBox) findViewById(R.id.is_purchaser);
                mIsPurchaserView.setError(error.getMessages());
                focus = (focus == null) ? mIsPurchaserView : focus;

            } else if ("WhrPurchased".equals(error.field)) {
                EditText mWherePurchasedView = (EditText) findViewById(R.id.where_purchased);
                mWherePurchasedView.setError(error.getMessages());
                focus = (focus == null) ? mWherePurchasedView : focus;

            } else if ("UseSoftscan".equals(error.field)) {
                CheckBox mUseSoftscanView = (CheckBox) findViewById(R.id.use_softscan);
                mUseSoftscanView.setError(error.getMessages());
                focus = (focus == null) ? mUseSoftscanView : focus;
            }
        }

        if (focus != null) {
            focus.requestFocus();
        }

    }

    private void onSuccess(RegistrationApiResponse response) {
        loading.dismiss();

        Intent i = new Intent();
        i.putExtra(OUT_WARRANTY_EXPIRATION_DATE, response.warranty.expiration.getTime());
        setResult(RESULT_OK, i);

        finish();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    class RegistrationTask extends AsyncTask<UserRegistration, Void, Object> {

        @Override
        protected Object doInBackground(UserRegistration... params) {
            return params[0].submit();
        }

        @Override
        protected void onPostExecute(final Object response) {
            if (response.getClass() == RegistrationApiResponse.class) {
                onSuccess((RegistrationApiResponse) response);
            } else if (response.getClass() == RegistrationApiErrorResponse.class) {
                onError((RegistrationApiErrorResponse) response);
            }
        }

        @Override
        protected void onCancelled() {
            if (loading != null) {
                loading.dismiss();
            }
        }
    }

}
