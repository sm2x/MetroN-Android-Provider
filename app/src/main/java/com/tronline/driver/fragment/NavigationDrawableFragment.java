package com.tronline.driver.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tronline.driver.R;
import com.tronline.driver.activity.EarningsActivity;
import com.tronline.driver.activity.HelpwebActivity;
import com.tronline.driver.activity.HistoryActivity;
import com.tronline.driver.activity.MainActivity;
import com.tronline.driver.activity.ProfileActivity;
import com.tronline.driver.activity.StatusAvailabilityActivity;
import com.tronline.driver.activity.UploadDocActivity;
import com.tronline.driver.activity.WelcomeActivity;
import com.tronline.driver.adapter.UserSettingsAdapter;
import com.tronline.driver.httpRequester.AsyncTaskCompleteListener;
import com.tronline.driver.httpRequester.VollyRequester;
import com.tronline.driver.model.UserSettings;
import com.tronline.driver.utils.AndyUtils;
import com.tronline.driver.utils.Commonutils;
import com.tronline.driver.utils.Const;
import com.tronline.driver.utils.ParseContent;
import com.tronline.driver.utils.PreferenceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by user on 12/28/2016.
 */
public class NavigationDrawableFragment extends Fragment implements AdapterView.OnItemClickListener, AsyncTaskCompleteListener {

    private ListView userSettingsListView;
    private MainActivity activity;
    private ImageView userIcon;
    private TextView userName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.navigation_drawer_layout, container, false);
        activity = (MainActivity) getActivity();
        userSettingsListView = (ListView) view.findViewById(R.id.lv_drawer_user_settings);
        userIcon = (ImageView) view.findViewById(R.id.iv_user_icon);
        userName = (TextView) view.findViewById(R.id.tv_user_name);
        String pictureUrl = new PreferenceHelper(activity).getPicture();
        String name = new PreferenceHelper(activity).getUser_name();
        if (!pictureUrl.equals("")) {
Log.e("asher","nav pic "+pictureUrl);
            Glide.with(activity).load(pictureUrl).error(R.drawable.defult_user).into(userIcon);

        }
        if (!name.equals("")) {
            userName.setText(name);
        }
        UserSettingsAdapter settingsAdapter = new UserSettingsAdapter(activity, getUserSettingsList());
        userSettingsListView.setAdapter(settingsAdapter);
        userSettingsListView.setOnItemClickListener(this);
        userIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(activity, ProfileActivity.class);
                startActivity(i);
            }
        });


        return view;
    }


    private List<UserSettings> getUserSettingsList() {
        List<UserSettings> userSettingsList = new ArrayList<>();
        userSettingsList.add(new UserSettings(R.drawable.home_map_marker, getString(R.string.my_home)));
        userSettingsList.add(new UserSettings(R.drawable.settings, getString(R.string.setting)));
        userSettingsList.add(new UserSettings(R.drawable.folder_upload, getString(R.string.doc)));
        userSettingsList.add(new UserSettings(R.drawable.clock_alert, getString(R.string.ride_history)));
        userSettingsList.add(new UserSettings(R.drawable.clock_alert, "Earnings"));
        userSettingsList.add(new UserSettings(R.drawable.help_circle, getString(R.string.my_help)));
        userSettingsList.add(new UserSettings(R.drawable.ic_power_off, getString(R.string.txt_logout)));
        return userSettingsList;
    }

    @Override
    public void onResume() {
        super.onResume();
//        activity.currentFragment = Const.UserSettingsFragment;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        activity.closeDrawer();
        switch (position) {
            case 0:

                break;
            case 1:
                if(activity.currentFragment.equals(Const.HOME_MAP_FRAGMENT)){
                    Intent settingIntent = new Intent(activity, StatusAvailabilityActivity.class);
                    startActivity(settingIntent);
                } else{
                    Commonutils.showtoast(getResources().getString(R.string.txt_trip_progress),activity);
                }

                break;
            case 2:
                Intent d = new Intent(activity, UploadDocActivity.class);
                startActivity(d);
                break;
            case 3:
                Intent intent = new Intent(activity, HistoryActivity.class);
                startActivity(intent);
                break;
            case 4:
                Intent earnings = new Intent(activity, EarningsActivity.class);
                startActivity(earnings);
                break;
            case 5:
                Intent a = new Intent(activity, HelpwebActivity.class);
                startActivity(a);
                break;
            case 6:
                showLogoutDailog();
                break;

        }

    }

    private void showhelpDailog() {
        final Dialog help_dialog = new Dialog(activity,R.style.DialogSlideAnim_leftright);
        help_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        help_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        help_dialog.setCancelable(true);
        help_dialog.setContentView(R.layout.help_layout);

        help_dialog.show();

    }

    private void showLogoutDailog() {

        final Dialog dialog = new Dialog(activity,R.style.DialogSlideAnim_leftright);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.logout_dialog);
        TextView btn_logout_yes = (TextView) dialog.findViewById(R.id.btn_logout_yes);
        btn_logout_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
              //
                   updateAvailabilityStatus("0");
              //  logout();


            }
        });
        TextView btn_logout_no = (TextView) dialog.findViewById(R.id.btn_logout_no);
        btn_logout_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();


    }

    private void logout() {
        if (!AndyUtils.isNetworkAvailable(activity)) {

            return;
        }
        Commonutils.progressdialog_show(activity,getString(R.string.logout_txt));
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.LOGOUT);
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(activity).getSessionToken());
        Log.d("asher","logout map "+ map.toString());
        new VollyRequester(activity, Const.POST, map, Const.ServiceCode.LOGOUT, this);

    }


    private void updateAvailabilityStatus(String status) {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            return;
        }
        HashMap<String, String> map = new HashMap<>();
        map.put(Const.Params.URL, Const.ServiceType.POST_AVAILABILITY_STATUS_URL);
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(activity).getSessionToken());
        map.put(Const.Params.STATUS, String.valueOf(status));

        AndyUtils.appLog("Ashutosh", "updateAvailabilityMap" + map);

        new VollyRequester(activity, Const.POST, map, Const.ServiceCode.POST_AVAILABILITY_STATUS, this);
    }


    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        switch (serviceCode) {
            case Const.ServiceCode.POST_AVAILABILITY_STATUS:
                AndyUtils.appLog("Ashutosh", "SignOutStatus" + response);
                if (new ParseContent(activity).isSuccess(response)) {
                    /*new PreferenceHelper(activity).Logout();
                    new PreferenceHelper(activity).clearRequestData();

                    Intent i = new Intent(activity, WelcomeActivity.class);
                    startActivity(i);
                    activity.finish();*/
                    logout();
                }
                break;

                   case Const.ServiceCode.LOGOUT:
                Log.d("asher", "logout Response" + response);
                Commonutils.progressdialog_hide();
                try {
                    JSONObject job = new JSONObject(response);

                    if (job.getString("success").equals("true")) {
                    //    dialog.dismiss();
                        new PreferenceHelper(activity).Logout();
                     //   updateAvailabilityStatus("0");

                        new PreferenceHelper(activity).Logout();
                        Intent i = new Intent(activity, WelcomeActivity.class);
                        startActivity(i);
                        activity.finish();
                    }else {
                        String error_code = job.optString("error_code");
                        if (error_code.equals("104")) {
                            AndyUtils.showShortToast("You have logged in other device!", activity);
                            new PreferenceHelper(activity).Logout();
                            startActivity(new Intent(activity, WelcomeActivity.class));
                            activity.finish();

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                       break;
        }
    }
}
