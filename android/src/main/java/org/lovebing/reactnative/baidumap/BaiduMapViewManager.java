package org.lovebing.reactnative.baidumap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapViewLayoutParams;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lovebing on 12/20/2015.
 */
public class BaiduMapViewManager extends ViewGroupManager<MapView> {

    private static final String REACT_CLASS = "RCTBaiduMapView";

    private ThemedReactContext mReactContext;

    private ReadableArray childrenPoints;
    private HashMap<String, Marker> mMarkerMap = new HashMap<>();
    private HashMap<String, List<Marker>> mMarkersMap = new HashMap<>();
    private TextView mMarkerText;
    private RoutePlanSearch mSearch;

    String startNodeStr = "西二旗地铁站";
    String endNodeStr = "百度科技园";
    int nowSearchType = -1; // 当前进行的检索，供判断浏览节点时结果使用。
    RouteLine route = null;
    OverlayManager routeOverlay = null;
    boolean useDefaultIcon = false;

    private BaiduMap mBaidumap;
    private MapView mapView;

    public String getName() {
        return REACT_CLASS;
    }


    public void initSDK(Context context) {
        SDKInitializer.initialize(context);
    }

    public MapView createViewInstance(ThemedReactContext context) {
        mReactContext = context;
        mapView =  new MapView(context);
        setListeners(mapView);

        setRolePlanListener();

        return mapView;
    }

    /**
     * 设置百度地图路线规划
     */
    private void setRolePlanListener() {
        mBaidumap = mapView.getMap();
        mBaidumap.clear();

        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(mReactContext, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                    // result.getSuggestAddrInfo()
                    AlertDialog.Builder builder = new AlertDialog.Builder(mReactContext);
                    builder.setTitle("提示");
                    builder.setMessage("检索地址有歧义，请重新设置。\n可通过getSuggestAddrInfo()接口获得建议查询信息");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
//                    nodeIndex = -1;
//                    mBtnPre.setVisibility(View.VISIBLE);
//                    mBtnNext.setVisibility(View.VISIBLE);

//                    if (result.getRouteLines().size() > 1) {
//                        nowResultbike = result;
//                        if (!hasShownDialogue) {
//                            MyTransitDlg myTransitDlg = new MyTransitDlg(RoutePlanDemo.this,
//                                    result.getRouteLines(),
//                                    RouteLineAdapter.Type.DRIVING_ROUTE);
//                            myTransitDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                                @Override
//                                public void onDismiss(DialogInterface dialog) {
//                                    hasShownDialogue = false;
//                                }
//                            });
//                            myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
//                                public void onItemClick(int position) {
//                                    route = nowResultbike.getRouteLines().get(position);
//                                    BikingRouteOverlay overlay = new MyBikingRouteOverlay(mBaidumap);
//                                    mBaidumap.setOnMarkerClickListener(overlay);
//                                    routeOverlay = overlay;
//                                    overlay.setData(nowResultbike.getRouteLines().get(position));
//                                    overlay.addToMap();
//                                    overlay.zoomToSpan();
//                                }
//
//                            });
//                            myTransitDlg.show();
//                            hasShownDialogue = true;
//                        }
//                    } else if (result.getRouteLines().size() == 1) {
//                        route = result.getRouteLines().get(0);
//                        BikingRouteOverlay overlay = new MyBikingRouteOverlay(mBaidumap);
//                        routeOverlay = overlay;
//                        mBaidumap.setOnMarkerClickListener(overlay);
//                        overlay.setData(result.getRouteLines().get(0));
//                        overlay.addToMap();
//                        overlay.zoomToSpan();
//                        mBtnPre.setVisibility(View.VISIBLE);
//                        mBtnNext.setVisibility(View.VISIBLE);
//                    } else {
//                        Log.d("route result", "结果数<0");
//                        return;
//                    }

                    if (result.getRouteLines().size() == 1) {
                        route = result.getRouteLines().get(0);
                        BikingRouteOverlay overlay = new MyBikingRouteOverlay(mBaidumap);
                        routeOverlay = overlay;
                        mBaidumap.setOnMarkerClickListener(overlay);
                        overlay.setData(result.getRouteLines().get(0));
                        overlay.addToMap();
                        overlay.zoomToSpan();

                    } else {
                        Log.d("route result", "结果数<0");
                        return;
                    }

                }
            }
        });

        // 设置起终点信息，对于tranist search 来说，城市名无意义
        PlanNode stNode = PlanNode.withCityNameAndPlaceName("北京", startNodeStr);
        PlanNode enNode = PlanNode.withCityNameAndPlaceName("北京", endNodeStr);

        mSearch.bikingSearch((new BikingRoutePlanOption())
                .from(stNode).to(enNode));
        nowSearchType = 4;
    }

    @Override
    public void addView(MapView parent, View child, int index) {
        if(childrenPoints != null) {
            Point point = new Point();
            ReadableArray item = childrenPoints.getArray(index);
            if(item != null) {
                point.set(item.getInt(0), item.getInt(1));
                MapViewLayoutParams mapViewLayoutParams = new MapViewLayoutParams
                        .Builder()
                        .layoutMode(MapViewLayoutParams.ELayoutMode.absoluteMode)
                        .point(point)
                        .build();
                parent.addView(child, mapViewLayoutParams);
            }
        }

    }

    @ReactProp(name = "zoomControlsVisible")
    public void setZoomControlsVisible(MapView mapView, boolean zoomControlsVisible) {
        mapView.showZoomControls(zoomControlsVisible);
    }

    @ReactProp(name="trafficEnabled")
    public void setTrafficEnabled(MapView mapView, boolean trafficEnabled) {
        mapView.getMap().setTrafficEnabled(trafficEnabled);
    }

    @ReactProp(name="baiduHeatMapEnabled")
    public void setBaiduHeatMapEnabled(MapView mapView, boolean baiduHeatMapEnabled) {
        mapView.getMap().setBaiduHeatMapEnabled(baiduHeatMapEnabled);
    }

    @ReactProp(name = "mapType")
    public void setMapType(MapView mapView, int mapType) {
        mapView.getMap().setMapType(mapType);
    }

    @ReactProp(name="zoom")
    public void setZoom(MapView mapView, float zoom) {
        MapStatus mapStatus = new MapStatus.Builder().zoom(zoom).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        mapView.getMap().setMapStatus(mapStatusUpdate);
    }
    @ReactProp(name="center")
    public void setCenter(MapView mapView, ReadableMap position) {
        if(position != null) {
            double latitude = position.getDouble("latitude");
            double longitude = position.getDouble("longitude");
            LatLng point = new LatLng(latitude, longitude);
            MapStatus mapStatus = new MapStatus.Builder()
                    .target(point)
                    .build();
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
            mapView.getMap().setMapStatus(mapStatusUpdate);
        }
    }

    @ReactProp(name="marker")
    public void setMarker(MapView mapView, ReadableMap option) {
        if(option != null) {
            String key = "marker_" + mapView.getId();
            Marker marker = mMarkerMap.get(key);
            if(marker != null) {
                MarkerUtil.updateMaker(marker, option);
            }
            else {
                marker = MarkerUtil.addMarker(mapView, option);
                mMarkerMap.put(key, marker);
            }
        }
    }

    @ReactProp(name="markers")
    public void setMarkers(MapView mapView, ReadableArray options) {
        String key = "markers_" + mapView.getId();
        List<Marker> markers = mMarkersMap.get(key);
        if(markers == null) {
            markers = new ArrayList<>();
        }
        for (int i = 0; i < options.size(); i++) {
            ReadableMap option = options.getMap(i);
            if(markers.size() > i + 1 && markers.get(i) != null) {
                MarkerUtil.updateMaker(markers.get(i), option);
            }
            else {
                markers.add(i, MarkerUtil.addMarker(mapView, option));
            }
        }
        if(options.size() < markers.size()) {
            int start = markers.size() - 1;
            int end = options.size();
            for (int i = start; i >= end; i--) {
                markers.get(i).remove();
                markers.remove(i);
            }
        }
        mMarkersMap.put(key, markers);
    }

    @ReactProp(name = "childrenPoints")
    public void setChildrenPoints(MapView mapView, ReadableArray childrenPoints) {
        this.childrenPoints = childrenPoints;
    }

    /**
     *
     * @param mapView
     */
    private void setListeners(final MapView mapView) {
        BaiduMap map = mapView.getMap();

        if(mMarkerText == null) {
            mMarkerText = new TextView(mapView.getContext());
            mMarkerText.setBackgroundResource(R.drawable.popup);
            mMarkerText.setPadding(32, 32, 32, 32);
        }
        map.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
                sendEvent(mapView, "onMapStatusChangeStart", getEventParams(mapStatus));
            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {
                sendEvent(mapView, "onMapStatusChange", getEventParams(mapStatus));
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                if(mMarkerText.getVisibility() != View.GONE) {
                    mMarkerText.setVisibility(View.GONE);
                }
                sendEvent(mapView, "onMapStatusChangeFinish", getEventParams(mapStatus));
            }

            private WritableMap getEventParams(MapStatus mapStatus) {
                WritableMap writableMap = Arguments.createMap();
                WritableMap target = Arguments.createMap();
                target.putDouble("latitude", mapStatus.target.latitude);
                target.putDouble("longitude", mapStatus.target.longitude);
                writableMap.putMap("target", target);
                writableMap.putDouble("zoom", mapStatus.zoom);
                writableMap.putDouble("overlook", mapStatus.overlook);
                return writableMap;
            }
        });

        map.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                sendEvent(mapView, "onMapLoaded", null);
            }
        });

        map.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mapView.getMap().hideInfoWindow();
                WritableMap writableMap = Arguments.createMap();
                writableMap.putDouble("latitude", latLng.latitude);
                writableMap.putDouble("longitude", latLng.longitude);
                sendEvent(mapView, "onMapClick", writableMap);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                WritableMap writableMap = Arguments.createMap();
                writableMap.putString("name", mapPoi.getName());
                writableMap.putString("uid", mapPoi.getUid());
                writableMap.putDouble("latitude", mapPoi.getPosition().latitude);
                writableMap.putDouble("longitude", mapPoi.getPosition().longitude);
                sendEvent(mapView, "onMapPoiClick", writableMap);
                return true;
            }
        });
        map.setOnMapDoubleClickListener(new BaiduMap.OnMapDoubleClickListener() {
            @Override
            public void onMapDoubleClick(LatLng latLng) {
                WritableMap writableMap = Arguments.createMap();
                writableMap.putDouble("latitude", latLng.latitude);
                writableMap.putDouble("longitude", latLng.longitude);
                sendEvent(mapView, "onMapDoubleClick", writableMap);
            }
        });

        map.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
//                if(marker.getTitle().length() > 0) {
//                    mMarkerText.setText(marker.getTitle());
//                    InfoWindow infoWindow = new InfoWindow(mMarkerText, marker.getPosition(), -80);
//                    mMarkerText.setVisibility(View.GONE);
//                    mapView.getMap().showInfoWindow(infoWindow);
//                }
//                else {
//                    mapView.getMap().hideInfoWindow();
//                }
//                WritableMap writableMap = Arguments.createMap();
//                WritableMap position = Arguments.createMap();
//                position.putDouble("latitude", marker.getPosition().latitude);
//                position.putDouble("longitude", marker.getPosition().longitude);
//                writableMap.putMap("position", position);
//                writableMap.putString("title", marker.getTitle());
//                sendEvent(mapView, "onMarkerClick", writableMap);
                return true;
            }
        });

    }

    /**
     *
     * @param eventName
     * @param params
     */
    private void sendEvent(MapView mapView, String eventName, @Nullable WritableMap params) {
        WritableMap event = Arguments.createMap();
        event.putMap("params", params);
        event.putString("type", eventName);
        mReactContext
                .getJSModule(RCTEventEmitter.class)
                .receiveEvent(mapView.getId(),
                        "topChange",
                        event);
    }



    private class MyBikingRouteOverlay extends BikingRouteOverlay {
        public MyBikingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
//                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
//                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }


    }

}
