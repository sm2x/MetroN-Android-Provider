package com.tronline.driver.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.tronline.driver.R;
import com.tronline.driver.httpRequester.AsyncTaskCompleteListener;
import com.tronline.driver.httpRequester.VollyRequester;
import com.tronline.driver.model.Earnings;
import com.tronline.driver.utils.AndyUtils;
import com.tronline.driver.utils.Commonutils;
import com.tronline.driver.utils.Const;
import com.tronline.driver.utils.PreferenceHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class EarningsActivity extends AppCompatActivity implements AsyncTaskCompleteListener {
    BarChart chart;
    ArrayList<BarEntry> BARENTRY;
    ArrayList<String> BarEntryLabels;
    private ArrayList<Earnings> earningslst;
    BarDataSet Bardataset;
    BarData BARDATA;
    TextView earningsToday, tripsToday, earning_total;
    ImageButton earnings_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earnings);
        chart = (BarChart) findViewById(R.id.weeklyChart);
        earningsToday = (TextView) findViewById(R.id.earnings_today);
        earnings_back = (ImageButton) findViewById(R.id.earnings_back);
        tripsToday = (TextView) findViewById(R.id.totalTrips);
        earning_total = (TextView) findViewById(R.id.earning_total);
        BARENTRY = new ArrayList<>();
        earningslst = new ArrayList<>();
        BarEntryLabels = new ArrayList<>();
        earnings_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        getEarnings();
    }

    private void getEarnings() {
        if (!AndyUtils.isNetworkAvailable(this)) {
            return;
        }
        Commonutils.progressdialog_show(this, "");
        //histroy_progress_bar.setVisibility(View.VISIBLE);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.EARNINGS);
        map.put(Const.Params.ID, new PreferenceHelper(this).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(this).getSessionToken());

        Log.d("mahi", map.toString());
        new VollyRequester(this, Const.POST, map, Const.ServiceCode.EARNINGS, this);
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        switch (serviceCode) {
            case Const.ServiceCode.EARNINGS:
                Log.d("mahi", "earnings" + response);
                Commonutils.progressdialog_hide();
                if (response != null) {
                    try {
                        JSONObject earningsObj = new JSONObject(response);
                        if (earningsObj.getString("success").equals("true")) {
                            earningslst.clear();
                            earning_total.setText(earningsObj.getString("currency") + earningsObj.getString("total_earnings"));
                            JSONArray earnArray = earningsObj.getJSONArray("earnings");
                            if (earnArray.length() > 0) {
                                JSONObject obj1 = earnArray.getJSONObject(0);
                                earningsToday.setText(earningsObj.getString("currency") + obj1.getString("total"));
                                tripsToday.setText(obj1.getString("trips") + " Trips");
                                for (int i = 1; i < earnArray.length(); i++) {
                                    JSONObject obj = earnArray.getJSONObject(i);
                                    BARENTRY.add(new BarEntry(Float.parseFloat(obj.getString("total")), i - 1));
                                    BarEntryLabels.add(obj.getString("day"));
                                }

                                Bardataset = new BarDataSet(BARENTRY, "This Week Earnings");

                                BARDATA = new BarData(BarEntryLabels, Bardataset);

                                Bardataset.setColors(ColorTemplate.COLORFUL_COLORS);

                                chart.setData(BARDATA);

                                chart.animateY(3000);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
