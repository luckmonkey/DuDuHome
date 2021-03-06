package com.dudu.navi.service;

import android.content.Context;
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
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.dudu.monitor.Monitor;
import com.dudu.monitor.utils.LocationUtils;
import com.dudu.navi.NavigationManager;
import com.dudu.navi.R;
import com.dudu.navi.entity.PoiResultInfo;
import com.dudu.navi.event.NaviEvent;
import com.dudu.navi.repo.ResourceManager;
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

    private GeocodeSearch geocoderSearch;

    private LatLonPoint latLonPoint;

    private String cityCode = "";

    private String cur_locationDesc = "";

    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索

    private List<PoiItem> poiItems = null;
    private List<PoiResultInfo> poiResultList = new ArrayList<>();
    private AMapLocation cur_location;

    private boolean hasResult;

    private boolean isNoticeFail = false;

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

        isNoticeFail = false;
        searchType = NavigationManager.getInstance(mContext).getSearchType();
        cur_location = Monitor.getInstance(mContext).getCurrentLocation();
        cityCode = LocationUtils.getInstance(mContext).getCurrentCity();
        if (cur_location != null) {
            latLonPoint = new LatLonPoint(cur_location.getLatitude(), cur_location.getLongitude());
        }

        switch (searchType) {
            case SEARCH_DEFAULT:
                return;
            case SEARCH_CUR_LOCATION:
                getCur_locationDesc();
                break;
            default:
                doSearch(keyword);
                break;

        }
        NavigationManager.getInstance(mContext).getLog().debug(" >>>>>>>>>>>>>>>>开始搜索{}, {}", searchType, keyword);
        Observable.timer(25, TimeUnit.SECONDS)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if (!hasResult) {
                            isNoticeFail = true;
                            searchFail("抱歉，搜索失败，请检查网络");
                        }

                    }
                });
    }

    private void doSearch(String keyword) {
        hasResult = false;
        if (!TextUtils.isEmpty(keyword)) {
            query = new PoiSearch.Query(keyword, "", cityCode);
            query.setPageSize(20);// 设置每页最多返回多少条poi item
            query.setPageNum(0);// 设置查第一页
            poiSearch = new PoiSearch(mContext, query);
            if (searchType == SearchType.SEARCH_NEARBY || searchType == SearchType.SEARCH_NEAREST) {
                if (latLonPoint != null)
                    poiSearch.setBound(new PoiSearch.SearchBound(latLonPoint, 2000));
            }
            poiSearch.setOnPoiSearchListener(onPoiSearchListener);
            poiSearch.searchPOIAsyn();

        } else {
            String playText = "您好，关键字有误，请重新输入";
            searchFail(playText);
        }

    }

    public void getCur_locationDesc() {
        hasResult = false;
        NavigationManager.getInstance(mContext).setSearchType(SearchType.SEARCH_CUR_LOCATION);

        if (cur_location != null) {
            if (!TextUtils.isEmpty(cur_location.getAddress())) {
                cur_locationDesc = cur_location.getAddress();
                hasResult = true;
                String playText = "您好，您现在在" + cur_locationDesc;
                ResourceManager.getInstance(mContext).setCur_locationDesc(playText);
                if (!isNoticeFail) {
                    EventBus.getDefault().post(NaviEvent.SearchResult.SUCCESS);
                }
                return;
            }
        }
        getCurLocation();
    }

    private void getCurLocation() {
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
                GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);

    }

    private PoiSearch.OnPoiSearchListener onPoiSearchListener = new PoiSearch.OnPoiSearchListener() {

        @Override
        public void onPoiSearched(PoiResult poiResult, int code) {
            ResourceManager.getInstance(mContext).getPoiResultList().clear();
            hasResult = true;
            if (code == 0) {
                if (poiResult != null && poiResult.getQuery() != null) {

                    // 取得搜索到的poiitems有多少页
                    poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
                    if (poiItems != null && poiItems.size() > 0) {
                        setPoiList();
                        if (!isNoticeFail) {
                            EventBus.getDefault().post(NaviEvent.SearchResult.SUCCESS);
                        }
                    } else {

                        searchFail(mContext.getString(R.string.no_result));
                    }
                } else {
                    searchFail(mContext.getString(R.string.no_result));
                }
            } else {
                NavigationManager.getInstance(mContext).getLog().debug("搜索失败 errorcode:{}", code);
                searchFail(mContext.getString(R.string.error_other));
            }


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
                    hasResult = true;
                    if (!isNoticeFail)
                        EventBus.getDefault().post(NaviEvent.SearchResult.SUCCESS);

                }
            }
        }

        @Override
        public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

        }
    };


    private void setPoiList() {
        LatLng startPoints_gaode = null;
        if (Monitor.getInstance(mContext).getCurrentLocation() != null) {
            startPoints_gaode = new LatLng(Monitor.getInstance(mContext).getCurrentLocation().getLatitude(),
                    Monitor.getInstance(mContext).getCurrentLocation().getLongitude());
        }

        if (startPoints_gaode != null && !poiItems.isEmpty()) {
            ResourceManager.getInstance(mContext).setPoiItems(poiItems);
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

    private void searchFail(String text) {

        EventBus.getDefault().post(NaviEvent.SearchResult.FAIL);
        EventBus.getDefault().post(new NaviEvent.NaviVoiceBroadcast(text, true));
        EventBus.getDefault().post(NaviEvent.ChangeSemanticType.NORMAL);
    }
}
