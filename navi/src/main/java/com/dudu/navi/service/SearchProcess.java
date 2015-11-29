package com.dudu.navi.service;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.dudu.monitor.Monitor;
import com.dudu.monitor.utils.LocationUtils;
import com.dudu.navi.NavigationManager;
import com.dudu.navi.R;
import com.dudu.navi.entity.PoiResultInfo;
import com.dudu.navi.event.NaviEvent;
import com.dudu.navi.repo.ResourceManager;
import com.dudu.navi.vauleObject.NavigationType;
import com.dudu.navi.vauleObject.SearchType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.functions.Action1;

/**
 * Created by lxh on 2015/11/25.
 */
public class SearchProcess {

    private static SearchProcess searchProcess;

    private Context mContext;

    private SearchType searchType;

    public static final String CURRENT_POI = "CURRENT_POI";

    private String locProvider = "";

    private boolean isGetCurdesc = false;

    private GeocodeSearch geocoderSearch;

    private LatLonPoint latLonPoint;

    private String cityCode = "";

    private String cur_locationDesc = "";

    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索

    private Bundle locBundle;
    private List<PoiItem> poiItems = null;
    private List<PoiResultInfo> poiResultList = new ArrayList<>();
    private AMapLocation cur_location;

    public SearchProcess(Context context) {
        this.mContext = context;
        geocoderSearch = new GeocodeSearch(context);
        geocoderSearch.setOnGeocodeSearchListener(geocodeSearchListener);
    }

    public static SearchProcess getInstance(Context context) {

        if (searchProcess == null)
            searchProcess = new SearchProcess(context);
        return searchProcess;
    }

    public void search(String keyword) {
        searchType = NavigationManager.getInstance(mContext).getSearchType();
        cur_location = Monitor.getInstance(mContext).getCurrentLocation();
        NavigationManager.getInstance(mContext).getLog().debug("开始搜索{}",searchType);
        if (cur_location != null) {
            locBundle = cur_location.getExtras();
            latLonPoint = new LatLonPoint(cur_location.getLatitude(), cur_location.getLongitude());
            cityCode = LocationUtils.getInstance(mContext).getCurrentCityCode();
            switch (searchType) {
                case SEARCH_DEFAULT:
                    break;
                case OPEN_NAVI:
                    EventBus.getDefault().post(NaviEvent.ChangeSemanticType.NAVIGATION);
                    EventBus.getDefault().post(new NaviEvent.NaviVoiceBroadcast("您好，请说出您想去的地方或者关键字"));
                    break;
                case SEARCH_COMMONADDRESS:
                    EventBus.getDefault().post(new NaviEvent.NaviVoiceBroadcast("您好，请说出您要添加的地址"));
                    EventBus.getDefault().post(NaviEvent.ChangeSemanticType.NAVIGATION);
                    break;
                case SEARCH_CUR_LOCATION:
                    getCur_locationDesc();
                    break;
                default:
                    doSearch(keyword);
                    break;

            }
        }

    }

    private void doSearch(String keyword) {

        NavigationManager.getInstance(mContext).setNavigationType(NavigationType.DEFAULT);
        if (!TextUtils.isEmpty(keyword)) {
            query = new PoiSearch.Query(keyword, "", cityCode);
            query.setPageSize(20);// 设置每页最多返回多少条poi item
            query.setPageNum(0);// 设置查第一页
            poiSearch = new PoiSearch(mContext, query);

            if (searchType == SearchType.SEARCH_NEARBY) {

                poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(latLonPoint.getLatitude(), latLonPoint.getLongitude()), 2000));
            }
            poiSearch.setOnPoiSearchListener(onPoiSearchListener);
            poiSearch.searchPOIAsyn();
        } else {
            String playText = "您好，关键字有误，请重新输入";
            EventBus.getDefault().post(new NaviEvent.NaviVoiceBroadcast(playText));
            EventBus.getDefault().post(NaviEvent.SearchResult.FAIL);
        }

    }

    public void getCur_locationDesc() {
        NavigationManager.getInstance(mContext).setSearchType(SearchType.SEARCH_CUR_LOCATION);
        if (locProvider != null && locProvider.equals("lbs")) {
            cur_locationDesc = locBundle.getString("desc");
            cityCode = locBundle.getString("citycode");
            String playText = "您好，您现在在" + cur_locationDesc;
            ResourceManager.getInstance(mContext).setCur_locationDesc(playText);
            EventBus.getDefault().post(NaviEvent.SearchResult.SUCCESS);
        } else {
            RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
                    GeocodeSearch.AMAP);
            geocoderSearch.getFromLocationAsyn(query);

            Observable.timer(15, TimeUnit.SECONDS)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if(!isGetCurdesc){
                            EventBus.getDefault().
                                    post(new NaviEvent.NaviVoiceBroadcast("抱歉，暂时无法获取到您的详细位置，请稍后再试"));
                            EventBus.getDefault().post(NaviEvent.SearchResult.FAIL);
                        }

                    }
                });
         
        }
    }

    private PoiSearch.OnPoiSearchListener onPoiSearchListener = new PoiSearch.OnPoiSearchListener() {

        @Override
        public void onPoiSearched(PoiResult poiResult, int code) {
            ResourceManager.getInstance(mContext).getPoiResultList().clear();

            if (code == 0) {
                if (poiResult != null && poiResult.getQuery() != null) {
                    // 取得搜索到的poiitems有多少页
                    poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
                    if (poiItems != null && poiItems.size() > 0) {
                        setPoiList();
                        EventBus.getDefault().post(NaviEvent.SearchResult.SUCCESS);
                    } else {
                        EventBus.getDefault().post(NaviEvent.ChangeSemanticType.NORMAL);
                        EventBus.getDefault().post(NaviEvent.SearchResult.FAIL);
                        EventBus.getDefault().post(new NaviEvent.NaviVoiceBroadcast(mContext.getString(R.string.no_result)));
                    }
                } else {
                    EventBus.getDefault().post(NaviEvent.SearchResult.FAIL);
                    EventBus.getDefault().post(new NaviEvent.NaviVoiceBroadcast(mContext.getString(R.string.no_result)));
                    EventBus.getDefault().post(NaviEvent.ChangeSemanticType.NORMAL);
                }
            } else {
                EventBus.getDefault().post(NaviEvent.SearchResult.FAIL);
                NavigationManager.getInstance(mContext).getLog().debug("搜索失败 errorcode:{}", code);
                EventBus.getDefault().post(new NaviEvent.NaviVoiceBroadcast(mContext.getString(R.string.error_other)));
                EventBus.getDefault().post(NaviEvent.ChangeSemanticType.NORMAL);
            }


        }

        @Override
        public void onPoiItemDetailSearched(PoiItemDetail poiItemDetail, int i) {

        }
    };

    private GeocodeSearch.OnGeocodeSearchListener geocodeSearchListener = new GeocodeSearch.OnGeocodeSearchListener() {
        @Override
        public void onRegeocodeSearched(RegeocodeResult result, int rCode) {

            if (rCode == 0) {
                if (result != null && result.getRegeocodeAddress() != null
                        && !TextUtils.isEmpty(result.getRegeocodeAddress().getFormatAddress())) {
                    NavigationManager.getInstance(mContext).getLog().debug("搜索到当前位置");
                    cityCode = result.getRegeocodeAddress().getCity();
                    cur_locationDesc = "您好，您现在在" + result.getRegeocodeAddress().getFormatAddress()
                            + "附近";
                    ResourceManager.getInstance(mContext).setCur_locationDesc(cur_locationDesc);
                    isGetCurdesc = true;
                    if(isGetCurdesc)
                         EventBus.getDefault().post(NaviEvent.SearchResult.SUCCESS);

                }
            }
        }

        @Override
        public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

        }
    };


    private void setPoiList() {
        if (latLonPoint != null && !poiItems.isEmpty()) {
            ResourceManager.getInstance(mContext).setPoiItems(poiItems);
            // 高德坐标转换为真实坐标
            LatLng startPoints_gaode = new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
            poiResultList.clear();
            for (int i = 0; i < poiItems.size(); i++) {
                PoiResultInfo poiResultInfo = new PoiResultInfo();
                poiResultInfo.setAddressDetial(poiItems.get(i).getSnippet());
                poiResultInfo.setAddressTitle(poiItems.get(i).getTitle());
                poiResultInfo.setLatitude(poiItems.get(i).getLatLonPoint()
                        .getLatitude());
                poiResultInfo.setLongitude(poiItems.get(i).getLatLonPoint()
                        .getLongitude());

                LatLng endPoints_gaode = new LatLng(poiItems.get(i)
                        .getLatLonPoint().getLatitude(), poiItems.get(i)
                        .getLatLonPoint().getLongitude());
                poiResultInfo.setDistance(AMapUtils.calculateLineDistance(startPoints_gaode, endPoints_gaode));
                poiResultList.add(poiResultInfo);
            }

            Collections.sort(poiResultList, new PoiResultInfo.MyComparator());

            ResourceManager.getInstance(mContext).setPoiResultList(poiResultList);
        }
    }
}