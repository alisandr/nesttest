/**
 * Copyright 2014 Nest Labs Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software * distributed under
 * the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chisw.nesttest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.nestapi.lib.API.AccessToken;
import com.nestapi.lib.API.Listener;
import com.nestapi.lib.API.NestAPI;
import com.nestapi.lib.API.SmokeCOAlarm;
import com.nestapi.lib.API.Structure;
import com.nestapi.lib.API.Thermostat;
import com.nestapi.lib.AuthManager;
import com.nestapi.lib.ClientMetadata;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        NestAPI.AuthenticationListener,
        Listener.StructureListener,
        Listener.ThermostatListener,
        Listener.SmokeCOAlarmListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String STRUCTURE_KEY = "structure_key";

    private static final int AUTH_TOKEN_REQUEST_CODE = 101;

    private ListView mDevicesListView;
    private Button mStructureAway;

    private Listener mUpdateListener;
    private NestAPI mNestApi;
    private AccessToken mToken;
    private Structure mStructure;
    private List<Thermostat> thermostats;
    private List<SmokeCOAlarm> smokeCOAlarms;
    private ArrayAdapter<String> deviceArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        deviceArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        mDevicesListView = (ListView) findViewById(R.id.main_listview);
        mDevicesListView.setOnItemClickListener(new ClickListener());
        thermostats = new ArrayList<>();
        smokeCOAlarms = new ArrayList<>();
        mNestApi = NestAPI.getInstance();
        mToken = Settings.loadAuthToken(this);
        if (mToken != null) {
            authenticate(mToken);
        } else {
            obtainAccessToken();
        }

        if (savedInstanceState != null) {
            updateView();
        }
    }

    private void updateView() {
        mDevicesListView.setAdapter(deviceArrayAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void obtainAccessToken() {
        Log.v(TAG, "starting auth flow...");
        final ClientMetadata metadata = new ClientMetadata.Builder()
                .setClientID(Constants.CLIENT_ID)
                .setClientSecret(Constants.CLIENT_SECRET)
                .setRedirectURL(Constants.REDIRECT_URL)
                .build();
        AuthManager.launchAuthFlow(this, AUTH_TOKEN_REQUEST_CODE, metadata);
    }

    private View.OnClickListener mStructureAwayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mStructure == null) {
                return;
            }
            Structure.AwayState awayState;
            switch (mStructure.getAwayState()) {
                case AUTO_AWAY:
                case AWAY:
                    awayState = Structure.AwayState.HOME;
                    mStructureAway.setText(R.string.away_state_home);
                    break;
                case HOME:
                    awayState = Structure.AwayState.AWAY;
                    mStructureAway.setText(R.string.away_state_away);
                    break;
                default:
                    return;
            }

            mNestApi.setStructureAway(mStructure.getStructureID(), awayState, null);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK || requestCode != AUTH_TOKEN_REQUEST_CODE) {
            return;
        }

        if (AuthManager.hasAccessToken(data)) {
            mToken = AuthManager.getAccessToken(data);
            Settings.saveAuthToken(this, mToken);
            Log.v(TAG, "Main Activity parsed auth token: " + mToken.getToken() + " expires: " + mToken.getExpiresIn());
            authenticate(mToken);
        } else {
            Log.e(TAG, "Unable to resolve access token from payload.");
        }
    }

    private void authenticate(AccessToken token) {
        Log.v(TAG, "Authenticating...");
        NestAPI.getInstance().authenticate(token, this);
    }

    @Override
    public void onAuthenticationSuccess() {
        Log.v(TAG, "Authentication succeeded.");
        fetchData();
    }

    @Override
    public void onAuthenticationFailure(int errorCode) {
        Log.v(TAG, "Authentication failed with error: " + errorCode);
    }

    private void fetchData() {
        Log.v(TAG, "Fetching data...");

        mUpdateListener = new Listener.Builder()
                .setStructureListener(this)
                .setThermostatListener(this)
                .setSmokeCOAlarmListener(this)
                .build();

        mNestApi.addUpdateListener(mUpdateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mNestApi.removeUpdateListener(mUpdateListener);
    }

    @Override
    public void onThermostatUpdated(Thermostat thermostat) {
        Log.v(TAG, String.format("Thermostat (%s) updated.", thermostat.getDeviceID()));
        deviceArrayAdapter.add(thermostat.getNameLong());
        thermostats.add(thermostat);
        updateView();
    }

    @Override
    public void onStructureUpdated(Structure structure) {
        Log.v(TAG, String.format("Structure (%s) updated.", structure.getStructureID()));
        mStructure = structure;
        updateView();
    }

    @Override
    public void onSmokeCOAlarmUpdated(@NonNull SmokeCOAlarm smokeCOAlarm) {
        Log.v(TAG, String.format("SmokeCOAlarm (%s) updated.", smokeCOAlarm.getDeviceID()));
        deviceArrayAdapter.add(smokeCOAlarm.getNameLong());
        smokeCOAlarms.add(smokeCOAlarm);
    }

    private class ClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int devisePos = -1;
            Intent intent = null;
            String deviceName = mDevicesListView.getItemAtPosition(position).toString();
            for (int i = 0; i < thermostats.size(); i++) {
                if (thermostats.get(i).getNameLong().equals(deviceName)) {
                    devisePos = i;
                    break;
                }
            }
            if (devisePos >= 0) {
                intent = new Intent(MainActivity.this, ThermostatActivity.class);
                intent.putExtra(ThermostatActivity.THERMOSTAT_KEY, thermostats.get(devisePos));
            } else {
                for (int i = 0; i < smokeCOAlarms.size(); i++) {
                    if (smokeCOAlarms.get(i).getNameLong().equals(deviceName)) {
                        devisePos = i;
                    }
                }
                if (devisePos >= 0) {
                    intent = new Intent(MainActivity.this, SmokeActivity.class);
                    intent.putExtra(SmokeActivity.SMOKE_KEY, smokeCOAlarms.get(devisePos));
                }
            }
            startActivity(intent);
        }
    }
}
