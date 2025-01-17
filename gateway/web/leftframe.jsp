<%@ page contentType="text/html;charset=utf-8" import="java.util.*"%>
<%@page import="com.xuanzhi.tools.authorize.AuthorizedServletFilter"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="css/dtree.css" type="text/css" rel="stylesheet" />
<script src="js/dtree.js" type=text/javascript></script>

<style type="text/css">
<!--
body,td,th {
	font-size: 14px;
}
body {
	margin-left: 5px;
	margin-top: 5px;
	margin-right: 0px;
	margin-bottom: 0px;
	background-color: #DDEDFF;
}
-->
</style></head>
<body>
<div align="center" class="dtree">
<a href="javascript:%20d.openAll();" class="node">全部展开</a> |
<a href="javascript:%20d.closeAll();" class="node">全部折叠</a><br/><br/>
</div>
<SCRIPT type=text/javascript>
		<!--

		d = new dTree('d');
		d.add(0,-1,'飘渺网关');

<%
		ArrayList<Object[]> al = new ArrayList<Object[]>();
		al.add(new Object[]{1,0,"网关状态",""});
		al.add(new Object[]{11,1,"与客户端链接情况","transport/conns.jsp?bean=gateway_mieshi_user_selector"});
		al.add(new Object[]{12,1,"与客户端链接情况2","transport/conns.jsp?bean=gateway_mieshi_user_selector2"});
		al.add(new Object[]{13,1,"与游戏服务器链接情况","transport/conns.jsp?bean=gateway_mieshi_UpdateResource_selector"});
		al.add(new Object[]{14,1,"与BOSS链接情况","transport/conns.jsp?bean=boss_system_client"});
		al.add(new Object[]{15,1,"与STAT链接情况","transport/conns.jsp?bean=stat_system_client"});
		
		al.add(new Object[]{16,1,"线程情况","thread/threadDump_1_5.jsp?s=&n="});
		al.add(new Object[]{16,1,"boss线程情况","http://119.254.154.201:12111/thread/threadDump_1_5.jsp?s=&n="});

		al.add(new Object[]{60,1,"测试apk和ipa包","resource/testPackage.jsp"});
		al.add(new Object[]{61,1,"测试资源文件列表","resource/testResourceFileList.jsp"});
		al.add(new Object[]{62,1,"正式apk和ipa包","resource/realPackage.jsp"});
		al.add(new Object[]{63,1,"正式资源文件列表","resource/realResourceFileList.jsp"});
		al.add(new Object[]{65,1,"资源zip包列表","resource/testResourcePackageList.jsp"});
		al.add(new Object[]{66,1,"<font color=green>正式资源发布</font>","resource/releasePackageAndResource.jsp"});
		al.add(new Object[]{67,1,"客户端弹窗","tip/tip.jsp"});
		
		al.add(new Object[]{68,1,"<font color=red>服务器列表管理</font>","server/serversNew.jsp"});
		al.add(new Object[]{69,1,"服务器内部测试人员管理","innertester/innerTester.jsp"});
		al.add(new Object[]{70,1,"AppStore版本管理","server/appstore_version.jsp"});
		al.add(new Object[]{71,1,"GooglePlay版本管理","server/googleplay_version.jsp"});
		al.add(new Object[]{711,1,"needToHttp版本管理","server/needToHttp_version.jsp"});
		
		//al.add(new Object[]{72,1,"用户信息查询","server/playerClientSimpleInfo.jsp"});
		//al.add(new Object[]{73,1,"新用户信息查询","server/xiugaiUserClientMsg.jsp"});
		//al.add(new Object[]{80,1,"授权可能错误查询","server/xiugaiUserClientMsg_check.jsp"});
		//al.add(new Object[]{76,1,"授权信息查询","server/showUserClientMsg.jsp"});
		//al.add(new Object[]{74,1,"修改用户设备号","server/modifyPlayerDeviceCode.jsp"});
		//al.add(new Object[]{75,1,"AppStore比对","server/newAppstore_match.jsp"});
		//al.add(new Object[]{77,1,"AppStore数据","server/appstore_usermsg.jsp"});
		
		al.add(new Object[]{22,1,"zip缓存数据","resource/zip_resource_list.jsp"});

	    
		al.add(new Object[]{24,1,"玩家角色信息","server/playerinfo.jsp"});
		al.add(new Object[]{25,1,"黑名单管理","server/deny_users.jsp"});
		al.add(new Object[]{28,1,"黑名单查询","server/show_deny_users.jsp"});
		al.add(new Object[]{29,1,"外挂功能管理","server/setWaiGuaProcessNames.jsp"});
		al.add(new Object[]{26,1,"DiskCache信息","diskcache/showalldiskcache.jsp"});
	    
		al.add(new Object[]{27,1,"UC接口测试","server/uc_test.jsp"});
		
		al.add(new Object[]{5,0,"统计",""});
		al.add(new Object[]{506,5,"当日在线人数统计","stat/onlinenum_tracker.jsp"});
		al.add(new Object[]{506,5,"服务器心跳统计","stat/stat_heartbeat.jsp"});
		al.add(new Object[]{500,5,"客户端统计","stat/stat2.jsp"});
		al.add(new Object[]{501,5,"机型统计","stat/stat_phone.jsp"});
		al.add(new Object[]{502,5,"联网类型统计","stat/stat_wifi.jsp"});
		al.add(new Object[]{503,5,"资源更新统计","stat/stat_res.jsp"});
		al.add(new Object[]{504,5,"分渠道激活历史追踪","stat/stat_jihuo.jsp"});
		
		al.add(new Object[]{6,0,"资源包共享节点",""});
		al.add(new Object[]{510,6,"生成203链接","sharednode/packageAll.jsp"});
		al.add(new Object[]{511,6,"资源包共享节点","sharednode/shared_node.jsp"});
		al.add(new Object[]{512,6,"MiniZip包共享","server/miniZipData.jsp"});
		
		al.add(new Object[]{2,0,"激活码管理",""});
		al.add(new Object[]{100,2,"查询激活码","confirmationCode/new_ActivotyCodeCheck.jsp"});
		al.add(new Object[]{101,2,"激活码管理","confirmationCode/new_ActivityList4bangding.jsp"});
		al.add(new Object[]{102,2,"批量作废激活码","confirmationCode/batchReviseCode.jsp"});
		
		al.add(new Object[]{3,0,"权限管理",""});
		al.add(new Object[]{200,3,"平台列表","authorize/platforms.jsp"});
		al.add(new Object[]{201,3,"用户列表","authorize/users.jsp"});
		al.add(new Object[]{202,3,"权限列表","authorize/permissions.jsp"});
		
		
		al.add(new Object[]{204,3,"角色列表","authorize/roles.jsp"});
		al.add(new Object[]{205,3,"添加权限","authorize/addpermission.jsp"});
		al.add(new Object[]{206,3,"测试","authorize/test.jsp"});
		al.add(new Object[]{207,3,"GM用户列表","authorize/gmusers.jsp"});
		al.add(new Object[]{208,3,"白名单","critical/testuser.jsp"});
		al.add(new Object[]{4,0,"修改密码",""});
		al.add(new Object[]{300,4,"修改密码","authorize/modifypassword.jsp"});
		al.add(new Object[]{301,4,"退出","authorize/logout.jsp"});
		
		al.add(new Object[]{7,0,"公告相关",""});
		al.add(new Object[]{300,7,"活动公告","gm/gmActivityNotice.jsp"});
		al.add(new Object[]{301,7,"登录公告","gm/gmLoginNotice.jsp"});
		al.add(new Object[]{302,7,"系统公告","gm/gmSystemNotice.jsp"});
		al.add(new Object[]{303,7,"定时滚屏","gm/systemTimeNotice.jsp"});
		
		
		al.add(new Object[]{8,0,"发布相关",""});
		al.add(new Object[]{300,8,"发布服务器","http://119.254.154.201:12111/admin/serverpublish.jsp"});
		al.add(new Object[]{9,0,"充值相关",""});
		al.add(new Object[]{100,9,"充值查询","http://119.254.154.201:12111/gm/manager/orderlist.jsp"});
		
		String baseURL = request.getRequestURL().toString();
		int k = baseURL.lastIndexOf("/");
		if(k > 0){
			baseURL = baseURL.substring(0,k);
		}
		for(int i = 0 ; i < al.size() ; i++){
			
			Object os[] = al.get(i);
			
			if(os[3].equals("")){
				out.println("d.add("+os[0]+","+os[1]+",'"+os[2]+"','"+os[3]+"');");
			}else{
				String url = baseURL + "/" +os[3];
				com.xuanzhi.tools.authorize.User user = (com.xuanzhi.tools.authorize.User)session.getAttribute(AuthorizedServletFilter.USER);
				if(user != null){
					boolean canAccess = com.xuanzhi.tools.authorize.AuthorizeManager.getInstance().isPlatformAccessEnable(user,url,request.getRemoteAddr());
					if(canAccess){
						out.println("d.add("+os[0]+","+os[1]+",'"+os[2]+"','"+os[3]+"');");
					}
				}
			}
			
			
		}
		
%>
		
		document.write(d);

		//-->
	</SCRIPT>
<div></div></body></html>
