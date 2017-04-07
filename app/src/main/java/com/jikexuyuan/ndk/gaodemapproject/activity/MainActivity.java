package com.jikexuyuan.ndk.gaodemapproject.activity;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.jikexuyuan.ndk.gaodemapproject.R;
import com.jikexuyuan.ndk.gaodemapproject.model.DemoDetails;
import com.jikexuyuan.ndk.gaodemapproject.views.FeatureView;

/**
 * Created by simon on 17/4/7.
 * 首页
 */

public class MainActivity extends ListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ListAdapter listAdapter = new CustomAdapter(this.getApplicationContext(), demos);
        setListAdapter(listAdapter);
    }

    private static class CustomAdapter extends ArrayAdapter<DemoDetails> {

        CustomAdapter(Context context, DemoDetails[] demoDetailses) {
            super(context, R.layout.feature, demoDetailses);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            FeatureView featureView;
            if (convertView instanceof FeatureView) {
                featureView = (FeatureView) convertView;
            } else {
                featureView = new FeatureView(getContext());
            }
            DemoDetails demoDetails = getItem(position);
            featureView.setTitleId(demoDetails != null ? demoDetails.titleId : 0);
            featureView.setDescriptionId(demoDetails != null ? demoDetails.descriptionId : 0);
            return featureView;
        }
    }

    private static final DemoDetails[] demos = {
            new DemoDetails(R.string.location,
                    R.string.location_dec, LocationActivity.class),
            new DemoDetails(R.string.intelligent_cruise,
                    R.string.intelligent_cruise_description, NaviActivity.class)
    };

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        DemoDetails demoDetails = (DemoDetails) getListAdapter().getItem(position);
        startActivity(new Intent(this.getApplicationContext(), demoDetails.activityClass));
    }
}
