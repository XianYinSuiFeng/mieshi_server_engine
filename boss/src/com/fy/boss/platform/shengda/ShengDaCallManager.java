package com.fy.boss.platform.shengda;

import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fy.boss.authorize.model.Passport;
import com.fy.boss.finance.model.OrderForm;
import com.fy.boss.finance.service.PlatformSavingCenter;
import com.fy.boss.tools.JacksonManager;
import com.fy.boss.platform.shengda.ShengDaCallManager;
import com.xuanzhi.tools.servlet.HttpUtils;
import com.xuanzhi.tools.text.DateUtil;
import com.xuanzhi.tools.text.StringUtil;

public class ShengDaCallManager {
	static ShengDaCallManager self;
	private static String KEY = "fjaw#$*eqjjidw";
	
	private String statUrl = "http://pay.wan.sdo.com/mobile";


	public void initialize() {
		ShengDaCallManager.self = this;
		System.out.println("[" + this.getClass().getName() + "] [initialized]");
	}


	public String cardSaving(Passport passport, OrderForm order) {
		try
		{
			long startTime = System.currentTimeMillis();

			String cardtype = "";

			//pay_type	是	充值类型:1为支付宝 2为易宝 3为神州付
			if(order.getSavingPlatform().indexOf("支付宝") >= 0)
			{
				cardtype = "1";
			}
			else if(order.getSavingPlatform().indexOf("易宝") >= 0)
			{
				cardtype = "2";
			}
			else if(order.getSavingPlatform().indexOf("神州付") >= 0)
			{
				cardtype = "3";
			}
			else
			{
				//未知支付方式
				cardtype = "999999";
			}


			/**
			 * 参数名	是否必选	含义
client_id	是	商户ID,由盛大分配
appid	是	游戏ID,飘渺寻仙曲的appid为1
areaname	是	区服名称
orderno	是	充值订单号
userid	是	用户唯一标识号.
username	是	用户名称(一般为登录名).
rechargefee	是	充值的金额,单位为元.注意:1.1不能写为1.10
qdid	是	渠道ID,此参数由下载的app应用传递过来,此参数为游戏方分配
pay_type	是	充值类型:1为支付宝 2为易宝 3为神州付
recharge_time	是	充值时间,格式为:2012-12-11 09:09:09
signature	是	签名验证字符串,生成规则参考签名算法
ext1	否	预留参数,可不传该参数,但如果传入该参数,必须将该参数参与签名计算
ext2	否	预留参数,可不传该参数,但如果传入该参数,必须将该参数参与签名计算
			 */
			// 调用盛大的充值接口 统计充值接口
			LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();

			params.put("client_id", "wangxian");
			params.put("appid", "1");
			params.put("areaname", order.getServerName());
			params.put("orderno", order.getOrderId());
			params.put("userid", passport.getId()+""); 
			params.put("username", passport.getUserName());
			params.put("rechargefee", String.valueOf(order.getPayMoney() / 100));
			params.put("qdid", order.getUserChannel());
			params.put("pay_type", cardtype);
			params.put("recharge_time", DateUtil.formatDate(new Date(order.getCreateTime()), "yyyy-MM-dd HH:mm:ss"));
			params.put("signature", sign(params));
			params.put("areaname", URLEncoder.encode(order.getServerName(), "utf-8"));

			// 拼请求查询字符串
			StringBuffer strbuf = new StringBuffer();
			Iterator<String> it = params.keySet().iterator();
			int i = 0;
			while (it.hasNext()) {
				String key = it.next();
				if (i == 0) {
					strbuf.append(key);
					strbuf.append("=");
					strbuf.append(params.get(key));
				} else {
					strbuf.append("&");
					strbuf.append(key);
					strbuf.append("=");
					strbuf.append(params.get(key));
				}
				i++;
			}

			// 拼接url和查询字符串

			String urlStr = statUrl + "?" + strbuf.toString();
			URL url = null;
			try {
				url = new URL(urlStr);
			} catch (Exception e) {
				PlatformSavingCenter.logger.error("[调用盛大统计接口] [失败] [生成url出现异常] ["+urlStr+"] ["+cardtype+"("+order.getSavingPlatform()+"::"+order.getSavingMedium()+")"+"] ["+order.getPayMoney()+"分] ["+params+"] ["+order.getUserChannel()+"] ["+passport.getUserName()+"] ["+passport.getId()+"] ["+KEY+"] [--] [--] [--] [--] ["+(System.currentTimeMillis()-startTime)+"ms]",e);
				return null;
			}
			Map headers = new HashMap();
			String content = "";
			try {
				byte bytes[] = HttpUtils.webPost(url, content.getBytes(), headers, 60000, 60000);
				String encoding = (String) headers.get(HttpUtils.ENCODING);
				Integer code = (Integer) headers.get(HttpUtils.Response_Code);
				String message = (String) headers.get(HttpUtils.Response_Message);

				content = new String(bytes, encoding);
				// 解释响应结果
				String returnValue = "ok";
				JacksonManager jm = JacksonManager.getInstance();
				Map dataMap = (Map)jm.jsonDecodeObject(content);
				//{"ret_code":0,"ret_message":"success","data":""}
				int status = ((Number)dataMap.get("ret_code")).intValue();
				
				if(status == 0)
				{
					if(PlatformSavingCenter.logger.isInfoEnabled())
					{
						PlatformSavingCenter.logger.info("[调用盛大统计接口] [成功] [ok] ["+urlStr+"] ["+cardtype+"("+order.getSavingPlatform()+"::"+order.getSavingMedium()+")"+"] ["+order.getPayMoney()+"分] ["+params+"] ["+order.getUserChannel()+"] ["+passport.getUserName()+"] ["+passport.getId()+"] ["+KEY+"] ["+encoding+"] ["+code+"] ["+message+"] ["+content+"] ["+(System.currentTimeMillis()-startTime)+"ms]");
					}
				}
				else
				{
					returnValue = "fail";
					if(PlatformSavingCenter.logger.isInfoEnabled())
					{
						PlatformSavingCenter.logger.info("[调用盛大统计接口] [失败] [接口返回失败码] ["+urlStr+"] ["+cardtype+"("+order.getSavingPlatform()+"::"+order.getSavingMedium()+")"+"] ["+order.getPayMoney()+"分] ["+params+"] ["+order.getUserChannel()+"] ["+passport.getUserName()+"] ["+passport.getId()+"] ["+KEY+"] ["+encoding+"] ["+code+"] ["+message+"] ["+content+"] ["+(System.currentTimeMillis()-startTime)+"ms]");
					}
				}
				
				return returnValue;

			} catch (Exception e) {
				PlatformSavingCenter.logger.error("[调用盛大统计接口] [失败] [调用接口出现异常] ["+urlStr+"] ["+cardtype+"("+order.getSavingPlatform()+"::"+order.getSavingMedium()+")"+"] ["+order.getPayMoney()+"分] ["+params+"] ["+order.getUserChannel()+"] ["+passport.getUserName()+"] ["+passport.getId()+"] ["+KEY+"] [--] [--] [--] ["+content+"] ["+(System.currentTimeMillis()-startTime)+"ms]",e);
				return null;
			}
		}
		catch(Exception e)
		{
			PlatformSavingCenter.logger.error("[调用盛大统计接口] [失败] [方法体内部出现异常] ["+statUrl+"] ["+"--"+"("+order.getSavingPlatform()+"::"+order.getSavingMedium()+")"+"] ["+order.getPayMoney()+"分] [--] ["+order.getUserChannel()+"] ["+passport.getUserName()+"] ["+passport.getId()+"] ["+KEY+"] [--] [--] [--] [--] [--ms]",e);
			return null;
		}

	}
	
	private String sign(HashMap<String,String> params){
		String keys[] = params.keySet().toArray(new String[0]);
		java.util.Arrays.sort(keys);
		StringBuffer sb = new StringBuffer();
		for(int i = 0 ; i < keys.length ; i++){
			String v = params.get(keys[i]);
			sb.append(keys[i]+"="+v);
		}
		String md5Str = sb.toString()+KEY;
		
		String sign = StringUtil.hash(md5Str);
		
		return sign;
	}

	public static ShengDaCallManager getInstance() {
		if(self == null)
		{
			self = new ShengDaCallManager();
			
		}
		return self;
	}
}