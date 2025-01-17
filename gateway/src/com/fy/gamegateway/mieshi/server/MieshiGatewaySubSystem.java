package com.fy.gamegateway.mieshi.server;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.fy.boss.authorize.exception.UserNameInValidException;
import com.fy.boss.authorize.exception.UsernameAlreadyExistsException;
import com.fy.boss.authorize.exception.UsernameNotExistsException;
import com.fy.boss.authorize.model.Passport;
import com.fy.boss.client.BossClientService;
import com.fy.gamegateway.announce.MessageContent;
import com.fy.gamegateway.announce.MessageEntity;
import com.fy.gamegateway.announce.MessageEntityManager;
import com.fy.gamegateway.getbackpassport.PassportRecord;
import com.fy.gamegateway.getbackpassport.RecordManager;
import com.fy.gamegateway.language.Translate;
import com.fy.gamegateway.menu.MenuWindowManager;
import com.fy.gamegateway.message.*;
import com.fy.gamegateway.mieshi.resource.manager.MiniResourceZipData;
import com.fy.gamegateway.mieshi.resource.manager.MiniResourceZipManager;
import com.fy.gamegateway.mieshi.resource.manager.PackageManager;
import com.fy.gamegateway.mieshi.resource.manager.PackageSharedNode;
import com.fy.gamegateway.mieshi.resource.manager.ResourceManager;
import com.fy.gamegateway.mieshi.resource.manager.ResourceSharedNode;
import com.fy.gamegateway.mieshi.resource.manager.ResourceSharedNodeManager;
import com.fy.gamegateway.mieshi.resource.manager.PackageManager.Package;
import com.fy.gamegateway.mieshi.resource.manager.PackageManager.Version;
import com.fy.gamegateway.mieshi.waigua.LoginEntity;
import com.fy.gamegateway.mieshi.waigua.NewLoginHandler;
import com.fy.gamegateway.stat.Client;
import com.fy.gamegateway.stat.ClientAccount;
import com.fy.gamegateway.stat.StatManager;
import com.fy.gamegateway.thirdpartner.ThirdPartDataEntity;
import com.fy.gamegateway.thirdpartner.ThirdPartDataEntityManager;
import com.fy.gamegateway.thirdpartner.kunlun.KunLunBossClientService;
import com.fy.gamegateway.thirdpartner.ruanlei.RuanleiAppstoreMatchService;
import com.fy.gamegateway.tools.ImageTool;
import com.sqage.stat.client.StatClientService;
import com.sqage.stat.model.UserRegistFlow;
import com.xuanzhi.tools.cache.Cacheable;
import com.xuanzhi.tools.cache.LruMapCache;
import com.xuanzhi.tools.cache.diskcache.DiskCache;
import com.xuanzhi.tools.cache.diskcache.concrete.DefaultDiskCache;
import com.xuanzhi.tools.servlet.HttpUtils;
import com.xuanzhi.tools.text.DateUtil;
import com.xuanzhi.tools.text.StringUtil;
import com.xuanzhi.tools.transport.Connection;
import com.xuanzhi.tools.transport.ConnectionException;
import com.xuanzhi.tools.transport.RequestMessage;
import com.xuanzhi.tools.transport.ResponseMessage;

public class MieshiGatewaySubSystem implements GameSubSystem {

	public static Logger logger = Logger.getLogger(MieshiGatewaySubSystem.class);

	public static String MIESHI_VERSION_PRIVATE_KEY = "MieShi20130421sq";		//灭世第一条协议NEW_MIESHI_GET_VERSION_INFO_REQ的密钥

	public static Map<String,String> lastLoginMap = new ConcurrentHashMap<String, String>();//存放最近一次登陆时间

	public static Map<String, String> registerCode = new ConcurrentHashMap<String, String>();			//注册验证码
	
	public boolean printTestLog;

	public static String[] registerRandomString = new String[]{"0","2","3","4","5","6","7","8","9",
		"a","b","c","d","e","f","g","h","i","j","k","m","n","o","p","q","r","s","t","u","v","w","x","y","z",
		"A","B","C","D","E","F","G","H","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z",};
	public static String[] smsRandomString = new String[]{"1","2","3","4","5","6","7","8","9"};
	public static int registerCodeNum = 4;
	public static int smsCodeNum = 6;

	public static boolean isOpenResourceUpdate = true;
	//不更新资源的渠道
	public static HashSet<String> qudaoNames = new HashSet<String>();

	public static  long clearTime = 20 * 60 * 1000l ;

	public static final int DATA_PACKET_SIZE = 4096;

	private boolean validateShenfenzheng;

	private  boolean showShenfenzheng;
	
	/**
	 * app测试帐号看到的服务器列表
	 */
	public static String appTestUserName = null;
	public static String [] appTestServerName = null;

	public static String[] BANHAOS = new String[]{"文网游备字[2012]M-RPG015号", "ISBN 978-7-900483-85-0",""};

	//崩溃的错误信息的diskcatch    mieshi_errormsg.ddc
	private String errorMsgPath;
	public DefaultDiskCache errorMsgDiskCache;
	private String shenfenzhengCachePath;
	public static final String ERROR_DAY_CACHE = "ERROR_DAY_CACHE";
	public static int CACHE_DISTIME = 20;
	public int errorToday = -1;
	public static final String ERROR_MSG_CACHE = "ERROR_MSG_CACHE";
	public ArrayList<String[]> errorMsgs = new ArrayList<String[]>();

	private BossClientService bossClientService;

	protected String privatekey = "sqageoverflow";

	private boolean openregister = true;

	protected static MieshiGatewaySubSystem self;

	private Map<String, ArrayList<String>> errorFileList = new ConcurrentHashMap<String, ArrayList<String>>(); // clientID取资源文件的时候找不到得资源文件名称

	public ConcurrentHashMap<String,String> clientId2Channel = new ConcurrentHashMap<String, String>();
	
	
	private String tipInfoCachePath;
	public String getTipInfoCachePath() {
		return tipInfoCachePath;
	}

	public void setTipInfoCachePath(String tipInfoCachePath) {
		this.tipInfoCachePath = tipInfoCachePath;
	}

	private String hefuPlayerCachePath;



	public String getHefuPlayerCachePath() {
		return hefuPlayerCachePath;
	}

	public void setHefuPlayerCachePath(String hefuPlayerCachePath) {
		this.hefuPlayerCachePath = hefuPlayerCachePath;
	}

	private  String hefuServerInfoCachePath;



	public String getHefuServerInfoCachePath() {
		return hefuServerInfoCachePath;
	}

	public void setHefuServerInfoCachePath(String hefuServerInfoCachePath) {
		this.hefuServerInfoCachePath = hefuServerInfoCachePath;
	}

	/**
	 * 合服提示用
	 */
	public DefaultDiskCache hefuTipInfoCache = null;

	/**
	 * 合服计数
	 */
	public DefaultDiskCache hefuPlayerInfoCache = null;

	/**
	 * 老服和新服的对应关系
	 */
	public DefaultDiskCache hefuServerInfoCache = null;

	InnerTesterManager innerTesterManager;
	public final static String[] allQuestion = { Translate.您父亲的名字, Translate.您母亲的名字, Translate.您最爱人的名字, Translate.您小学的名字, Translate.您手机号的后四位, Translate.您的出生日期, Translate.您的真实姓名 };

	// 标记网关真正发布正式资源包和程序包
	private boolean publishingPackageAndResource = false;

	public boolean isPublishingPackageAndResource() {
		return publishingPackageAndResource;
	}

	public void setPublishingPackageAndResource(boolean b) {
		publishingPackageAndResource = b;
	}
	
	public static MieshiGatewaySubSystem getInstance() {
		return self;
	}

	public InnerTesterManager getInnerTesterManager() {
		return innerTesterManager;
	}

	public void setInnerTesterManager(InnerTesterManager innerTesterManager) {
		this.innerTesterManager = innerTesterManager;
	}

	public BossClientService getBossClientService() {
		return bossClientService;
	}

	//存储用户的密码
	//DefaultDiskCache passwordCache ;
	//Ok_Pasword ok_password;

	public void initialize() throws Exception {
		long now = System.currentTimeMillis();

		if (isOpenResourceUpdate) {
			try{
				qudaoNames.add("PINGSHEN_XUNXIAN");
				logger.warn("载入需要下载的clientID " + qudaoNames.size() + "个");
			}catch(Exception e) {
				logger.error("载入需要下载的clientID出错:", e);
				throw e;
			}
		}

		/*
		passwordCache = new DefaultDiskCache(new File("/home/game/resin_gateway/webapps/game_gateway/WEB-INF/diskCacheFileRoot/mieshi_password.ddc"),"mieshi-password-cache",365L*24*3600*1000L);

		passwordCache.setMaxDiskSize(10L*1024*1024*1024);
		passwordCache.setMaxMemorySize(64*1024*1024);
		passwordCache.setMaxElementNum(1024*1024*1024);

		ok_password = new Ok_Pasword();
		ok_password.init();
		 */


		self = this;

		hefuTipInfoCache =  new DefaultDiskCache(new File(getTipInfoCachePath()), "合服提示信息缓存", 1000L*60*60*24*10000l);
		hefuPlayerInfoCache = new DefaultDiskCache(new File(getHefuPlayerCachePath()), "合服角色信息缓存", 1000L*60*60*24*10000l);
		System.out.println(this.getClass().getName() + " initialize successfully [" + (System.currentTimeMillis() - now) + "]");
		logger.info(this.getClass().getName() + " initialize successfully [" + (System.currentTimeMillis() - now) + "]");
	}

	public void setBossClientService(BossClientService bossClientService) {
		this.bossClientService = bossClientService;
	}

	public boolean isOpenregister() {
		return openregister;
	}

	public void setOpenregister(boolean openregister) {
		this.openregister = openregister;
	}

	public String[] getInterestedMessageTypes() {
		return new String[] { "HEARTBEAT_CHECK_REQ", "USER_LOGIN_REQ", "GET_TIPS_REQ", "PASSPORT_REGISTER_REQ", "QUERY_ACCOUNT_VALID_REQ",
				"QUERY_SERVER_LIST_REQ", "USER_GETBACK_PASSWORD_REQ", "USER_GET_PASSWORD_PROTECT_INFO_REQ", "USER_LEAVE_SERVER_REQ",
				"USER_SET_PASSWORD_PROTECT_INFO_REQ", "USER_UPDATE_PASSWORD_REQ", "MIESHI_GET_RESOURCE_FILE_REQ",
				"MIESHI_GET_VERSION_INFO_REQ", "MIESHI_GET_RESOURCE_FILE_LIST_REQ", "MIESHI_GET_RESOURCE_REQ", "QUERY_DISPLAYER_INFO_REQ",
				"MIESHI_GET_RESOURCE_PACKAGE_INFO_REQ", "MIESHI_RESOURCE_PROGRESS_REQ", "USER_CHANGE_PASSWORD_REQ", "USER_QUERY_PROTECT_QUESTION_REQ",
				"PLATFORM_ARGS_REQ", "SERVERSLIST_AND_PASSPORTQUESTION_REQ", "ATTENTIONS_REQ", "PASSPORT_GETBACK_REQ","PHONE_UIID_INFO_REQ","SEND_CLIENT_EXTENDINFO_REQ",
				"QUERY_LOGIN_INFO_REQ", "CLIENT_ERROR_REQ", "GET_REGISTER_IMAGE_REQ", "GET_REGISTER_IMAGE_NEW_REQ","PASSPORT_REGISTER_NEW_REQ", "PASSPORT_REGISTER_PRO_REQ","USER_PROTECT_BAN_REQ",
				"NEW_USER_LOGIN_REQ", "NEW_OPTION_SELECT_REQ", "NEW_MIESHI_GET_VERSION_INFO_REQ", "NEW_USER_CHANGE_PASSWORD_REQ",
				"SERVER_HAND_CLIENT_0_RES", "SERVER_HAND_CLIENT_1_RES", "SERVER_HAND_CLIENT_2_RES", "SERVER_HAND_CLIENT_3_RES", "SERVER_HAND_CLIENT_4_RES", "SERVER_HAND_CLIENT_5_RES", "SERVER_HAND_CLIENT_6_RES", "SERVER_HAND_CLIENT_7_RES", "SERVER_HAND_CLIENT_8_RES", "SERVER_HAND_CLIENT_9_RES", 
				"SERVER_HAND_CLIENT_10_RES", "SERVER_HAND_CLIENT_11_RES", "SERVER_HAND_CLIENT_12_RES", "SERVER_HAND_CLIENT_13_RES", "SERVER_HAND_CLIENT_14_RES", "SERVER_HAND_CLIENT_15_RES", "SERVER_HAND_CLIENT_16_RES", "SERVER_HAND_CLIENT_17_RES", "SERVER_HAND_CLIENT_18_RES", "SERVER_HAND_CLIENT_19_RES", 
				"SERVER_HAND_CLIENT_20_RES", "SERVER_HAND_CLIENT_21_RES", "SERVER_HAND_CLIENT_22_RES", "SERVER_HAND_CLIENT_23_RES", "SERVER_HAND_CLIENT_24_RES", "SERVER_HAND_CLIENT_25_RES", "SERVER_HAND_CLIENT_26_RES", "SERVER_HAND_CLIENT_27_RES", "SERVER_HAND_CLIENT_28_RES", "SERVER_HAND_CLIENT_29_RES", 
				"SERVER_HAND_CLIENT_30_RES", "SERVER_HAND_CLIENT_31_RES", "SERVER_HAND_CLIENT_32_RES", "SERVER_HAND_CLIENT_33_RES", "SERVER_HAND_CLIENT_34_RES", "SERVER_HAND_CLIENT_35_RES", "SERVER_HAND_CLIENT_36_RES", "SERVER_HAND_CLIENT_37_RES", "SERVER_HAND_CLIENT_38_RES", "SERVER_HAND_CLIENT_39_RES", 
				"SERVER_HAND_CLIENT_40_RES", "SERVER_HAND_CLIENT_41_RES", "SERVER_HAND_CLIENT_42_RES", "SERVER_HAND_CLIENT_43_RES", "SERVER_HAND_CLIENT_44_RES", "SERVER_HAND_CLIENT_45_RES", "SERVER_HAND_CLIENT_46_RES", "SERVER_HAND_CLIENT_47_RES", "SERVER_HAND_CLIENT_48_RES", "SERVER_HAND_CLIENT_49_RES", 
				"SERVER_HAND_CLIENT_50_RES", "SERVER_HAND_CLIENT_51_RES", "SERVER_HAND_CLIENT_52_RES", "SERVER_HAND_CLIENT_53_RES", "SERVER_HAND_CLIENT_54_RES", "SERVER_HAND_CLIENT_55_RES", "SERVER_HAND_CLIENT_56_RES", "SERVER_HAND_CLIENT_57_RES", "SERVER_HAND_CLIENT_58_RES", "SERVER_HAND_CLIENT_59_RES", 
				"SERVER_HAND_CLIENT_60_RES", "SERVER_HAND_CLIENT_61_RES", "SERVER_HAND_CLIENT_62_RES", "SERVER_HAND_CLIENT_63_RES", "SERVER_HAND_CLIENT_64_RES", "SERVER_HAND_CLIENT_65_RES", "SERVER_HAND_CLIENT_66_RES", "SERVER_HAND_CLIENT_67_RES", "SERVER_HAND_CLIENT_68_RES", "SERVER_HAND_CLIENT_69_RES", 
				"SERVER_HAND_CLIENT_70_RES", "SERVER_HAND_CLIENT_71_RES", "SERVER_HAND_CLIENT_72_RES", "SERVER_HAND_CLIENT_73_RES", "SERVER_HAND_CLIENT_74_RES", "SERVER_HAND_CLIENT_75_RES", "SERVER_HAND_CLIENT_76_RES", "SERVER_HAND_CLIENT_77_RES", "SERVER_HAND_CLIENT_78_RES", "SERVER_HAND_CLIENT_79_RES",
				"SERVER_HAND_CLIENT_80_RES", "SERVER_HAND_CLIENT_81_RES", "SERVER_HAND_CLIENT_82_RES", "SERVER_HAND_CLIENT_83_RES", "SERVER_HAND_CLIENT_84_RES", "SERVER_HAND_CLIENT_85_RES", "SERVER_HAND_CLIENT_86_RES", "SERVER_HAND_CLIENT_87_RES", "SERVER_HAND_CLIENT_88_RES", "SERVER_HAND_CLIENT_89_RES",
				"SERVER_HAND_CLIENT_90_RES", "SERVER_HAND_CLIENT_91_RES", "SERVER_HAND_CLIENT_92_RES", "SERVER_HAND_CLIENT_93_RES", "SERVER_HAND_CLIENT_94_RES", "SERVER_HAND_CLIENT_95_RES", "SERVER_HAND_CLIENT_96_RES", "SERVER_HAND_CLIENT_97_RES", "SERVER_HAND_CLIENT_98_RES", "SERVER_HAND_CLIENT_99_RES", 
				"TRY_GETPLAYERINFO_RES","WAIT_FOR_OTHER_RES","GET_REWARD_2_PLAYER_RES","REQUESTBUY_GET_ENTITY_INFO_RES","PLAYER_SOUL_RES","CARD_TRYSAVING_RES","GANG_WAREHOUSE_JOURNAL_RES","GET_WAREHOUSE_RES","QUERY__GETAUTOBACK_RES","GET_ZONGPAI_NAME_RES","TRY_LEAVE_ZONGPAI_RES","REBEL_EVICT_NEW_RES","GET_PLAYERTITLE_RES","MARRIAGE_TRY_BEQSTART_RES","MARRIAGE_GUESTNEW_OVER_RES","MARRIAGE_HUNLI_RES","COUNTRY_COMMENDCANCEL_RES","COUNTRY_NEWQIUJIN_RES","GET_COUNTRYJINKU_RES","CAVE_NEWBUILDING_RES","CAVE_FIELD_RES","CAVE_NEW_PET_RES","CAVE_TRY_SCHEDULE_RES","CAVE_SEND_COUNTYLIST_RES","PLAYER_NEW_LEVELUP_RES","CLEAN_FRIEND_LIST_RES","DO_ACTIVITY_NEW_RES","REF_TESK_LIST_RES","DELTE_PET_INFO_RES","MARRIAGE_DOACTIVITY_RES","LA_FRIEND_RES","TRY_NEWFRIEND_LIST_RES","QINGQIU_PET_INFO_RES","REMOVE_ACTIVITY_NEW_RES","TRY_LEAVE_GAME_RES","GET_TESK_LIST_RES","GET_GAME_PALAYERNAME_RES","GET_ACTIVITY_JOINIDS_RES","QUERY_GAMENAMES_RES","GET_PET_NBWINFO_RES","CLONE_FRIEND_LIST_RES","QUERY_OTHERPLAYER_PET_MSG_RES","CSR_GET_PLAYER_RES","HAVE_OTHERNEW_INFO_RES","SHANCHU_FRIENDLIST_RES","QUERY_TESK_LIST_RES","CL_HORSE_INFO_RES","CL_NEWPET_MSG_RES","GET_ACTIVITY_NEW_RES","DO_SOME_RES","TY_PET_RES","EQUIPMENT_GET_MSG_RES","EQU_NEW_EQUIPMENT_RES","DELETE_FRIEND_LIST_RES","DO_PET_EQUIPMENT_RES","QILING_NEW_INFO_RES","HORSE_QILING_INFO_RES","HORSE_EQUIPMENT_QILING_RES","PET_EQU_QILING_RES","MARRIAGE_TRYACTIVITY_RES","PET_TRY_QILING_RES","PLAYER_CLEAN_QILINGLIST_RES","DELETE_TESK_LIST_RES","GET_HEIMINGDAI_NEWLIST_RES","QUERY_CHOURENLIST_RES","QINCHU_PLAYER_RES","REMOVE_FRIEND_LIST_RES","TRY_REMOVE_CHOUREN_RES","REMOVE_CHOUREN_RES","DELETE_TASK_LIST_RES","PLAYER_TO_PLAYER_DEAL_RES","AUCTION_NEW_LIST_MSG_RES","REQUEST_BUY_PLAYER_INFO_RES","BOOTHER_PLAYER_MSGNAME_RES","BOOTHER_MSG_CLEAN_RES","TRY_REQUESTBUY_CLEAN_ALL_RES","SCHOOL_INFONAMES_RES","PET_NEW_LEVELUP_RES","VALIDATE_ASK_NEW_RES","JINGLIAN_NEW_TRY_RES","JINGLIAN_NEW_DO_RES","JINGLIAN_PET_RES","SEE_NEWFRIEND_LIST_RES","EQU_PET_HUN_RES","PET_ADD_HUNPO_RES","PET_ADD_SHENGMINGVALUE_RES","HORSE_REMOVE_HUNPO_RES","PET_REMOVE_HUNPO_RES","PLAYER_CLEAN_HORSEHUNLIANG_RES","GET_NEW_LEVELUP_RES","DO_HOSEE2OTHER_RES","TRYDELETE_PET_INFO_RES","HAHA_ACTIVITY_MSG_RES","VALIDATE_NEW_RES","VALIDATE_ANDSWER_NEW_RES","PLAYER_ASK_TO_OTHWE_RES","GA_GET_SOME_RES","OTHER_PET_LEVELUP_RES","MY_OTHER_FRIEDN_RES","DOSOME_SB_MSG_RES",
				"THIRDPART_PROMOTE_REQ","NEW_QUERY_SERVER_LIST_REQ","ANNOUNCEMENT_REQ","QUERY_WHITE_LIST_REQ","GET_PHONE_CODE_REQ","QUICK_REGISTER_REQ"
		};
	}

	public String getName() {
		return "MieshiGatewaySubSystem";
	}

	public String encrypt(String username, String password) {
		return StringUtil.hash(username + "|" + password + "|" + privatekey);
	}

	public boolean authenticated(String username, String password, String authCode) {
		if (username == null || authCode == null) {
			return false;
		}
		String code = encrypt(username, password);
		if (code.equals(authCode)) {
			return true;
		}
		return false;
	}

	public static class WaitItem {
		Connection conn;
		RequestMessage message;
	}

	public void handleWaitingList() {
		// publish end
		MieshiGatewayServer gateway = MieshiGatewayServer.getInstance();
		if (waittingList.size() > 0) {
			synchronized (waittingList) {
				WaitItem wis[] = waittingList.toArray(new WaitItem[0]);
				waittingList.clear();

				for (int i = 0; i < wis.length; i++) {
					WaitItem w = wis[i];
					if (w.conn.getState() != Connection.CONN_STATE_CLOSE) {
						ResponseMessage message = handleReqeustMessage(w.conn, w.message);
						if (message instanceof MIESHI_GET_VERSION_INFO_RES) {
							MIESHI_GET_VERSION_INFO_RES res = (MIESHI_GET_VERSION_INFO_RES)message;
							if (res != null) {
								gateway.sendMessageToClient(w.conn, res);
							}
						}else if (message instanceof NEW_MIESHI_GET_VERSION_INFO_REQ) {
							
						
							
							NEW_MIESHI_GET_VERSION_INFO_REQ res1 = (NEW_MIESHI_GET_VERSION_INFO_REQ) handleReqeustMessage(w.conn, w.message);
							
							
							
							if (res1 != null) {
								gateway.sendMessageToClient(w.conn, res1);
							}
						}
					}
				}
			}
		}
	}

	ArrayList<WaitItem> waittingList = new ArrayList<WaitItem>();

	//尝试密码，错误的帐号
	public static class TryPasswordAndErrorUser implements Cacheable{
		String username;
		ArrayList<Long> errorTimeList = new ArrayList<Long>();

		public void addOneErrorTimes(){
			errorTimeList.add(System.currentTimeMillis());
		}

		public int getErrorTimesInSomeDuration(long duration){
			int count = 0;
			long now = System.currentTimeMillis();
			for(int i = 0 ; i < errorTimeList.size() ; i++){
				Long t = errorTimeList.get(i);
				if(t.longValue() + duration > now){
					count++;
				}
			}
			return count;
		}

		public int getSize(){
			return 1;
		}
	}

	/**
	 * AppStore用户尝试黑卡代冲的，做个标记用于提醒用户
	 */
	java.util.Hashtable<String,GatewayClient> appstoreBlackCardNotifyMap = new java.util.Hashtable<String,GatewayClient>();

	/**
	 * 控制密码错误输入次数
	 */
	LruMapCache passwordErrorCache = new LruMapCache(10240,1800000L,false,"Error-Password");

	/**
	 * 记录ClientID与UUID或者DeviceID对应
	 */
	LruMapCache clientId2UUID = new LruMapCache(1000000,240*3600000L,false,"Client-UUID");

	public static class ClientId2UUID implements Cacheable{
		public String uuid;
		public String deviceId;
		public String phoneType = "";
		public String gpuOtherName = "";
		public ClientId2UUID(String u,String d){
			uuid = u;
			deviceId = d;
		}
		public int getSize(){
			return 1;
		}
	}

	private int notifySomeUserPasswordError(String username){
		TryPasswordAndErrorUser u = (TryPasswordAndErrorUser)passwordErrorCache.get(username);
		if(u == null){
			u = new TryPasswordAndErrorUser();
			u.username = username;
			passwordErrorCache.put(username,u);
		}
		u.addOneErrorTimes();
		return u.getErrorTimesInSomeDuration(1800000);
	}

	/**
	 * 判断一个渠道是否为AppStore的渠道
	 * 
	 * APPSTORE_XUNXIAN
	 * APPSTOREGUOJI_MIESHI
	 * 
	 * @param channel
	 * @return
	 */
	public boolean isAppStoreChannel(String channel){

		if(channel == null) return false;

		if(channel.equalsIgnoreCase("APPSTORE_XUNXIAN")) return true;
		if(channel.equalsIgnoreCase("KUNLUNSDKAPPSTORE_XUNXIAN")) return true;
		if(channel.equalsIgnoreCase("KUNLUNAPPSTORE_XUNXIAN")) return true;
		if(channel.equalsIgnoreCase("MALAIIOS_MIESHI")) return true;
		if(channel.equalsIgnoreCase("KOREAAPPSTORE_XUNXIAN")) return true;
		if(channel.equalsIgnoreCase("KOREASDKAPPSTORE_XUNXIAN")) return true;
		if(channel.equalsIgnoreCase("APPSTORE2014_MIESHI")) return true;

		return false;
	}

	///////////////////////////////////////////////////////////////////////
	//
	// 限制一个client一天登录的用户名数量
	//
	//尝试密码，错误的帐号
	public static class ClientLoginItem implements Cacheable{

		public String clientId;

		public ClientLoginItem(String cid){
			clientId = cid;
		}

		public ConcurrentHashMap<String,java.util.Date> loginUserMap = new ConcurrentHashMap<String,java.util.Date>();

		public void checkMap(long now){
			String userNames[] = loginUserMap.keySet().toArray(new String[0]);
			Calendar cal = Calendar.getInstance(); 
			cal.setTimeInMillis(now);
			int day = cal.get(Calendar.DAY_OF_YEAR);
			for(int i = 0 ; i < userNames.length ; i++){
				java.util.Date d = loginUserMap.get(userNames[i]);
				cal.setTime(d);
				int dd = cal.get(Calendar.DAY_OF_YEAR);
				if(dd != day){
					loginUserMap.remove(userNames[i]);
				}
			}
		}

		public int getLoginUserCountInOneDay(long now){
			String userNames[] = loginUserMap.keySet().toArray(new String[0]);
			Calendar cal = Calendar.getInstance(); 
			cal.setTimeInMillis(now);
			int day = cal.get(Calendar.DAY_OF_YEAR);
			int count = 0;
			for(int i = 0 ; i < userNames.length ; i++){
				java.util.Date d = loginUserMap.get(userNames[i]);
				cal.setTime(d);
				int dd = cal.get(Calendar.DAY_OF_YEAR);
				if(dd == day){
					count++;
				}
			}
			return count;
		}

		public int getSize(){
			return 1;
		}
	}

	public static class ClientRegisterItem implements Cacheable{

		public String clientId;

		public ClientRegisterItem(String cid){
			clientId = cid;
		}

		public ConcurrentHashMap<String,java.util.Date> registerUserMap = new ConcurrentHashMap<String,java.util.Date>();

		public void checkMap(long now){
			String userNames[] = registerUserMap.keySet().toArray(new String[0]);
			Calendar cal = Calendar.getInstance(); 
			cal.setTimeInMillis(now);
			int day = cal.get(Calendar.DAY_OF_YEAR);
			for(int i = 0 ; i < userNames.length ; i++){
				java.util.Date d = registerUserMap.get(userNames[i]);
				cal.setTime(d);
				int dd = cal.get(Calendar.DAY_OF_YEAR);
				if(dd != day){
					registerUserMap.remove(userNames[i]);
				}
			}
		}

		public int getRegisterUserCountInOneDay(long now){
			String userNames[] = registerUserMap.keySet().toArray(new String[0]);
			Calendar cal = Calendar.getInstance(); 
			cal.setTimeInMillis(now);
			int day = cal.get(Calendar.DAY_OF_YEAR);
			int count = 0;
			for(int i = 0 ; i < userNames.length ; i++){
				java.util.Date d = registerUserMap.get(userNames[i]);
				cal.setTime(d);
				int dd = cal.get(Calendar.DAY_OF_YEAR);
				if(dd == day){
					count++;
				}
			}
			return count;
		}

		public int getSize(){
			return 1;
		}
	}

	public LruMapCache clientLoginCache = new LruMapCache(1024*1024,48*3600*1000L,false,"CLIENT-LOGIN");
	public LruMapCache clientRegisterCache = new LruMapCache(1024*1024,48*3600*1000L,false,"CLIENT-REGISTER");

	public static int MAX_ONE_CLIENT_LOGIN_NUMS_ONEDAY = 10;
	public static int MAX_ONE_CLIENT_REGISTER_NUMS_ONEDAY = 5;

	/**
	 * 返回true，标识可以登录，
	 * 返回false，标识不可以登录，今天登录成功的用户名，已经达到上限
	 * @param clientId
	 * @param username
	 * @return
	 */
	public synchronized boolean checkClientLogin(String clientId,String username){

		if(clientId == null || clientId.trim().length() == 0) return true;
		clientId = clientId.trim();
		if (innerTesterManager.isInnerTester(clientId)) {
			return true;
		}
		ClientLoginItem cli = (ClientLoginItem)clientLoginCache.get(clientId);
		if(cli == null){
			cli = new ClientLoginItem(clientId);
			clientLoginCache.put(clientId, cli);
		}
		cli.checkMap(System.currentTimeMillis());

		int count = cli.getLoginUserCountInOneDay(System.currentTimeMillis());
		if(count >= MAX_ONE_CLIENT_LOGIN_NUMS_ONEDAY){
			if(cli.loginUserMap.containsKey(username) == false){
				return false;
			}
		}
		cli.loginUserMap.put(username, new java.util.Date());
		return true;
	}

	public synchronized boolean checkClientRegister(String clientId,String username){

		if(clientId == null || clientId.trim().length() == 0) return false;
		clientId = clientId.trim();
		ClientRegisterItem cli = (ClientRegisterItem)clientRegisterCache.get(clientId);
		if(cli == null){
			cli = new ClientRegisterItem(clientId);
			clientRegisterCache.put(clientId, cli);
		}
		cli.checkMap(System.currentTimeMillis());

		int count = cli.getRegisterUserCountInOneDay(System.currentTimeMillis());
		if(count >= MAX_ONE_CLIENT_REGISTER_NUMS_ONEDAY){
			return false;
		}
		cli.registerUserMap.put(username, new java.util.Date());
		return true;
	}

	//是否要坚持最后一次登录和此次登录的设备是否一致
	public static boolean enableCheckLastLogin = true;

	//是否启用注册限制
	public static boolean enableCheckRegisterNumOneDay = true;


	public static boolean isSimplePassword(String s){
		if(s == null || s.length() < 6) return true;
		char chars[] = s.toCharArray();
		boolean digit = true;
		for(int i = 0 ; i < chars.length ; i++){
			if(Character.isDigit(chars[i]) == false){
				digit = false;
				break;
			}
		}
		if(digit && s.length() < 9){
			return true;
		}
		return false;
	}

	//	public static boolean 提示玩家修改密码 = true;
	//	
	//	
	//	public static class ModifyPassword implements java.io.Serializable{
	//
	//		/**
	//		 * 
	//		 */
	//		private static final long serialVersionUID = -5767674917700614252L;
	//		
	//		public long lastNotifyTime = 0;
	//		public boolean hasModified = false;
	//		
	//	}
	//	
	//	public void userChangePasword(String username,String password){
	//		long now = System.currentTimeMillis();
	//		
	//		ModifyPassword mp = (ModifyPassword)passwordCache.get(username);
	//		if(mp == null){
	//			mp = new ModifyPassword();
	//		}
	//		mp.hasModified = true;
	//		mp.lastNotifyTime = now;
	//		
	//		passwordCache.put(username, mp);
	//	}


	//	public OPEN_WINDOW_REQ checkPasswordModify(java.util.Date rdate,GatewayClient client,String username,String password){
	//		
	//		if(提示玩家修改密码 == false) return null;
	//		if(isSimplePassword(password) == false) return null;
	//		long now = System.currentTimeMillis();
	//		
	//		java.util.Date ddd = DateUtil.parseDate("2013-04-20", "yyyy-MM-dd");
	//		
	//		if(rdate != null && rdate.getTime() > ddd.getTime()) return null;
	//		
	//		ModifyPassword mp = (ModifyPassword)passwordCache.get(username);
	//		boolean needNotify = false;
	//		if(mp == null){
	//			//提示玩家
	//			needNotify = true;
	//		}else{
	//			if(mp.hasModified == false){
	//				Calendar cal = Calendar.getInstance();
	//				cal.setTimeInMillis(now);
	//				int d1 = cal.get(Calendar.DAY_OF_YEAR);
	//				cal.setTimeInMillis(mp.lastNotifyTime);
	//				int d2 = cal.get(Calendar.DAY_OF_YEAR);
	//				//if(d1 != d2){
	//					needNotify = true;
	//				//}
	//			}
	//		}
	//		if(needNotify){
	//			if(mp == null){
	//				mp = new ModifyPassword();
	//			}
	//			mp.lastNotifyTime = now;
	//			passwordCache.put(username, mp);
	//			
	//			
	//			int width = 0;
	//			int height = 0;
	//			String[] btns = new String[] { Translate.确定};
	//			byte[] oType = new byte[] { (byte) 0};
	//			
	//			String descriptionInUUB = "我们发现近期盗号问题比较严重，而您的密码过于简单，建议您尽快修改为复杂的密码（6位以上字母和数字组合的），我们也会在近期上线设备授权系统，以保护您的财产。给您带来的不便深表歉意。";
	//			
	//			OPEN_WINDOW_REQ owq = new OPEN_WINDOW_REQ(GameMessageFactory.nextSequnceNum(), 0, Translate.帐号安全警告, descriptionInUUB, width, height, btns, oType);
	//			
	//			logger.warn("[提示用户修改密码] [密码过于简单] [username:"+username+"] "+client.getLogStr()+" [cost:"+(System.currentTimeMillis() - now)+"ms]");
	//			
	//			return owq;
	//
	//		}
	//		return null;
	//	}

	/**
	 * 检查上一次登录和此次登录的设备是否一致，
	 * 如果不一致，将提醒用户。
	 * 
	 * 返回null，标识不用提醒
	 * 
	 * @param client
	 * @param username
	 * @return
	 */
	public OPEN_WINDOW_REQ checkLastLoginAndCurrentLogin(GatewayClient client,String username){

		long now = System.currentTimeMillis();

		MieshiServerInfoManager sm = MieshiServerInfoManager.getInstance();

		boolean enable = sm.isTestUser(username);

		if(enable == false && enableCheckLastLogin == false) return null;

		StatManager stat = StatManager.getInstance();

		if(stat == null) return null;

		String platform = client.getPlatform();

		if(platform.equalsIgnoreCase("IOS") == false && platform.equalsIgnoreCase("Android") == false) return null;

		if(client.getClientId() == null || client.getClientId().length() == 0) return null;

		String deviceId = null;
		if(platform.equalsIgnoreCase("IOS")){
			deviceId = client.uuid;
		}else{
			deviceId = client.deviceId;
		}
		if(deviceId == null || deviceId.length() == 0) return null;

		try{
			List<ClientAccount> caList = stat.em4Account.query(ClientAccount.class, "username=? and hasSuccessLogin='T'", new Object[]{username}, "loginTimeForLast desc", 1, 2);
			if(caList.size() == 0) return null;
			ClientAccount ca = caList.get(0);

			if(ca.getClientId() == null || ca.getClientId().equals(client.getClientId())){
				return null;
			}

			List<Client> cList = stat.em4Client1.query(Client.class, "clientId=?", new Object[]{ca.getClientId()}, "", 1, 2);
			if(cList.size() == 0) return null;
			Client cc = cList.get(0);

			platform = cc.getPlatform();
			if(platform.equalsIgnoreCase("IOS") == false && platform.equalsIgnoreCase("Android") == false) return null;

			String lastDeviceId = null;
			if(platform.equalsIgnoreCase("IOS")){
				lastDeviceId = cc.getUuid();
			}else{
				lastDeviceId = cc.getDeviceId();
			}
			if(lastDeviceId == null || lastDeviceId.length() == 0) return null;

			if(deviceId.equals(lastDeviceId)) return null;

			//这次登录的设备号，和上一次的不同，要提示用户
			String lastLoginTime = DateUtil.formatDate(ca.getLoginTimeForLast(), Translate.yyyy年MM月dd日HH点mm分ss秒);
			String descriptionInUUB = Translate.translateString(Translate.提醒详细, new String[][]{{Translate.STRING_1,lastLoginTime},{Translate.STRING_2,cc.getPhoneType()}});
			//String descriptionInUUB = "您的帐号在"+lastLoginTime+"被不同的设备["+cc.getPhoneType()+"]登录过，请注意您的帐号安全！必要时请及时修改您的登录密码，以防止帐号被盗或者贵重物品被转移！";
			int width = 0;
			int height = 0;
			String[] btns = new String[] { Translate.确定};
			byte[] oType = new byte[] { (byte) 0};

			OPEN_WINDOW_REQ owq = new OPEN_WINDOW_REQ(GameMessageFactory.nextSequnceNum(), 0, Translate.帐号安全警告, descriptionInUUB, width, height, btns, oType);

			logger.warn("[登录设备检查] [发现与上一次登录设备不同] [username:"+username+"] "+client.getLogStr()+" [lastClient:"+cc.getClientId()+"] [lastPhoneType:"+cc.getPhoneType()+"] [lastPlatform:"+cc.getPlatform()+"] [lastDeviceId:"+lastDeviceId+"] [lastLoginTime:"+lastLoginTime+"] [cost:"+(System.currentTimeMillis() - now)+"ms]");

			return owq;

		}catch(Exception e){
			logger.warn("[登录设备检查] [出现异常] [username:"+username+"] "+client.getLogStr()+" [lastClient:-] [lastPhoneType:-] [lastPlatform:-] [lastDeviceId:-] [lastLoginTime:-]  [cost:"+(System.currentTimeMillis() - now)+"ms]",e);

			return null;
		}
	}

	public ResponseMessage handleReqeustMessage(Connection conn, RequestMessage message) {

		long startTime = System.currentTimeMillis();
		Passport conpassport = (Passport) conn.getAttachment();
		MieshiGatewayServer gateway = MieshiGatewayServer.getInstance();
		GatewayClient client = (GatewayClient) conn.getAttachmentData("client");
		if (client == null) {
			client = new GatewayClient("");
			conn.setAttachmentData("client", client);
		}
		client.setLastReceiveTime(startTime);
		client.setReceiveNum(client.getReceiveNum() + 1);
		if (message instanceof MIESHI_RESOURCE_PROGRESS_REQ) {
			MIESHI_RESOURCE_PROGRESS_REQ req = (MIESHI_RESOURCE_PROGRESS_REQ) message;
			if (logger.isInfoEnabled()) {
				logger.info("[" + conn.getName() + "] [客户端资源更新进度] [" + req.getProgress() + "] [" + message.getTypeDescription() + "] " + client.getLogStr());
			}
			StatManager stat = StatManager.getInstance();
			if (stat != null) {
				stat.notifyResourceProcess(client, req.getProgress());
			}
			{
				//发送版权信息
				if(client.getChannel().toLowerCase().contains("korea") || client.getChannel().toLowerCase().contains("kunlun") || client.getChannel().toLowerCase().contains("malai"))
				{
					//不发送版号
				}
				else
				{
					String[] infos = BANHAOS;
					SEND_BANHAO_TO_CLIENT_REQ req1 = new SEND_BANHAO_TO_CLIENT_REQ(GameMessageFactory.nextSequnceNum(), infos);
					gateway.sendMessageToClient(conn, req1);
				}

			}
			MIESHI_RESOURCE_PROGRESS_RES res = new MIESHI_RESOURCE_PROGRESS_RES(req.getSequnceNum(), req.getProgress());
			return res;
		}

		if( message instanceof PHONE_UIID_INFO_REQ ){
			PHONE_UIID_INFO_REQ req = (PHONE_UIID_INFO_REQ) message;

			if(client != null && client.getClientId() != null && client.getClientId().length() > 0){
				if(req.getUUID().length() > 0){
					client.setUuid(req.getUUID());
				}
				if(req.getDEVICEID().length() > 0){
					client.setDeviceId(req.getDEVICEID());
				}
				if(req.getMACADDRESS().length() > 0){
					client.setMacAddress(req.getMACADDRESS());
				}
				if(req.getIMSI().length() > 0){
					client.setImsi(req.getIMSI());
				}
				ClientId2UUID c2u = new ClientId2UUID(req.getUUID(),req.getDEVICEID());
				c2u.phoneType = client.getPhoneType();
				c2u.gpuOtherName = client.getGpuOtherName();
				clientId2UUID.put(client.getClientId(),c2u);

				conn.setAttachmentData("PHONE_UIID_INFO_REQ", req);

				StatManager stat = StatManager.getInstance();
				if (stat != null) {
					//* 更新UUID，只有原来的UUID没有设置过，才能更新。
					//* 已经设置过UUID，是不更新的。为了解决篡改UUID用黑卡帮助别人充值的问题
					stat.notifyPhoneUUIDUpdate(client,req.getUUID(),req.getDEVICEID(),req.getMACADDRESS(),req.getIMSI());
				}

				if (logger.isInfoEnabled()) {
					logger.info("[" + conn.getName() + "] [用户手机信息] [UUID="+req.getUUID()+"] [DEVICEID=" + req.getDEVICEID() + "] [MACADDRESS="+req.getMACADDRESS()+"] [IMSI=" + req.getIMSI() + "] " + client.getLogStr());
				}

			}else{
				conn.setAttachmentData("PHONE_UIID_INFO_REQ", req);
			}

		}

		if (this.isPublishingPackageAndResource() && (message instanceof MIESHI_GET_VERSION_INFO_REQ || message instanceof NEW_MIESHI_GET_VERSION_INFO_REQ)) {

			
			
			
			String descriptionInUUB = Translate.系统正在发布新的资源;
			int width = 0;
			int height = 0;
			String[] btns = new String[] { Translate.继续等待, Translate.退出游戏 };
			byte[] oType = new byte[] { (byte) 0, (byte) 1 };

			OPEN_WINDOW_REQ owq = new OPEN_WINDOW_REQ(GameMessageFactory.nextSequnceNum(), 0, "", descriptionInUUB, width, height, btns, oType);

			gateway.sendMessageToClient(conn, owq);

			if (logger.isInfoEnabled()) {
				logger.info("[" + conn.getName() + "] [系统正在发布资源，直接返回给用户提示] [" + message.getTypeDescription() + "] " + client.getLogStr());
			}

			WaitItem w = new WaitItem();
			w.conn = conn;
			w.message = message;
			waittingList.add(w);
			

			return null;
		}

		if (isPublishingPackageAndResource() && (message instanceof HEARTBEAT_CHECK_REQ)) {
			HEARTBEAT_CHECK_REQ req = (HEARTBEAT_CHECK_REQ) message;
			HEARTBEAT_CHECK_RES res = new HEARTBEAT_CHECK_RES(req.getSequnceNum(), req.getGameTimeOfClient(), req.getSpeed());
			return res;
		}

		if (isPublishingPackageAndResource()) {
			if (logger.isInfoEnabled()) {
				logger.info("[" + conn.getName() + "] [系统正在发布资源，直接丢弃消息] [" + message.getTypeDescription() + "] " + client.getLogStr());
			}
			return null;
		}

		handleWaitingList();

		if (message instanceof QUERY_DISPLAYER_INFO_REQ) {
			QUERY_DISPLAYER_INFO_REQ req = (QUERY_DISPLAYER_INFO_REQ) message;

			String channel = req.getChannel();
			String platform = req.getPlatform();

			if (channel.toLowerCase().contains("uc_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "UCUSER", "注册帐号", "请输入UC昵称", "输入密码", "确认密码", "游戏帐号/UC号", "输入密码",
						new String[0], new String[0]);
				return res;
			} else if (channel.toLowerCase().matches("uc\\d+_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "UCUSER", "注册帐号", "请输入UC昵称", "输入密码", "确认密码", "游戏帐号/UC号", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().matches("ucsdk\\d+_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "UCSDKUSER", "注册帐号", "请输入UC昵称", "输入密码", "确认密码", "游戏帐号/UC号", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().matches("ucsdk+_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "UCSDKUSER", "注册帐号", "请输入UC昵称", "输入密码", "确认密码", "游戏帐号/UC号", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("ucnewsdk")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "UCSDKUSER", "注册帐号", "请输入UC昵称", "输入密码", "确认密码", "游戏帐号/UC号", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("newucsdk")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "UCSDKUSER", "注册帐号", "请输入UC昵称", "输入密码", "确认密码", "游戏帐号/UC号", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("newwdjsdk")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "WANDOUJIASDKUSER", "注册帐号", "请输入UC昵称", "输入密码", "确认密码", "游戏帐号/UC号", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("dcn_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "DCNUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名/当乐号",
						"输入密码", new String[0], new String[0]);
				return res;
			} else if (channel.toLowerCase().matches("dcn\\d+_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "DCNUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名/当乐号",
						"输入密码", new String[0], new String[0]);
				return res;
			} 
			else if (channel.toLowerCase().matches("win8dcn\\d*_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "DCNUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名/当乐号",
						"输入密码", new String[0], new String[0]);
				return res;
			} 

			else if (channel.toLowerCase().contains("91zhushou_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "91USER", "注册帐号", "输入用户名", "输入密码", "确认密码", "91账号", "输入密码",
						new String[0], new String[0]);
				return res;
			} else if (channel.toLowerCase().matches("91zhushou\\d+_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "91USER", "注册帐号", "输入用户名", "输入密码", "确认密码", "91账号", "输入密码",
						new String[0], new String[0]);
				return res;
			} else if (channel.toLowerCase().contains("91_mieshi")) {
				// myzdf 修改稿的
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "SQAGE", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			} else if (channel.toLowerCase().matches("91\\d+_mieshi")) {
				// myzdf 修改稿的
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "SQAGE", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			} else if (channel.toLowerCase().contains("qq_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "QQUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			} else if (channel.toLowerCase().matches("qq\\d+_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "QQUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("lenovo_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "LENOVOUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("sinawei_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "SINAWEIUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().matches("sinawei\\d+_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "SINAWEIUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("sinaweinew_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "SINAWEIUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().matches("sinaweinew\\d+_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "SINAWEIUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("baoruan_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "BAORUANUSER", "注册帐号", "宝软一号通账号", "输入密码", "确认密码", "宝软一号通账号", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().matches("baoruan\\d+_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "BAORUANUSER", "注册帐号", "宝软一号通账号", "输入密码", "确认密码", "宝软一号通账号", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("tbtsdk_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "TBTSDKUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().matches("tbtsdk\\d+_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "TBTSDKUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("qqzone")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "QQUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			} else if (channel.toLowerCase().matches("qqzone\\d+_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "QQUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("qqopenapi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "QQUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			} else if (channel.toLowerCase().matches("qqopenapi\\d+_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "QQUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("yunyousdk")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "YUNYOUUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			} else if (channel.toLowerCase().matches("yunyousdk\\d+_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "YUNYOUUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("xiaomisdk")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "XIAOMIUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("xiaomi2sdk")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "XIAOMIUSER2", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			} else if (channel.toLowerCase().matches("xiaomisdk\\d+_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "XIAOMIUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("360sdk")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "360SDKUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			} else if (channel.toLowerCase().matches("360sdk\\d+_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "360SDKUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("360newsdk")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "360SDKUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			} else if (channel.toLowerCase().matches("360newsdk\\d+_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "360SDKUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			
			else if (channel.toLowerCase().contains("360jiekou")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "360SDKUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("kunlunandroid")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "KUNLUNUSER", "註冊帳號", "請輸入帳號", "輸入密碼", "確認密碼", "郵箱", "密碼",
						new String[0], new String[0]);
				return res;
			}
			/*else if (channel.toLowerCase().contains("kunlunandroid")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "KUNLUNUSER", "註冊帳號", "請輸入用戶名", "輸入密碼", "確認密碼", "用戶名", "密碼",
						new String[0], new String[0]);
				return res;
			}*/
			else if (channel.toLowerCase().contains("kunlunappstore")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "KUNLUNUSER", "註冊帳號", "請輸入帳號", "輸入密碼", "確認密碼", "郵箱", "密碼",
						new String[0], new String[0]);
				return res;
			}
			
			else if (channel.toLowerCase().contains("kunlunsdkandroid")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "KUNLUNSDKUSER", "註冊帳號", "請輸入帳號", "輸入密碼", "確認密碼", "郵箱", "密碼",
						new String[0], new String[0]);
				return res;
			}
			
			else if (channel.toLowerCase().contains("kunlunsdkappstore")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "KUNLUNSDKUSER", "註冊帳號", "請輸入帳號", "輸入密碼", "確認密碼", "郵箱", "密碼",
						new String[0], new String[0]);
				return res;
			}
			
			
			//			else if (channel.toLowerCase().contains("malai")) {
			//				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "MALAIUSER", "注册帐号", "请输入邮箱", "输入密码", "确认密码", "邮箱", "密码",
			//						new String[0], new String[0]);
			//				return res;
			//			}
			else if (channel.toLowerCase().contains("malai")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "MALAIUSER", "注册帐号", "请输入帐号", "输入密码", "确认密码", "帐号", "密码",
						new String[0], new String[0]);
				return res;
			}
			
			else if (channel.toLowerCase().contains("koreasdk")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "KOREASDKUSER", "계정 생성", "메일을 입력하세요", "비밀번호를 입력하세요", "비밀번호를 확인하세요", "메일", "비밀번호",
						new String[0], new String[0]);
				return res;
			}
			
			else if (channel.toLowerCase().contains("korea")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "KOREAUSER", "계정 생성", "메일을 입력하세요", "비밀번호를 입력하세요", "비밀번호를 확인하세요", "메일", "비밀번호",
						new String[0], new String[0]);
				return res;
			}


			/*	else if (channel.toLowerCase().contains("kunlunappstore")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "KUNLUNUSER", "註冊帳號", "請輸入用戶名", "輸入密碼", "確認密碼", "用戶名", "密碼",
						new String[0], new String[0]);
				return res;
			}*/
			
			else if (channel.toLowerCase().contains("duokuapi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "DUOKUUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("baiduyy")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "DUOKUUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			
			else if (channel.toLowerCase().contains("553sdk")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "553USER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("3gsdk")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "3GUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("ledou")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "LENOVOUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("walisdk")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "WALIUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			} else if (channel.toLowerCase().matches("walisdk\\d+_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "WALIUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if (channel.toLowerCase().contains("wandoujiasdk")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "WANDOUJIASDKUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			} 
			else if (channel.contains("SOGOU")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "SOGOUUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if(channel.toLowerCase().contains("baidusdk"))
			{
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "BAIDUSDKUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if(channel.toLowerCase().contains("kuaiyongsdk"))
			{
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "KUAIYONGSDKUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if(channel.toLowerCase().contains("mumayisdk"))
			{
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "MUMAYIUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			
			else if(channel.toLowerCase().contains("meizusdk"))
			{
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "MEIZUUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if(channel.toLowerCase().contains("meizunewsdk"))
			{
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "MEIZUUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if(channel.toLowerCase().contains("huaweisdk"))
			{
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "HUAWEISDKUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if(channel.toLowerCase().contains("huaweinewsdk"))
			{
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "HUAWEISDKUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if(channel.toLowerCase().contains("xysdk"))
			{
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "XYSDKUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			else if(channel.toLowerCase().contains("139newsdk_mieshi"))
			{
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "YDSDKUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;//XUNXIANJUEAPPSTORE_XUNXIAN
			}else if(channel.toLowerCase().contains("xunxianjueappstore_xunxian"))
			{
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "XMWANSDKUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("qqnewsdk_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "QQUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("newbaidusdk_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "BAIDUSDKUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("new91sdk_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "91USER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("kaopu_mieshi")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "KAOPUUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "用户名", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("qqysdk_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "QQUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("opposdk_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "OPPOUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("vivosdk_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "VIVOUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("yijiesdk_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "YIJIEUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("yijie01sdk_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "YIJIEUSER2", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("yijie02sdk_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "YIJIEUSER3", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("u8sdk_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "U8USER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("qileappstore_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "HUIYAOUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("huogameappstore_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "HUOGAMEUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("huogame2appstore_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "HUOGAMEUSER2", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("huogame3appstore_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "HUOGAMEUSER3", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("huashengsdk_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "V8USER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("haodongsdk_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "HAODONGUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("jiuzhoupiaomiaoqu_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "JIUZHOUPIAOMIAOUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("guopansdk_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "GUOPANSDKUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("guopan2sdk_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "GUOPANSDKUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;//XUNXIANJUEAPPSTORE_XUNXIAN
			}else if (channel.toLowerCase().contains("xunxianjueappstore_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "XUNXIANJUEUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("lehaihaiappstore_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "LEHAIHAIUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("xiao7appstore_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "XIAO7USER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("anjiuappstore_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "ANJIUUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("maiyouappstore_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "MAIYOUUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("aiwanappstore_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "AIWANUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("aiwan2appstore_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "AIWAN2USER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("huogame4appstore_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "HUOGAMEUSER4", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("huogame5appstore_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "HUOGAMEUSER5", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("quicksdk_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "QUICKUSER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("quick2sdk_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "QUICKUSER2", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("quick3sdk_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "QUICKUSER3", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("6533bt_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "6533USER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}else if (channel.toLowerCase().contains("185bt_xunxian")) {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "185USER", "注册帐号", "输入用户名", "输入密码", "确认密码", "QQ号", "输入密码",
						new String[0], new String[0]);
				return res;
			}
			
			else {
				QUERY_DISPLAYER_INFO_RES res = new QUERY_DISPLAYER_INFO_RES(req.getSequnceNum(), "SQAGE", Translate.注册帐号, Translate.输入用户名, Translate.输入密码, Translate.确认密码, Translate.输入用户名, Translate.输入密码,
						new String[0], new String[0]);
				return res;
			}
		} else if (message instanceof MIESHI_GET_VERSION_INFO_REQ) {
			MIESHI_GET_VERSION_INFO_REQ req = (MIESHI_GET_VERSION_INFO_REQ)message;
			return handle_MIESHI_GET_VERSION_INFO(conn, client, req.getSequnceNum(), req.getClientId(), req.getChannel(), req.getClientPlatform(),
					req.getGpu(), req.getGpuOtherName(), req.getPhoneType(), "--", req.getClientProgramVersion(), req.getClientResourceVersion(),
					req.getStr2(), req.getStr1(), new String[0]);
		} else if (message instanceof NEW_MIESHI_GET_VERSION_INFO_REQ) {
			NEW_MIESHI_GET_VERSION_INFO_REQ req = (NEW_MIESHI_GET_VERSION_INFO_REQ)message;
			
			String clientid = req.getClientId();                              
			String channel = req.getChannel();                                
			if(channel != null && channel.equals("APPSTORE_XUNXIAN"))   
			{                                                                 
				clientId2Channel.put(clientid, channel);                        
			}                                                                 
			
			StringBuffer sb = new StringBuffer("");
			sb.append(req.getClientId()).append(req.getChannel()).append(req.getPlatform()).append(req.getGpu()).append(req.getGpuOtherName())
			.append(req.getPhoneType()).append(req.getMac()).append(req.getClientProgramVersion()).append(req.getClientResourceVersion())
			.append(req.getNetMode()).append(req.getPackageType());
			for (int i = 0; i < req.getExtraInfo().length;i ++) {
				sb.append(req.getExtraInfo()[i]);
			} 
			sb.append(MIESHI_VERSION_PRIVATE_KEY);
			String serMD5 = StringUtil.hash(sb.toString());

			StringBuffer logsb = new StringBuffer("");
			logsb
			.append("[").append(req.getClientId()).append("] ")
			.append("[").append(req.getChannel()).append("] ")
			.append("[").append(req.getPlatform()).append("] ")
			.append("[").append(req.getGpu()).append("] ")
			.append("[").append(req.getGpuOtherName()).append("] ")
			.append("[").append(req.getPhoneType()).append("] ")
			.append("[").append(req.getMac()).append("] ")
			.append("[").append(req.getClientProgramVersion()).append("] ")
			.append("[").append(req.getClientResourceVersion()).append("] ")
			.append("[").append(req.getNetMode()).append("] ")
			.append("[").append(req.getPackageType()).append("]");
			for (int i = 0; i < req.getExtraInfo().length;i ++) {
				logsb.append(" [").append(req.getExtraInfo()[i]).append("]");
			} 
			if (!serMD5.equals(req.getMd5())) {
				logger.warn("[协议MD5不匹配] ["+conn.getName()+"] ["+logsb.toString()+"] ["+req.getMac()+"] ["+serMD5+"]");
				return null;
			}

			MIESHI_GET_VERSION_INFO_RES res = handle_MIESHI_GET_VERSION_INFO(conn, client, req.getSequnceNum(), req.getClientId(), req.getChannel(), req.getPlatform(), req.getGpu(), req.getGpuOtherName(), req.getPhoneType(), req.getMac(), req.getClientProgramVersion(), req.getClientResourceVersion(), req.getNetMode(), req.getPackageType(), req.getExtraInfo());
			if (res != null) {
				gateway.sendMessageToClient(conn, res);
			}
			logger.warn("[新version协议] [成功] ["+logsb+"] ["+res.getLink()+"] ["+res.getNewPackageSize()+"]");
			return null;
		} else if (message instanceof MIESHI_GET_RESOURCE_PACKAGE_INFO_REQ) {
			MIESHI_GET_RESOURCE_PACKAGE_INFO_REQ req = (MIESHI_GET_RESOURCE_PACKAGE_INFO_REQ) message;
			String clientId = req.getClientId();
			String channel = req.getChannel();
			String platform = req.getClientPlatform();
			String gpu = req.getGpu();
			String clientCurrentProgramVersion = req.getClientProgramVersion();
			String clientCurrentResourceVersion = req.getClientResourceVersion();
			String str1 = req.getStr1();
			String str2 = req.getStr2();
			logger.warn("MIESHI_GET_RESOURCE_PACKAGE_INFO_REQ   ["+str1+"]");
			if (channel.equals("139NEWSDK_MIESHI")) {
				MIESHI_GET_RESOURCE_PACKAGE_INFO_RES res = new MIESHI_GET_RESOURCE_PACKAGE_INFO_RES(
						req.getSequnceNum(), false, "", 0, "");
				return res;
			}
			if (str1.equals("MINI")) {
				MiniResourceZipData data = MiniResourceZipManager.instance.getMiniResourceUrl(clientCurrentResourceVersion);
				if (data != null) {
					MiniResourceZipManager.logger.warn("[用户尝试miniZIP] ["+clientId+"] ["+channel+"] ["+platform+"] ["+gpu+"] ["+clientCurrentResourceVersion+"]");
					String url = data.getDownUrl(channel, platform, client.getGpu());
					if (url != null && url.length() > 0) {
						int zipsize = (int)data.getSize(gpu, platform);
						MIESHI_GET_RESOURCE_PACKAGE_INFO_RES res = new MIESHI_GET_RESOURCE_PACKAGE_INFO_RES(
								req.getSequnceNum(), true, url.substring(url.lastIndexOf("/")+1), zipsize, url);
						MiniResourceZipManager.logger.warn("[用户更新miniZIP] ["+clientId+"] ["+channel+"] ["+platform+"] ["+gpu+"] ["+clientCurrentResourceVersion+"] ["+url+"] ["+zipsize+"]");
						return res;
					}
				}
				
				try
				{
					StatManager manager = StatManager.getInstance();
					if(manager != null)
						manager.notifyStartDownloadResource(req.getClientId());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
				MIESHI_GET_RESOURCE_PACKAGE_INFO_RES res = new MIESHI_GET_RESOURCE_PACKAGE_INFO_RES(
						req.getSequnceNum(), false, "", 0, "");
				return res;
			}else {
				String link = "";
				
				boolean isTest = innerTesterManager.isInnerTester(clientId);
				PackageManager pm = PackageManager.getInstance();
				ResourceManager rm = ResourceManager.getInstance();
				
				//			if(gpu != null && gpu.equalsIgnoreCase("etc_general")){
				//				gpu = "etc_res";
				//				req.setGpu("etc_res");
				//			}
				
				if (isTest) {
					// 寻找尽可能高版本的zip资源包,尽量避免单个文件下载
					File file = null;
					int resVer = rm.getTestResourceVersion();
					Version[] packageVers = pm.getTestVersions();
					String zipRoot = rm.getTestResourceRootPath();
					while (file == null) {
						for (int i = packageVers.length - 1; i >= 0; i--) {
							String vs = packageVers[i].versionString;
							String filename = "";
							if (platform.toLowerCase().equals("android")) {
								filename = "pmxx_res_" + vs + "_" + resVer + "_" + gpu + "_Android.zip";
							} else if (platform.toLowerCase().equals("ios")) {
								filename = "pmxx_res_" + vs + "_" + resVer + "_" + gpu + "_IOS.zip";
							}else if (platform.toLowerCase().equals("win8")) {
								filename = "pmxx_res_" + vs + "_" + resVer + "_" + gpu + "_WIN8.zip";
							}
							
							File f = new File(zipRoot + filename);
							
							if (f.exists()) {
								file = f;
								break;
							}
							else
							{
								if (logger.isInfoEnabled()) {
									logger.info("[" + conn.getName() + "] [最新测试资源zip包检查] [在默认的资源包位置没有找到文件]" +
											"[" + message.getTypeDescription() + "] [" + f.exists() + "] [file:" + f
											+ "] [url:" + (rm.getTestResourceRootHttpPath() + f.getName()) + "][file的绝对路径为:"+f.getAbsolutePath()+"]" + client.getLogStr() + " [str1:" + str1 + "] [str2:" + str2
											+ "]");
								}
							}
						}
						resVer--;
					}
					if (file == null) {
						file = new File(zipRoot + "pmxx_res_最新客户端版本_" + rm.getTestResourceVersion() + "_" + gpu + "_" + platform + ".zip");
					}
					
					if (logger.isInfoEnabled()) {
						logger.info("[" + conn.getName() + "] [最新测试资源zip包检查] [" + message.getTypeDescription() + "] [" + file.exists() + "] [file:" + file
								+ "] [url:" + (rm.getTestResourceRootHttpPath() + file.getName()) + "]" + client.getLogStr() + " [str1:" + str1 + "] [str2:" + str2
								+ "]");
					}
					
					if (file.exists()) {
						MIESHI_GET_RESOURCE_PACKAGE_INFO_RES res = new MIESHI_GET_RESOURCE_PACKAGE_INFO_RES(req.getSequnceNum(), true, file.getName(), file
								.length(), rm.getTestResourceRootHttpPath() + file.getName());
						return res;
					} else {
						MIESHI_GET_RESOURCE_PACKAGE_INFO_RES res = new MIESHI_GET_RESOURCE_PACKAGE_INFO_RES(req.getSequnceNum(), false, file.getName(), 0, rm
								.getTestResourceRootHttpPath()
								+ file.getName());
						return res;
					}
					
				} else {
					
					ResourceSharedNode.ResourcePackage sharedURL = null;
					ResourceSharedNode node = null;
					ResourceSharedNodeManager rsnm = ResourceSharedNodeManager.getInstance();
					if (rsnm != null) {
						node = rsnm.getResourceSharedNodeByChannel(channel);
						if (node != null && node.isEnable()) {
							if (platform.toLowerCase().equals("android")) {
								sharedURL = node.getURLForDownLoad(gpu, "Android");
							} else if (platform.toLowerCase().equals("ios")) {
								sharedURL = node.getURLForDownLoad(gpu, "IOS");
							}else if (platform.toLowerCase().equals("win8")) {
								sharedURL = node.getURLForDownLoad(gpu, "WIN8");
							}
						}
					}
					if (sharedURL != null) {
						
						if (logger.isInfoEnabled()) {
							logger.info("[" + conn.getName() + "] [从资源共享节点下载资源包] [节点:" + node.getName() + "] [" + message.getTypeDescription() + "] [filesize:"
									+ sharedURL.fileSize + "] [file:" + sharedURL.filename + "] [url:" + sharedURL.url + "] " + client.getLogStr() + " [str1:"
									+ str1 + "] [str2:" + str2 + "]");
						}
						
						MIESHI_GET_RESOURCE_PACKAGE_INFO_RES res = new MIESHI_GET_RESOURCE_PACKAGE_INFO_RES(req.getSequnceNum(), true, sharedURL.filename,
								sharedURL.fileSize, sharedURL.url);
						return res;
					}
					
					// 寻找尽可能高版本的zip资源包,尽量避免单个文件下载
					File file = null;
					int resVer = rm.getRealResourceVersion();
					Version[] packageVers = pm.getRealVersions();
					String zipRoot = rm.getRealResourceRootPath();
					while (file == null) {
						for (int i = packageVers.length - 1; i >= 0; i--) {
							String vs = packageVers[i].versionString;
							String filename = "";
							if (platform.toLowerCase().equals("android")) {
								filename = "pmxx_res_" + vs + "_" + resVer + "_" + gpu + "_Android.zip";
							} else if (platform.toLowerCase().equals("ios")) {
								filename = "pmxx_res_" + vs + "_" + resVer + "_" + gpu + "_IOS.zip";
							}else if (platform.toLowerCase().equals("win8")) {
								filename = "pmxx_res_" + vs + "_" + resVer + "_" + gpu + "_WIN8.zip";
							}
							
							File f = new File(zipRoot + filename);
							if (f.exists()) {
								file = f;
								break;
							}
							else
							{
								if (logger.isDebugEnabled()) {
									logger.debug("[" + conn.getName() + "] [最新测试资源zip包检查] [" + message.getTypeDescription() + "] [" + f.exists() + "] [file:" + f
											+ "] [url:" + (rm.getTestResourceRootHttpPath() + f.getName()) + "]" + client.getLogStr() + " [str1:" + str1 + "] [str2:" + str2
											+ "]");
								}
							}
						}
						resVer--;
					}
					if (file == null) {
						file = new File(zipRoot + "pmxx_res_最新客户端版本_" + rm.getRealResourceVersion() + "_" + gpu + "_" + platform + ".zip");
					}
					
					
					if (logger.isInfoEnabled()) {
						logger.info("[" + conn.getName() + "] [最新正式资源zip包检查] [" + message.getTypeDescription() + "] [" + file.exists() + "] [file:" + file
								+ "] [url:" + (rm.getRealResourceRootHttpPath() + file.getName()) + "]" + client.getLogStr() + " [str1:" + str1 + "] [str2:" + str2
								+ "]");
					}
					
					if (file.exists()) {
						MIESHI_GET_RESOURCE_PACKAGE_INFO_RES res = new MIESHI_GET_RESOURCE_PACKAGE_INFO_RES(req.getSequnceNum(), true, file.getName(), file
								.length(), rm.getRealResourceRootHttpPath() + file.getName());
						return res;
					} else {
						MIESHI_GET_RESOURCE_PACKAGE_INFO_RES res = new MIESHI_GET_RESOURCE_PACKAGE_INFO_RES(req.getSequnceNum(), false, file.getName(), 0, rm
								.getRealResourceRootHttpPath()
								+ file.getName());
						return res;
					}
				}
			}
		} else if (message instanceof MIESHI_GET_RESOURCE_FILE_LIST_REQ) {
			MIESHI_GET_RESOURCE_FILE_LIST_REQ req = (MIESHI_GET_RESOURCE_FILE_LIST_REQ) message;
			String clientId = req.getClientId();
			String channel = req.getChannel();
			String platform = req.getClientPlatform();
			String gpu = req.getGpu();
			String clientCurrentProgramVersion = req.getClientProgramVersion();
			String clientCurrentResourceVersion = req.getClientResourceVersion();
			String phoneType = req.getPhoneType();
			ResourceManager rm = ResourceManager.getInstance();

			String path = rm.getTestResourceRootPath();
			if (!innerTesterManager.isInnerTester(clientId)) {
				path = rm.getRealResourceRootPath() + "/" + rm.getRandomResourcePath();
			}
			File file = new File(path + "/" + rm.getResourceFileForClient());

			ArrayList<MIESHI_RESOURCE_FILE_REQ> resultList = new ArrayList<MIESHI_RESOURCE_FILE_REQ>();

			int length = rm.constructResouceDataForClient(rm.getResourceFileForClient(), ""
					+ (innerTesterManager.isInnerTester(clientId) ? rm.getTestResourceVersion() : rm.getRealResourceVersion()), file, resultList);

			if (length <= 0) {
				MIESHI_GET_RESOURCE_FILE_LIST_RES res = new MIESHI_GET_RESOURCE_FILE_LIST_RES(req.getSequnceNum(), 1, ""
						+ (innerTesterManager.isInnerTester(clientId) ? rm.getTestResourceVersion() : rm.getRealResourceVersion()), 0, 0);

				logger.warn("[" + conn.getName() + "] [获取资源文列表] [失败] [文件不存在] [大小:0] [拆包:0] [" + message.getTypeDescription() + "] " + client.getLogStr());
				return res;
			} else {

				MIESHI_GET_RESOURCE_FILE_LIST_RES res = new MIESHI_GET_RESOURCE_FILE_LIST_RES(req.getSequnceNum(), 0, ""
						+ (innerTesterManager.isInnerTester(clientId) ? rm.getTestResourceVersion() : rm.getRealResourceVersion()), length, resultList.size());
				gateway.sendMessageToClient(conn, res);
				for (int i = 0; i < resultList.size(); i++) {
					gateway.sendMessageToClient(conn, resultList.get(i));
				}
				if (logger.isInfoEnabled()) {
					logger.info("[" + conn.getName() + "] [获取资源文件列表] [成功] [OK] [大小:" + length + "] [拆包:" + resultList.size() + "] ["
							+ message.getTypeDescription() + "] " + client.getLogStr());
				}
				return null;
			}
		} else if (message instanceof MIESHI_GET_RESOURCE_REQ) {
			MIESHI_GET_RESOURCE_REQ req = (MIESHI_GET_RESOURCE_REQ) message;

			
			String clientId = req.getClientId();
			String channel = req.getChannel();
			String platform = req.getClientPlatform();
			String gpu = req.getGpu();
			String clientCurrentProgramVersion = req.getClientProgramVersion();
			String clientCurrentResourceVersion = req.getClientResourceVersion();
			String phoneType = req.getPhoneType();
			

			
			
			StatManager stat = StatManager.getInstance();
			if(stat != null)
			{
				stat.notifyStartDownloadResource(clientId);
			}

			//			if(gpu != null && gpu.equalsIgnoreCase("etc_general")){
			//				gpu = "etc_res";
			//				req.setGpu("etc_res");
			//			}

			ResourceManager rm = ResourceManager.getInstance();


			ArrayList<MIESHI_RESOURCE_FILE_REQ> resultList = new ArrayList<MIESHI_RESOURCE_FILE_REQ>();
			String fileVersion = rm.getFileVersion(!innerTesterManager.isInnerTester(clientId), req.getFileName(), gpu);
			File resourceFile = rm.getResourceFile(!innerTesterManager.isInnerTester(clientId), req.getFileName(), gpu, platform);

			int length = rm.constructResouceDataForClient(req.getFileName(), fileVersion, resourceFile, resultList);

			if (length <= 0) {

				if (req.getFileName().indexOf("_alpha") >= 0) {
					logger.warn("[" + conn.getName() + "] [获取资源数据] [失败] [文件不存在，生产一个假文件返回客户端] [大小:0] [拆包:0] [file:" + req.getFileName() + "] ["
							+ message.getTypeDescription() + "] [fileVersion:" + fileVersion + "] [file:" + resourceFile + "] " + client.getLogStr());

					resultList.clear();
					byte bytes[] = new byte[1024];

					length = rm.constructMemoryDataForClient(req.getFileName(), bytes, resultList);

					MIESHI_GET_RESOURCE_RES res = new MIESHI_GET_RESOURCE_RES(req.getSequnceNum(), req.getFileName(), fileVersion, length, resultList.size());
					gateway.sendMessageToClient(conn, res);
					for (int i = 0; i < resultList.size(); i++) {
						gateway.sendMessageToClient(conn, resultList.get(i));
					}
					return null;
				} else {
					ArrayList<String> list = errorFileList.get(clientId);
					if (list == null) {
						list = new ArrayList<String>();
						errorFileList.put(clientId, list);
					}
					String fileName = req.getFileName();
					char[] chars = fileName.toCharArray();
					for (int i = 0; i < chars.length; i++) {
						if ((chars[i] >= 'a' && chars[i] <= 'z')) {
							continue;
						} else if ((chars[i] >= 'A' && chars[i] <= 'Z')) {
							continue;
						} else if ((chars[i] >= '0' && chars[i] <= '9')) {
							continue;
						} else if (chars[i] == '.' || chars[i] == '_' || chars[i] == '-' || chars[i] == '/' || chars[i] == ' ') {
							continue;
						} else {
							logger.warn("[" + conn.getName() + "] [clientid=" + clientId + "] [获取资源数据] [失败] [文件名字非法] [fileName=" + fileName + "]");
							list.add(fileName);
							break;
						}
					}
					if (fileName.equals("")) {
						logger.warn("[" + conn.getName() + "] [clientid=" + clientId + "] [获取资源数据] [失败] [文件名字非法] [fileName=" + fileName + "]");
						list.add(fileName);
					}
					if (list.size() > 10) {
						// 如果出现了10个取不到得资源就通知客户端去删除资源文件列表，重新下载
						logger.warn("[" + conn.getName() + "] [clientid=" + clientId + "] [获取资源数据] [失败] [文件名字非法达到10个] [发协议让用户删除sd的资源列表]");
						MIESHI_DELETE_ONE_RESOURCE_RES res = new MIESHI_DELETE_ONE_RESOURCE_RES(GameMessageFactory.nextSequnceNum(), 0,
								"pmxx_res/resourceClient.bin", Translate.您的资源列表出现错误重新下载);
						gateway.sendMessageToClient(conn, res);
						errorFileList.remove(clientId);
						return null;
					}
					logger.warn("[" + conn.getName() + "] [获取资源数据] [失败] [文件不存在] [大小:0] [拆包:0] [file:" + req.getFileName() + "] ["
							+ message.getTypeDescription() + "] [fileVersion:" + fileVersion + "] [file:" + resourceFile + "] " + client.getLogStr()
							+ "[ clientClientID=" + clientId + ", " + phoneType + "," + gpu + "]");
					MIESHI_GET_RESOURCE_RES res = new MIESHI_GET_RESOURCE_RES(req.getSequnceNum(), req.getFileName(), "", 0, 0);
					return res;
				}
			} else {

				MIESHI_GET_RESOURCE_RES res = new MIESHI_GET_RESOURCE_RES(req.getSequnceNum(), req.getFileName(), fileVersion, length, resultList.size());
				gateway.sendMessageToClient(conn, res);
				for (int i = 0; i < resultList.size(); i++) {
					gateway.sendMessageToClient(conn, resultList.get(i));
				}
				if (logger.isDebugEnabled()) {
					logger.debug("[" + conn.getName() + "] [获取资源数据] [成功] [OK] [大小:" + length + "] [拆包:" + resultList.size() + "] [file:" + req.getFileName()
							+ "] [" + message.getTypeDescription() + "] [fileVersion:" + fileVersion + "] [file:" + resourceFile + "] " + client.getLogStr());
				}
				return null;
			}
		} else if (message instanceof HEARTBEAT_CHECK_REQ) {
			HEARTBEAT_CHECK_REQ req = (HEARTBEAT_CHECK_REQ) message;

			HEARTBEAT_CHECK_RES res = new HEARTBEAT_CHECK_RES(req.getSequnceNum(), req.getGameTimeOfClient(), req.getSpeed());
			return res;
		} 
		
		else if (message instanceof NEW_USER_LOGIN_REQ) {
			NEW_USER_LOGIN_REQ req = (NEW_USER_LOGIN_REQ)message;
			LoginEntity entity = new LoginEntity(req.getUsername(), req.getPassword(), req.getChannel(), req.getClientId(), req.getMac(), req.getPlatform(), req.getPhoneType(), req.getGpuType(), req.getProgramVersion(), req.getNetworkMode(), req.getChannelUserFlag(), req.getResourceVersion(), req.getSequnceNum(), req.getUsertype());
			//robot
//			if (!ClientIDManager.isRightClientID(req.getClientId())){
//				conn.close();
//				NewLoginHandler.logger.warn("[登录协议ClientID验证不通过] ["+conn.getName()+"] ["+entity.getLogString()+"]");
//				return new NEW_USER_LOGIN_RES(req.getSequnceNum(), (byte)3, "数据非法", "", "登录失败", "", new String[0]);
//			}
			StringBuffer sb = new StringBuffer("");
			sb.append(req.getClientId()).append(req.getUsername()).append(req.getPassword()).append(req.getUsertype());
			sb.append(req.getPlatform()).append(req.getChannel()).append(req.getChannelUserFlag());
			sb.append(req.getPhoneType()).append(req.getGpuType()).append(req.getMac()).append(req.getNetworkMode());
			sb.append(req.getResourceVersion()).append(req.getProgramVersion());
			sb.append(NewLoginHandler.USER_LOGIN_PRIVATEKEY);
			String serverMd5 = StringUtil.hash(sb.toString());

			client.setClientId(req.getClientId());
			client.setChannel(req.getChannel());
			client.setPlatform(req.getPlatform());
			client.setClientCurrentProgramVersion(req.getProgramVersion());
			client.setClientCurrentResourceVersion(req.getResourceVersion());
			client.setPhoneType(req.getPhoneType());
			client.setGpuOtherName(req.getGpuType());
			client.setNetworkMode(req.getNetworkMode());
			
			//isRobot
//			if (serverMd5.equals(req.getMd5String())) {
				
				return NewLoginHandler.instance.doLogin(conn, entity);
			
//			}else {
//				conn.close();
//				NewLoginHandler.logger.warn("[登录协议MD5验证不通过] ["+conn.getName()+"] ["+entity.getLogString()+"]");
//				return null;
//			}
		} else if (message instanceof NEW_OPTION_SELECT_REQ) {
			MenuWindowManager.handle_NEW_OPTION_SELECT_REQ(conn, (NEW_OPTION_SELECT_REQ)message);
			return null;
		} else if (message instanceof USER_LEAVE_SERVER_REQ) {
			USER_LEAVE_SERVER_REQ req = (USER_LEAVE_SERVER_REQ) message;
			if (client != null) {
				client.setStepTotal(1);
				client.setStepNow(1);
			}

			conn.close();

			logger.warn("[" + req.getClientId() + "] [" + conn.getName() + "] [用户主动要求断开连接] [" + req.getUsername() + "]");
			return new USER_LEAVE_SERVER_RES(GameMessageFactory.nextSequnceNum());
		}
		else if (message instanceof QUERY_ACCOUNT_VALID_REQ) {
			QUERY_ACCOUNT_VALID_REQ req = (QUERY_ACCOUNT_VALID_REQ) message;
			String name = req.getAccountName();
			Passport p = null;
			try {
				// p = bossClientService.getPassport(name);
				p = bossClientService.getPassportByUserName(name);
			} catch (Exception e) {

			}
			QUERY_ACCOUNT_VALID_RES res;
			if (p != null) {
				res = new QUERY_ACCOUNT_VALID_RES(req.getSequnceNum(), false, Translate.用户名已存在, req.getAccountName());
			} else {
				if (name.length() < 6) {
					res = new QUERY_ACCOUNT_VALID_RES(req.getSequnceNum(), false, Translate.用户名必须6位或者以上, req.getAccountName());
				} else if (name.length() > 30) {
					res = new QUERY_ACCOUNT_VALID_RES(req.getSequnceNum(), false, Translate.用户名必须30位以下, req.getAccountName());
				} else {
					res = new QUERY_ACCOUNT_VALID_RES(req.getSequnceNum(), true, Translate.可以使用, req.getAccountName());
				}
			}

			logger.info("[" + conn.getName() + "] [检查名字是否可用] [" + res.getValid() + "] [" + res.getDescription() + "] [username:" + name + "] "
					+ client.getLogStr());
			return res;

		} else if (message instanceof GET_TIPS_REQ) {
			
//			GET_TIPS_REQ req = (GET_TIPS_REQ) message;
//
//			TipManager tm = TipManager.getInstance();
//			String tips[] = tm.getArrayTips();
//
//			GET_TIPS_RES res = new GET_TIPS_RES(req.getSequnceNum(), tips);
//			return res;

		} else if (message instanceof USER_CHANGE_PASSWORD_REQ) {
			USER_CHANGE_PASSWORD_REQ req = (USER_CHANGE_PASSWORD_REQ) message;
			String clientId = req.getClientId();
			String username = req.getUsername();
			String passwd = req.getPasswd();
			String newpasswd = req.getNewpasswd().trim();
			String protectAnswer = req.getProtectAnswer();

			Passport passport = null;
			try {
				// passport = bossClientService.getPassport(username);

				passport = bossClientService.getPassportByUserName(username);
				if (passport != null) {
					if(passport.getLastLoginChannel().toLowerCase().contains("korea"))
					{
						KunLunBossClientService kunLunBossClientService = KunLunBossClientService.getInstance();
						int result = kunLunBossClientService.updatePass(username, passwd, newpasswd, passport.getLastLoginChannel());
						if(result == 0)
						{
							logger.info("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd() + "][密保答案："
									+ req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][成功][cost:"
									+ (System.currentTimeMillis() - startTime) + "ms] ");

							USER_CHANGE_PASSWORD_RES res = new USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 0, "");
							return res;	
						}
						else
						{
							logger.warn("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd() + "][密保答案："
									+ req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][失败][密码不正确！][cost:"
									+ (System.currentTimeMillis() - startTime) + "ms] ");
							USER_CHANGE_PASSWORD_RES res = new USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 1, "");
							return res;
						}
					}



					// 如果有密保答案
					if (passport.getSecretAnswer() != null) {
						// 如果密保答案和传入答案相等
						if (protectAnswer.equals(passport.getSecretAnswer())) {
							// 如果密码和传入密码相等
							if (StringUtil.hash(passwd).equals(passport.getPassWd())) {
								passport.setPassWd(StringUtil.hash(newpasswd));

								boolean isOk = bossClientService.update(passport);
								if (isOk) // 如果更新成功
								{
									try {
										//更新最后一次登录信息
										bossClientService.login(req.getUsername(), newpasswd, req.getClientId(), client.getChannel(), client.getPlatform(),
												client.getPhoneType(), client.getNetworkMode());
									} catch (Exception e) {
										logger.error("[登录更新Passport信息时发生异常] ["+req.getUsername()+"]", e);
									}

									//
									//userChangePasword(username,newpasswd);

									logger.info("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd()
											+ "][密保答案：" + req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][成功][cost:"
											+ (System.currentTimeMillis() - startTime) + "ms] ");

									USER_CHANGE_PASSWORD_RES res = new USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 0, "");
									return res;
								} else // 如果更新失败
								{
									logger.warn("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd()
											+ "][密保答案：" + req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][失败][未知错误！][cost:"
											+ (System.currentTimeMillis() - startTime) + "ms] ");
									USER_CHANGE_PASSWORD_RES res = new USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 5, "");
									return res;
								}
							} else // 如果密码不相等
							{
								logger.warn("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd() + "][密保答案："
										+ req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][失败][密码不正确！][cost:"
										+ (System.currentTimeMillis() - startTime) + "ms] ");
								USER_CHANGE_PASSWORD_RES res = new USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 1, "");
								return res;
							}
						} else // 如果传入的答案和passport 答案不相等
						{
							logger.warn("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd() + "][密保答案："
									+ req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][失败][答案不正确！][cost:"
									+ (System.currentTimeMillis() - startTime) + "ms] ");
							USER_CHANGE_PASSWORD_RES res = new USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 2, "");
							return res;
						}
					} else // 如果没有密保
					{
						// 如果密码和传入密码相等
						if (StringUtil.hash(passwd).equals(passport.getPassWd())) {
							passport.setPassWd(StringUtil.hash(newpasswd));
							boolean isOk = bossClientService.update(passport);
							if (isOk) {

								try {
									//更新最后一次登录信息
									bossClientService.login(req.getUsername(), newpasswd, req.getClientId(), client.getChannel(), client.getPlatform(),
											client.getPhoneType(), client.getNetworkMode());
								} catch (Exception e) {
									logger.error("[登录更新Passport信息时发生异常] ["+req.getUsername()+"]", e);
								}

								logger.info("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd() + "][密保答案："
										+ req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][成功][cost:"
										+ (System.currentTimeMillis() - startTime) + "ms] ");
								USER_CHANGE_PASSWORD_RES res = new USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 0, "");
								return res;
							}

							logger.warn("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd() + "][密保答案："
									+ req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][失败][未知错误！][cost:"
									+ (System.currentTimeMillis() - startTime) + "ms] ");
							USER_CHANGE_PASSWORD_RES res = new USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 5, "");
							return res;
						} else // 如果密码不相等
						{
							logger.warn("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd() + "][密保答案："
									+ req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][失败][密码不正确！][cost:"
									+ (System.currentTimeMillis() - startTime) + "ms] ");
							USER_CHANGE_PASSWORD_RES res = new USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 1, "");
							return res;
						}
					}

				} else // 如果无法查出passport
				{
					logger.warn("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd() + "][密保答案："
							+ req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][失败][Passport不存在！][cost:"
							+ (System.currentTimeMillis() - startTime) + "ms] ");
					USER_QUERY_PROTECT_QUESTION_RES res = new USER_QUERY_PROTECT_QUESTION_RES(message.getSequnceNum(), (byte) 3, username, "");
					return res;
				}
			} catch (Exception e) {
				logger.error("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd() + "][密保答案："
						+ req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][失败][出现异常！][cost:" + (System.currentTimeMillis() - startTime)
						+ "ms] ");
				USER_CHANGE_PASSWORD_RES res = new USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 4, "");
				return res;
			}
		} else if (message instanceof NEW_USER_CHANGE_PASSWORD_REQ) {
			NEW_USER_CHANGE_PASSWORD_REQ req = (NEW_USER_CHANGE_PASSWORD_REQ) message;
			String clientId = req.getClientId();
			String username = req.getUsername();
			String passwd = req.getPasswd();
			String newpasswd = req.getNewpasswd().trim();
			String protectAnswer = req.getProtectAnswer();

			Passport passport = null;
			try {
				// passport = bossClientService.getPassport(username);

				passport = bossClientService.getPassportByUserName(username);
				if (passport != null) {
					if(passport.getLastLoginChannel().toLowerCase().contains("korea"))
					{
						KunLunBossClientService kunLunBossClientService = KunLunBossClientService.getInstance();
						int result = kunLunBossClientService.updatePass(username, passwd, newpasswd, passport.getLastLoginChannel());
						if(result == 0)
						{
							logger.info("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd() + "][密保答案："
									+ req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][成功][cost:"
									+ (System.currentTimeMillis() - startTime) + "ms] ");

							NEW_USER_CHANGE_PASSWORD_RES res = new NEW_USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 0, "");
							return res;	
						}
						else
						{
							logger.warn("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd() + "][密保答案："
									+ req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][失败][密码不正确！][cost:"
									+ (System.currentTimeMillis() - startTime) + "ms] ");
							NEW_USER_CHANGE_PASSWORD_RES res = new NEW_USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 1, "");
							return res;
						}
					}



					// 如果有密保答案
					if (passport.getSecretAnswer() != null) {
						// 如果密保答案和传入答案相等
						if (protectAnswer.equals(passport.getSecretAnswer())) {
							// 如果密码和传入密码相等
							if (StringUtil.hash(passwd).equals(passport.getPassWd())) {
								//是否有授权
								passport.setPassWd(StringUtil.hash(newpasswd));

								boolean isOk = bossClientService.update(passport);
								if (isOk) // 如果更新成功
								{
									try {
										//更新最后一次登录信息
										bossClientService.login(req.getUsername(), newpasswd, req.getClientId(), client.getChannel(), client.getPlatform(),
												client.getPhoneType(), client.getNetworkMode());
									} catch (Exception e) {
										logger.error("[登录更新Passport信息时发生异常] ["+req.getUsername()+"]", e);
									}

									//
									//userChangePasword(username,newpasswd);

									logger.info("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd()
											+ "][密保答案：" + req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][成功][cost:"
											+ (System.currentTimeMillis() - startTime) + "ms] ");

									NEW_USER_CHANGE_PASSWORD_RES res = new NEW_USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 0, "");
									return res;
								} else // 如果更新失败
								{
									logger.warn("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd()
											+ "][密保答案：" + req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][失败][未知错误！][cost:"
											+ (System.currentTimeMillis() - startTime) + "ms] ");
									NEW_USER_CHANGE_PASSWORD_RES res = new NEW_USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 5, "");
									return res;
								}
							} else // 如果密码不相等
							{
								logger.warn("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd() + "][密保答案："
										+ req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][失败][密码不正确！][cost:"
										+ (System.currentTimeMillis() - startTime) + "ms] ");
								NEW_USER_CHANGE_PASSWORD_RES res = new NEW_USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 1, "");
								return res;
							}
						} else // 如果传入的答案和passport 答案不相等
						{
							logger.warn("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd() + "][密保答案："
									+ req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][失败][答案不正确！][cost:"
									+ (System.currentTimeMillis() - startTime) + "ms] ");
							NEW_USER_CHANGE_PASSWORD_RES res = new NEW_USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 2, "");
							return res;
						}
					} else // 如果没有密保
					{
						// 如果密码和传入密码相等
						if (StringUtil.hash(passwd).equals(passport.getPassWd())) {
							passport.setPassWd(StringUtil.hash(newpasswd));
							boolean isOk = bossClientService.update(passport);
							if (isOk) {

								try {
									//更新最后一次登录信息
									bossClientService.login(req.getUsername(), newpasswd, req.getClientId(), client.getChannel(), client.getPlatform(),
											client.getPhoneType(), client.getNetworkMode());
								} catch (Exception e) {
									logger.error("[登录更新Passport信息时发生异常] ["+req.getUsername()+"]", e);
								}

								logger.info("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd() + "][密保答案："
										+ req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][成功][cost:"
										+ (System.currentTimeMillis() - startTime) + "ms] ");
								NEW_USER_CHANGE_PASSWORD_RES res = new NEW_USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 0, "");
								return res;
							}

							logger.warn("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd() + "][密保答案："
									+ req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][失败][未知错误！][cost:"
									+ (System.currentTimeMillis() - startTime) + "ms] ");
							NEW_USER_CHANGE_PASSWORD_RES res = new NEW_USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 5, "");
							return res;
						} else // 如果密码不相等
						{
							logger.warn("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd() + "][密保答案："
									+ req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][失败][密码不正确！][cost:"
									+ (System.currentTimeMillis() - startTime) + "ms] ");
							NEW_USER_CHANGE_PASSWORD_RES res = new NEW_USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 1, "");
							return res;
						}
					}

				} else // 如果无法查出passport
				{
					logger.warn("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd() + "][密保答案："
							+ req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][失败][Passport不存在！][cost:"
							+ (System.currentTimeMillis() - startTime) + "ms] ");
					USER_QUERY_PROTECT_QUESTION_RES res = new USER_QUERY_PROTECT_QUESTION_RES(message.getSequnceNum(), (byte) 3, username, "");
					return res;
				}
			} catch (Exception e) {
				logger.error("[" + conn.getName() + "][用户:" + username + "][原密码：" + req.getPasswd() + "][新密码：" + req.getNewpasswd() + "][密保答案："
						+ req.getProtectAnswer() + "][clientId:" + req.getClientId() + "][修改密码][失败][出现异常！][cost:" + (System.currentTimeMillis() - startTime)
						+ "ms] ");
				NEW_USER_CHANGE_PASSWORD_RES res = new NEW_USER_CHANGE_PASSWORD_RES(message.getSequnceNum(), (byte) 4, "");
				return res;
			}
		} else if (message instanceof USER_QUERY_PROTECT_QUESTION_REQ) {
			USER_QUERY_PROTECT_QUESTION_REQ req = (USER_QUERY_PROTECT_QUESTION_REQ) message;
			String clientId = req.getClientId();
			String username = req.getUsername();
			try {
				// passport = bossClientService.getPassport(username);
				Passport passport = bossClientService.getPassportByUserName(username);
				if (passport != null) // 如果有passport
				{
					// 判断是否有密保
					// 有密保
					if (!StringUtils.isEmpty(passport.getSecretQuestion())) {
						logger.info("[" + conn.getName() + "][用户:" + username + "][密保问题：" + passport.getSecretQuestion() + "][密保答案："
								+ passport.getSecretAnswer() + "][clientId:" + req.getClientId() + "][查询密保问题][成功][cost:"
								+ (System.currentTimeMillis() - startTime) + "ms] ");
						USER_QUERY_PROTECT_QUESTION_RES res = new USER_QUERY_PROTECT_QUESTION_RES(message.getSequnceNum(), (byte) 0, username, passport
								.getSecretQuestion());
						return res;
					} else {
						// 无密保
						logger.warn("[" + conn.getName() + "][用户:" + username + "][密保问题：" + passport.getSecretQuestion() + "][密保答案："
								+ passport.getSecretAnswer() + "][clientId:" + req.getClientId() + "][查询密保问题][成功][无密保问题][cost:"
								+ (System.currentTimeMillis() - startTime) + "ms] ");
						USER_QUERY_PROTECT_QUESTION_RES res = new USER_QUERY_PROTECT_QUESTION_RES(message.getSequnceNum(), (byte) 1, username, "");
						return res;
					}
				} else // 无用户
				{
					logger.warn("[" + conn.getName() + "][用户:" + username + "][clientId:" + req.getClientId() + "][查询密保问题][失败][无此用户][cost:"
							+ (System.currentTimeMillis() - startTime) + "ms] ");
					USER_QUERY_PROTECT_QUESTION_RES res = new USER_QUERY_PROTECT_QUESTION_RES(message.getSequnceNum(), (byte) 2, username, "");
					return res;
				}

			} catch (UsernameNotExistsException e) {
				logger.warn("[" + conn.getName() + "][用户:" + username + "][clientId:" + req.getClientId() + "][查询密保问题][失败][无此用户][cost:"
						+ (System.currentTimeMillis() - startTime) + "ms] ");
				USER_QUERY_PROTECT_QUESTION_RES res = new USER_QUERY_PROTECT_QUESTION_RES(message.getSequnceNum(), (byte) 2, username, "");
				return res;
			} catch (Exception e) {
				logger.error("[" + conn.getName() + "][用户:" + username + "][clientId:" + req.getClientId() + "][查询密保问题][失败][内部错误][cost:"
						+ (System.currentTimeMillis() - startTime) + "ms] ");
				USER_QUERY_PROTECT_QUESTION_RES res = new USER_QUERY_PROTECT_QUESTION_RES(message.getSequnceNum(), (byte) 3, username, "");
				return res;
			}

		} else if (message instanceof PLATFORM_ARGS_REQ) {
			PLATFORM_ARGS_REQ req = (PLATFORM_ARGS_REQ) message;
			String userType = req.getUserType();
			if (userType.equals("QQUSER")) {
				PLATFORM_ARGS_RES res = new PLATFORM_ARGS_RES(message.getSequnceNum(), "940", "181", "182",
						"http://211.139.188.52:8899/resource/mbox/netGame/android/TxNetGameManager_release.apk");
				/*PLATFORM_ARGS_RES res = new PLATFORM_ARGS_RES(message.getSequnceNum(), "940", "181", "182",
						"http://mg.3g.qq.com/TxNetGameManager.apk");*/
				return res;
			} else if (userType.equals("91USER")) {
				// 91时，cpId为appId, gameId为appKey, apkUrl不为空时，客户端更新用咱们自己网关的包
				PLATFORM_ARGS_RES res = new PLATFORM_ARGS_RES(message.getSequnceNum(), "102110", "80ebd0ce0aeb10d428769c5a9498b80f580a007ab104b350", "", "own");
				return res;
			} else {
				// logger.warn("[获取平台参数] [失败：没有为此userType分配参数]
				// [userType:"+userType+"]");
			}
		} else if (message instanceof SERVERSLIST_AND_PASSPORTQUESTION_REQ) {
			SERVERSLIST_AND_PASSPORTQUESTION_REQ req = (SERVERSLIST_AND_PASSPORTQUESTION_REQ) message;
			String areaType = "一区";
			StringBuffer sb = new StringBuffer();
			if (req.getAreatype() != null && !req.getAreatype().equals("")) {
				areaType = req.getAreatype();
			}

			MieshiServerInfoManager sm = MieshiServerInfoManager.getInstance();
			String areas[] = sm.getServerCategories();
			MieshiServerInfoManager mm = MieshiServerInfoManager.getInstance();
			MieshiServerInfo si[] = mm.getServerInfoSortedListByCategory(areaType);

			List<String> al = new ArrayList<String>();
			for (MieshiServerInfo serverInfo : si) {
				if (serverInfo != null && serverInfo.getName() != null && !serverInfo.getName().equals("")) {
					al.add(serverInfo.getName());
					sb.append(serverInfo.getName() + ",");
				}
			}
			if (logger.isInfoEnabled()) {
				logger.info("[获取区服务器密保问题] [成功] [区：" + areaType + "] [服务器：" + sb.toString() + "]");
			}

			SERVERSLIST_AND_PASSPORTQUESTION_RES res = new SERVERSLIST_AND_PASSPORTQUESTION_RES(message.getSequnceNum(), areas, al.toArray(new String[0]),
					areaType, allQuestion);
			return res;
		} else if (message instanceof ATTENTIONS_REQ) {
			String text = Translate.请不要将您的账号信息透露给他人;
			ATTENTIONS_RES res = new ATTENTIONS_RES(message.getSequnceNum(), text);
			return res;
		} else if (message instanceof PASSPORT_GETBACK_REQ) {
			PASSPORT_GETBACK_REQ req = (PASSPORT_GETBACK_REQ) message;
			long passportid = req.getPassportid();
			String clientId = req.getClientId();
			String channel = req.getChannel();
			String MobileType = req.getMobileType();
			String areaname = req.getAreaname();
			String name = req.getName();
			String servername = req.getServername();
			String registerMobile = req.getRegisterMobile();
			String username = req.getUsername();
			String secretQuestion = req.getSecretQuestion();
			String secretAnswer = req.getSecretAnswer();
			String lastChargeDate = req.getLastChargeDate();
			String lastChargeAmount = req.getLastChargeAmount();
			String lastLoginDate = req.getLastLoginDate();

			String mess = "";

			byte resulttype = 1;

			if (areaname != null && !areaname.trim().equals("") && servername != null && !servername.trim().equals("") && registerMobile != null
					&& !registerMobile.trim().equals("") && name != null && !name.trim().equals("")) {
				try {
					PassportRecord precord = new PassportRecord();
					precord.setClientId(clientId);
					precord.setAreaname(areaname);
					precord.setServername(servername);
					precord.setCommittime(new java.util.Date(System.currentTimeMillis()));
					precord.setName(name);
					precord.setUsername(username);
					precord.setPassportanswer(secretAnswer);
					precord.setMobiletype(MobileType);
					precord.setPassportquestion(secretQuestion);
					precord.setTelnumber(registerMobile);
					precord.setChannel(channel);
					precord.setLastchargeamount(lastChargeAmount);
					precord.setLastchargedate(lastChargeDate);
					precord.setLastlogindate(lastLoginDate);
					precord.setState(Translate.等待处理);

					RecordManager rm = RecordManager.getInstance();
					if (rm != null) {
						rm.savePassportRecord(precord);
					}
					//					mess = "我们已经成功收录了您的信息；\n 工作人员将在1个工作日内为您核实您填写的信息；\n 并通过电话的方式主动联系您，请耐心等待。";
					mess = Translate.尊敬的玩家经核实近期有很多恶意找回密码的信息被提交;
					resulttype = 0;
					if (logger.isInfoEnabled())
						logger.info("[提交密保记录] [成功] [设过密保] [手机号：" + registerMobile + "] [用户名：" + username + "] [角色名：" + name + "] [所选大区：" + areaname
								+ "] [所选服务器：" + servername + "]");
					PASSPORT_GETBACK_RES res = new PASSPORT_GETBACK_RES(message.getSequnceNum(), resulttype, mess);
					return res;

				} catch (Exception e) {
					mess = Translate.通过密保找回密码出现异常;
					logger.warn("[提交密保记录] [异常] [原因：" + mess + "] [手机号：" + registerMobile + "] [用户名：" + username + "] [角色名：" + name + "] [所选大区：" + areaname
							+ "] [所选服务器：" + servername + "]", e);
					PASSPORT_GETBACK_RES res = new PASSPORT_GETBACK_RES(message.getSequnceNum(), resulttype, mess);
					return res;
				}
			} else {
				mess = Translate.必填信息不完整;
				logger.warn("[提交密保记录] [异常] [原因：" + mess + "] [手机号：" + registerMobile + "] [用户名：" + username + "] [角色名：" + name + "] [所选大区：" + areaname
						+ "] [所选服务器：" + servername + "]");
				PASSPORT_GETBACK_RES res = new PASSPORT_GETBACK_RES(message.getSequnceNum(), resulttype, mess);
				return res;
			}
		}
		else if (message instanceof SEND_CLIENT_EXTENDINFO_REQ)
		{
			logger.info("[收取手机号] [已经收到消息] [] [] []");

			SEND_CLIENT_EXTENDINFO_REQ req = (SEND_CLIENT_EXTENDINFO_REQ) message;
			String[] mess = new String[]{"成功"};
			String[] infos = req.getInfos();

			logger.info("[收取手机号] [已经收到消息] [收到数组的长度为"+infos.length+"] [] []");


			String clientid = infos[0];
			String mobilenum = infos[1];
			String username = infos[2];
			String os = infos[3];
			String channel = infos[4];

			StatManager stat = StatManager.getInstance();
			if (stat != null) {
				stat.notifySaveClientExtendInfo(clientid, mobilenum, username, os, channel);
			}


			return new SEND_CLIENT_EXTENDINFO_RES(message.getSequnceNum(), mess);
		}
		else if (message instanceof QUERY_LOGIN_INFO_REQ)
		{
			long st = System.currentTimeMillis();

			QUERY_LOGIN_INFO_REQ req = (QUERY_LOGIN_INFO_REQ) message;
			String[] mess = null;
			QUERY_LOGIN_INFO_RES res = null;
			String username = req.getInfos()[0];
			String loginTime = lastLoginMap.get(username);
			int removecount = 0;
			//数组 0 为登陆用户名
			try
			{
				if(loginTime != null)
				{
					mess = new String[]{loginTime};
					res = new QUERY_LOGIN_INFO_RES(message.getSequnceNum(),mess,0,"");
					if(logger.isDebugEnabled())
					{
						logger.debug("["+conn.getName()+"] ["+Thread.currentThread().getName()+"] [游戏服反查用户登陆时间] [成功] [准备清理缓存] ["+username+"] ["+loginTime+"] ["+lastLoginMap+"] ["+clearTime+"] ["+(System.currentTimeMillis()-st)+"ms]");
					}
				}
				else
				{
					if(logger.isDebugEnabled())
					{
						logger.debug("["+conn.getName()+"] ["+Thread.currentThread().getName()+"] [游戏服反查用户登陆时间] [失败] [获取loginTime为空] ["+username+"] ["+loginTime+"] ["+lastLoginMap+"] ["+clearTime+"] ["+(System.currentTimeMillis()-st)+"ms]");
					}

				}

			}
			catch(Exception e)
			{
				logger.error("["+conn.getName()+"] ["+Thread.currentThread().getName()+"] [游戏服反查用户登陆时间] [失败] [出现异常] ["+req.getInfos()[0]+"] [--] ["+lastLoginMap.size()+"] ["+clearTime+"] ["+(System.currentTimeMillis()-st)+"ms]",e);
			}

			//清理缓存
			//清理缓存
			try
			{
				long llogintime = Long.parseLong(loginTime);
				Iterator<String> it = lastLoginMap.keySet().iterator();


				while(it.hasNext())
				{
					try
					{
						String key = it.next();
						String v = lastLoginMap.get(key);
						if(v != null)
						{
							long t = Long.parseLong(v);

							if((st - t) > clearTime)
							{
								lastLoginMap.remove(key);
								removecount+=1;
							}
						}
					}
					catch(Exception e)
					{
						if(logger.isDebugEnabled())
						{
							logger.debug("["+conn.getName()+"] ["+Thread.currentThread().getName()+"] [游戏服反查用户登陆时间] [--] [清理缓存出现异常] ["+req.getInfos()[0]+"] [--] ["+lastLoginMap.size()+"] ["+clearTime+"] ["+(System.currentTimeMillis()-st)+"ms]",e);
						}
					}

				}

				if(logger.isDebugEnabled())
				{
					logger.debug("["+conn.getName()+"] ["+Thread.currentThread().getName()+"] [游戏服反查用户登陆时间] [--] [清理缓存成功] ["+req.getInfos()[0]+"] [--] ["+lastLoginMap.size()+"] ["+clearTime+"] ["+removecount+"] ["+(System.currentTimeMillis()-st)+"ms]");
				}

			}
			catch(Exception e)
			{
				logger.error("["+conn.getName()+"] ["+Thread.currentThread().getName()+"] [游戏服反查用户登陆时间] [--] [清理缓存出现异常] ["+req.getInfos()[0]+"] [--] ["+lastLoginMap.size()+"] ["+clearTime+"] ["+(System.currentTimeMillis()-st)+"ms]",e);
			}



			if(mess == null)
			{
				mess = new String[]{""};
			}

			if(res != null)
			{
				if(logger.isInfoEnabled())
					logger.info("["+conn.getName()+"] ["+Thread.currentThread().getName()+"] [游戏服反查用户登陆时间] [成功] [ok] ["+req.getInfos()[0]+"] ["+loginTime+"] ["+lastLoginMap.size()+"] ["+clearTime+"] ["+removecount+"] ["+(System.currentTimeMillis()-st)+"ms]");
				return res;
			}
			else
			{
				if(logger.isInfoEnabled())
					logger.info("["+conn.getName()+"] ["+Thread.currentThread().getName()+"] [游戏服反查用户登陆时间] [失败] [--] ["+req.getInfos()[0]+"] ["+loginTime+"] ["+lastLoginMap.size()+"] ["+clearTime+"] ["+removecount+"] ["+(System.currentTimeMillis()-st)+"ms]");
				return new QUERY_LOGIN_INFO_RES(message.getSequnceNum(), mess,1,"查找登陆时间失败");
			}

		}else if (message instanceof CLIENT_ERROR_REQ) {
			CLIENT_ERROR_REQ req = (CLIENT_ERROR_REQ)message;
			try{
				int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
				if (errorMsgDiskCache == null) {
					errorMsgDiskCache = new DefaultDiskCache(new File(getErrorMsgPath()), "崩溃日志", 1000L*60*60*24*CACHE_DISTIME);
					Serializable a = errorMsgDiskCache.get(ERROR_DAY_CACHE);
					if (a != null) {
						errorToday = Integer.parseInt(a.toString());
					}else {
						errorToday = today;
						errorMsgDiskCache.put(ERROR_DAY_CACHE, errorToday);
					}
					Serializable b = errorMsgDiskCache.get(ERROR_MSG_CACHE + errorToday);
					if (b != null) {
						errorMsgs = (ArrayList<String[]>)b;
					}else {
						errorMsgs.clear();
						errorMsgDiskCache.put(ERROR_MSG_CACHE + errorToday, errorMsgs);
					}
				}

				if (errorToday != today) {
					errorMsgDiskCache.put(ERROR_MSG_CACHE + errorToday, errorMsgs);
					errorToday = today;
					errorMsgDiskCache.put(ERROR_DAY_CACHE, errorToday);
					errorMsgs.clear();
				}
				String clientID = req.getClientId();
				String pVersion = req.getClientProgramVersion();
				String phoneType = req.getPhoneType();
				String[] errorMsg = req.getErrorMsg();
				errorMsgs.add(errorMsg);
				errorMsgDiskCache.put(ERROR_MSG_CACHE + errorToday, errorMsgs);
				logger.warn("[收到用户崩溃信息] [cid="+clientID+"] [pv="+pVersion+"] [pt="+phoneType+"] ["+Arrays.toString(errorMsg)+"]");
			}catch (Exception e) {
				logger.error("CLIENT_ERROR_REQ协议处理", e);
			}
		}else if (message instanceof GET_REGISTER_IMAGE_REQ) {
			GET_REGISTER_IMAGE_REQ req = (GET_REGISTER_IMAGE_REQ)message;
			GET_REGISTER_IMAGE_RES res = creatRegisterCode(req.getSequnceNum(), req.getClientId());
			return res;
		}
		else if (message instanceof GET_REGISTER_IMAGE_NEW_REQ) {
			GET_REGISTER_IMAGE_NEW_REQ req = (GET_REGISTER_IMAGE_NEW_REQ)message;
			GET_REGISTER_IMAGE_NEW_RES res = creatRegisterCode(req.getSequnceNum(), req.getClientId(),showShenfenzheng);
			return res;
		}
		else if (message instanceof PASSPORT_REGISTER_PRO_REQ) {
			PASSPORT_REGISTER_PRO_REQ req = (PASSPORT_REGISTER_PRO_REQ) message;

			String yanZhengMa = req.getYanzhengma();
			String clientID = req.getClientId();
			String serverCode = registerCode.get(clientID);

			String realname = req.getRealname();
			String shenfenzheng = req.getShenfenzheng();

			PassportExtend passportExtend = new PassportExtend();
			passportExtend.realname = realname;
			passportExtend.shenfenzheng = shenfenzheng;

			String[] others = new String[0];

			if (serverCode == null) {
				PASSPORT_REGISTER_PRO_RES res = new PASSPORT_REGISTER_PRO_RES(req.getSequnceNum(), 2, Translate.验证码不存在, req.getUsername(), "", "", "", (byte) 0,others);
				return res;
			}
			if (!serverCode.equalsIgnoreCase(yanZhengMa)) {
				PASSPORT_REGISTER_PRO_RES res = new PASSPORT_REGISTER_PRO_RES(req.getSequnceNum(), 2, Translate.验证码不正确, req.getUsername(), "", "", "", (byte) 0,others);
				return res;
			}else {
				registerCode.remove(clientID);
				logger.warn("验证码验证通过 [" + clientID + "] [" + yanZhengMa +"] [" + serverCode +"]");
			}


			// 不开放注册
			if (!openregister) {
				logger.warn("[" + conn.getName() + "] [用户注册] [失败] [不开放注册] [username:" + req.getUsername() + "] [passwd:" + req.getPasswd()
						+ "] [passportId:--] [shenfenzheng:"+req.getShenfenzheng()+"] " + client.getLogStr());
				PASSPORT_REGISTER_PRO_RES res = new PASSPORT_REGISTER_PRO_RES(req.getSequnceNum(), 2, "不开放注册", req.getUsername(), "", "", "", (byte) 0,others);
				return res;
			}

			String userType = req.getUsertype();

			client.setChannel(req.getChannel());
			client.setPlatform(req.getPlatform());

			if(conn.getAttachmentData("client") != null)
			{
				GatewayClient gClient = (GatewayClient) conn.getAttachmentData("client");
				client.setClientId(gClient.getClientId());
			}




			String clientId = req.getClientId();
			PackageManager pm = PackageManager.getInstance();
			if (pm != null && pm.需要用户手动更新客户端的列表 != null && pm.需要用户手动更新客户端的列表.get(clientId) != null && pm.需要用户手动更新客户端的列表.get(clientId) == true) {
				logger.warn("[" + conn.getName() + "] [用户注册] [userType:" + userType + "] [失败] [需要强制用户手动更新包] [username:" + req.getUsername() + "] [passwd:"
						+ req.getPasswd() + "] [uid:--] [shenfenzheng:"+req.getShenfenzheng()+"] [realname:"+req.getRealname()+"] " + client.getLogStr());
				String des = "";
				if (req.getChannel().equals("DCN_MIESHI")) {
					des = PackageManager.手动更新描述[0];
				} else if (req.getChannel().equals("UC_MIESHI")) {
					des = PackageManager.手动更新描述[1];
				} else {
					des = PackageManager.手动更新描述[2];
				}
				PASSPORT_REGISTER_PRO_RES res = new PASSPORT_REGISTER_PRO_RES(req.getSequnceNum(), 2, des, req.getUsername(), "", "", "", (byte) 0,others);
				return res;
			}

			if(userType == null)
			{
				userType = "";
			}

			if(StringUtils.isEmpty(userType))
			{
				if(req.getChannel().toLowerCase().contains("kunlunandroid"))
				{
					userType = "KUNLUNUSER";
				}
				if(req.getChannel().toLowerCase().contains("kunlunappstore"))
				{
					userType = "KUNLUNUSER";
				}
				if(req.getChannel().toLowerCase().contains("duokuapi"))
				{
					userType = "DUOKUUSER";
				}
				if(req.getChannel().toLowerCase().contains("baoruan"))
				{
					userType = "BAORUANUSER";
				}
			}

			try {
				String username = req.getUsername();
				username = username.trim();
				// 去掉英文空格
				username = username.replaceAll(" ", "");
				// 去掉中文空格
				username = username.replaceAll(" ", "");
				String passwd = req.getPasswd();
				String jarid = req.getClientId();
				String channelstr = req.getChannel().trim();
				client.setChannel(channelstr);

				boolean valid = isValidUsername(username);
				if(userType.equals("KUNLUNUSER"))
				{
					valid = isValidUsername4Kunlun(username);
				}
				if (!valid) {
					StatManager stat = StatManager.getInstance();
					if (stat != null) {
						stat.notifyRegister(clientId, username, false, "SQAGE");
					}

					String result = Translate.帐号须是数字字母下划线组合;
					PASSPORT_REGISTER_PRO_RES res = new PASSPORT_REGISTER_PRO_RES(req.getSequnceNum(), 0, result, username, "", "", "", ((Byte) (byte) 0).byteValue(),others);
					logger.warn("[" + conn.getName() + "] [用户注册] [失败] [" + result + "] [usertype:"+userType+"] [username:" + username + "] [passwd:" + passwd + "] [passportId:--] [shenfenzheng:"+req.getShenfenzheng()+"] [realname:"+req.getRealname()+"] "
							+ client.getLogStr());
					return res;
				}

				String succMess = null;
				PASSPORT_REGISTER_PRO_RES res11 = validateShenfenzheng(req.getShenfenzheng(),req,conn);
				if(res11 != null)
				{
					if(res11.getResult().contains("由于您的年龄未满18岁"))
					{
						succMess = res11.getResult();
					}
					else
					{
						return res11;
					}
				}



				// 注册结果,0成功,1用户名已存在,2其他错误
				Passport passport = bossClientService.register(username, passwd, jarid, "", req.getUsertype(), channelstr, "", req.getPlatform(), client
						.getPhoneType(), client.getNetworkMode());

				if (passport != null) {
					client.setPassport(username);
					//如果是APPSTORE_XUNXIAN渠道的用户注册 则需要机型验证串
					if(passport.getRegisterChannel().contains("APPSTORE") || isAppStoreChannel(passport.getRegisterChannel())
							|| isModifyId(passport.getRegisterChannel()))
					{
						boolean isOk = false;
						//req.getWhitecontent 是 客户端传入的标识串 格式为：IOS UUID=xxxxMACADDRESS=xxxx  Android   DEVICEID=xxxxIMSI=xxxxMACADDRESS=xxxx

						isOk = bossClientService.updateAppstoreIdentify(passport,req.getWhitecontent());
						if(isOk)
						{
							if(logger.isInfoEnabled())
								logger.info("[注册用户] [更新标识串] [成功] [passportId:"+passport.getId()+"] [验证串:"+req.getWhitecontent()+"] [cost:"+(System.currentTimeMillis()-startTime)+"]");
						}
						else
						{
							logger.warn("[注册用户] [更新标识串] [失败] [passportId:"+passport.getId()+"] [验证串:"+req.getWhitecontent()+"] [cost:"+(System.currentTimeMillis()-startTime)+"]");
						}
					}
				}



				PASSPORT_REGISTER_PRO_RES res = new PASSPORT_REGISTER_PRO_RES(req.getSequnceNum(), 0, "", username, "", "", "", (byte) 0,others);
				StatManager stat = StatManager.getInstance();
				if (stat != null) {
					stat.notifyRegister(clientId, username, true, "SQAGE");
				}

				// //////////////添加注册用户 start///////////////////////////
				UserRegistFlow userRegistFlow = new UserRegistFlow();
				userRegistFlow.setDidian("未知"); // 地点
				userRegistFlow.setEmei("43co-sdf0-sdf-we454");
				userRegistFlow.setGame("缥缈寻仙曲");
				userRegistFlow.setHaoma("11111111111"); // 手机号码
				userRegistFlow.setJixing(client.getPlatform()); // 手机机型
				userRegistFlow.setUserName(username);// 用户名
				userRegistFlow.setNations(""); // 国家
				userRegistFlow.setQudao(client.getChannel()); // 渠道
				userRegistFlow.setRegisttime(System.currentTimeMillis()); // 用户注册时间
				// /////////以下字段是为快速注册的用户使用，如果不是快速注册，可以填默认值，或者（“”）空字符串
				// start////////////
				userRegistFlow.setPlayerName("--"); // //角色名
				userRegistFlow.setCreatPlayerTime(System.currentTimeMillis()); // //创建角色时间
				userRegistFlow.setFenQu("--"); // //分区
				// /////////以下字段是为快速注册的用户使用，如果不是快速注册，可以填默认值，或者（“”）空字符串
				// start////////////
				StatClientService statClientService = StatClientService.getInstance();
				if (statClientService != null)
					statClientService.sendUserRegistFlow("缥缈寻仙曲", userRegistFlow);

				// //////////////添加注册用户 end/////////////////////////////
				PASSPORT_REGISTER_PRO_RES res1 = checkRegisterLimit(conn, client, req);

				if(res1 != null)
				{
					return res1;
				}

				saveShenfenzheng(username, passportExtend);
				if(succMess != null)
				{
					//给客户端发送弹出窗口协议 提示
					MenuWindowManager.sendSimpleMsg(conn, "注册成功", succMess);
				}
				logger.warn("[" + conn.getName() + "] [用户注册] [成功] [OK] [username:" + username + "] [passwd:" + passwd + "] [passportId:" + passport.getId()
						+ "] [shenfenzheng:"+req.getShenfenzheng()+"] [realname:"+req.getRealname()+"] " + client.getLogStr());
				return res;
			} catch (UserNameInValidException e) {
				logger.warn("[用户注册][失败][账号名含有非法字符][username:" + req.getUsername() + "][shenfenzheng:"+req.getShenfenzheng()+"] [realname:"+req.getRealname()+"][cost:" + (System.currentTimeMillis() - startTime) + "ms]");
				PASSPORT_REGISTER_PRO_RES res = new PASSPORT_REGISTER_PRO_RES(req.getSequnceNum(), (byte) 3, "用户名含有非法字符", req.getUsername(), "", "", "", (byte) 0,others);
				return res;
			} catch (Exception e) {

				StatManager stat = StatManager.getInstance();
				if (stat != null) {
					stat.notifyRegister(clientId, req.getUsername(), false, "SQAGE");
				}

				String error = Translate.用户名已存在;
				byte result = 2;
				if (e instanceof UsernameAlreadyExistsException) {
					result = 1;
					logger.warn("[" + conn.getName() + "] [用户注册] [失败] [用户名已存在] [username:" + req.getUsername() + "] [passwd:" + req.getPasswd()
							+ "] [passportId:--] [shenfenzheng:"+req.getShenfenzheng()+"] [realname:"+req.getRealname()+"] " + client.getLogStr());
				} else {
					result = 2;
					error = Translate.未知错误;
					logger.warn("[" + conn.getName() + "] [用户注册] [失败] [注册失败，服务器出现未知错误] [username:" + req.getUsername() + "] [passwd:" + req.getPasswd()
							+ "] [passportId:--] [shenfenzheng:"+req.getShenfenzheng()+"] [realname:"+req.getRealname()+"] " + client.getLogStr(), e);
				}
				PASSPORT_REGISTER_PRO_RES res = new PASSPORT_REGISTER_PRO_RES(req.getSequnceNum(), result, error, req.getUsername(), "", "", "", (byte) 0,others);
				return res;
			}
		}
		else if (message instanceof USER_PROTECT_BAN_REQ) {
			USER_PROTECT_BAN_REQ req = (USER_PROTECT_BAN_REQ)message;
			String clientID = req.getClientId();
			String userName = req.getUserName();
			String platform = req.getPlatform();
			String mibaowenti = req.getMibaowenti();
			String mibaodaan = req.getMibaodaan();
			String phoneNum = req.getPhoneNum();


			USER_PROTECT_BAN_RES res = new USER_PROTECT_BAN_RES(req.getSequnceNum(), Translate.账号保护性封停成功);
			logger.warn("保护封停账号 ["+clientID+"] ["+userName+"] [" +platform+"] ["+mibaowenti+"] [" +mibaodaan+"] [" +phoneNum+"]");
			return res;
		}
		else if(message instanceof THIRDPART_PROMOTE_REQ)
		{
			THIRDPART_PROMOTE_REQ req = (THIRDPART_PROMOTE_REQ)message;
			//udid mac idfa dm dv clientId channel
			String[] infos = req.getInfos();
			String udid = infos[0];
			String mac = infos[1];
			String idfa = infos[2];
			String dm = infos[3];
			String dv = infos[4];
			String clientId = infos[5];
			String channel = infos[6];
			mac = mac.replaceAll(":", "");
			idfa = idfa.replaceAll("-", "");
			String ip = conn.getRemoteAddress();
			/**
			 * 以idfa做条件 
			 */
			String whereSql = null;
			String value = null;
			if(!StringUtils.isEmpty(idfa))
			{
				whereSql = " idfa = ? ";
				value = idfa;
			}
			else if(!StringUtils.isEmpty(mac) && !"020000000000".equalsIgnoreCase(mac))
			{
				whereSql = " mac = ? ";
				value = mac;
			}
			
			if(whereSql != null)
			{
				ThirdPartDataEntityManager dataEntityManager = ThirdPartDataEntityManager.getInstance();
				List<ThirdPartDataEntity> lst;
				try {
					lst = dataEntityManager.queryForWhere(whereSql, new Object[]{value},"id",1,5000);
					if(lst != null && lst.size() > 0)
					{
						Iterator<ThirdPartDataEntity> it = lst.iterator();
						while(it.hasNext())
						{
							ThirdPartDataEntity dataEntity = it.next();
							if(dataEntity.getStatus() == ThirdPartDataEntity.DATA_IS_VALID)
							{
								if(logger.isInfoEnabled())
									logger.info("[金山统计] [已有激活数据] [什么都不做] ["+dataEntity.getLogString()+"] [cost:"+(System.currentTimeMillis()-startTime)+"ms]");
								return null;
							}
						}
						//都未激活但是有匹配的数据
						
						try
						{
							ThirdPartDataEntity dataEntity = lst.get(lst.size()-1);
							if(dataEntity != null)
							{
								List<String> changedFieldsList = new ArrayList<String>();
								dataEntity.setStatus(ThirdPartDataEntity.DATA_IS_VALID);
								changedFieldsList.add("status");
								dataEntity.setUpdateTime(System.currentTimeMillis());
								changedFieldsList.add("updateTime");
								dataEntity.setIp(ip);
								changedFieldsList.add("ip");
								dataEntityManager.saveOrUpdate(dataEntity,false,changedFieldsList.toArray(new String[0]));
								if(logger.isInfoEnabled())
									logger.info("[金山统计] [插入数据] [成功] ["+dataEntity.getLogString()+"] [cost:"+(System.currentTimeMillis()-startTime)+"ms]");
								//通知金山
								String url = "http://c.ios.ijinshan.com/receiver/active";
								LinkedHashMap<String,String> params = new LinkedHashMap<String,String>();
								params.put("ckey",dataEntity.getQudaohao());
								params.put("clickid", dataEntity.getClickid());
								params.put("udid",dataEntity.getOpenudid());
								params.put("mac",dataEntity.getMac());
								params.put("idfa", dataEntity.getIdfa());
								params.put("ip", dataEntity.getIp());
								params.put("dm",dataEntity.getDm());
								params.put("dv",dataEntity.getDv());
								
								HashMap headers = new HashMap();
								
								String[] key = params.keySet().toArray(new String[params.size()]);

								StringBuffer sb = new StringBuffer();
								for (String k : key) {
									sb.append(k);
									sb.append("=");
									sb.append(params.get(k));
									sb.append("&");
								}
								
								String content = sb.toString().substring(0, sb.length() - 1);
								
								try {
									byte bytes[] = HttpUtils.webPost(new java.net.URL(url), content.getBytes(), headers, 60000, 60000);
									
									String encoding = (String)headers.get(HttpUtils.ENCODING);
									Integer code = (Integer)headers.get(HttpUtils.Response_Code);
									String httpmessage = (String)headers.get(HttpUtils.Response_Message);
									content = new String(bytes,encoding);
									if(logger.isInfoEnabled())
										logger.info("[调用接口] [得到响应内容] [encoding:"+encoding+"] [code:"+(code == null ?"--":code.intValue())+"] [message:"+httpmessage+"] ["+content+"] [url:"+url+"] [params:"+params+"] ["+(System.currentTimeMillis()-startTime)+"ms]");
								} catch (Exception e) {
									logger.error("[调用接口] [成功] [encoding:--] [code:--] [message:--] ["+content+"]  [channel:"+channel+"] [url:"+url+"] [params:"+params+"] ["+(System.currentTimeMillis()-startTime)+"ms]",e);
								}
							}
							else
							{
								logger.warn("[金山统计] [失败] [不应该出现的情况] [没有拿到entity] [udid:"+udid+"] [mac:"+mac+"] [idfa:"+idfa+"] [dm:"+dm+"] [dv:"+dv+"] [clientId:"+clientId+"] [channel:"+channel+"] [cost:"+(System.currentTimeMillis()-startTime)+"ms]");
							}
							
						}
						catch(Exception e)
						{
							logger.error("[金山统计] [更新数据] [失败] [出现未知异常] [udid:"+udid+"] [mac:"+mac+"] [idfa:"+idfa+"] [dm:"+dm+"] [dv:"+dv+"] [clientId:"+clientId+"] [channel:"+channel+"] [cost:"+(System.currentTimeMillis()-startTime)+"ms]",e);
						}
						
						
					}
					else
					{
						logger.error("[金山统计] [失败] [未查询到匹配数据] [udid:"+udid+"] [mac:"+mac+"] [idfa:"+idfa+"] [dm:"+dm+"] [dv:"+dv+"] [clientId:"+clientId+"] [channel:"+channel+"] [cost:"+(System.currentTimeMillis()-startTime)+"ms]");
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error("[金山统计] [失败] [出现未知异常] [udid:"+udid+"] [mac:"+mac+"] [idfa:"+idfa+"] [dm:"+dm+"] [dv:"+dv+"] [clientId:"+clientId+"] [channel:"+channel+"] [cost:"+(System.currentTimeMillis()-startTime)+"ms]",e);
				}
				return null;
				
			}
			else
			{
				logger.warn("[金山统计] [失败] [出现非法数据] [udid:"+udid+"] [mac:"+mac+"] [idfa:"+idfa+"] [dm:"+dm+"] [dv:"+dv+"] [clientId:"+clientId+"] [channel:"+channel+"] [cost:"+(System.currentTimeMillis()-startTime)+"ms]");
			}
			
			
		} else if (message instanceof NEW_QUERY_SERVER_LIST_REQ) {
			NEW_QUERY_SERVER_LIST_REQ req = (NEW_QUERY_SERVER_LIST_REQ) message;

			//client.setClientId(req.getClientId());
			String username = req.getUsername();
			Passport p = null;
			try {
				p = BossClientService.getInstance().getPassportByUserName(username);
			} catch (Exception e) {
				logger.warn("[获取服务器列表] [查找passport] [出现异常] ["+username+"]",e);	
			}
			if(p != null){
				//快用修正username
				if(p.getLastLoginChannel() != null && p.getLastLoginChannel().contains("KUAIYONGSDK")){
					username = p.getUserName();
				}
			}

			MieshiPlayerInfoManager pm = MieshiPlayerInfoManager.getInstance();
			MieshiServerInfoManager sm = MieshiServerInfoManager.getInstance();

			String categories[] = sm.getServerCategories();
			ArrayList<ServerInfoForClient> al = new ArrayList<ServerInfoForClient>();
			if(p != null)
			{
				if(StringUtils.isEmpty(client.getChannel()))
				{
					client.setChannel(p.getLastLoginChannel());
				}
			}

			for (int i = 0; i < categories.length; i++) {
				MieshiServerInfo[] serverInfos = sm.getServerInfoSortedListByCategory(categories[i]);
				for (int j = 0; j < serverInfos.length; j++) {
					MieshiServerInfo s = serverInfos[j];
//					if(s.getName().equals("首测（1区）") || s.getName().equals("涅槃封测1") || s.getName().equals("涅槃封测2")){
//						if(!client.getChannel().contains("JIUZHOUAPPSTORE_XUNXIAN") && !client.getChannel().contains("XIAOMI2SDK_XUNXIAN") && !client.getChannel().contains("YIJIE01SDK_XUNXIAN") 
//								&& !client.getChannel().contains("QUICK3SDK_XUNXIAN") && !client.getChannel().contains("YIJIE02SDK_XUNXIAN") && !client.getChannel().contains("QUICKSDK_XUNXIAN")){
//							continue;
//						}
//					}
//					if(client.getChannel().contains("JIUZHOUAPPSTORE_XUNXIAN")||client.getChannel().contains("XIAOMI2SDK_XUNXIAN")||client.getChannel().contains("QUICK3SDK_XUNXIAN") ||client.getChannel().contains("QUICKSDK_XUNXIAN") || client.getChannel().contains("YIJIE01SDK_XUNXIAN") || client.getChannel().contains("YIJIE02SDK_XUNXIAN")){
//						if(!s.getName().equals("首测（1区）") && !s.getName().equals("涅槃封测1") && !s.getName().equals("涅槃封测2")){
//							continue;
//						}
//					}
					if(client.getChannel().contains("PINGSHEN_XUNXIAN")){
						if(!s.getName().equals("测试服")){
							continue;
						}
					}
					
					if (s.getServerType() == MieshiServerInfo.SERVERTYPE_仅铸剑渠道可见 
							&& sm.isTestUser(username) == false && (!client.getChannel().equalsIgnoreCase("JIUZHOUAPPSTORE_XUNXIAN") && !client.getChannel().equalsIgnoreCase("XIAOMI2SDK_XUNXIAN") &&
									!client.getChannel().equalsIgnoreCase("YIJIE01SDK_XUNXIAN") && !client.getChannel().equalsIgnoreCase("QUICKSDK_XUNXIAN"))){
						continue;
					}
					
					if (s.getServerType() == MieshiServerInfo.SERVERTYPE_仅神仙谱渠道可见 
							&& sm.isTestUser(username) == false && (!client.getChannel().equalsIgnoreCase("QUICK3SDK_XUNXIAN") && !client.getChannel().equalsIgnoreCase("YIJIE02SDK_XUNXIAN") && !client.getChannel().equalsIgnoreCase("JIUZHOUAPPSTORE_XUNXIAN"))){
						continue;
					}
					
//					System.out.println(s.getServerType()+"--"+sm.isTestUser(username)+"--"+client.getChannel().equalsIgnoreCase("185BT_XUNXIAN"));
					if (s.getServerType() != MieshiServerInfo.SERVERTYPE_185 && sm.isTestUser(username) == false && (client.getChannel().equalsIgnoreCase("185BT_XUNXIAN"))){
						continue;
					}
					
					if (s.getServerType() == MieshiServerInfo.SERVERTYPE_除了铸剑神仙谱appstore渠道都可见 
							&& sm.isTestUser(username) == false && (client.getChannel().equalsIgnoreCase("QUICK3SDK_XUNXIAN") || client.getChannel().equalsIgnoreCase("YIJIE02SDK_XUNXIAN")
							|| client.getChannel().equalsIgnoreCase("JIUZHOUAPPSTORE_XUNXIAN") || client.getChannel().equalsIgnoreCase("XIAOMI2SDK_XUNXIAN")
							|| client.getChannel().equalsIgnoreCase("YIJIE01SDK_XUNXIAN") || client.getChannel().equalsIgnoreCase("QUICKSDK_XUNXIAN") || client.getChannel().equalsIgnoreCase("APPSTORE_XUNXIAN") || client.getChannel().equalsIgnoreCase("QILEAPPSTORE_XUNXIAN")||
							client.getChannel().equalsIgnoreCase("HUOGAMEAPPSTORE_XUNXIAN")|| client.getChannel().equalsIgnoreCase("HUOGAME2APPSTORE_XUNXIAN") ||
							client.getChannel().equalsIgnoreCase("HUOGAME4APPSTORE_XUNXIAN")|| client.getChannel().equalsIgnoreCase("HUOGAME5APPSTORE_XUNXIAN")
							|| client.getChannel().equalsIgnoreCase("APPSTOREGUOJI_XUNXIAN")
							|| client.getChannel().equalsIgnoreCase("JIUZHOUPIAOMIAOQU_XUNXIAN") || client.getChannel().equalsIgnoreCase("XUNXIANJUEAPPSTORE_XUNXIAN")
							|| client.getChannel().equalsIgnoreCase("AIWANAPPSTORE_XUNXIAN")|| client.getChannel().equalsIgnoreCase("AIWAN2APPSTORE_XUNXIAN")
							)){
						continue;
					}
					
					
					if(client.getChannel().contains("ZHUJIAN_XUNXIAN")){
						if(!s.getName().equals("渠道审核服")){
							continue;
						}
					}
					
					if(client.getChannel().contains("HAODONGSDK_XUNXIAN"))
					{
//						"10.70.61.171","14.17.22.47","10.70.58.162","163.177.68.35",
						if(conn.getRemoteAddress().contains("183.236.71.55") || conn.getRemoteAddress().contains("163.177.68.35")
								|| conn.getRemoteAddress().contains("10.70.61.171") || conn.getRemoteAddress().contains("14.17.22.47")|| 
								conn.getRemoteAddress().contains("10.70.58.162")){
							if(!s.getName().equals("渠道审核服")){
								continue;
							}
						}
						logger.warn("浩动服务器列表:"+s.getName()+"---"+conn.getRemoteAddress());
					}
					if(client.getChannel().contains("AIWANAPPSTORE_XUNXIAN") || client.getChannel().contains("AIWAN2APPSTORE_XUNXIAN"))
					{
						if(!s.getName().equals("苹果提审服")){
							continue;
						}
						logger.warn("爱玩服务器列表:"+s.getName()+"---"+conn.getRemoteAddress());
					}
					
					if(client.getChannel().contains("YIJIESDK_XUNXIAN")){
						try {
							if(p.getPassWd() != null && p.getPassWd().contains("###@@@")){
								String sdk = p.getPassWd().split("###@@@")[1];
								if(sdk.equals("81AB24E2162D54BC")){
									if(!s.getName().equals("渠道审核服")){
										continue;
									}
								}
							}
							logger.warn("易接服务器列表:"+s.getName()+"---"+conn.getRemoteAddress()+"---"+p.getPassWd());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					if (s.getServerType() == MieshiServerInfo.SERVERTYPE_正式对外的服务器 
							&& sm.isTestUser(username) == false && (client.getChannel().equalsIgnoreCase("APPSTORE_XUNXIAN") || client.getChannel().equalsIgnoreCase("QILEAPPSTORE_XUNXIAN")||
									client.getChannel().equalsIgnoreCase("HUOGAMEAPPSTORE_XUNXIAN")|| client.getChannel().equalsIgnoreCase("HUOGAME2APPSTORE_XUNXIAN") ||
									client.getChannel().equalsIgnoreCase("HUOGAME4APPSTORE_XUNXIAN")|| client.getChannel().equalsIgnoreCase("HUOGAME5APPSTORE_XUNXIAN")
									|| client.getChannel().equalsIgnoreCase("APPSTOREGUOJI_XUNXIAN")
									|| client.getChannel().equalsIgnoreCase("JIUZHOUPIAOMIAOQU_XUNXIAN") || client.getChannel().equalsIgnoreCase("XUNXIANJUEAPPSTORE_XUNXIAN")
									|| client.getChannel().equalsIgnoreCase("AIWANAPPSTORE_XUNXIAN")|| client.getChannel().equalsIgnoreCase("AIWAN2APPSTORE_XUNXIAN"))) {
						continue;
					}
					
					if (s.getServerType() == MieshiServerInfo.SERVERTYPE_内部测试服务器 
							&& sm.isTestUser(username) == false) {
						continue;
					}
					
					if (s.getServerType() == MieshiServerInfo.SERVERTYPE_UC和国服不可见 
							&& (client.getChannel().contains("NEWUCSDK_XUNXIAN") || client.getChannel().contains("YOUAI_XUNXIAN"))) {
						continue;
					}
					
					if(s.getServerType() == MieshiServerInfo.SERVERTYPE_91看不见的服务器){
						if(!client.getChannel().contains("YOUAI_XUNXIAN")){
							continue;
						}
					}
					
//					if(sm.isTestUser(username) == false && 
//							s.getServerType() == MieshiServerInfo.SERVERTYPE_WIN8专用服务器
//							&& (client.getChannel().toLowerCase().contains("win8") == false) && (client.getChannel().toLowerCase().contains("wp8") == false)) {
//						continue;
//					}

					if(sm.isTestUser(username) == false && 
							s.getServerType() == MieshiServerInfo.SERVERTYPE_苹果渠道服务器 
							&& (
//									client.getChannel().equalsIgnoreCase("APPSTORE_XUNXIAN") == false &&
									client.getChannel().equalsIgnoreCase("HUOGAME4APPSTORE_XUNXIAN") == false &&
									client.getChannel().equalsIgnoreCase("HUOGAME5APPSTORE_XUNXIAN") == false &&
//									client.getChannel().equalsIgnoreCase("JIUZHOUPIAOMIAOQU_XUNXIAN") == false &&
									client.getChannel().equalsIgnoreCase("XUNXIANJUEAPPSTORE_XUNXIAN") == false && 
									client.getChannel().equalsIgnoreCase("AIWANAPPSTORE_XUNXIAN") == false &&
									client.getChannel().equalsIgnoreCase("AIWAN2APPSTORE_XUNXIAN") == false)
							   ){
						continue;
					}

//					if(s.getServerType() == MieshiServerInfo.SERVERTYPE_苹果正式服务器 
//							&& (
//									client.getChannel().contains("APPSTORE_XUNXIAN") == false &&
//									client.getChannel().equalsIgnoreCase("JIUZHOUPIAOMIAOQU_XUNXIAN") == false && 
//									client.getChannel().contains("ANJIUAPPSTORE_XUNXIAN")&&
//									client.getChannel().contains("LEHAIHAIAPPSTORE_XUNXIAN")&&
//									client.getChannel().contains("XIAO7APPSTORE_XUNXIAN")
//							   ) 
//							&& sm.isTestUser(username) == false) {
//						continue;
//					}
					
					if(s.getServerType() == MieshiServerInfo.SERVERTYPE_苹果正式服务器 && sm.isTestUser(username) == false){
						//越狱
						if(client.getChannel().contains("XIAO7APPSTORE_XUNXIAN") || client.getChannel().contains("LEHAIHAIAPPSTORE_XUNXIAN") || client.getChannel().contains("XIAO7APPSTORE_XUNXIAN")){
							continue;
						}
						if(client.getChannel().equalsIgnoreCase("APPSTORE_XUNXIAN") == false && client.getChannel().equalsIgnoreCase("JIUZHOUPIAOMIAOQU_XUNXIAN") == false
								&& client.getChannel().equalsIgnoreCase("JIUZHOUAPPSTORE_XUNXIAN") == false && client.getChannel().equalsIgnoreCase("QILEAPPSTORE_XUNXIAN") == false){
							continue;
						}
					}
					
//					if(sm.isTestUser(username) == false && 
//							s.getServerType() == MieshiServerInfo.SERVERTYPE_苹果国际服 
//							&& client.getChannel().equalsIgnoreCase("APPSTOREGUOJI_MIESHI") == false) {
//						continue;
//					}

//					if((s.getServerType() != MieshiServerInfo.SERVERTYPE_苹果国际服 && s.getServerType() != MieshiServerInfo.SERVERTYPE_苹果正式服务器 )
//							&& (client.getChannel().equalsIgnoreCase("APPSTOREGUOJI_MIESHI") || client.getChannel().equalsIgnoreCase("APPSTORE2014_MIESHI") )
//							&& sm.isTestUser(username) == false ) {
//						continue;
//					}

//					if(sm.isTestUser(username) == false && 
//							s.getServerType() == MieshiServerInfo.SERVERTYPE_苹果台湾服 
//							&& client.getChannel().equalsIgnoreCase("APPSTORETAIWAN_MIESHI") == false) {
//						continue;
//					}
//
//					if(s.getServerType() != MieshiServerInfo.SERVERTYPE_苹果台湾服 
//							&& client.getChannel().equalsIgnoreCase("APPSTORETAIWAN_MIESHI")) {
//						continue;
//					}
//					{
//						//移动专服,
//						if(sm.isTestUser(username) == false && 
//								s.getServerType() == MieshiServerInfo.SERVERTYPE_移动专服
//								&& (client.getChannel().toLowerCase().matches("139sdk\\d*_mieshi")  == false && client.getChannel().toLowerCase().matches("139newsdk\\d*_mieshi")  == false &&  client.getChannel().toLowerCase().matches("adhome\\d*_mieshi")  == false &&  client.getChannel().toLowerCase().matches("yidongmm\\d*_mieshi")  == false  &&  client.getChannel().toLowerCase().matches("newmmsdk\\d*_mieshi")  == false  ) ) {
//							continue;
//						}
//
//						if(sm.isTestUser(username) == false && 
//								s.getServerType() != MieshiServerInfo.SERVERTYPE_移动专服
//								&& (client.getChannel().toLowerCase().matches("139sdk\\d*_mieshi") ||client.getChannel().toLowerCase().matches("139newsdk\\d*_mieshi") || client.getChannel().toLowerCase().matches("adhome\\d*_mieshi")  || client.getChannel().toLowerCase().matches("yidongmm\\d*_mieshi") || client.getChannel().toLowerCase().matches("newmmsdk\\d*_mieshi"))) {
//							continue;
//						}
//					}

//					if(sm.isTestUser(username) == false && s.getServerType() == MieshiServerInfo.SERVERTYPE_文化部测试服务器 && client.getChannel().equalsIgnoreCase("wenhuabu_MIESHI") == false){
//						continue;
//					}
//
//					if(s.getServerType() != MieshiServerInfo.SERVERTYPE_文化部测试服务器 && client.getChannel().equalsIgnoreCase("wenhuabu_MIESHI")){
//						continue;
//					}

					//
//					if(s.getServerType() == MieshiServerInfo.SERVERTYPE_第三方市场服务器){
//						String channel = client.getChannel();
//						boolean isDCN = false;
//						boolean isUC = false;
//						if (channel.toLowerCase().contains("uc_mieshi")) {
//							isUC =  true;
//						}else if (channel.toLowerCase().contains("ucsdk_mieshi")) {
//							isUC =  true;
//						} 
//						else if (channel.toLowerCase().matches("uc\\d+_mieshi")) {
//							isUC =  true;
//						}
//						else if (channel.toLowerCase().matches("ucnewsdk_mieshi")) {
//							isUC =  true;
//						} 
//						else if (channel.toLowerCase().matches("ucnewsdk\\d+_mieshi")) {
//							isUC =  true;
//						} 
//
//						else if (channel.toLowerCase().contains("dcn_mieshi")) {
//							isDCN = true;
//						} else if (channel.toLowerCase().matches("dcn\\d+_mieshi")) {
//							isDCN = true;
//						}
//
//						if(sm.isTestUser(username) == false  && (isDCN || isUC)){
//							continue;
//						}
//					}

//					if(s.getServerType() == MieshiServerInfo.SERVERTYPE_91看不见的服务器){
//						String channel = client.getChannel();
//						boolean is91 = false;
//						if (channel.toLowerCase().contains("91zhushou_mieshi")) {
//							is91 =  true;
//						} else if (channel.toLowerCase().matches("91zhushou\\d+_mieshi")) {
//							is91 =  true;
//						} 
//						else if(channel.toLowerCase().matches("new91sdk\\d_mieshi")){
//							is91 =  true;
//						} else if(channel.toLowerCase().contains("new91sdk_mieshi")){
//							is91 =  true;
//						}
//						if(is91){
//							continue;
//						}
//					}
					
					
//					if(s.getServerType() == MieshiServerInfo.SERVERTYPE_UC看不见的服务器){
//						String channel = client.getChannel();
//						boolean isUC = false;
//						if (channel.toLowerCase().contains("uc_mieshi")) {
//							isUC =  true;
//						}else if (channel.toLowerCase().contains("ucsdk_mieshi")) {
//							isUC =  true;
//						} 
//						else if (channel.toLowerCase().matches("uc\\d+_mieshi")) {
//							isUC =  true;
//						}
//						else if (channel.toLowerCase().matches("ucnewsdk_mieshi")) {
//							isUC =  true;
//						} 
//						else if (channel.toLowerCase().matches("ucnewsdk\\d+_mieshi")) {
//							isUC =  true;
//						} 
//						if(isUC){
//							continue;
//						}
//					}
					

//					if(s.getServerType() == MieshiServerInfo.SERVERTYPE_UC专用服务器){
//						String channel = client.getChannel();
//						boolean isUC = false;
//						if (channel.toLowerCase().contains("uc_mieshi")) {
//							isUC =  true;
//						}else if (channel.toLowerCase().contains("ucsdk_mieshi")) {
//							isUC =  true;
//						} 
//						else if (channel.toLowerCase().matches("uc\\d+_mieshi")) {
//							isUC =  true;
//						}
//						else if (channel.toLowerCase().matches("ucnewsdk_mieshi")) {
//							isUC =  true;
//						} 
//						else if (channel.toLowerCase().matches("ucnewsdk\\d+_mieshi")) {
//							isUC =  true;
//						} 
//
//						if(sm.isTestUser(username) == false && isUC == false){
//							continue;
//						}
//					}

//					if(s.getServerType() == MieshiServerInfo.SERVERTYPE_91专用服务器){
//						String channel = client.getChannel();
//						boolean is91 = false;
//						if (channel.toLowerCase().contains("91zhushou_mieshi")) {
//							is91 =  true;
//						} else if (channel.toLowerCase().matches("91zhushou\\d+_mieshi")) {
//							is91 =  true;
//						} else if(channel.toLowerCase().matches("new91sdk\\d_mieshi")){
//							is91 =  true;
//						} else if(channel.toLowerCase().contains("new91sdk_mieshi")){
//							is91 =  true;
//						}
//						if(sm.isTestUser(username) == false && is91 == false){
//							continue;
//						}
//					}

//					if(s.getServerType() == MieshiServerInfo.SERVERTYPE_当乐与市场类渠道服务器){
//						String channel = client.getChannel();
//						boolean is91 = false;
//						if (channel.toLowerCase().contains("91zhushou_mieshi")) {
//							is91 =  true;
//						} else if (channel.toLowerCase().matches("91zhushou\\d+_mieshi")) {
//							is91 =  true;
//						} 
//						else if(channel.toLowerCase().matches("new91sdk\\d_mieshi")){
//							is91 =  true;
//						} else if(channel.toLowerCase().contains("new91sdk_mieshi")){
//							is91 =  true;
//						}
//						boolean isUC = false;
//						if (channel.toLowerCase().contains("uc_mieshi")) {
//							isUC =  true;
//						}else if (channel.toLowerCase().contains("ucsdk_mieshi")) {
//							isUC =  true;
//						} 
//						else if (channel.toLowerCase().matches("uc\\d+_mieshi")) {
//							isUC =  true;
//						}
//						else if (channel.toLowerCase().matches("ucnewsdk_mieshi")) {
//							isUC =  true;
//						} 
//						else if (channel.toLowerCase().matches("ucnewsdk\\d+_mieshi")) {
//							isUC =  true;
//						} 
//
//						if(sm.isTestUser(username) == false && (is91 == true || isUC == true)){
//							continue;
//						}
//					}

//					if(s.getServerType() == MieshiServerInfo.SERVERTYPE_91与UC共用服务器){
//						String channel = client.getChannel();
//						boolean is91 = false;
//						if (channel.toLowerCase().contains("91zhushou_mieshi")) {
//							is91 =  true;
//						} else if (channel.toLowerCase().matches("91zhushou\\d+_mieshi")) {
//							is91 =  true;
//						} 
//						else if(channel.toLowerCase().matches("new91sdk\\d_mieshi")){
//							is91 =  true;
//						} else if(channel.toLowerCase().contains("new91sdk_mieshi")){
//							is91 =  true;
//						}
//						boolean isUC = false;
//						if (channel.toLowerCase().contains("uc_mieshi")) {
//							isUC =  true;
//						}else if (channel.toLowerCase().contains("ucsdk_mieshi")) {
//							isUC =  true;
//						} 
//						else if (channel.toLowerCase().matches("uc\\d+_mieshi")) {
//							isUC =  true;
//						}
//						else if (channel.toLowerCase().matches("ucnewsdk_mieshi")) {
//							isUC =  true;
//						} 
//						else if (channel.toLowerCase().matches("ucnewsdk\\d+_mieshi")) {
//							isUC =  true;
//						} 
//
//						if(sm.isTestUser(username) == false && (!is91 == true && !isUC == true)){
//							continue;
//						}
//					}

					if(client.getChannel().toLowerCase().contains("win8") || client.getChannel().toLowerCase().contains("wp8"))
					{
						if(!isWin8Show(s.getName()) && sm.isTestUser(username) == false)
						{
							continue;
						}
					}


					ServerInfoForClient c = new ServerInfoForClient();
					c.setCategory(s.getCategory());
					c.setClientid(s.getClientid());
					c.setServerid(s.getServerid());
					c.setName(s.getName());
					c.setOnlinePlayerNum(s.getOnlinePlayerNum());

					if (s.getStatus() == MieshiServerInfo.STATUS_OFF || s.getStatus() == MieshiServerInfo.STATUS_WEIHU) {
						c.setDescription(s.getDescription());
						c.setStatus(MieshiServerInfo.STATUS_OFF);
						c.setStatusName(MieshiServerInfo.STATUS_DESP[MieshiServerInfo.STATUS_OFF]);
						c.setIp("");
						c.setPort(0);
					} else if (s.getStatus() == MieshiServerInfo.INNER_TEST) {
						if (sm.isTestUser(username)) {
							c.setDescription(Translate.内部人员开发进入);
							c.setStatus(MieshiServerInfo.STATUS_NEW);
							c.setStatusName(MieshiServerInfo.STATUS_DESP[MieshiServerInfo.STATUS_NEW]);
							c.setIp(s.getIp());
							c.setPort(s.getPort());
						} else {
							c.setDescription(s.getDescription());
							c.setStatus(MieshiServerInfo.STATUS_OFF);
							c.setStatusName(MieshiServerInfo.STATUS_DESP[MieshiServerInfo.STATUS_OFF]);
							c.setIp("");
							c.setPort(0);
						}
					} else {
						if (s.getDescription() == null || s.getDescription().trim().length() == 0) {
							c.setDescription(s.autoGenerateDescription());
						} else {
							c.setDescription(s.getDescription());
						}

						c.setStatus(s.getStatus());
						c.setStatusName(MieshiServerInfo.STATUS_DESP[s.getStatus()]);
						c.setIp(s.getIp());
						c.setPort(s.getPort());
					}


					MieshiPlayerInfo pi = null;

					//显示级别最高的角色
					MieshiPlayerInfo[] mps =  pm.getMieshiPlayerInfosByUsername(username, serverInfos[j].getRealname());

					int maxLevel = 0;
					for(MieshiPlayerInfo mp : mps)
					{
						if(mp.getLevel() > maxLevel)
						{
							maxLevel = mp.getLevel();
							pi = mp;
						}
					}


					if (pi != null) {
						c.setHasPlayer(true);
						c.setPlayerName(pi.getPlayerName());
						c.setCareer(pi.getCareer());
						c.setLevel(pi.getLevel());
						c.lastLoginTime = pi.lastAccessTime;

					} else {
						c.setHasPlayer(false);
						c.setPlayerName("");
						c.setCareer(0);
						c.setLevel(0);
					}

					al.add(c);

					if(pi != null)
					{
						try
						{
							long now = System.currentTimeMillis();
							HeFuTipConfigInfo heFuTipConfigInfo  = null;
	
							//获取playerinfo数组 找到其中的服务器
							MieshiPlayerInfoManager playerInfoManager =  MieshiPlayerInfoManager.getInstance();
							if(playerInfoManager != null)
							{
								MieshiPlayerInfo[] playerInfos = playerInfoManager.getMieshiPlayerInfoByUsername(username);
	
								for(MieshiPlayerInfo mp : playerInfos)
								{
									String serverName = mp.getServerRealName();
									heFuTipConfigInfo =   (HeFuTipConfigInfo)hefuTipInfoCache.get(serverName);
									//发现这个玩家的账号中有被合过服的角色 ，则就应该弹窗 而不应该判断是哪个服务器
									if(heFuTipConfigInfo != null)
									{
										//符合要弹窗的条件 准备进行弹窗
	
										//判断提示次数
										String playerCacheKey = pi.getPlayerId()+"";
										//去playerinfocache中拿信息 如果没有则new 一个
										HeFuPlayerInfo heFuPlayerInfo =  (HeFuPlayerInfo) hefuPlayerInfoCache.get(playerCacheKey);
	
										if(heFuPlayerInfo == null)
										{
											heFuPlayerInfo = new HeFuPlayerInfo();
											heFuPlayerInfo.playerId = pi.getPlayerId();
											heFuPlayerInfo.oldPlayerName = pi.getPlayerName();
											heFuPlayerInfo.nowPlayerName = pi.getPlayerName();
											heFuPlayerInfo.oldServerRealName = heFuTipConfigInfo.oldServerRealName;
											heFuPlayerInfo.username = pi.getUserName();
											heFuPlayerInfo.nowServerRealName = heFuTipConfigInfo.nowServerRealName;
										}
										else
										{
	
										}
										long tipNow =  System.currentTimeMillis();
	
										if (heFuPlayerInfo.tipedNum < heFuTipConfigInfo.tipNum && heFuTipConfigInfo.beginTime <= tipNow && tipNow <= heFuTipConfigInfo.endTime )
										{
											//弹出提示
											//tipedNum+1
											//重新放回map中
											heFuPlayerInfo.tipedNum++;
											hefuPlayerInfoCache.put(playerCacheKey, heFuPlayerInfo);
	
											String descriptionInUUB = heFuTipConfigInfo.tipContent;
											MenuWindowManager.sendSimpleMsg(conn, "合服提示", descriptionInUUB);
											logger.info("[发送合服提示消息给用户] [成功] [ok] ["+heFuPlayerInfo.username+"] ["+heFuPlayerInfo.playerId+"] ["+heFuPlayerInfo.oldPlayerName+"] ["+heFuPlayerInfo.nowPlayerName+"] ["+heFuPlayerInfo.oldServerRealName+"] ["+heFuPlayerInfo.nowServerRealName+"] ["+heFuPlayerInfo.tipedNum+"] ["+heFuTipConfigInfo.tipNum+"] ["+heFuTipConfigInfo.beginTime+"] ["+heFuTipConfigInfo.endTime+"] ["+heFuTipConfigInfo.oldServerRealName+"] ["+heFuTipConfigInfo.nowServerRealName+"]");
	
											/*if(!heFuPlayerInfo.oldServerRealName.equals(heFuPlayerInfo.nowServerRealName))
											{
												//将数据库中的信息
												MieshiPlayerInfoManager.getInstance().updatePlayerInfo4Hefu(username,heFuPlayerInfo.oldServerRealName,heFuPlayerInfo.nowServerRealName,heFuPlayerInfo.playerId,heFuPlayerInfo.nowPlayerName);
												logger.info("[发送合服提示消息给用户] [更新玩家在网关的存储信息] [ok] ["+heFuPlayerInfo.username+"] ["+heFuPlayerInfo.playerId+"] ["+heFuPlayerInfo.oldPlayerName+"] ["+heFuPlayerInfo.nowPlayerName+"] ["+heFuPlayerInfo.oldServerRealName+"] ["+heFuPlayerInfo.nowServerRealName+"] ["+heFuPlayerInfo.tipedNum+"] ["+heFuTipConfigInfo.tipNum+"] ["+heFuTipConfigInfo.beginTime+"] ["+heFuTipConfigInfo.endTime+"] ["+heFuTipConfigInfo.oldServerRealName+"] ["+heFuTipConfigInfo.nowServerRealName+"] [cost:"+(System.currentTimeMillis()-now)+"ms]");
											}*/
											break;
	
										}
										else
										{
											//超出时间则进行更新
	
											logger.info("[发送合服提示消息给用户] [已不满足提示条件] [ok] ["+heFuPlayerInfo.username+"] ["+heFuPlayerInfo.playerId+"] ["+heFuPlayerInfo.oldPlayerName+"] ["+heFuPlayerInfo.nowPlayerName+"] ["+heFuPlayerInfo.oldServerRealName+"] ["+heFuPlayerInfo.nowServerRealName+"] ["+heFuPlayerInfo.tipedNum+"] ["+heFuTipConfigInfo.tipNum+"] ["+heFuTipConfigInfo.beginTime+"] ["+heFuTipConfigInfo.endTime+"] ["+heFuTipConfigInfo.oldServerRealName+"] ["+heFuTipConfigInfo.nowServerRealName+"]");
										}
									}
	
								}
	
							}
	
						}
						catch(Exception e)
						{
							String mess = "";
							mess = "[发送合服提示消息给用户] [失败] [出现异常] ["+pi.userName+"] ["+pi.playerId+"] ["+pi.playerName+"] ["+pi.serverRealName+"]";
							logger.error(mess,e);
						}
					}

				}
			}
			String lastCategory = "";
			ServerInfoForClient lastLoginClient = null;
			long lastLoginTime = 0;
			for (int i = 0; i < al.size(); i++) {
				ServerInfoForClient c = al.get(i);
				if (c.isHasPlayer() && c.lastLoginTime > lastLoginTime) {
					lastLoginTime = c.lastLoginTime;
					lastLoginClient = c;
				}
			}

			if (lastLoginClient != null) {
				lastLoginClient.setDescription(lastLoginClient.getDescription() + Translate.最近登陆);
			}

			if (lastLoginClient == null && al.size() > 0) {

				ArrayList<ServerInfoForClient> tuijianList = new ArrayList<ServerInfoForClient>();

				for (int i = 0; i < al.size(); i++) {
					ServerInfoForClient c = al.get(i);
					if(sm.tuijianServerInfo != null){
						for(MieshiServerInfo si : sm.tuijianServerInfo){
							if(c.getName().equals(si.getName())){
								if(tuijianList.contains(c) == false){
									tuijianList.add(c);
								}
							}
						}
					}
				}
				if(tuijianList.size() > 0){
					int k = (int)(Math.random() * tuijianList.size());
					if(k >= tuijianList.size()) k = tuijianList.size()-1;
					lastLoginClient = tuijianList.get(k);
				}else{
					lastLoginClient = al.get(0);
				}
			}

			if (lastLoginClient != null) {
				lastCategory = lastLoginClient.getCategory() + "/" + lastLoginClient.getName();
			} else if (sm.getServerCategories() != null && sm.getServerCategories().length > 0) {
				lastCategory = sm.getServerCategories()[0] + "/";
				if (sm.getServerInfoListByCategory(sm.getServerCategories()[0]) != null
						&& sm.getServerInfoListByCategory(sm.getServerCategories()[0]).length > 0) {
					lastCategory = lastCategory + sm.getServerInfoListByCategory(sm.getServerCategories()[0])[0];
				} else {
					lastCategory = lastCategory + "a";
				}
			} else {
				lastCategory = "a/a";
			}

			//调整最后登录的服务器，如果最后登录的服务器在列表框外，那么将最后登录的服务器放入到列表框内
			if(lastLoginClient != null){
				int count = 0;
				int index = -1;
				for (int i = 0; i < al.size(); i++) {
					ServerInfoForClient c = al.get(i);
					if(c == lastLoginClient){
						break;
					}
					if(c.getCategory().equals(lastLoginClient.getCategory())){
						count++;
						if(count == 4){
							index = i;
							break;
						}
					}
				}
				//标识最后登录的在列表框中显示不下了，显示到列表框外了
				if(count >= 4 && index != -1){
					al.remove(lastLoginClient);
					al.add(index, lastLoginClient);
				}
			}
			ArrayList<ServerInfoForClient> newServerList = new ArrayList<ServerInfoForClient>();
			if(al != null && al.size() > 0)
			{
				//排序 有角色的排在前面 并且角色等级高的在前面
				//将有角色的服务器列表进行排序 最高等级的角色放在最前面
				Collections.sort(al, new Comparator<ServerInfoForClient>() {

					@Override
					public int compare(ServerInfoForClient o1,
							ServerInfoForClient o2) {
						if(o1.isHasPlayer() && o2.isHasPlayer())
						{
							if(o1.getLevel() > o2.getLevel())
							{
								return -1; //不换位置
							}
							else if(o1.getLevel() < o2.getLevel())
							{
								return 1; //交换位置 o2在前
							}
							else
							{
								return 0;
							}
						}
						else if(o1.isHasPlayer() && !o2.isHasPlayer())
						{
							return -1;
						}
						else if(!o1.isHasPlayer() && o2.isHasPlayer())
						{
							return 1;
						}
						else
						{
							return 0;
						}
							
					}
					
					
				});
			}
			
			//判断新服
			String[] newServerName = new String[]{"","",""};
			MieshiServerInfoManager mieshiServerInfoManager = MieshiServerInfoManager.getInstance();
			MieshiServerInfo[] newMieshiServerInfos = mieshiServerInfoManager.getNewServerInfoList();
			int addsuccsum = 0;
			for(int j = newMieshiServerInfos.length - 1; j >= 0; j--)
			{
				for(int k = 0; k < al.size(); k++)
				{
					ServerInfoForClient sc = al.get(k);
					if(sc != null)
					{
						if(sc.getName().equals(newMieshiServerInfos[j].getRealname()))
						{
							
							if(addsuccsum < 3)
							{
								newServerName[addsuccsum] = sc.getName();
							}
							addsuccsum++;
						}
					}
				}
			}
			
			//推荐服
			String[] tuijianServerName = new String[]{"","",""}; 
			MieshiServerInfo[] tuijianMieshiServerInfos = mieshiServerInfoManager.getTuijianServerInfoList();
			addsuccsum = 0;
			for(int j = tuijianMieshiServerInfos.length - 1; j >= 0; j--)
			{
				for(int k = 0; k < al.size(); k++)
				{
					ServerInfoForClient sc = al.get(k);
					if(sc != null)
					{
						if(sc.getName().equals(tuijianMieshiServerInfos[j].getRealname()))
						{
							
							if(addsuccsum < 3)
							{
								tuijianServerName[addsuccsum] = sc.getName();
							}
							addsuccsum++;
						}
					}
				}
			}
			
			//苹果测试帐号服务器列表限制,只能看到测试服
			ArrayList<ServerInfoForClient> appTestUserServerList = new ArrayList<ServerInfoForClient>();
			if(al != null && appTestUserName != null && appTestServerName != null && username != null){
				if(username.equals(appTestUserName)){
					for(ServerInfoForClient info : al){
						for(String name : appTestServerName){
							if(name != null && name.trim().equals(info.getName())){
								appTestUserServerList.add(info);
							}
						}
					}
				}
			}
			
			if(appTestUserServerList.size() > 0){
				al = appTestUserServerList;
			}
			
			NEW_QUERY_SERVER_LIST_RES res = new NEW_QUERY_SERVER_LIST_RES(req.getSequnceNum(), lastCategory, categories,newServerName,tuijianServerName, al.toArray(new ServerInfoForClient[0]));
			logger.info("[" + conn.getName() + "] [获取服务器列表] [成功] [ok] [username:" + username + "] [区:" + lastCategory + "] [服:"
					+ (lastLoginClient == null ? "--" : lastLoginClient.getName()) + "] [服务器数量:" + al.size() + "] [appTestUserServerList:"+appTestUserServerList.size()+"]" + client.getLogStr());

			return res;
		}else if(message instanceof GET_PHONE_CODE_REQ){
			return NewLoginHandler.instance.handle_GET_PHONE_CODE_REQ(conn, message);
		}else if(message instanceof QUICK_REGISTER_REQ){
			return NewLoginHandler.instance.handle_QUICK_REGISTER_REQ(conn, message);
		}
		else if(message instanceof ANNOUNCEMENT_REQ)
		{
			ANNOUNCEMENT_REQ req = (ANNOUNCEMENT_REQ) message;
			int mestype = req.getAnnouncetype();
			MessageEntityManager messageEntityManager = MessageEntityManager.getInstance();
			List<MessageEntity> lst = null;
			List<MessageEntity> removeList = new ArrayList<MessageEntity>();
			String wheresql = "mesType = ? and starttime <= ? and endtime >= ?";
			Object[] values = null;
			String[] contents =  new String[0];
			String username = req.getInfos()[0];
			
			try {
				long nowTime = System.currentTimeMillis();
				values = new Object[]{mestype,nowTime,nowTime};
				if(values != null)
				{
					lst = messageEntityManager.queryForWhere(wheresql, values, "id desc", 1, 10000);
					MieshiServerInfoManager sm = MieshiServerInfoManager.getInstance();
					
					if(lst != null && lst.size() > 0)
					{
						Iterator<MessageEntity> it = lst.iterator();
						while(it.hasNext())
						{
							MessageEntity messageEntity = it.next();
							if (messageEntity.getServerType() == MieshiServerInfo.SERVERTYPE_内部测试服务器 
									&& sm.isTestUser(username) == false) {
//								it.remove();
								removeList.add(messageEntity);
							}
	
							if(sm.isTestUser(username) == false && 
									messageEntity.getServerType() == MieshiServerInfo.SERVERTYPE_苹果正式服务器 
									&& req.getChannel().equalsIgnoreCase("APPSTORE_XUNXIAN") == false) {
//								it.remove();
								removeList.add(messageEntity);
							}
	
							if(messageEntity.getServerType() != MieshiServerInfo.SERVERTYPE_苹果正式服务器 
									&& req.getChannel().equalsIgnoreCase("APPSTORE_XUNXIAN") 
									&& sm.isTestUser(username) == false) {
//								it.remove();
								removeList.add(messageEntity);
							}
//	
//							if(sm.isTestUser(username) == false && 
//									messageEntity.getServerType() == MieshiServerInfo.SERVERTYPE_苹果国际服 
//									&& req.getChannel().equalsIgnoreCase("APPSTOREGUOJI_MIESHI") == false) {
////								it.remove();
//								removeList.add(messageEntity);
//							}
//	
//							if(messageEntity.getServerType() != MieshiServerInfo.SERVERTYPE_苹果国际服 
//									&& req.getChannel().equalsIgnoreCase("APPSTOREGUOJI_MIESHI")
//									&& sm.isTestUser(username) == false) {
////								it.remove();
//								removeList.add(messageEntity);
//							}
//	
//							if(sm.isTestUser(username) == false && 
//									messageEntity.getServerType() == MieshiServerInfo.SERVERTYPE_苹果台湾服 
//									&& req.getChannel().equalsIgnoreCase("APPSTORETAIWAN_MIESHI") == false) {
////								it.remove();
//								removeList.add(messageEntity);
//							}
//	
//							if(messageEntity.getServerType() != MieshiServerInfo.SERVERTYPE_苹果台湾服 
//									&& req.getChannel().equalsIgnoreCase("APPSTORETAIWAN_MIESHI")) {
////								it.remove();
//								removeList.add(messageEntity);
//							}
//							{
//								//移动专服,
//								if(sm.isTestUser(username) == false && 
//										messageEntity.getServerType() == MieshiServerInfo.SERVERTYPE_移动专服
//										&& (req.getChannel().toLowerCase().matches("139sdk\\d*_mieshi")  == false &&  req.getChannel().toLowerCase().matches("adhome\\d*_mieshi")  == false && req.getChannel().toLowerCase().matches("yidongmm\\d*_mieshi")  == false  ) ) {
////									it.remove();
//									removeList.add(messageEntity);
//								}
//	
//								if(sm.isTestUser(username) == false && 
//										messageEntity.getServerType() != MieshiServerInfo.SERVERTYPE_移动专服
//										&& (req.getChannel().toLowerCase().matches("139sdk\\d*_mieshi") || req.getChannel().toLowerCase().matches("adhome\\d*_mieshi") || req.getChannel().toLowerCase().matches("yidongmm\\d*_mieshi"))) {
////									it.remove();
//									removeList.add(messageEntity);
//								}
//							}
	
//							if(sm.isTestUser(username) == false && messageEntity.getServerType() == MieshiServerInfo.SERVERTYPE_文化部测试服务器 && req.getChannel().equalsIgnoreCase("wenhuabu_MIESHI") == false){
////								it.remove();
//								removeList.add(messageEntity);
//							}
//	
//							if(messageEntity.getServerType() != MieshiServerInfo.SERVERTYPE_文化部测试服务器 && req.getChannel().equalsIgnoreCase("wenhuabu_MIESHI")){
////								it.remove();
//								removeList.add(messageEntity);
//							}
	
							//
//							if(messageEntity.getServerType() == MieshiServerInfo.SERVERTYPE_第三方市场服务器){
//								String channel = req.getChannel();
//								boolean isDCN = false;
//								boolean isUC = false;
//								if (channel.toLowerCase().contains("uc_mieshi")) {
//									isUC =  true;
//								}else if (channel.toLowerCase().contains("ucsdk_mieshi")) {
//									isUC =  true;
//								} 
//								else if (channel.toLowerCase().matches("uc\\d+_mieshi")) {
//									isUC =  true;
//								}
//								else if (channel.toLowerCase().matches("ucnewsdk_mieshi")) {
//									isUC =  true;
//								} 
//								else if (channel.toLowerCase().matches("ucnewsdk\\d+_mieshi")) {
//									isUC =  true;
//								} 
//	
//								else if (channel.toLowerCase().contains("dcn_mieshi")) {
//									isDCN = true;
//								} else if (channel.toLowerCase().matches("dcn\\d+_mieshi")) {
//									isDCN = true;
//								}
//	
//								if(sm.isTestUser(username) == false  && (isDCN || isUC)){
////									it.remove();
//									removeList.add(messageEntity);
//								}
//							}
	
//							if(messageEntity.getServerType() == MieshiServerInfo.SERVERTYPE_91看不见的服务器){
//								String channel = req.getChannel();
//								boolean is91 = false;
//								if (channel.toLowerCase().contains("91zhushou_mieshi")) {
//									is91 =  true;
//								} else if (channel.toLowerCase().matches("91zhushou\\d+_mieshi")) {
//									is91 =  true;
//								} 
//								else if(channel.toLowerCase().matches("new91sdk\\d_mieshi")){
//									is91 =  true;
//								} else if(channel.toLowerCase().contains("new91sdk_mieshi")){
//									is91 =  true;
//								}
//								if(is91){
////									it.remove();
//									removeList.add(messageEntity);
//								}
//							}
	
//							if(messageEntity.getServerType() == MieshiServerInfo.SERVERTYPE_UC专用服务器){
//								String channel = req.getChannel();
//								boolean isUC = false;
//								if (channel.toLowerCase().contains("uc_mieshi")) {
//									isUC =  true;
//								}else if (channel.toLowerCase().contains("ucsdk_mieshi")) {
//									isUC =  true;
//								} 
//								else if (channel.toLowerCase().matches("uc\\d+_mieshi")) {
//									isUC =  true;
//								}
//								else if (channel.toLowerCase().matches("ucnewsdk_mieshi")) {
//									isUC =  true;
//								} 
//								else if (channel.toLowerCase().matches("ucnewsdk\\d+_mieshi")) {
//									isUC =  true;
//								} 
//	
//								if(sm.isTestUser(username) == false && isUC == false){
////									it.remove();
//									removeList.add(messageEntity);
//								}
//							}
	
//							if(messageEntity.getServerType() == MieshiServerInfo.SERVERTYPE_91专用服务器){
//								String channel = req.getChannel();
//								boolean is91 = false;
//								if (channel.toLowerCase().contains("91zhushou_mieshi")) {
//									is91 =  true;
//								} else if (channel.toLowerCase().matches("91zhushou\\d+_mieshi")) {
//									is91 =  true;
//								} 
//								else if(channel.toLowerCase().matches("new91sdk\\d_mieshi")){
//									is91 =  true;
//								} else if(channel.toLowerCase().contains("new91sdk_mieshi")){
//									is91 =  true;
//								}
//								if(sm.isTestUser(username) == false && is91 == false){
////									it.remove();
//									removeList.add(messageEntity);
//								}
//							}
	
//							if(messageEntity.getServerType() == MieshiServerInfo.SERVERTYPE_当乐与市场类渠道服务器){
//								String channel = req.getChannel();
//								boolean is91 = false;
//								if (channel.toLowerCase().contains("91zhushou_mieshi")) {
//									is91 =  true;
//								} else if (channel.toLowerCase().matches("91zhushou\\d+_mieshi")) {
//									is91 =  true;
//								} 
//								else if(channel.toLowerCase().matches("new91sdk\\d_mieshi")){
//									is91 =  true;
//								} else if(channel.toLowerCase().contains("new91sdk_mieshi")){
//									is91 =  true;
//								}
//								boolean isUC = false;
//								if (channel.toLowerCase().contains("uc_mieshi")) {
//									isUC =  true;
//								}else if (channel.toLowerCase().contains("ucsdk_mieshi")) {
//									isUC =  true;
//								} 
//								else if (channel.toLowerCase().matches("uc\\d+_mieshi")) {
//									isUC =  true;
//								}
//								else if (channel.toLowerCase().matches("ucnewsdk_mieshi")) {
//									isUC =  true;
//								} 
//								else if (channel.toLowerCase().matches("ucnewsdk\\d+_mieshi")) {
//									isUC =  true;
//								} 
//	
//								if(sm.isTestUser(username) == false && (is91 == true || isUC == true)){
////									it.remove();
//									removeList.add(messageEntity);
//								}
//							}
	
//							if(messageEntity.getServerType() == MieshiServerInfo.SERVERTYPE_91与UC共用服务器){
//								String channel = req.getChannel();
//								boolean is91 = false;
//								if (channel.toLowerCase().contains("91zhushou_mieshi")) {
//									is91 =  true;
//								} else if (channel.toLowerCase().matches("91zhushou\\d+_mieshi")) {
//									is91 =  true;
//								} 
//								else if(channel.toLowerCase().matches("new91sdk\\d_mieshi")){
//									is91 =  true;
//								} else if(channel.toLowerCase().contains("new91sdk_mieshi")){
//									is91 =  true;
//								}
//								boolean isUC = false;
//								if (channel.toLowerCase().contains("uc_mieshi")) {
//									isUC =  true;
//								}else if (channel.toLowerCase().contains("ucsdk_mieshi")) {
//									isUC =  true;
//								} 
//								else if (channel.toLowerCase().matches("uc\\d+_mieshi")) {
//									isUC =  true;
//								}
//								else if (channel.toLowerCase().matches("ucnewsdk_mieshi")) {
//									isUC =  true;
//								} 
//								else if (channel.toLowerCase().matches("ucnewsdk\\d+_mieshi")) {
//									isUC =  true;
//								} 
//	
//								if(sm.isTestUser(username) == false && (!is91 == true && !isUC == true)){
////									it.remove();
//									removeList.add(messageEntity);
//								}
//							}
						}

						if(lst != null && removeList != null && removeList.size() > 0){
							lst.removeAll(removeList);
						}
						
						MessageContent messageContent = ((MessageEntity)lst.get(0)).getMessageContent();
						if(messageContent != null)
						{
							if(messageContent.getContent() != null)
							{
								contents = new String[1];
								contents[0] = messageContent.getContent();
								ANNOUNCEMENT_RES res = new ANNOUNCEMENT_RES(req.getSequnceNum(),req.getAnnouncetype(), contents);
								logger.info("[" + conn.getName() + "] [获取公告提示] [成功] [ok] [username:" + username + "] [lst:"+lst!=null?lst.size():"null"+"] [removeList:"+removeList.size()+"] " + ((MessageEntity)lst.get(0)).getLogString());

								return res;
							}
						}
					}
				}
				
			} catch (Exception e) {
				logger.error("[" + conn.getName() + "] [获取公告提示] [失败] [出现未知异常] [username:" + username + "] [channel:"+req.getChannel()+"]",e);
				ANNOUNCEMENT_RES res = new ANNOUNCEMENT_RES(req.getSequnceNum(),req.getAnnouncetype(), contents);
				return res;
			}	
			
			logger.error("[" + conn.getName() + "] [获取公告提示] [失败] [没有匹配公告或者提示] [username:" + username + "] [channel:"+req.getChannel()+"]");
			ANNOUNCEMENT_RES res = new ANNOUNCEMENT_RES(req.getSequnceNum(), req.getAnnouncetype(),contents);
			return res;
		}


		logger.warn("[receive-request] [success] [return-null] [" + message.getTypeDescription() + "] [len:" + message.getLength() + "] [" + conn.getName()
				+ "] [" + conn.getIdentity() + "] [cost:" + (System.currentTimeMillis() - startTime) + "]");
		return null;
	}

	/**
	 * @param conn
	 * @param client
	 * @param req
	 */
	public PASSPORT_REGISTER_RES checkRegisterLimit(Connection conn, GatewayClient client,
			PASSPORT_REGISTER_REQ req) {
		if(enableCheckRegisterNumOneDay)
		{
			try
			{
				boolean canReg = checkClientRegister(req.getClientId(),req.getUsername());

				if(!canReg)
				{
					String returnMesage = "今日注册数已经达到限制";

					if(req.getChannel().toLowerCase().contains("kunlun"))
					{
						returnMesage = "今日註冊數已經達到限制";
					}

					if(req.getChannel().toLowerCase().contains("korea"))
					{
						returnMesage = Translate.注册数限制;
					}


					logger.warn("[" + conn.getName() + "] ["+Thread.currentThread().getName()+"] [用户注册] [失败] [今日注册数已经达到限制] [username:" + req.getUsername() + "] [passwd:" + req.getPasswd()
							+ "] [passportId:--] [一天注册限制数:"+MAX_ONE_CLIENT_REGISTER_NUMS_ONEDAY+"] " + client.getLogStr());
					PASSPORT_REGISTER_RES res = new PASSPORT_REGISTER_RES(req.getSequnceNum(), 2, returnMesage, req.getUsername(), "", "", "", (byte) 0);
					return res;
				}
			}
			catch(Exception e)
			{
				logger.error("[" + conn.getName() + "] ["+Thread.currentThread().getName()+"] [用户注册] [出现异常] [限制注册未启用] [username:" + req.getUsername() + "] [passwd:" + req.getPasswd()
						+ "] [passportId:--] [一天注册限制数:"+MAX_ONE_CLIENT_REGISTER_NUMS_ONEDAY+"] " + client.getLogStr(),e);
				return null;
			}
		}

		return null;
	}

	public PASSPORT_REGISTER_NEW_RES checkRegisterLimit(Connection conn, GatewayClient client,
			PASSPORT_REGISTER_NEW_REQ req) {
		if(enableCheckRegisterNumOneDay)
		{
			try
			{
				boolean canReg = checkClientRegister(req.getClientId(),req.getUsername());

				if(!canReg)
				{
					String returnMesage = "今日注册数已经达到限制";

					if(req.getChannel().toLowerCase().contains("kunlun"))
					{
						returnMesage = "今日註冊數已經達到限制";
					}

					if(req.getChannel().toLowerCase().contains("korea"))
					{
						returnMesage = Translate.注册数限制;
					}


					logger.warn("[" + conn.getName() + "] ["+Thread.currentThread().getName()+"] [用户注册] [失败] [今日注册数已经达到限制] [username:" + req.getUsername() + "] [passwd:" + req.getPasswd()
							+ "] [passportId:--] [一天注册限制数:"+MAX_ONE_CLIENT_REGISTER_NUMS_ONEDAY+"] " + client.getLogStr());
					PASSPORT_REGISTER_NEW_RES res = new PASSPORT_REGISTER_NEW_RES(req.getSequnceNum(), 2, returnMesage, req.getUsername(), "", "", "", (byte) 0);
					return res;
				}
			}
			catch(Exception e)
			{
				logger.error("[" + conn.getName() + "] ["+Thread.currentThread().getName()+"] [用户注册] [出现异常] [限制注册未启用] [username:" + req.getUsername() + "] [passwd:" + req.getPasswd()
						+ "] [passportId:--] [一天注册限制数:"+MAX_ONE_CLIENT_REGISTER_NUMS_ONEDAY+"] " + client.getLogStr(),e);

			}

			return null;
		}

		return null;
	}


	public PASSPORT_REGISTER_PRO_RES checkRegisterLimit(Connection conn, GatewayClient client,
			PASSPORT_REGISTER_PRO_REQ req) {
		if(enableCheckRegisterNumOneDay)
		{
			try
			{
				boolean canReg = checkClientRegister(req.getClientId(),req.getUsername());

				if(!canReg)
				{
					String returnMesage = "今日注册数已经达到限制";

					if(req.getChannel().toLowerCase().contains("kunlun"))
					{
						returnMesage = "今日註冊數已經達到限制";
					}

					if(req.getChannel().toLowerCase().contains("korea"))
					{
						returnMesage = Translate.注册数限制;
					}


					logger.warn("[" + conn.getName() + "] ["+Thread.currentThread().getName()+"] [用户注册] [失败] [今日注册数已经达到限制] [username:" + req.getUsername() + "] [passwd:" + req.getPasswd()
							+ "] [passportId:--] [一天注册限制数:"+MAX_ONE_CLIENT_REGISTER_NUMS_ONEDAY+"] " + client.getLogStr());
					PASSPORT_REGISTER_PRO_RES res = new PASSPORT_REGISTER_PRO_RES(req.getSequnceNum(), 2, returnMesage, req.getUsername(), "", "", "", (byte) 0,new String[0]);
					return res;
				}
			}
			catch(Exception e)
			{
				logger.error("[" + conn.getName() + "] ["+Thread.currentThread().getName()+"] [用户注册] [出现异常] [限制注册未启用] [username:" + req.getUsername() + "] [passwd:" + req.getPasswd()
						+ "] [passportId:--] [一天注册限制数:"+MAX_ONE_CLIENT_REGISTER_NUMS_ONEDAY+"] " + client.getLogStr(),e);

			}

			return null;
		}

		return null;
	}



	/**
	 * 此方法用来判断是否去更新用户的lastLoginIp为设备标识
	 */
	public boolean isModifyId(String channel)
	{
		if(channel == null) return false;

		if(channel.equalsIgnoreCase("APPSTORE_XUNXIAN")) return true;
		if(channel.equalsIgnoreCase("KUNLUNAPPSTORE_XUNXIAN")) return true;
		if(channel.contains("AMAZON")) return true;

		return false;
	}



	/**
	 * 此方法在渠道用户到渠道登录成功后，处理， 创建我们库里的用户
	 * 
	 * @param req
	 * @param client
	 * @param conn
	 * @return
	 */
	private USER_LOGIN_RES handleChannelLoginSuccessForThirdPart(String userIdInChannel, USER_LOGIN_REQ req, GatewayClient client, Connection conn) {
		long now = System.currentTimeMillis();
		String passwd = req.getPassword();
		String channel = req.getChannel();

		String userType = req.getUsertype();

		Passport passportForUid = null;
		Passport passportForName = null;

		try {
			//此方法用的sql是   username='?' or nickname='?'
			passportForUid = bossClientService.getPassportByUserName(userIdInChannel);
		} catch (Exception e) {
		}

		if (req.getUsername().equals(userIdInChannel)) {
			passportForName = null;
		} else {
			try {
				//passportForName = bossClientService.getPassportByUserName(arg0)
			} catch (Exception e) {
			}
		}

		Passport passport = null;
		if (passportForUid == null && passportForName == null) {
			try {

				passport = bossClientService.register(userIdInChannel, passwd, req.getClientId(), req.getUsername(), req.getUsertype(), channel, "", req
						.getPlatform(), client.getPhoneType(), client.getNetworkMode());

				String authCode = encrypt(req.getUsername(), passwd);
				USER_LOGIN_RES res = new USER_LOGIN_RES(req.getSequnceNum(), (byte) 0, "", authCode, "");
				conn.setAttachment(passport);
				client.setPassport(req.getUsername());

				StatManager stat = StatManager.getInstance();
				if (stat != null) {
					stat.notifyRegister(req.getClientId(), req.getUsername(), true, req.getChannel());
					stat.notifyLogin(req.getClientId(), req.getUsername(), true);
				}

				UserRegistFlow userRegistFlow = new UserRegistFlow();
				userRegistFlow.setDidian("未知"); // 地点
				userRegistFlow.setEmei("43co-sdf0-sdf-we454");
				userRegistFlow.setGame("灭世OL");
				userRegistFlow.setHaoma("11111111111"); // 手机号码
				userRegistFlow.setJixing(client.getPlatform()); // 手机机型
				userRegistFlow.setUserName(userIdInChannel);// 用户名
				userRegistFlow.setNations(""); // 国家
				userRegistFlow.setQudao(client.getChannel()); // 渠道
				userRegistFlow.setRegisttime(System.currentTimeMillis()); // 用户注册时间
				// /////////以下字段是为快速注册的用户使用，如果不是快速注册，可以填默认值，或者（“”）空字符串
				// start////////////
				userRegistFlow.setPlayerName("--"); // //角色名
				userRegistFlow.setCreatPlayerTime(System.currentTimeMillis()); // //创建角色时间
				userRegistFlow.setFenQu("--"); // //分区
				// /////////以下字段是为快速注册的用户使用，如果不是快速注册，可以填默认值，或者（“”）空字符串
				// start////////////
				StatClientService statClientService = StatClientService.getInstance();
				if (statClientService != null)
					statClientService.sendUserRegistFlow("灭世OL", userRegistFlow);

				logger.info("[" + conn.getName() + "] [用户注册并登录] [userType:" + userType + "] [成功] [ok] [username:" + req.getUsername() + "] [passwd:" + passwd
						+ "] [authCode:" + authCode + "] " + client.getLogStr() + " [cost:"+(System.currentTimeMillis() - now)+"ms]");
				return res;

			} catch (UsernameAlreadyExistsException ex) {

				StatManager stat = StatManager.getInstance();
				if (stat != null) {
					stat.notifyRegister(req.getClientId(), req.getUsername(), false, req.getChannel());
				}

				String reason = Translate.登录失败用户名已存在;
				USER_LOGIN_RES res = new USER_LOGIN_RES(req.getSequnceNum(), (byte) 2, reason, "", "登录失败");
				client.setPassport(req.getUsername());
				logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [失败] [" + reason + "] [username:" + req.getUsername() + "] [passwd:"
						+ passwd + "] [authCode:--] " + client.getLogStr());
				return res;
			} catch (Exception ex) {

				StatManager stat = StatManager.getInstance();
				if (stat != null) {
					stat.notifyRegister(req.getClientId(), req.getUsername(), false, req.getChannel());
				}

				String reason = Translate.登录失败注册时出现未知错误;
				USER_LOGIN_RES res = new USER_LOGIN_RES(req.getSequnceNum(), (byte) 2, reason, "", "登录失败");
				client.setPassport(req.getUsername());
				logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [失败] [" + reason + "] [username:" + req.getUsername() + "] [passwd:"
						+ passwd + "] [authCode:--] " + client.getLogStr(), ex);
				return res;
			}
		}

		// 已经存在的用户

		// 更新密码和nickName
		if (passportForUid != null) {
			//腾讯全部是专服，不需要做用户重名的判断
			if (channel.equals(passportForUid.getRegisterChannel()) || userType.equals("QQUSER")  || userType.contains("UC") || userType.contains("DUOKU") || userType.contains("MEIZUUSER")) {


				if (StringUtil.hash(passwd).equals(passportForUid.getPassWd()) == false) {
					// 我们库里的密码和
					// passport.setPasswd(passwd);

					if(channel.toUpperCase().contains("DCN") || channel.toUpperCase().contains("UC")){
						if( req.getClientId().equals(passportForUid.getLastLoginClientId()) == false){
							String reason = "登录失败，由于您通过非客户端或使用其他来源客户端更改过密码，请使用最后一次更改前的密码登陆后再使用新密码登录即可正常进行游戏。如有问题请联系我们：4009889988！";
							USER_LOGIN_RES res = new USER_LOGIN_RES(req.getSequnceNum(), (byte) 2, reason, "", "登录失败");
							client.setPassport(req.getUsername());

							logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [失败] [" + reason + ",other_clientid=" + passportForUid.getLastLoginClientId()
									+ "] [username:" + req.getUsername() + "] [passwd:" + passwd + "] [authCode:--] " + client.getLogStr());
							return res;
						}
					}

					passportForUid.setPassWd(StringUtil.hash(passwd));
					try {
						boolean isOk = bossClientService.update(passportForUid);
						if (isOk) {
							logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [自动修改密码成功] [渠道用户可能修改了密码] [username:" + req.getUsername()
									+ "] [passwd:" + passwd + "] [authCode:--] " + client.getLogStr());
						} else {
							logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [自动修改密码失败] [渠道用户可能修改了密码] [username:" + req.getUsername()
									+ "] [passwd:" + passwd + "] [authCode:--] " + client.getLogStr());
						}
					} catch (Exception e) {
						logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [自动修改密码失败] [渠道用户可能修改了密码] [username:" + req.getUsername()
								+ "] [passwd:" + passwd + "] [authCode:--] " + client.getLogStr(), e);
					}
				}

				if (passportForUid.getNickName() == null
						|| (!req.getUsername().equals(userIdInChannel) && !passportForUid.getNickName().equals(req.getUsername()))) {


					Passport passport4NickName = null;
					try {
						passport4NickName = bossClientService.getPassportByUserName(req.getUsername());
					} catch (Exception e1) {
					}

					if(passport4NickName == null)
					{
						passportForUid.setNickName(req.getUsername());
						try {
							boolean isOk = bossClientService.update(passportForUid);
							if (isOk) {
								logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [自动修改nickName成功] [username:" + req.getUsername()
										+ "] [uid:" + userIdInChannel + "] [nickname:" + req.getUsername() + "] [passwd:" + passwd + "] [authCode:--] "
										+ client.getLogStr());
							} else {
								logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [自动修改nickName失败] [username:" + req.getUsername()
										+ "] [uid:" + userIdInChannel + "] [nickname:" + req.getUsername() + "] [passwd:" + passwd + "] [authCode:--] "
										+ client.getLogStr());
							}
						} catch (Exception e) {
							logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [自动修改nickName失败] [username:" + req.getUsername() + "] [uid:"
									+ userIdInChannel + "] [nickname:" + req.getUsername() + "] [passwd:" + passwd + "] [authCode:--] " + client.getLogStr(), e);
						}
					}
				}

			} else {

				StatManager stat = StatManager.getInstance();
				if (stat != null) {
					stat.notifyLogin(req.getClientId(), req.getUsername(), false);
				}

				String reason = Translate.登录失败存在同名用户但来自不同渠道;
				USER_LOGIN_RES res = new USER_LOGIN_RES(req.getSequnceNum(), (byte) 2, reason, "", "登录失败");
				client.setPassport(req.getUsername());
				logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [失败] [" + reason + ",other_channel=" + passportForUid.getRegisterChannel()
						+ "] [username:" + req.getUsername() + "] [passwd:" + passwd + "] [authCode:--] " + client.getLogStr());
				return res;
			}
		}

		if (passportForName != null) {
			if (channel.equals(passportForName.getRegisterChannel())) {

				if (StringUtil.hash(passwd).equals(passportForName.getPassWd()) == false) {
					// 我们库里的密码和
					// passport.setPasswd(passwd);

					if(channel.toUpperCase().contains("DCN") || channel.toUpperCase().contains("UC")){
						if( req.getClientId().equals(passportForName.getLastLoginClientId()) == false){
							String reason = "登录失败，由于您通过非客户端或使用其他来源客户端更改过密码，请使用最后一次更改前的密码登陆后再使用新密码登录即可正常进行游戏。如有问题请联系我们：4009889988！";
							USER_LOGIN_RES res = new USER_LOGIN_RES(req.getSequnceNum(), (byte) 2, reason, "", "登录失败");
							client.setPassport(req.getUsername());

							logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [失败] [" + reason + ",other_clientid=" + passportForName.getLastLoginClientId()
									+ "] [username:" + req.getUsername() + "] [passwd:" + passwd + "] [authCode:--] " + client.getLogStr());
							return res;
						}
					}

					passportForName.setPassWd(StringUtil.hash(passwd));
					try {
						boolean isOk = bossClientService.update(passportForName);
						if (isOk) {
							logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [自动修改密码成功] [渠道用户可能修改了密码] [username:" + req.getUsername()
									+ "] [passwd:" + passwd + "] [authCode:--] " + client.getLogStr());
						} else {
							logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [自动修改密码失败] [渠道用户可能修改了密码] [username:" + req.getUsername()
									+ "] [passwd:" + passwd + "] [authCode:--] " + client.getLogStr());
						}
					} catch (Exception e) {
						logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [自动修改密码失败] [渠道用户可能修改了密码] [username:" + req.getUsername()
								+ "] [passwd:" + passwd + "] [authCode:--] " + client.getLogStr(), e);
					}
				}

				if (passportForName.getNickName() == null
						|| (!req.getUsername().equals(userIdInChannel) && !passportForName.getNickName().equals(userIdInChannel))) {

					Passport passport4NickName = null;
					try {
						passport4NickName = bossClientService.getPassportByUserName(userIdInChannel);
					} catch (Exception e1) {
					}

					if(passport4NickName == null)
					{
						passportForName.setNickName(userIdInChannel);
						try {
							boolean isOk = bossClientService.update(passportForName);
							if (isOk) {
								logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [自动修改nickName成功] [username:" + req.getUsername()
										+ "] [uid:" + userIdInChannel + "] [nickname:" + userIdInChannel + "] [passwd:" + passwd + "] [authCode:--] "
										+ client.getLogStr());
							} else {
								logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [自动修改nickName失败] [username:" + req.getUsername()
										+ "] [uid:" + userIdInChannel + "] [nickname:" + userIdInChannel + "] [passwd:" + passwd + "] [authCode:--] "
										+ client.getLogStr());
							}
						} catch (Exception e) {
							logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [自动修改nickName失败] [username:" + req.getUsername() + "] [uid:"
									+ userIdInChannel + "] [nickname:" + userIdInChannel + "] [passwd:" + passwd + "] [authCode:--] " + client.getLogStr(), e);
						}
					}
				}

			} else {

				StatManager stat = StatManager.getInstance();
				if (stat != null) {
					stat.notifyLogin(req.getClientId(), req.getUsername(), false);
				}

				String reason = Translate.登录失败存在同名用户但来自不同渠道;
				USER_LOGIN_RES res = new USER_LOGIN_RES(req.getSequnceNum(), (byte) 2, reason, "", Translate.登录失败);
				client.setPassport(req.getUsername());
				// logger.info("[" + conn.getName() + "] [用户登录] [userType:" +
				// userType + "] [失败] [" + reason + ",other_channel=" +
				// passport.getChannelkey() + "] [username:" + req.getUsername()
				// + "] [passwd:" + passwd + "] [authCode:--] " +
				// client.getLogStr());
				logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [失败] [" + reason + ",other_channel=" + passport.getRegisterChannel()
						+ "] [username:" + req.getUsername() + "] [passwd:" + passwd + "] [authCode:--] " + client.getLogStr());
				return res;
			}
		}

		String authCode = encrypt(req.getUsername(), passwd);

		USER_LOGIN_RES res = new USER_LOGIN_RES(req.getSequnceNum(), (byte) 0, "", authCode, "");
		conn.setAttachment(passport);

		client.setPassport(req.getUsername());

		try {
			bossClientService.login(req.getUsername(), passwd, req.getClientId(), req.getChannel(), req.getPlatform(),
					client.getPhoneType(), client.getNetworkMode());
		} catch (Exception e) {
			logger.error("[登录更新Passport信息时发生异常] ["+req.getUsername()+"]", e);
		}

		StatManager stat = StatManager.getInstance();
		if (stat != null) {
			stat.notifyLogin(req.getClientId(), req.getUsername(), true);
		}

		logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [成功] [ok] [username:" + req.getUsername() + "] [passwd:" + passwd
				+ "] [authCode:" + authCode + "] " + client.getLogStr() + " [cost:"+(System.currentTimeMillis() - now)+"ms]");
		return res;
	}

	/**
	 * 此方法在渠道用户到渠道登录成功后，处理， 创建我们库里的用户
	 * 
	 * @param req
	 * @param client
	 * @param conn
	 * @return
	 */
	private USER_LOGIN_RES handleChannelLoginSuccessForKunlunUser(String userIdInChannel, USER_LOGIN_REQ req, GatewayClient client, Connection conn) {
		long now = System.currentTimeMillis();
		String passwd = req.getPassword();
		String channel = req.getChannel();

		String userType = req.getUsertype();

		Passport passportForUid = null;
		Passport passportForName = null;

		try {
			//此方法用的sql是   username='?' or nickname='?'
			passportForUid = bossClientService.getPassportByUserName(userIdInChannel);
		} catch (Exception e) {
		}

		if (req.getUsername().equals(userIdInChannel)) {
			passportForName = null;
		} else {
			try {
				//passportForName = bossClientService.getPassportByUserName(arg0)
			} catch (Exception e) {
			}
		}

		Passport passport = null;
		if (passportForUid == null && passportForName == null) {
			try {

				passport = bossClientService.register(userIdInChannel, passwd, req.getClientId(), req.getUsername(), req.getUsertype(), channel, "", req
						.getPlatform(), client.getPhoneType(), client.getNetworkMode());

				String authCode = encrypt(req.getUsername(), passwd);
				USER_LOGIN_RES res = new USER_LOGIN_RES(req.getSequnceNum(), (byte) 0, "", authCode, "");
				conn.setAttachment(passport);
				client.setPassport(req.getUsername());

				StatManager stat = StatManager.getInstance();
				if (stat != null) {
					stat.notifyRegister(req.getClientId(), req.getUsername(), true, req.getChannel());
					stat.notifyLogin(req.getClientId(), req.getUsername(), true);
				}

				UserRegistFlow userRegistFlow = new UserRegistFlow();
				userRegistFlow.setDidian("未知"); // 地点
				userRegistFlow.setEmei("43co-sdf0-sdf-we454");
				userRegistFlow.setGame("灭世OL");
				userRegistFlow.setHaoma("11111111111"); // 手机号码
				userRegistFlow.setJixing(client.getPlatform()); // 手机机型
				userRegistFlow.setUserName(userIdInChannel);// 用户名
				userRegistFlow.setNations(""); // 国家
				userRegistFlow.setQudao(client.getChannel()); // 渠道
				userRegistFlow.setRegisttime(System.currentTimeMillis()); // 用户注册时间
				// /////////以下字段是为快速注册的用户使用，如果不是快速注册，可以填默认值，或者（“”）空字符串
				// start////////////
				userRegistFlow.setPlayerName("--"); // //角色名
				userRegistFlow.setCreatPlayerTime(System.currentTimeMillis()); // //创建角色时间
				userRegistFlow.setFenQu("--"); // //分区
				// /////////以下字段是为快速注册的用户使用，如果不是快速注册，可以填默认值，或者（“”）空字符串
				// start////////////
				StatClientService statClientService = StatClientService.getInstance();
				if (statClientService != null)
					statClientService.sendUserRegistFlow("灭世OL", userRegistFlow);

				logger.info("[" + conn.getName() + "] [用户注册并登录] [userType:" + userType + "] [成功] [ok] [username:" + req.getUsername() + "] [passwd:" + passwd
						+ "] [authCode:" + authCode + "] " + client.getLogStr() + " [cost:"+(System.currentTimeMillis() - now)+"ms]");
				return res;

			} catch (UsernameAlreadyExistsException ex) {

				StatManager stat = StatManager.getInstance();
				if (stat != null) {
					stat.notifyRegister(req.getClientId(), req.getUsername(), false, req.getChannel());
				}

				String reason = Translate.登录失败用户名已存在;
				USER_LOGIN_RES res = new USER_LOGIN_RES(req.getSequnceNum(), (byte) 2, reason, "", "登录失败");
				client.setPassport(req.getUsername());
				logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [失败] [" + reason + "] [username:" + req.getUsername() + "] [passwd:"
						+ passwd + "] [authCode:--] " + client.getLogStr());
				return res;
			} catch (Exception ex) {

				StatManager stat = StatManager.getInstance();
				if (stat != null) {
					stat.notifyRegister(req.getClientId(), req.getUsername(), false, req.getChannel());
				}

				String reason = Translate.登录失败注册时出现未知错误;
				USER_LOGIN_RES res = new USER_LOGIN_RES(req.getSequnceNum(), (byte) 2, reason, "", "登录失败");
				client.setPassport(req.getUsername());
				logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [失败] [" + reason + "] [username:" + req.getUsername() + "] [passwd:"
						+ passwd + "] [authCode:--] " + client.getLogStr(), ex);
				return res;
			}
		}

		// 已经存在的用户

		// 更新密码和nickName
		if (passportForUid != null) {
			//腾讯全部是专服，不需要做用户重名的判断
			if (userType.equalsIgnoreCase("KUNLUNUSER")||userType.equalsIgnoreCase("MALAIUSER") || userType.equalsIgnoreCase("KOREAUSER")) {


				if (StringUtil.hash(passwd).equals(passportForUid.getPassWd()) == false) {
					// 我们库里的密码和
					// passport.setPasswd(passwd);

					passportForUid.setPassWd(StringUtil.hash(passwd));
					try {
						boolean isOk = bossClientService.update(passportForUid);
						if (isOk) {
							logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [自动修改密码成功] [渠道用户可能修改了密码] [username:" + req.getUsername()
									+ "] [passwd:" + passwd + "] [authCode:--] " + client.getLogStr());
						} else {
							logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [自动修改密码失败] [渠道用户可能修改了密码] [username:" + req.getUsername()
									+ "] [passwd:" + passwd + "] [authCode:--] " + client.getLogStr());
						}
					} catch (Exception e) {
						logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [自动修改密码失败] [渠道用户可能修改了密码] [username:" + req.getUsername()
								+ "] [passwd:" + passwd + "] [authCode:--] " + client.getLogStr(), e);
					}
				}

				if (passportForUid.getNickName() == null
						|| (!req.getUsername().equals(userIdInChannel) && !passportForUid.getNickName().equals(req.getUsername()))) {
					passportForUid.setNickName(req.getUsername());
					try {
						boolean isOk = bossClientService.update(passportForUid);
						if (isOk) {
							logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [自动修改nickName成功] [username:" + req.getUsername()
									+ "] [uid:" + userIdInChannel + "] [nickname:" + req.getUsername() + "] [passwd:" + passwd + "] [authCode:--] "
									+ client.getLogStr());
						} else {
							logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [自动修改nickName失败] [username:" + req.getUsername()
									+ "] [uid:" + userIdInChannel + "] [nickname:" + req.getUsername() + "] [passwd:" + passwd + "] [authCode:--] "
									+ client.getLogStr());
						}
					} catch (Exception e) {
						logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [自动修改nickName失败] [username:" + req.getUsername() + "] [uid:"
								+ userIdInChannel + "] [nickname:" + req.getUsername() + "] [passwd:" + passwd + "] [authCode:--] " + client.getLogStr(), e);
					}
				}

			} else {

				StatManager stat = StatManager.getInstance();
				if (stat != null) {
					stat.notifyLogin(req.getClientId(), req.getUsername(), false);
				}

				String reason = Translate.登录失败存在同名用户但来自不同渠道;
				USER_LOGIN_RES res = new USER_LOGIN_RES(req.getSequnceNum(), (byte) 2, reason, "", "登录失败");
				client.setPassport(req.getUsername());
				// logger.info("[" + conn.getName() + "] [用户登录] [userType:" +
				// userType + "] [失败] [" + reason + ",other_channel=" +
				// passport.getChannelkey() + "] [username:" + req.getUsername()
				// + "] [passwd:" + passwd + "] [authCode:--] " +
				// client.getLogStr());
				logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [失败] [" + reason + ",other_channel=" + passportForUid.getRegisterChannel()
						+ "] [username:" + req.getUsername() + "] [passwd:" + passwd + "] [authCode:--] " + client.getLogStr());
				return res;
			}
		}

		//		
		// if(passport != null)
		// {
		// if(StringUtils.isEmpty(lr.password))
		// {
		// logger.warn("[UC用户在本地登陆失败后] [调用UC登录接口] [更新passPort UC密码] [失败:返回密码为空]
		// [uc返回状态:"+lr.status+"] [uc返回消息："+lr.message+"] [Passport
		// Id:"+passport.getId()+"] [Passport
		// Name:"+passport.getUserName()+"]");
		// }
		// else
		// {
		// passport.setUcPassword(lr.password);
		// //设置ucpassword
		// boolean isOk = bossClientService.update(passport);
		// if(isOk)
		// logger.info("[UC用户在本地登陆失败后] [调用UC登录接口] [更新passPort UC密码] [成功]
		// [UC密码:"+lr.password+"] [Passport Id:"+passport.getId()+"] [Passport
		// Name:"+passport.getUserName()+"]");
		// else
		// logger.warn("[UC用户在本地登陆失败后] [调用UC登录接口] [更新passPort UC密码]
		// [失败:更新passport出错] [UC密码:"+lr.password+"] [Passport
		// Id:"+passport.getId()+"] [Passport
		// Name:"+passport.getUserName()+"]");
		//					
		// }
		// }

		String authCode = encrypt(req.getUsername(), passwd);

		USER_LOGIN_RES res = new USER_LOGIN_RES(req.getSequnceNum(), (byte) 0, "", authCode, "");
		conn.setAttachment(passport);

		client.setPassport(req.getUsername());

		try {
			bossClientService.login(req.getUsername(), passwd, req.getClientId(), req.getChannel(), req.getPlatform(),
					client.getPhoneType(), client.getNetworkMode());
		} catch (Exception e) {
			logger.error("[登录更新Passport信息时发生异常] ["+req.getUsername()+"]", e);
		}

		StatManager stat = StatManager.getInstance();
		if (stat != null) {
			stat.notifyLogin(req.getClientId(), req.getUsername(), true);
		}

		logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [成功] [ok] [username:" + req.getUsername() + "] [passwd:" + passwd
				+ "] [authCode:" + authCode + "] " + client.getLogStr() + " [cost:"+(System.currentTimeMillis() - now)+"ms]");
		return res;
	}


	public boolean handleResponseMessage(Connection conn, RequestMessage req, ResponseMessage message) throws ConnectionException, Exception {
		logger.warn("[handleResponseMessage] ["+conn.getName()+"] ["+message.getTypeDescription()+"]");
		if (message instanceof SERVER_HAND_CLIENT_1_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_1_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_2_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_2_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_3_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_3_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_4_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_4_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_5_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_5_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_6_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_6_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_7_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_7_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_8_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_8_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_9_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_9_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_10_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_10_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_11_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_11_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_12_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_12_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_13_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_13_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_14_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_14_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_15_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_15_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_16_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_16_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_17_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_17_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_18_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_18_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_19_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_19_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_20_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_20_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_21_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_21_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_22_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_22_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_23_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_23_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_24_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_24_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_25_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_25_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_26_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_26_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_27_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_27_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_28_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_28_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_29_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_29_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_30_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_30_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_31_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_31_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_32_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_32_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_33_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_33_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_34_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_34_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_35_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_35_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_36_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_36_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_37_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_37_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_38_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_38_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_39_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_39_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_40_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_40_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_41_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_41_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_42_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_42_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_43_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_43_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_44_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_44_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_45_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_45_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_46_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_46_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_47_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_47_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_48_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_48_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_49_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_49_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_50_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_50_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_51_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_51_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_52_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_52_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_53_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_53_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_54_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_54_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_55_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_55_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_56_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_56_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_57_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_57_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_58_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_58_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_59_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_59_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_60_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_60_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_61_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_61_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_62_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_62_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_63_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_63_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_64_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_64_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_65_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_65_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_66_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_66_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_67_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_67_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_68_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_68_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_69_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_69_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_70_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_70_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_71_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_71_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_72_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_72_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_73_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_73_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_74_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_74_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_75_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_75_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_76_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_76_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_77_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_77_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_78_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_78_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_79_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_79_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_80_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_80_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_81_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_81_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_82_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_82_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_83_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_83_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_84_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_84_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_85_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_85_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_86_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_86_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_87_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_87_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_88_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_88_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_89_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_89_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_90_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_90_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_91_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_91_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_92_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_92_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_93_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_93_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_94_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_94_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_95_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_95_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_96_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_96_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_97_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_97_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_98_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_98_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_99_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_99_RES(conn, req, message);
		}else if (message instanceof SERVER_HAND_CLIENT_100_RES) {
			NewLoginHandler.instance.handle_SERVER_HAND_CLIENT_100_RES(conn, req, message);
			//TODO:~~~~~~~~~~~~`
			//TODO:~~~~~~~~~~~~`
			//TODO:~~~~~~~~~~~~`  下面是新协议，下次换上面
			//TODO:~~~~~~~~~~~~`
			//TODO:~~~~~~~~~~~~`
		} else if (message instanceof TRY_GETPLAYERINFO_RES) {
			NewLoginHandler.instance.handle_TRY_GETPLAYERINFO_RES(conn, req, message);
		} else if (message instanceof WAIT_FOR_OTHER_RES) {
			NewLoginHandler.instance.handle_WAIT_FOR_OTHER_RES(conn, req, message);
		} else if (message instanceof GET_REWARD_2_PLAYER_RES) {
			NewLoginHandler.instance.handle_GET_REWARD_2_PLAYER_RES(conn, req, message);
		} else if (message instanceof REQUESTBUY_GET_ENTITY_INFO_RES) {
			NewLoginHandler.instance.handle_REQUESTBUY_GET_ENTITY_INFO_RES(conn, req, message);
		} else if (message instanceof PLAYER_SOUL_RES) {
			NewLoginHandler.instance.handle_PLAYER_SOUL_RES(conn, req, message);
		} else if (message instanceof CARD_TRYSAVING_RES) {
			NewLoginHandler.instance.handle_CARD_TRYSAVING_RES(conn, req, message);
		} else if (message instanceof GANG_WAREHOUSE_JOURNAL_RES) {
			NewLoginHandler.instance.handle_GANG_WAREHOUSE_JOURNAL_RES(conn, req, message);
		} else if (message instanceof GET_WAREHOUSE_RES) {
			NewLoginHandler.instance.handle_GET_WAREHOUSE_RES(conn, req, message);
		} else if (message instanceof QUERY__GETAUTOBACK_RES) {
			NewLoginHandler.instance.handle_QUERY__GETAUTOBACK_RES(conn, req, message);
		} else if (message instanceof GET_ZONGPAI_NAME_RES) {
			NewLoginHandler.instance.handle_GET_ZONGPAI_NAME_RES(conn, req, message);
		} else if (message instanceof TRY_LEAVE_ZONGPAI_RES) {
			NewLoginHandler.instance.handle_TRY_LEAVE_ZONGPAI_RES(conn, req, message);
		} else if (message instanceof REBEL_EVICT_NEW_RES) {
			NewLoginHandler.instance.handle_REBEL_EVICT_NEW_RES(conn, req, message);
		} else if (message instanceof GET_PLAYERTITLE_RES) {
			NewLoginHandler.instance.handle_GET_PLAYERTITLE_RES(conn, req, message);
		} else if (message instanceof MARRIAGE_TRY_BEQSTART_RES) {
			NewLoginHandler.instance.handle_MARRIAGE_TRY_BEQSTART_RES(conn, req, message);
		} else if (message instanceof MARRIAGE_GUESTNEW_OVER_RES) {
			NewLoginHandler.instance.handle_MARRIAGE_GUESTNEW_OVER_RES(conn, req, message);
		} else if (message instanceof MARRIAGE_HUNLI_RES) {
			NewLoginHandler.instance.handle_MARRIAGE_HUNLI_RES(conn, req, message);
		} else if (message instanceof COUNTRY_COMMENDCANCEL_RES) {
			NewLoginHandler.instance.handle_COUNTRY_COMMENDCANCEL_RES(conn, req, message);
		} else if (message instanceof COUNTRY_NEWQIUJIN_RES) {
			NewLoginHandler.instance.handle_COUNTRY_NEWQIUJIN_RES(conn, req, message);
		} else if (message instanceof GET_COUNTRYJINKU_RES) {
			NewLoginHandler.instance .handle_GET_COUNTRYJINKU_RES(conn, req, message);
		} else if (message instanceof CAVE_NEWBUILDING_RES) {
			NewLoginHandler.instance .handle_CAVE_NEWBUILDING_RES(conn, req, message);
		} else if (message instanceof CAVE_FIELD_RES) {
			NewLoginHandler.instance.handle_CAVE_FIELD_RES(conn, req, message);
		} else if (message instanceof CAVE_NEW_PET_RES) {
			NewLoginHandler.instance.handle_CAVE_NEW_PET_RES(conn, req, message);
		} else if (message instanceof CAVE_TRY_SCHEDULE_RES) {
			NewLoginHandler.instance.handle_CAVE_TRY_SCHEDULE_RES(conn, req, message);
		} else if (message instanceof CAVE_SEND_COUNTYLIST_RES) {
			NewLoginHandler.instance.handle_CAVE_SEND_COUNTYLIST_RES(conn, req, message);
		} else if (message instanceof PLAYER_NEW_LEVELUP_RES) {
			NewLoginHandler.instance.handle_PLAYER_NEW_LEVELUP_RES(conn, req, message);
		} else if (message instanceof CLEAN_FRIEND_LIST_RES) {
			NewLoginHandler.instance.handle_CLEAN_FRIEND_LIST_RES(conn, req, message);
		} else if (message instanceof DO_ACTIVITY_NEW_RES) {
			NewLoginHandler.instance.handle_DO_ACTIVITY_NEW_RES(conn, req, message);
		} else if (message instanceof REF_TESK_LIST_RES) {
			NewLoginHandler.instance.handle_REF_TESK_LIST_RES(conn, req, message);
		} else if (message instanceof DELTE_PET_INFO_RES) {
			NewLoginHandler.instance.handle_DELTE_PET_INFO_RES(conn, req, message);
		} else if (message instanceof MARRIAGE_DOACTIVITY_RES) {
			NewLoginHandler.instance.handle_MARRIAGE_DOACTIVITY_RES(conn, req, message);
		} else if (message instanceof LA_FRIEND_RES) {
			NewLoginHandler.instance.handle_LA_FRIEND_RES(conn, req, message);
		} else if (message instanceof TRY_NEWFRIEND_LIST_RES) {
			NewLoginHandler.instance.handle_TRY_NEWFRIEND_LIST_RES(conn, req, message);
		} else if (message instanceof QINGQIU_PET_INFO_RES) {
			NewLoginHandler.instance.handle_QINGQIU_PET_INFO_RES(conn, req, message);
		} else if (message instanceof REMOVE_ACTIVITY_NEW_RES) {
			NewLoginHandler.instance.handle_REMOVE_ACTIVITY_NEW_RES(conn, req, message);
		} else if (message instanceof TRY_LEAVE_GAME_RES) {
			NewLoginHandler.instance.handle_TRY_LEAVE_GAME_RES(conn, req, message);
		} else if (message instanceof GET_TESK_LIST_RES) {
			NewLoginHandler.instance.handle_GET_TESK_LIST_RES(conn, req, message);
		} else if (message instanceof GET_GAME_PALAYERNAME_RES) {
			NewLoginHandler.instance.handle_GET_GAME_PALAYERNAME_RES(conn, req, message);
		} else if (message instanceof GET_ACTIVITY_JOINIDS_RES) {
			NewLoginHandler.instance.handle_GET_ACTIVITY_JOINIDS_RES(conn, req, message);
		} else if (message instanceof QUERY_GAMENAMES_RES) {
			NewLoginHandler.instance.handle_QUERY_GAMENAMES_RES(conn, req, message);
		} else if (message instanceof GET_PET_NBWINFO_RES) {
			NewLoginHandler.instance.handle_GET_PET_NBWINFO_RES(conn, req, message);
		} else if (message instanceof CLONE_FRIEND_LIST_RES) {
			NewLoginHandler.instance.handle_CLONE_FRIEND_LIST_RES(conn, req, message);
		} else if (message instanceof QUERY_OTHERPLAYER_PET_MSG_RES) {
			NewLoginHandler.instance.handle_QUERY_OTHERPLAYER_PET_MSG_RES(conn, req, message);
		} else if (message instanceof CSR_GET_PLAYER_RES) {
			NewLoginHandler.instance.handle_CSR_GET_PLAYER_RES(conn, req, message);
		} else if (message instanceof HAVE_OTHERNEW_INFO_RES) {
			NewLoginHandler.instance.handle_HAVE_OTHERNEW_INFO_RES(conn, req, message);
		} else if (message instanceof SHANCHU_FRIENDLIST_RES) {
			NewLoginHandler.instance.handle_SHANCHU_FRIENDLIST_RES(conn, req, message);
		} else if (message instanceof QUERY_TESK_LIST_RES) {
			NewLoginHandler.instance.handle_QUERY_TESK_LIST_RES(conn, req, message);
		} else if (message instanceof CL_HORSE_INFO_RES) {
			NewLoginHandler.instance.handle_CL_HORSE_INFO_RES(conn, req, message);
		} else if (message instanceof CL_NEWPET_MSG_RES) {
			NewLoginHandler.instance.handle_CL_NEWPET_MSG_RES(conn, req, message);
		} else if (message instanceof GET_ACTIVITY_NEW_RES) {
			NewLoginHandler.instance.handle_GET_ACTIVITY_NEW_RES(conn, req, message);
		} else if (message instanceof DO_SOME_RES) {
			NewLoginHandler.instance.handle_DO_SOME_RES(conn, req, message);
		} else if (message instanceof TY_PET_RES) {
			NewLoginHandler.instance.handle_TY_PET_RES(conn, req, message);
		} else if (message instanceof EQUIPMENT_GET_MSG_RES) {
			NewLoginHandler.instance.handle_EQUIPMENT_GET_MSG_RES(conn, req, message);
		} else if (message instanceof EQU_NEW_EQUIPMENT_RES) {
			NewLoginHandler.instance.handle_EQU_NEW_EQUIPMENT_RES(conn, req, message);
		} else if (message instanceof DELETE_FRIEND_LIST_RES) {
			NewLoginHandler.instance.handle_DELETE_FRIEND_LIST_RES(conn, req, message);
		} else if (message instanceof DO_PET_EQUIPMENT_RES) {
			NewLoginHandler.instance.handle_DO_PET_EQUIPMENT_RES(conn, req, message);
		} else if (message instanceof QILING_NEW_INFO_RES) {
			NewLoginHandler.instance.handle_QILING_NEW_INFO_RES(conn, req, message);
		} else if (message instanceof HORSE_QILING_INFO_RES) {
			NewLoginHandler.instance.handle_HORSE_QILING_INFO_RES(conn, req, message);
		} else if (message instanceof HORSE_EQUIPMENT_QILING_RES) {
			NewLoginHandler.instance.handle_HORSE_EQUIPMENT_QILING_RES(conn, req, message);
		} else if (message instanceof PET_EQU_QILING_RES) {
			NewLoginHandler.instance.handle_PET_EQU_QILING_RES(conn, req, message);
		} else if (message instanceof MARRIAGE_TRYACTIVITY_RES) {
			NewLoginHandler.instance.handle_MARRIAGE_TRYACTIVITY_RES(conn, req, message);
		} else if (message instanceof PET_TRY_QILING_RES) {
			NewLoginHandler.instance.handle_PET_TRY_QILING_RES(conn, req, message);
		} else if (message instanceof PLAYER_CLEAN_QILINGLIST_RES) {
			NewLoginHandler.instance.handle_PLAYER_CLEAN_QILINGLIST_RES(conn, req, message);
		} else if (message instanceof DELETE_TESK_LIST_RES) {
			NewLoginHandler.instance.handle_DELETE_TESK_LIST_RES(conn, req, message);
		} else if (message instanceof GET_HEIMINGDAI_NEWLIST_RES) {
			NewLoginHandler.instance.handle_GET_HEIMINGDAI_NEWLIST_RES(conn, req, message);
		} else if (message instanceof QUERY_CHOURENLIST_RES) {
			NewLoginHandler.instance.handle_QUERY_CHOURENLIST_RES(conn, req, message);
		} else if (message instanceof QINCHU_PLAYER_RES) {
			NewLoginHandler.instance.handle_QINCHU_PLAYER_RES(conn, req, message);
		} else if (message instanceof REMOVE_FRIEND_LIST_RES) {
			NewLoginHandler.instance.handle_REMOVE_FRIEND_LIST_RES(conn, req, message);
		} else if (message instanceof TRY_REMOVE_CHOUREN_RES) {
			NewLoginHandler.instance.handle_TRY_REMOVE_CHOUREN_RES(conn, req, message);
		} else if (message instanceof REMOVE_CHOUREN_RES) {
			NewLoginHandler.instance.handle_REMOVE_CHOUREN_RES(conn, req, message);
		} else if (message instanceof DELETE_TASK_LIST_RES) {
			NewLoginHandler.instance.handle_DELETE_TASK_LIST_RES(conn, req, message);
		} else if (message instanceof PLAYER_TO_PLAYER_DEAL_RES) {
			NewLoginHandler.instance.handle_PLAYER_TO_PLAYER_DEAL_RES(conn, req, message);
		} else if (message instanceof AUCTION_NEW_LIST_MSG_RES) {
			NewLoginHandler.instance.handle_AUCTION_NEW_LIST_MSG_RES(conn, req, message);
		} else if (message instanceof REQUEST_BUY_PLAYER_INFO_RES) {
			NewLoginHandler.instance.handle_REQUEST_BUY_PLAYER_INFO_RES(conn, req, message);
		} else if (message instanceof BOOTHER_PLAYER_MSGNAME_RES) {
			NewLoginHandler.instance.handle_BOOTHER_PLAYER_MSGNAME_RES(conn, req, message);
		} else if (message instanceof BOOTHER_MSG_CLEAN_RES) {
			NewLoginHandler.instance.handle_BOOTHER_MSG_CLEAN_RES(conn, req, message);
		} else if (message instanceof TRY_REQUESTBUY_CLEAN_ALL_RES) {
			NewLoginHandler.instance.handle_TRY_REQUESTBUY_CLEAN_ALL_RES(conn, req, message);
		} else if (message instanceof SCHOOL_INFONAMES_RES) {
			NewLoginHandler.instance.handle_SCHOOL_INFONAMES_RES(conn, req, message);
		} else if (message instanceof PET_NEW_LEVELUP_RES) {
			NewLoginHandler.instance.handle_PET_NEW_LEVELUP_RES(conn, req, message);
		} else if (message instanceof VALIDATE_ASK_NEW_RES) {
			NewLoginHandler.instance.handle_VALIDATE_ASK_NEW_RES(conn, req, message);
		} else if (message instanceof JINGLIAN_NEW_TRY_RES) {
			NewLoginHandler.instance.handle_JINGLIAN_NEW_TRY_RES(conn, req, message);
		} else if (message instanceof JINGLIAN_NEW_DO_RES) {
			NewLoginHandler.instance.handle_JINGLIAN_NEW_DO_RES(conn, req, message);
		} else if (message instanceof JINGLIAN_PET_RES) {
			NewLoginHandler.instance.handle_JINGLIAN_PET_RES(conn, req, message);
		} else if (message instanceof SEE_NEWFRIEND_LIST_RES) {
			NewLoginHandler.instance.handle_SEE_NEWFRIEND_LIST_RES(conn, req, message);
		} else if (message instanceof EQU_PET_HUN_RES) {
			NewLoginHandler.instance.handle_EQU_PET_HUN_RES(conn, req, message);
		} else if (message instanceof PET_ADD_HUNPO_RES) {
			NewLoginHandler.instance.handle_PET_ADD_HUNPO_RES(conn, req, message);
		} else if (message instanceof PET_ADD_SHENGMINGVALUE_RES) {
			NewLoginHandler.instance.handle_PET_ADD_SHENGMINGVALUE_RES(conn, req, message);
		} else if (message instanceof HORSE_REMOVE_HUNPO_RES) {
			NewLoginHandler.instance.handle_HORSE_REMOVE_HUNPO_RES(conn, req, message);
		} else if (message instanceof PET_REMOVE_HUNPO_RES) {
			NewLoginHandler.instance.handle_PET_REMOVE_HUNPO_RES(conn, req, message);
		} else if (message instanceof PLAYER_CLEAN_HORSEHUNLIANG_RES) {
			NewLoginHandler.instance.handle_PLAYER_CLEAN_HORSEHUNLIANG_RES(conn, req, message);
		} else if (message instanceof GET_NEW_LEVELUP_RES) {
			NewLoginHandler.instance.handle_GET_NEW_LEVELUP_RES(conn, req, message);
		} else if (message instanceof DO_HOSEE2OTHER_RES) {
			NewLoginHandler.instance.handle_DO_HOSEE2OTHER_RES(conn, req, message);
		} else if (message instanceof TRYDELETE_PET_INFO_RES) {
			NewLoginHandler.instance.handle_TRYDELETE_PET_INFO_RES(conn, req, message);
		} else if (message instanceof HAHA_ACTIVITY_MSG_RES) {
			NewLoginHandler.instance.handle_HAHA_ACTIVITY_MSG_RES(conn, req, message);
		} else if (message instanceof VALIDATE_NEW_RES) {
			NewLoginHandler.instance.handle_VALIDATE_NEW_RES(conn, req, message);
		} else if (message instanceof VALIDATE_ANDSWER_NEW_RES) {
			NewLoginHandler.instance.handle_VALIDATE_ANDSWER_NEW_RES(conn, req, message);
		} else if (message instanceof PLAYER_ASK_TO_OTHWE_RES) {
			NewLoginHandler.instance.handle_PLAYER_ASK_TO_OTHWE_RES(conn, req, message);
		} else if (message instanceof GA_GET_SOME_RES) {
			NewLoginHandler.instance.handle_GA_GET_SOME_RES(conn, req, message);
		} else if (message instanceof OTHER_PET_LEVELUP_RES) {
			NewLoginHandler.instance.handle_OTHER_PET_LEVELUP_RES(conn, req, message);
		} else if (message instanceof MY_OTHER_FRIEDN_RES) {
			NewLoginHandler.instance.handle_MY_OTHER_FRIEDN_RES(conn, req, message);
		} else if (message instanceof DOSOME_SB_MSG_RES) {
			NewLoginHandler.instance.handle_DOSOME_SB_MSG_RES(conn, req, message);
		}
		return true;
	}

	public boolean isValidUsername(String s) {
		if (s == null || s.length() == 0) {
			return false;
		}
		char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if ((chars[i] >= '0' && chars[i] <= '9') || (chars[i] >= 'a' && chars[i] <= 'z') || (chars[i] >= 'A' && chars[i] <= 'Z') || (chars[i] == '-')
					|| (chars[i] == '_') || (chars[i] == '.')) {

			} else {
				return false;
			}
		}
		return true;
	}
	public boolean isValidUsername4Kunlun(String s) {
		if (s == null || s.length() == 0) {
			return false;
		}
		return true;
	}

	private boolean handleChannelRegisterSuccess(Connection conn, GatewayClient client, RequestMessage message, String reqUserName, String userIdInChannel) {

		String passwd;
		String channel;
		String userType;
		String clientID;
		String reqUserType;
		String platform;
		if (message instanceof PASSPORT_REGISTER_PRO_REQ) {
			PASSPORT_REGISTER_PRO_REQ req = (PASSPORT_REGISTER_PRO_REQ)message;
			passwd = req.getPasswd();
			channel = req.getChannel();
			userType = req.getUsertype();
			clientID = req.getClientId();
			reqUserType = req.getUsertype();
			platform = req.getPlatform();
		}
		else {
			return false;
		}


		try {
			//bossClientService.register(reqUserName, passwd, "", "", "", "", channel, channel, false, 0);

			//Object obj[] = bossClientService.register(reqUserName, passwd, null, null, null, channel, null, null, null, null);
			bossClientService.register(userIdInChannel, passwd, clientID, reqUserName, reqUserType, channel, "", platform, client.getPhoneType(), client.getNetworkMode());

			client.setPassport(reqUserName);

			// //////////////添加注册用户 start///////////////////////////
			UserRegistFlow userRegistFlow = new UserRegistFlow();
			userRegistFlow.setDidian("未知"); // 地点
			userRegistFlow.setEmei("43co-sdf0-sdf-we454");
			userRegistFlow.setGame("灭世OL");
			userRegistFlow.setHaoma("11111111111"); // 手机号码
			userRegistFlow.setJixing(client.getPlatform()); // 手机机型
			userRegistFlow.setUserName(userIdInChannel);// 用户名
			userRegistFlow.setNations(""); // 国家
			userRegistFlow.setQudao(client.getChannel()); // 渠道
			userRegistFlow.setRegisttime(System.currentTimeMillis()); // 用户注册时间
			// /////////以下字段是为快速注册的用户使用，如果不是快速注册，可以填默认值，或者（“”）空字符串 start////////////
			userRegistFlow.setPlayerName("--"); // //角色名
			userRegistFlow.setCreatPlayerTime(System.currentTimeMillis()); // //创建角色时间
			userRegistFlow.setFenQu("--"); // //分区
			// /////////以下字段是为快速注册的用户使用，如果不是快速注册，可以填默认值，或者（“”）空字符串 start////////////
			StatClientService statClientService = StatClientService.getInstance();
			if (statClientService != null) statClientService.sendUserRegistFlow("灭世OL", userRegistFlow);

			return true;
		} catch (UsernameAlreadyExistsException ex) {
			String reason = Translate.自动注册失败用户名已存在;
			logger.warn("[" + conn.getName() + "] [渠道用户自动注册] [userType:" + userType + "] [失败] [" + reason + "] [username:" + reqUserName + "] [channelUsername:" + userIdInChannel + "] [passwd:" + passwd + "] " + client.getLogStr());
			return false;
		} catch (Exception ex) {
			String reason = Translate.自动注册失败注册时出现未知错误;
			logger.warn("[" + conn.getName() + "] [渠道用户自动注册] [userType:" + userType + "] [失败] [" + reason + "] [username:" + reqUserName + "] [channelUsername:" + userIdInChannel + "] [passwd:" + passwd + "] " + client.getLogStr());
			return false;
		}
	}

	public void setErrorMsgPath(String errorMsgPath) {
		this.errorMsgPath = errorMsgPath;
	}

	public String getErrorMsgPath() {
		return errorMsgPath;
	}

	/*
	 * 封号用户
	 */
	public USER_LOGIN_RES judgeDenyUser(String username,String passwd,GatewayClient client,Connection conn,String userType,USER_LOGIN_REQ req) {
		MieshiServerInfoManager sm = MieshiServerInfoManager.getInstance();
		DenyUserEntity du = sm.getDenyUser(username);
		if (du != null) {

			if (du != null && du.enableDeny
					&& (du.foroverDeny || (System.currentTimeMillis() > du.denyStartTime && System.currentTimeMillis() < du.denyEndTime))) {

				String reason = "";
				if (du.foroverDeny) {
					//					reason = "登录失败,您被永久性限制登录,原因是:" + du.reason + ",如有问题请联系我们：4009889988";
					reason = Translate.translateString(Translate.登录失败您被永久性限制登录, new String[][]{{Translate.STRING_1,du.reason}});
				} else {
					//					reason = "登录失败,您被临时限制登录,截止到" + DateUtil.formatDate(new java.util.Date(du.denyEndTime), "yyyy-MM-dd HH:mm:ss") + "，原因是:" + du.reason;
					reason = Translate.translateString(Translate.登录失败您被临时限制登录, new String[][]{{Translate.STRING_1,DateUtil.formatDate(new java.util.Date(du.denyEndTime), "yyyy-MM-dd HH:mm:ss")},{Translate.STRING_2,du.reason}});
				}

				USER_LOGIN_RES res = new USER_LOGIN_RES(req.getSequnceNum(), (byte) 4, reason, "", Translate.登录失败);
				client.setPassport(username);

				logger.info("[" + conn.getName() + "] [用户登录] [userType:" + userType + "] [失败] [" + reason + "] [username:" + username + "] [passwd:"
						+ passwd + "] [authCode:--] " + client.getLogStr());
				return res;

			}
		}

		return null;
	}

	public GET_REGISTER_IMAGE_RES creatRegisterCode (long reqNum, String clientID) {
		String registerCodeString = "";
		Random ran = new Random();
		for (int i = 0; i < registerCodeNum; i++) {
			int index = ran.nextInt(registerRandomString.length);
			registerCodeString += registerRandomString[index];
		}
		byte[] imageData = ImageTool.buildPng(registerCodeString);
		registerCode.put(clientID, registerCodeString);
		GET_REGISTER_IMAGE_RES res = new GET_REGISTER_IMAGE_RES(reqNum, imageData);
		logger.warn("生成注册验证码成功 "+"clientID=["+clientID+"] code=["+registerCodeString+"]");
		return res;
	}
	
	public static String getSmsCode(){
		String code = "";
		Random ran = new Random();
		for (int i = 0; i < smsCodeNum; i++) {
			int index = ran.nextInt(smsRandomString.length);
			code += smsRandomString[index];
		}
		return code;
	}
	
	public GET_REGISTER_IMAGE_NEW_RES creatRegisterCode (long reqNum, String clientID,boolean isShowShenfenzheng) {
		String registerCodeString = "";
		Random ran = new Random();
		for (int i = 0; i < registerCodeNum; i++) {
			int index = ran.nextInt(registerRandomString.length);
			registerCodeString += registerRandomString[index];
		}
		byte[] imageData = ImageTool.buildPng(registerCodeString);
		registerCode.put(clientID, registerCodeString);
		String channel = clientId2Channel.remove(clientID);
		if(channel != null)
		{
			if(channel.equals("APPSTORE_XUNXIAN"))
			{
				isShowShenfenzheng = false;
			}
		}
		
		
		String[] others = new String[]{isShowShenfenzheng+""};
		GET_REGISTER_IMAGE_NEW_RES res = new GET_REGISTER_IMAGE_NEW_RES(reqNum, imageData,others);
		logger.warn("生成注册验证码成功 "+"clientID=["+clientID+"] code=["+registerCodeString+"] [isShowShenfenzheng:"+isShowShenfenzheng+"] [channel:"+channel+"]" );
		return res;
	}

	public static boolean isWin8Update = true;
	public static String WIN8URL = "http://apps.microsoft.com/windows/zh-cn/app/online/4e0c688f-fb90-4e2d-ab38-9cda045342a4";
	public MIESHI_GET_VERSION_INFO_RES handle_MIESHI_GET_VERSION_INFO (Connection conn, GatewayClient client, long sequnceNum, String clientId, String channel, String platform, String gpu, String gpuOther, String phoneType, String mac, String clientVersion, String resourceVersion, String netMode, String packageType, String[] extraInfo) {

		ResourceManager rm = ResourceManager.getInstance();
		/**
		 * 接收到客户端获取版本信息的请求，返回服务器端版本信息响应
		 * 如果服务器端程序版本和客户端当前程序版本不一致，那么把程序包的下载链接返回客户端
		 * 
		 * @param req
		 * @return
		 */
		MIESHI_GET_VERSION_INFO_RES res = null;

		// 更好渠道串
		if (channel != null && channel.equalsIgnoreCase("us_ios_1")) {
			channel = "UC_MIESHI";
		} else if (channel != null && channel.equalsIgnoreCase("us_ios_2")) {
			channel = "UC_MIESHI";
		} else if (channel != null && channel.equalsIgnoreCase("us_ios_3")) {
			channel = "UC_MIESHI";
		} else if (channel != null && channel.equalsIgnoreCase("91_mieshi")) {
			channel = "SQAGE_MIESHI";
		}

		String clientCurrentProgramVersion = clientVersion;
		String clientCurrentResourceVersion = resourceVersion;

		if(!StringUtils.isEmpty(netMode))
		{
			if(netMode.length() > 20)
			{
				netMode = netMode.substring(0,20);
			}
		}

		String link = "";

		client.setClientId(clientId);
		client.setChannel(channel);
		client.setPlatform(platform);
		client.setGpu(gpu);
		client.setClientCurrentProgramVersion(clientCurrentProgramVersion);
		client.setClientCurrentResourceVersion(clientCurrentResourceVersion);
		client.setPhoneType(phoneType);
		client.setGpuOtherName(gpuOther);
		client.setNetworkMode(netMode);

		boolean needToUpdateProgram = false;
		boolean needToForceUpdate = false;
		int newPackageSize = 0;
		String serverResourceVersion = "" + (innerTesterManager.isInnerTester(clientId) ? rm.getTestResourceVersion() : rm.getRealResourceVersion());
		String serverProgramVersion = clientCurrentProgramVersion;


		StatManager stat = StatManager.getInstance();
		if (stat != null) {
			stat.notifyVersionCheck(clientId, channel, platform, gpu, clientCurrentProgramVersion, clientCurrentResourceVersion, serverProgramVersion,
					serverResourceVersion, phoneType, gpuOther, packageType, netMode);

			PHONE_UIID_INFO_REQ uuidReq = (PHONE_UIID_INFO_REQ)conn.getAttachmentData("PHONE_UIID_INFO_REQ");

			if(uuidReq != null){
				if(uuidReq.getUUID().length() > 0){
					client.setUuid(uuidReq.getUUID());
				}
				if(uuidReq.getDEVICEID().length() > 0){
					client.setDeviceId(uuidReq.getDEVICEID());
				}
				if(uuidReq.getMACADDRESS().length() > 0){
					client.setMacAddress(uuidReq.getMACADDRESS());
				}
				if(uuidReq.getIMSI().length() > 0){
					client.setImsi(uuidReq.getIMSI());
				}
				ClientId2UUID c2u = new ClientId2UUID(uuidReq.getUUID(),uuidReq.getDEVICEID());
				c2u.phoneType = phoneType;
				c2u.gpuOtherName = gpuOther;

				clientId2UUID.put(client.getClientId(),c2u);
				//* 更新UUID，只有原来的UUID没有设置过，才能更新。
				//* 已经设置过UUID，是不更新的。为了解决篡改UUID用黑卡帮助别人充值的问题
				stat.notifyPhoneUUIDUpdate(client,uuidReq.getUUID(),uuidReq.getDEVICEID(),uuidReq.getMACADDRESS(),uuidReq.getIMSI());

				if (logger.isInfoEnabled()) {
					logger.info("[" + conn.getName() + "] [用户手机信息] [UUID="+uuidReq.getUUID()+"] [DEVICEID=" + uuidReq.getDEVICEID() + "] [MACADDRESS="+uuidReq.getMACADDRESS()+"] [IMSI=" + uuidReq.getIMSI() + "] " + client.getLogStr());
				}

			}
		}

		//对apptore需要特殊处理，因为appstore的版本只能到appstore去更新
		//我们的做法是：
		//   网关有一个后台可以配置appstore的更新地址，版本好
		if(isAppStoreChannel(channel) || channel.equalsIgnoreCase("APPSTOREGUOJI_MIESHI")){

			AppstoreVersionManager avm = AppstoreVersionManager.getInstance();

			if(avm != null && avm.hasNewVersion(clientId, clientCurrentProgramVersion, channel, platform, gpu)){
				com.fy.gamegateway.mieshi.server.AppstoreVersionManager.Version newVersion = avm.getNewPackageVersion(clientId, clientCurrentProgramVersion, channel, platform, gpu);
				if(newVersion != null){
					needToUpdateProgram = true;
					link = newVersion.downUrl;
					serverProgramVersion = newVersion.versionString;
				}else{
					needToUpdateProgram = false;
				}
			}else{
				needToUpdateProgram = false;
			}

			res = new MIESHI_GET_VERSION_INFO_RES(sequnceNum, needToUpdateProgram, needToForceUpdate, serverProgramVersion, newPackageSize, link,
					serverResourceVersion);


			//回调软猎的接口，接口内部会过滤，只有软猎的macaddress才通知他们
			RuanleiAppstoreMatchService ruanlie = RuanleiAppstoreMatchService.getInstance();
			if(ruanlie != null){
				ruanlie.notifyUserActive(client.getMacAddress(), "wangxian", client.getChannel());
			}

			if (logger.isInfoEnabled()) {
				logger.info("[" + conn.getName() + "] [检查版本] [MIESHI_GET_VERSION_INFO] [needToUpdateProgram:" + needToUpdateProgram
						+ "] [needToForceUpdate:" + needToForceUpdate + "] [serverResourceVersion:" + serverResourceVersion + "] [link:" + link + "] "
						+ client.getLogStr() + " [str1:" + packageType + "] [str2:" + netMode + "] ["+mac+"]");
			}

		}

		//为了台湾版的飘渺寻仙曲
		if (res == null) {
			if (channel.equalsIgnoreCase("KUNLUNSDKANDROID_MIESHI") || channel.equalsIgnoreCase("KUNLUNANDROID_MIESHI") || channel.equalsIgnoreCase("KOREAGOOGLE_MIESHI") 
					|| channel.equalsIgnoreCase("GOOGLEINAPPBILLING_MIESHI") || channel.equalsIgnoreCase("KOREASDKGOOGLE_MIESHI")){
				GooglePlayVersionManager avm = GooglePlayVersionManager.getInstance();

				if(avm != null && avm.hasNewVersion(clientId, clientCurrentProgramVersion, channel, platform, gpu)){
					String newVersion = avm.getNewPackageVersion(clientId, clientCurrentProgramVersion, channel, platform, gpu);
					if(newVersion != null){
						needToUpdateProgram = true;
						link = avm.getAppstoreDownloadURL();
						serverProgramVersion = newVersion;
					}else{
						needToUpdateProgram = false;
					}
				}else{
					needToUpdateProgram = false;
				}

				res = new MIESHI_GET_VERSION_INFO_RES(sequnceNum, needToUpdateProgram, needToForceUpdate, serverProgramVersion, newPackageSize, link,
						serverResourceVersion);

				//回调软猎的接口，接口内部会过滤，只有软猎的macaddress才通知他们
				if (logger.isInfoEnabled()) {
					logger.info("[" + conn.getName() + "] [检查版本] [MIESHI_GET_VERSION_INFO] [needToUpdateProgram:" + needToUpdateProgram
							+ "] [needToForceUpdate:" + needToForceUpdate + "] [serverResourceVersion:" + serverResourceVersion + "] [link:" + link + "] "
							+ client.getLogStr() + " [str1:" + packageType + "] [str2:" + netMode + "] ["+mac+"]");
				}
			}
		}
		if (res == null) {
			if(NeedToHttpVersionManager.isNeedChannel(channel)) {
				NeedToHttpVersionManager avm = NeedToHttpVersionManager.getInstance();

				if(avm != null && avm.hasNewVersion(clientId, clientCurrentProgramVersion, channel, platform, gpu)){
					com.fy.gamegateway.mieshi.server.NeedToHttpVersionManager.Version newVersion = avm.getNewPackageVersion(clientId, clientCurrentProgramVersion, channel, platform, gpu);
					if(newVersion != null){
						needToUpdateProgram = true;
						link = newVersion.versionUrl;
						serverProgramVersion = newVersion.versionString;
					}else{
						needToUpdateProgram = false;
					}
				}else{
					needToUpdateProgram = false;
				}

				res = new MIESHI_GET_VERSION_INFO_RES(sequnceNum, needToUpdateProgram, needToForceUpdate, serverProgramVersion, newPackageSize, link,
						serverResourceVersion);

				logger.warn("[" + conn.getName() + "] [needToHttp检查版本] [MIESHI_GET_VERSION_INFO] [needToUpdateProgram:" + needToUpdateProgram
						+ "] [needToForceUpdate:" + needToForceUpdate + "] [serverResourceVersion:" + serverResourceVersion + "] [link:" + link + "] "
						+ client.getLogStr() + " [str1:" + packageType + "] [str2:" + netMode + "] ["+mac+"]");
			}
		}

		if (res == null) {
			PackageManager pm = PackageManager.getInstance();

			// 由于使用新的包策略，所有包都用auto包

//			if (platform.toLowerCase().equals("android")) {
//
//				if(gpu != null && gpu.equalsIgnoreCase("android_res")){
//					//通用的资源格式,用etc
//					//对应的程序包就存在两个了,一个是auto包,自动识别gpu的型号,下载不同的资源
//					//一个是etc_general包,只下载etc格式的资源
//					gpu = "android_res";
//				}else{
//					//
//					gpu = "auto";
//				}
//			}

			boolean b = pm.hasNewVersion(clientId, clientCurrentProgramVersion, channel, platform, gpu);

			if (b == true) {
				needToUpdateProgram = true;

				Package p = pm.getNewPackage(clientId, clientCurrentProgramVersion, channel, platform, gpu);
				if (p != null) {
					newPackageSize = p.packageSize;
					link = p.httpDownloadURL;
					needToForceUpdate = p.version.forceUpdate;
					serverProgramVersion = p.version.versionString;

					if (p.platform.equalsIgnoreCase("IOS")) {
						// link =
						// "itms-services://?action=download-manifest&url="+p.httpDownloadURL;
						// .ipa
						// int kk = link.lastIndexOf(".");
						// link = link.substring(0,kk) + ".plist";
						//if (pm.getTestHttpRoot().endsWith("/")) {
						//	link = pm.getTestHttpRoot() + "download.jsp";
						//} else {
						//	link = pm.getTestHttpRoot() + "/download.jsp";
						//}
						if (InnerTesterManager.getInstance().isInnerTester(clientId)) {
//							 link =
//							 "itms-services://?action=download-manifest&url="+p.httpDownloadURL;
//							 int kk = link.lastIndexOf(".");
//							 link = link.substring(0,kk) + ".plist";
//							 link = link.replace("http", "https");
							link = "http://116.213.192.216:8882/game_gateway/packages/download.jsp";
							link += "?ci=" + clientId + "&cv=" + clientCurrentProgramVersion + "&cc=" + channel + "&cp=" + platform + "&gpu=" + gpu;
						}else {
							link = "http://116.213.192.216:8882/game_gateway/real/packages/"+PackageManager.getInstance().getRandomPackagePath()+"/download.jsp";
							link += "?ci=" + clientId + "&cv=" + clientCurrentProgramVersion + "&cc=" + channel + "&cp=" + platform + "&gpu=" + gpu;
						}
					} else {
						ResourceSharedNodeManager nm = ResourceSharedNodeManager.getInstance();
						if (nm != null) {
							PackageSharedNode node = nm.getPackageSharedNodeByChannel(channel, gpu);
							if (node != null && node.isValid()) {
								try {
									PackageManager.Version v = new PackageManager.Version(node.getPackageVersion());
									if (v.compareTo(p.version) == 0) {
										// 采用第三方的节点
										link = node.getUrl();
									}
								} catch (Exception e) {
									// nothing
								}
							}
						}
					}
				} else {
					needToUpdateProgram = false;
				}
			}

			if (isOpenResourceUpdate) {
				if (qudaoNames.contains(channel)) {
					//如果在不更新的渠道里面  就不更新，返回227，但这个地方如果他之前不是227  也会更新
					serverResourceVersion = clientCurrentResourceVersion;
				}
			}

			res = new MIESHI_GET_VERSION_INFO_RES(sequnceNum, needToUpdateProgram, needToForceUpdate, serverProgramVersion, newPackageSize, link,
					serverResourceVersion);

			if (logger.isInfoEnabled()) {
				logger.info("[" + conn.getName() + "] [检查版本] [] [needToUpdateProgram:" + needToUpdateProgram
						+ "] [needToForceUpdate:" + needToForceUpdate + "] [serverResourceVersion:" + serverResourceVersion + "] [link:" + link + "] "
						+ client.getLogStr() + " [str1:" + packageType + "] [str2:" + netMode + "] ["+mac+"]");
			}
		}
		
		if (!needToUpdateProgram && res != null) {
			//检查资源MiniZip包
			if (!clientCurrentResourceVersion.equals(serverResourceVersion) && platform.equalsIgnoreCase("Android")) {
				MiniResourceZipData data = MiniResourceZipManager.instance.getMiniResourceUrl(clientCurrentResourceVersion);
				if (data != null) {
					String url = data.getDownUrl(channel, platform, client.getGpu());
					if (url != null && url.length() > 0) {
						int ss = (int)data.getSize(client.getGpu(), client.getPlatform());
						res = new MIESHI_GET_VERSION_INFO_RES(sequnceNum, needToUpdateProgram, needToForceUpdate, serverProgramVersion, ss, url,
								data.getNowResourceVersion());
						if (MiniResourceZipManager.logger.isInfoEnabled()) {
							MiniResourceZipManager.logger.info("[" + conn.getName() + "] [下载miniZIP] [] [needToUpdateProgram:" + needToUpdateProgram
									+ "] [needToForceUpdate:" + needToForceUpdate + "] [serverResourceVersion:" + res.getServerResourceVersion() + "] [link:" + res.getLink() + "] [size="+ss+"]"
									+ client.getLogStr() + " [str1:" + packageType + "] [str2:" + netMode + "] ["+mac+"]");
						}
					}
				}
			}
		}

		return res;
	}

	public PASSPORT_REGISTER_PRO_RES validateShenfenzheng(String shenfenzheng,PASSPORT_REGISTER_PRO_REQ req,Connection conn)
	{
		String channel = req.getChannel();
		if(validateShenfenzheng)
		{
			if(channel != null)
			{
				if(channel.contains("APPSTORE_XUNXIAN"))
				{
					return null;
				}
			}
			
			String errinfo = IDCardValidate(shenfenzheng);
			if(errinfo != null && errinfo.length() > 0  )
			{
				if(errinfo.contains("由于您的年龄未满18岁"))
				{
					PASSPORT_REGISTER_PRO_RES res = new PASSPORT_REGISTER_PRO_RES(req.getSequnceNum(), 0, errinfo, req.getUsername(), "", "", "", (byte) 0,new String[0]);
					return res;
				}
				else
				{

					PASSPORT_REGISTER_PRO_RES res = new PASSPORT_REGISTER_PRO_RES(req.getSequnceNum(), 2, errinfo, req.getUsername(), "", "", "", (byte) 0,new String[0]);
					return res;
				}
			}
			else
			{
				return null;
			}

		}
		else
		{
			return null;
		}
	}

	/* 公民身份号码是特征组合码，由十七位数字本体码和一位校验码组成。排列顺序从左至右依次为：六位数字地址码， 
	 八位数字出生日期码，三位数字顺序码和一位数字校验码。 
	 2、地址码(前六位数） 
	 表示编码对象常住户口所在县(市、旗、区)的行政区划代码，按GB/T2260的规定执行。 
	 3、出生日期码（第七位至十四位） 
	 * 表示编码对象出生的年、月、日，按GB/T7408的规定执行，年、月、日代码之间不用分隔符。 
	 * 4、顺序码（第十五位至十七位） 
	 * 表示在同一地址码所标识的区域范围内，对同年、同月、同日出生的人编定的顺序号， 
	 * 顺序码的奇数分配给男性，偶数分配给女性。 
	 * 5、校验码（第十八位数） 
	 * （1）十七位数字本体码加权求和公式 S = Sum(Ai * Wi), i = 0,  , 16 ，先对前17位数字的权求和 
	 * Ai:表示第i位置上的身份证号码数字值 Wi:表示第i位置上的加权因子 Wi: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 
	 * 2 （2）计算模 Y = mod(S, 11) （3）通过模得到对应的校验码 Y: 0 1 2 3 4 5 6 7 8 9 10 校验码: 1 0 
	 * X 9 8 7 6 5 4 3 2 
	 */ 

	public boolean isValidateShenfenzheng() {
		return validateShenfenzheng;
	}

	public void setValidateShenfenzheng(boolean validateShenfenzheng) {
		this.validateShenfenzheng = validateShenfenzheng;
	}

	public boolean isShowShenfenzheng() {
		return showShenfenzheng;
	}

	public void setShowShenfenzheng(boolean showShenfenzheng) {
		this.showShenfenzheng = showShenfenzheng;
	}

	public static void main(String[] args) {
		//String result = IDCardValidate("11117877777d777");
		//System.out.println(result);
		String a = "619741";
		int b = 619741;
		System.out.println(a.equals(""+b));
		for(int i=0;i<10;i++){
			System.out.println(getSmsCode());
		}
	}
	
	/** 
	 * 功能：身份证的有效验证 
	 * @param IDStr 身份证号 
	 * @return 有效：返回"" 无效：返回String信息 
	 * @throws ParseException 
	 */ 
	public static String IDCardValidate(String IDStr) { 
		try
		{
			String errorInfo = "";// 记录错误信息 
			String[] ValCodeArr = { "1", "0", "x", "9", "8", "7", "6", "5", "4", 
					"3", "2" }; 
			String[] Wi = { "7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7", 
					"9", "10", "5", "8", "4", "2" }; 
			String Ai = ""; 
			// ================ 号码的长度 15位或18位 ================ 
			if (IDStr.length() != 15 && IDStr.length() != 18) { 
				errorInfo = "对不起，您输入的身份证号码有误。"; 
				return errorInfo; 
			} 
			// =======================(end)======================== 

			// ================ 数字 除最后以为都为数字 ================ 
			if (IDStr.length() == 18) { 
				Ai = IDStr.substring(0, 17); 
			} else if (IDStr.length() == 15) { 
				Ai = IDStr.substring(0, 6) + "19" + IDStr.substring(6, 15); 
			} 
			if (isNumeric(Ai) == false) { 
				errorInfo = "对不起，您输入的身份证号码有误。"; 
				return errorInfo; 
			} 
			// =======================(end)======================== 

			// ================ 出生年月是否有效 ================ 
			String strYear = Ai.substring(6, 10);// 年份 
			String strMonth = Ai.substring(10, 12);// 月份 
			String strDay = Ai.substring(12, 14);// 月份 
			if (isDate(strYear + "-" + strMonth + "-" + strDay) == false) { 
				errorInfo = "对不起，您输入的身份证号码无效，请您核对后重新输入。"; 
				return errorInfo; 
			} 
			GregorianCalendar gc = new GregorianCalendar(); 
			SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd"); 
			if ((gc.get(Calendar.YEAR) - Integer.parseInt(strYear)) > 150 
					|| (gc.getTime().getTime() - s.parse( 
							strYear + "-" + strMonth + "-" + strDay).getTime()) < 0) { 
				errorInfo = "对不起，您输入的身份证号码无效，请您核对后重新输入。"; 
				return errorInfo; 
			} 
			if (Integer.parseInt(strMonth) > 12 || Integer.parseInt(strMonth) == 0) { 
				errorInfo = "对不起，您输入的身份证号码无效，请您核对后重新输入。"; 
				return errorInfo; 
			} 
			if (Integer.parseInt(strDay) > 31 || Integer.parseInt(strDay) == 0) { 
				errorInfo = "对不起，您输入的身份证号码无效，请您核对后重新输入。"; 
				return errorInfo; 
			} 

			//判断是否已经满十八岁
			if ((gc.get(Calendar.YEAR) - Integer.parseInt(strYear)) < 18  || ((gc.get(Calendar.YEAR) - Integer.parseInt(strYear)) == 18 && (gc.get(Calendar.MONTH) - Integer.parseInt(strMonth)) < 0 ))
			{
				errorInfo = "尊敬的用户您好：\n"+
						"  由于您的年龄未满18岁，为了保护您的身心健康，我们将将您纳入到防沉迷系统中。\n"+
						"  当天累计游戏时间超过3小时，游戏收益（经验，金钱）减半。"+
						"当天累计游戏时间超过5小时，游戏收益为0。"; 
				return errorInfo; 
			}

			// =====================(end)===================== 

			// ================ 地区码时候有效 ================ 

			if (areaCodeMap.get(Ai.substring(0, 2)) == null) { 
				errorInfo = "对不起，您输入的身份证号码无效，请您核对后重新输入。"; 
				return errorInfo; 
			} 
			// ============================================== 

			// ================ 判断最后一位的值 ================ 
			int TotalmulAiWi = 0; 
			for (int i = 0; i < 17; i++) { 
				TotalmulAiWi = TotalmulAiWi 
						+ Integer.parseInt(String.valueOf(Ai.charAt(i))) 
						* Integer.parseInt(Wi[i]); 
			} 
			int modValue = TotalmulAiWi % 11; 
			String strVerifyCode = ValCodeArr[modValue]; 
			Ai = Ai + strVerifyCode; 

			if (IDStr.length() == 18) { 
				if (Ai.equalsIgnoreCase(IDStr) == false) { 
					errorInfo = "尊敬的用户您好，您输入的身份证无效，不是合法的身份证号码。"; 
					return errorInfo; 
				} 
			} else { 
				return ""; 
			} 
			// =====================(end)===================== 
			return ""; 
		}
		catch(Exception e)
		{
			logger.error("[注册用户] [解析身份证号] [失败] [出现异常]",e);
			return "尊敬的用户您好，您输入的身份证无效，不是合法的身份证号码。";
		}
	} 

	private static Map<String,String> areaCodeMap = new ConcurrentHashMap<String, String>();
	static
	{
		areaCodeMap.put("11", "北京"); 
		areaCodeMap.put("12", "天津"); 
		areaCodeMap.put("13", "河北"); 
		areaCodeMap.put("14", "山西"); 
		areaCodeMap.put("15", "内蒙古"); 
		areaCodeMap.put("21", "辽宁"); 
		areaCodeMap.put("22", "吉林"); 
		areaCodeMap.put("23", "黑龙江"); 
		areaCodeMap.put("31", "上海"); 
		areaCodeMap.put("32", "江苏"); 
		areaCodeMap.put("33", "浙江"); 
		areaCodeMap.put("34", "安徽"); 
		areaCodeMap.put("35", "福建"); 
		areaCodeMap.put("36", "江西"); 
		areaCodeMap.put("37", "山东"); 
		areaCodeMap.put("41", "河南"); 
		areaCodeMap.put("42", "湖北"); 
		areaCodeMap.put("43", "湖南"); 
		areaCodeMap.put("44", "广东"); 
		areaCodeMap.put("45", "广西"); 
		areaCodeMap.put("46", "海南"); 
		areaCodeMap.put("50", "重庆"); 
		areaCodeMap.put("51", "四川"); 
		areaCodeMap.put("52", "贵州"); 
		areaCodeMap.put("53", "云南"); 
		areaCodeMap.put("54", "西藏"); 
		areaCodeMap.put("61", "陕西"); 
		areaCodeMap.put("62", "甘肃"); 
		areaCodeMap.put("63", "青海"); 
		areaCodeMap.put("64", "宁夏"); 
		areaCodeMap.put("65", "新疆"); 
		areaCodeMap.put("71", "台湾"); 
		areaCodeMap.put("81", "香港"); 
		areaCodeMap.put("82", "澳门"); 
		areaCodeMap.put("91", "国外"); 
	}


	/** 
	 * 功能：判断字符串是否为数字 
	 * @param str 
	 * @return 
	 */ 
	private static boolean isNumeric(String str) { 
		Pattern pattern = Pattern.compile("[0-9]*"); 
		Matcher isNum = pattern.matcher(str); 
		if (isNum.matches()) { 
			return true; 
		} else { 
			return false; 
		} 
	} 

	/** 
	 * 功能：判断字符串是否为日期格式 
	 * @param str 
	 * @return 
	 */ 
	public static boolean isDate(String strDate) { 
		Pattern pattern = Pattern 
				.compile("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s(((0?[0-9])|([1-2][0-3]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$"); 
		Matcher m = pattern.matcher(strDate); 
		if (m.matches()) { 
			return true; 
		} else { 
			return false; 
		} 
	} 

	public static DiskCache shenfenzhengCache;

	public void saveShenfenzheng(String username,PassportExtend passportExtend){
		if(shenfenzhengCache == null){
			shenfenzhengCache =new DefaultDiskCache(new File(getShenfenzhengCachePath()), "身份证存储缓存", 1000L*60*60*24*365);
		}
		shenfenzhengCache.put(username, passportExtend);
		if(logger.isInfoEnabled())
			logger.info("[存储用户身份证号] ["+username+"] ["+passportExtend.realname+"] ["+passportExtend.shenfenzheng+"]");
	}

	public PassportExtend getShenfenzhengCache(String username){
		if(shenfenzhengCache != null){
			PassportExtend shenfenzheng = (PassportExtend)shenfenzhengCache.get(username);
			if( shenfenzheng != null){
				return shenfenzheng;
			}else{
				return null;
			}
		}else{
			return null;
		}
	}

	public String getShenfenzhengCachePath() {
		return shenfenzhengCachePath;
	}

	public void setShenfenzhengCachePath(String shenfenzhengCachePath) {
		this.shenfenzhengCachePath = shenfenzhengCachePath;
	}

	public static String[]  ServersShowForWin8 = new String[]{"花好月圆","普陀梵音","傲世转生","天脉传说","群雄风云","傲视遮天","潘多拉星","潘多拉星333"};

	private boolean isWin8Show(String serverName){
		for( String str : ServersShowForWin8){
			if(logger.isDebugEnabled())
				logger.debug(str+":"+serverName);
			if(serverName.equals(str)){
				return true;
			}
		}
		return false;
	}

}


