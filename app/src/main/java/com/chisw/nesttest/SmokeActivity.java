package com.chisw.nesttest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.nestapi.lib.API.SmokeCOAlarm;

/**
 * Created by Ann on 17.12.2015.
 */
public class SmokeActivity extends AppCompatActivity {

    public static final String SMOKE_KEY = "smoke_key";
    private SmokeCOAlarm smokeCOAlarm;

    private TextView tvName;
    private TextView tvUiColor;
    private TextView tvCOstate;
    private TextView tvSmokeState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smoke);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        smokeCOAlarm = getIntent().getParcelableExtra(SMOKE_KEY);
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

    private void initView(){
        tvName = (TextView)findViewById(R.id.smoke_name);
        tvUiColor = (TextView)findViewById(R.id.smoke_ui_color);
        tvCOstate = (TextView)findViewById(R.id.smoke_co_state);
        tvSmokeState = (TextView)findViewById(R.id.smoke_state);
    }

    private void updateView(){
        Log.d("Smoke",smokeCOAlarm.toJSON().toString());
        tvName.setText(smokeCOAlarm.getName());
        tvUiColor.setText(smokeCOAlarm.getUIColorState());
        tvCOstate.setText(smokeCOAlarm.getCOAlarmState());
        tvSmokeState.setText(smokeCOAlarm.getSmokeAlarmState());
    }
}
