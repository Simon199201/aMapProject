package com.jikexuyuan.ndk.gaodemapproject.activity;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.enums.AimLessMode;
import com.amap.api.navi.model.AMapCongestionLink;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.jikexuyuan.ndk.gaodemapproject.R;
import com.jikexuyuan.ndk.gaodemapproject.TTSController;

import java.util.Timer;
import java.util.TimerTask;


public class NaviActivity extends Activity implements AMapNaviListener {

    private static final int INIT = 1;
    public static final String TAG = "wlx";
    private MapView mapView;
    private AMap aMap;
    private Marker myLocationMarker;

    // 是否需要跟随定位
    private boolean isNeedFollow = true;

    // 处理静止后跟随的timer
    private Timer needFollowTimer;

    // 屏幕静止DELAY_TIME之后，再次跟随
    private long DELAY_TIME = 5000;
    private AMapNavi aMapNavi;
    private TTSController ttsManager;
    private Double mLongitude;
    private Double mLatitude;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case INIT:
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(19));
                    LatLng latLng = new LatLng(mLatitude,mLongitude);
                    // 显示定位小图标，初始化时已经创建过了，这里修改位置即可
                    myLocationMarker.setPosition(latLng);
                    if (isNeedFollow) {

                        // 跟随
                        aMap.animateCamera(CameraUpdateFactory.changeLatLng(latLng));
                    }
                    break;
            }
            return false;
        }
    });
//    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_intelligent_broadcast);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写

        init();
    }

    /**
     * 初始化各种对象
     */
    private void init() {
        Log.e(TAG, "init: ");
        if (aMap == null) {
            aMapNavi = AMapNavi.getInstance(this);
            aMapNavi.startAimlessMode(AimLessMode.CAMERA_AND_SPECIALROAD_DETECTED);

            ttsManager = TTSController.getInstance(this);
            ttsManager.init();

            aMapNavi.addAMapNaviListener(this);
            aMapNavi.addAMapNaviListener(ttsManager);

            aMap = mapView.getMap();

            // 初始化 显示我的位置的Marker
            myLocationMarker = aMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(getResources(), R.drawable.car))));
            if (getIntent() != null && getIntent().hasExtra("Longitude")) {

                Double longitude = getIntent().getDoubleExtra("Longitude", 0);
                Double latitude = getIntent().getDoubleExtra("Latitude", 0);
                Log.e(TAG, "getIntent: Longitude is\t" + longitude + "Latitude is\t" + latitude);
                mLongitude = longitude;
                mLatitude = latitude;
                mHandler.sendEmptyMessageDelayed(1, 2000);
            }
            setMapInteractiveListener();
        }

    }

    /**
     * 设置导航监听
     */
    private void setMapInteractiveListener() {

        aMap.setOnMapTouchListener(new AMap.OnMapTouchListener() {

            @Override
            public void onTouch(MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 按下屏幕
                        // 如果timer在执行，关掉它
                        clearTimer();
                        // 改变跟随状态
                        isNeedFollow = false;
                        break;

                    case MotionEvent.ACTION_UP:
                        // 离开屏幕
                        startTimerSomeTimeLater();
                        break;

                    default:
                        break;
                }
            }
        });

    }

    /**
     * 取消timer任务
     */
    private void clearTimer() {
        if (needFollowTimer != null) {
            needFollowTimer.cancel();
            needFollowTimer = null;
        }
    }

    /**
     * 如果地图在静止的情况下
     */
    private void startTimerSomeTimeLater() {
        // 首先关闭上一个timer
        clearTimer();
        needFollowTimer = new Timer();
        // 开启一个延时任务，改变跟随状态
        needFollowTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                isNeedFollow = true;
            }
        }, DELAY_TIME);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        ttsManager.destroy();
        aMapNavi.stopAimlessMode();
        aMapNavi.destroy();
    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onInitNaviSuccess() {

    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation location) {
        if (location != null && location.getCoord().getLongitude()!=0.0) {
            LatLng latLng = new LatLng(location.getCoord().getLatitude(),
                    location.getCoord().getLongitude());
            // 显示定位小图标，初始化时已经创建过了，这里修改位置即可
            myLocationMarker.setPosition(latLng);
            Log.e(TAG, "onLocationChange: " + location.getCoord().getLatitude() + "\t" + location.getCoord().getLongitude());
            if (isNeedFollow) {
                // 跟随
                aMap.animateCamera(CameraUpdateFactory.changeLatLng(latLng));
            }
        } else {
            Toast.makeText(NaviActivity.this, "定位出现异常",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGetNavigationText(int i, String s) {
    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteSuccess() {

    }

    @Override
    public void onCalculateRouteFailure(int i) {

    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapCameraInfos) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] amapServiceAreaInfos) {

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {
    }

    //在巡航过程中，出现特殊道路设施（如：测速摄像头、测速雷达；违章摄像头；铁路道口；应急车道等等）时，回进到 OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] infos)，通过 AMapNaviTrafficFacilityInfo  对象可获取道路交通设施信息。
    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {
        Toast.makeText(this, "(trafficFacilityInfo.coor_X+trafficFacilityInfo.coor_Y+trafficFacilityInfo.distance+trafficFacilityInfo.limitSpeed):" + (aMapNaviTrafficFacilityInfo.getCoorX() + aMapNaviTrafficFacilityInfo.getCoorY() + aMapNaviTrafficFacilityInfo.getDistance() + aMapNaviTrafficFacilityInfo.getLimitSpeed()), Toast.LENGTH_LONG).show();
    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {

    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {
        for (AMapNaviTrafficFacilityInfo info :
                aMapNaviTrafficFacilityInfos) {
            Toast.makeText(this, "(trafficFacilityInfo.coor_X+trafficFacilityInfo.coor_Y+trafficFacilityInfo.distance+trafficFacilityInfo.limitSpeed):" + (info.getCoorX() + info.getCoorY() + info.getDistance() + info.getLimitSpeed()), Toast.LENGTH_LONG).show();
        }
    }

    //获取巡航统计数据 连续5个点速度大于15km/h后触发 updateAimlessModeStatistics 回调，通过 AimLessModeStat 对象可获取巡航的连续行驶距离和连续启用时间
    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {
        Toast.makeText(this, "看log", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "distance=" + aimLessModeStat.getAimlessModeDistance());
        Log.d(TAG, "time=" + aimLessModeStat.getAimlessModeTime());
    }

    //在巡航过程中，出现拥堵长度大于500米且拥堵时间大于5分钟时，会进到 updateAimlessModeCongestionInfo 回调中，通过 AimLessModeCongestionInfo 对象，可获取到道路拥堵信息
    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {
        Toast.makeText(this, "看log", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "roadName=" + aimLessModeCongestionInfo.getRoadName());
        Log.d(TAG, "CongestionStatus=" + aimLessModeCongestionInfo.getCongestionStatus());
        Log.d(TAG, "eventLonLat=" + aimLessModeCongestionInfo.getEventLon() + "," + aimLessModeCongestionInfo.getEventLat());
        Log.d(TAG, "length=" + aimLessModeCongestionInfo.getLength());
        Log.d(TAG, "time=" + aimLessModeCongestionInfo.getTime());
        for (AMapCongestionLink link :
                aimLessModeCongestionInfo.getAmapCongestionLinks()) {
            Log.d(TAG, "status=" + link.getCongestionStatus());
            for (NaviLatLng latlng : link.getCoords()
                    ) {
                Log.d(TAG, latlng.toString());
            }
        }
    }

    @Override
    public void onPlayRing(int i) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviinfo) {
    }
}
