package com.jikexuyuan.ndk.gaodemapproject.model;

import android.app.Activity;


public class DemoDetails {
    public final int titleId;
    public final int descriptionId;
    public final Class<? extends android.app.Activity> activityClass;

    public DemoDetails(int titleId, int descriptionId, Class<? extends Activity> activityClass) {
        this.titleId = titleId;
        this.descriptionId = descriptionId;
        this.activityClass = activityClass;
    }
}
