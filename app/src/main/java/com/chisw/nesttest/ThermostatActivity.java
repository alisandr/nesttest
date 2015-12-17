package com.chisw.nesttest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.nestapi.lib.API.Thermostat;

/**
 * Created by Ann on 16.12.2015.
 */
public class ThermostatActivity extends AppCompatActivity {


    public static final String THERMOSTAT_KEY = "thermostat_key";
    private Thermostat thermostat;

    private TextView tvName;
    private TextView tvTempScale;
    private TextView tvCanCool;
    private TextView tvCanhHeat;
    private TextView tvHasFan;
    private TextView tvHasLeaf;
    private TextView tvFanTimer;
    private TextView tvTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thermostat);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        thermostat = getIntent().getParcelableExtra(THERMOSTAT_KEY);
        initView();
        updateView();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initView() {
        tvName = (TextView) findViewById(R.id.thermo_name);
        tvTempScale = (TextView) findViewById(R.id.thermo_temp);
        tvCanCool = (TextView) findViewById(R.id.thermo_can_cool);
        tvCanhHeat = (TextView) findViewById(R.id.thermo_can_heat);
        tvHasFan = (TextView) findViewById(R.id.thermo_has_fan);
        tvHasLeaf = (TextView) findViewById(R.id.thermo_has_leaf);
        tvFanTimer = (TextView) findViewById(R.id.thermo_fan_timer);
        tvTemp = (TextView) findViewById(R.id.thermo_target_temp);
    }

    private void updateView(){

        Log.d("The",thermostat.toJSON().toString());
        tvName.setText(thermostat.getName());
        String tempScale = thermostat.getTemperatureScale();
        tvTempScale.setText(tempScale);
        switch (tempScale){
            case"F": tvTemp.setText(String.valueOf(thermostat.getTargetTemperatureF())); break;
            case"C": tvTemp.setText(String.valueOf(thermostat.getTargetTemperatureC())); break;
        }
        tvCanCool.setText(String.valueOf(thermostat.canCool()));
        tvCanhHeat.setText(String.valueOf(thermostat.canHeat()));
        tvHasFan.setText(String.valueOf(thermostat.hasFan()));
        tvHasLeaf.setText(String.valueOf(thermostat.hasLeaf()));
        tvFanTimer.setText(thermostat.getFanTimerTimeout());
    }
}
