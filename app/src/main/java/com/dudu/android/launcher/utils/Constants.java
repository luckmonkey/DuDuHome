package com.dudu.android.launcher.utils;

import java.util.UUID;

public class Constants {

	public static final String FLOW_UPDATE_BROADCAST = "broadcast_flow_update";
	
	public static final String VIDEO_PREVIEW_BROADCAST = "broadcast_video_preview";

	public static final boolean DEBUG = true;
	public static boolean IS_SHOW_MESSAGE = true;
	public static final UUID UUIDS = UUID.fromString(UUID.randomUUID().toString());
	public static final String XUFEIID = "55bda6e9";

	public static final String TTS_BROADCAST = "COM.DUDU.ANDROID.LAUNCHER.BROADCAST.TTS_BROADCASTRECEIVER_ACTION";
	public static final String UNDERSTANDER_DIALOG_BROADCAST = "COM.DUDU.ANDROID.LAUNCHER.BROADCAST.UNDERSTANDER_DIALOG_BROADCASTRECEIVER_ACTION";

	public static final String IN_MESSAGE_BROADCAST = "COM.DUDU.ANDROID.LAUNCHER.BROADCAST.IN_MESSAGE_BROADCAST_ACTION";
	public static final String OUT_MESSAGE_BROADCAST = "COM.DUDU.ANDROID.LAUNCHER.BROADCAST.OUT_MESSAGE_BROADCAST_ACTION";

	public static final String CHECKBOX_BROADCAST_ACTION = "COM.DUDU.ANDROID.LAUNCHER.BROADCAST.CHECKBOX_BROADCAST_ACTION";

	public static final String LOCAL_ACTION = "com.dudu.android.launcher.service.TtsBroadcastReceiverLocal";
	public static final String CLOUD_ACTION = "com.dudu.android.launcher.service.TtsBroadcastReceiverCloud";

	public static final String UPLOAD_CONTACTS = "COM.DUDU.ANDROID.LAUNCHER.service.UploadContactsReceiver";

	public static final String VOICE_IDLE_MONTOR = "com.dudu.android.launcher.voice.idle.monitor";

	public static final String WAKEUPER_SERVICE = "com.dudu.android.launcher.service.WakeuperService";
	public static final String MESSAGESHOW_SERVICE = "com.dudu.android.launcher.service.MessageShowService";
	public static final String SPEECHUNDERSTANDER_SERVICE = "com.dudu.android.launcher.service.SpeechUnderstanderService";
	public static final String BLUETOOTH_INCALL = "android.intent.action.incall";
	public static final String BLUETOOTH_CONNECT = "android.intent.action.connect";

	public static final String KEY_GRAMMAR_BNF_ID = "grammar_bnf_id";

	public static final String NAVIGATION = "导航";
	public static final String MUSIC = "音乐";
	public static final String REDIO = "收音机";
	public static final String BLUETOOTH = "蓝牙";
	public static final String VIDEO = "视频";
	public static final String WIFIB = "WIFI";
	public static final String WIFI = "wifi";
	public static final String HOTSPOT = "热点";
	public static final String MESSAGE_SHOW = "消息显示";
	public static final String SPEECH = "语音";
	public static final String TRAFFIC = "流量";
	public static final String LUXIANG = "录像";
	public static final String SHEXIANG = "摄像";
	public static final String JIEDAN = "接单";
	public static final String CLZJ = "车辆";

	public static final String EXECUTE = "执行";
	public static final String REALTIME = "实时";
	public static final String EXIT = "退出";
	public static final String OPEN = "打开";
	public static final String START = "开始";
	public static final String CLOSE = "关闭";
	public static final String BACK = "返回";
	public static final String YES = "是";
	public static final String NO = "否";
	public static final String JIE = "接";

	public static final String CALL = "打";
	public static final String SEND = "发";
	public static final String SYN = "同步";

	public static final String ANSWER = "接听";
	public static final String ANSWER_CALL = "接听电话";
	public static final String REJECT = "拒绝接听";
	public static final String REJECT_ANSWER = "拒绝";
	public static final String END_CALL = "结束通话";
	public static final String END = "结束";
	public static final String HANGUP = "挂断";
	public static final String HANGUP_CALL = "挂断电话";
	
//	public static final String WAKEUP_WORDS = "您有什么需要？";
	public static final String WAKEUP_WORDS = "您好。";
	public static final String WAKEUP_NETWORK_UNAVAILABLE = "网络状态关闭，请检查网络。";
	
	public static final String UNDERSTAND_EXIT = "嘟嘟累了，稍后与你再见。";
	public static final String UNDERSTAND_MISUNDERSTAND = "嘟嘟识别不了，请重试";
	public static final String UNDERSTAND_NO_INPUT = "嘟嘟识别不了，请重试";

	public static final String ONE1 = "1";
	public static final String ONE = "一";
	public static final String TWO1 = "2";
	public static final String TWO = "二";
	public static final String THREE1 = "3";
	public static final String THREE = "三";
	public static final String FOUR1 = "4";
	public static final String FOUR = "四";
	public static final String FIVE1 = "5";
	public static final String FIVE = "五";
	public static final String SIX1 = "6";
	public static final String SIX = "六";
	public static final String SEVEN1 = "7";
	public static final String SEVEN = "七";
	public static final String EIGHT1 = "8";
	public static final String EIGHT = "八";
	public static final String NINE1 = "9";
	public static final String NINE = "九";
	public static final String TEN1 = "10";
	public static final String TEN = "十";
	public static final String ELEVEN1 = "11";
	public static final String ELEVEN = "十一";
	public static final String TWELVE1 = "12";
	public static final String TWELVE = "十二";
	public static final String THIRTEEN1 = "13";
	public static final String THIRTEEN = "十三";
	public static final String FOURTEEN1 = "14";
	public static final String FOURTEEN = "十四";
	public static final String FIFTEEN1 = "15";
	public static final String FIFTEEN = "十五";
	public static final String SIXTEEN1 = "16";
	public static final String SIXTEEN = "十六";
	public static final String SEVENTEEN1 = "17";
	public static final String SEVENTEEN = "十七";
	public static final String EIGHTEEN1 = "18";
	public static final String EIGHTEEN = "十八";
	public static final String NINETEEN1 = "19";
	public static final String NINETEEN = "十九";
	public static final String TWENTW1 = "20";
	public static final String TWENTW = "二十";

	public static final String HOME = "家";
	public static final String HOMETOWN = "老家";
	public static final String COMPANY = "公司";

	public static final String HOME_TYPE = "home";
	public static final String HOMETOWN_TYPE = "hometown";
	public static final String COMPANY_TYPE = "company";

	public static final String CMD_CHOISE = "choise";
	public static final String CMD_ADDRESS = "address";
	public static final String CMD_OPTION = "option";
	public static final String CMD_TYPE = "type";

	public static final String DELETE_CONTACTS = "联系人";


	public static final int GUARD_TIME = 5 * 1000;
	
	public static final int MESSAGE_TIME = 5 * 1000;

	public static final int MESSAGE_TTS_TIME = 5 * 1000;
	
	public static final int VOICE_WAKEUP_CURTHRESH = 10;
	
	public static final int VOICE_MISUNDERSTAND_REAPEAT_COUNT = 3;

	// 代表我的位置
	public static final String CURRENT_POI = "CURRENT_POI";
	// 地址不够详细
	public static final String LOC_BASIC = "LOC_BASIC";

	/**
	 * 播报完成之后 不开启语义跟界面，但是要开启唤醒
	 */
	public static final int TTS_ZERO = 0;
	/**
	 * 播报完成之后 开启语义
	 */
	public static final int TTS_ONE = 1;
	/**
	 * 播报完成之后 启动界面
	 */
	public static final int TTS_TWO = 2;

	/**
	 * 播报完成之后 开启语音读写
	 */
	public static final int TTS_THREE = 3;

	/**
	 * 播报完成之后 开启离线命令输入
	 */
	public static final int TTS_FOUR = 4;

	/**
	 * 播报完成之后 什么都不做
	 */
	public static final int TTS_EIGHT = 8;

	/**
	 * Action：开启服务器
	 */
	public static final String ACTION_START_SERVER = "ACTION_STARRT_SERVER";

	/**
	 * Action：关闭后台Service
	 */
	public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";

	/**
	 * Action：连接成功
	 */
	public static final String ACTION_CONNECT_SUCCESS = "ACTION_CONNECT_SUCCESS";
	/**
	 * Action：到Service的数据
	 */
	public static final String ACTION_DATA_TO_SERVICE = "ACTION_DATA_TO_SERVICE";
	/**
	 * Action：连接错误
	 */
	public static final String ACTION_CONNECT_ERROR = "ACTION_CONNECT_ERROR";

	/**
	 * Message类型标识符，连接成功
	 */
	public static final int MESSAGE_CONNECT_SUCCESS = 0x00000002;

	/**
	 * Message：连接失败
	 */
	public static final int MESSAGE_CONNECT_ERROR = 0x00000003;

	/**
	 * Message：读取到一个对象
	 */
	public static final int MESSAGE_READ_OBJECT = 0x00000004;

	public static final int NO_VALUE_FLAG = -999;// 无
	public static final int SUNNY = 0;// 晴
	public static final int CLOUDY = 1;// 多云
	public static final int OVERCAST = 2;// 阴
	public static final int SHOWER = 3;// 阵雨
	public static final int THUNDERSHOWER = 4;// 雷阵雨
	public static final int LIGHT_RAIN = 5;// 小雨
	public static final int MODERATE_RAIN = 6;// 中雨
	public static final int HEAVY_RAIN = 7;// 大雨
	public static final int STORM = 8;// 暴风雨
	public static final int HEAVY_STORM = 9;// 大暴风雨
	public static final int SEVERE_STORM = 10;// 飓风

	public static final int LIGHT_SNOW = 11;// 小雪
	public static final int MODERATE_SNOW = 12;// 中雪
	public static final int HEAVY_SNOW = 13;// 大雪
	public static final int SNOWSTORM = 14;// 暴雪
	public static final int SNOW_SHOWER = 15;// 阵雪
	public static final int FOGGY = 16;// 雾
	public static final int LIGHT_TO_MODERATE_RAIN = 17;
	public static final int MODERATE_TO_HEAVY_RAIN = 18;
	public static final int RAIN_TO_STORM = 19;
	public static final int STORM_TO_HEAVY_STORM = 20;
	public static final int HEAVY_TO_SEVERE_STORM = 21;
	public static final int LIGHT_TO_MODERATE_SNOW = 22;
	public static final int MODERATE_TO_HEAVY_SNOW = 23;
	public static final int HEAVY_TO_SNOWSTORM = 24;

	// public static final int STRONGSANDSTORM = 18;//强沙尘暴
	// public static final int SANDSTORM = 19;//沙尘暴
	// public static final int SAND = 20;//沙尘
	// public static final int BLOWING_SAND = 21;//风沙
	// public static final int ICE_RAIN = 22;//冻雨
	// public static final int DUST = 23;//尘土
	// public static final int HAZE = 24;//霾

	public static final String FIND = "<find>";
	public static final String NEARBY = "<nearby>";
	public static final String POINT = "<point>";
	public static final String OL_OPEN = "<open>";
	public static final String OL_CLOSE = "<close>";
	public static final String APP = "<app>";
	public static final String VOICE = "<voice>";
	public static final String BIGSMALL = "<bigsmall>";
	public static final String TURNUP = "<turnup>";
	public static final String TURNDOWN = "<turndown>";
	public static final String WEATHER = "<weather>";
	public static final String SITE = "<site>";
	public static final String DATE = "<date>";
	
	// Intent相关参数常量
	public static final String PARAM_VOICE_PLAY_TEXT = "play_text";
	public static final String PARAM_VOICE_TTS_TYPE = "tts_type";
	public static final String PARAM_VOICE_SHOW_MESSAGE = "show_message";
	
	public static final String PARAM_MAP_DATA = "map_data";
	
	public static final String SERVICE_NAVI = "navi";
	public static final String NAVI_TRAFFIC_BROADCAST = "路况播报";
	public static final String NAVI_TRAFFIC = "路况";
	public static final String RERURN_JOURNEY = "返程";
	public static final String NAVI_PREVIEW = "全程预览";
	public static final String REALTIME_TRAFFIC = "实时路况";
	public static final String NAVI_LISTEN = "听";
	public static final String NAVI_LOOK = "查看";
	
	public static final String SERVICE_BACK_MAIN = "back_main";
	
	public static final String MAP_CHOOSEPREFERENCE = "MAP_CHOOSEPREFERENCE";
	public static final String MAP_BAIDU = "MAP_BAIDU";
	public static final String MAP_GAODE = "MAP_GAODE";
	public static final String DEFAULT_MAP = "DEFAULT_MAP";

    public static final String XUFEIID = "55bda6e9";
}
