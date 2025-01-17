package com.fy.boss.platform.huawei;

import java.util.Arrays;

import com.fy.boss.authorize.model.Passport;
import com.fy.boss.finance.model.OrderForm;
import com.fy.boss.finance.service.OrderFormManager;
import com.fy.boss.finance.service.PlatformSavingCenter;
import com.fy.boss.platform.huawei.HuaweiYeepaySavingManager;
import com.xuanzhi.tools.text.DateUtil;

public class HuaweiYeepaySavingManager {
	protected static HuaweiYeepaySavingManager m_self = null;
	
	public void initialize() throws Exception{
		m_self = this;
		System.out.println("["+this.getClass().getName()+"] [initialized]");
		PlatformSavingCenter.logger.info("["+this.getClass().getName()+"] [initialized]");
	}
	

	public String cardSaving(Passport passport, String cardtype, int mianzhi, String cardno, String cardpass, String otherDesp,
			 String server, String channel, String orderID) {
		
		long now = System.currentTimeMillis();
		
		OrderFormManager orderFormManager = OrderFormManager.getInstance();

    	//先生成一个订单
    	java.util.Date cdate = new java.util.Date();
    	OrderForm order = new OrderForm();
    	order.setUserChannel(channel);
    	order.setServerName(server);
    	order.setCreateTime(now);
    	order.setPassportId(passport.getId());
    	order.setMemo1(otherDesp);
    	order.setSavingPlatform("华为SDK");
    	order.setSavingMedium(cardtype);
		order.setHandleResult(OrderForm.HANDLE_RESULT_NOBACK);
    	order.setMediumInfo("华为SDK充值");
    	
    	orderFormManager.createOrderForm(order);
    	long id = order.getId();
    	String orderId = "hw"+DateUtil.formatDate(cdate,"yyyyMMdd") + "-" + id;
    	order.setOrderId(orderId);
    	try {
			orderFormManager.update(order);
			if(PlatformSavingCenter.logger.isInfoEnabled())
				PlatformSavingCenter.logger.info("[华为SDK充值调用] [成功] [OK] [账号:"+passport.getUserName()+"] [orderId:"+order.getOrderId()+"] [username:"+passport.getUserName()+"] [channel:"+channel+"] [server:"+server+"] ["+(System.currentTimeMillis()-now)+"ms]");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			PlatformSavingCenter.logger.warn("[华为SDK充值调用] [生成订单失败] [更新订单失败] [账号:"+passport.getUserName()+"] [orderId:"+order.getOrderId()+"] [username:"+passport.getUserName()+"] [channel:"+channel+"] [server:"+server+"] ["+(System.currentTimeMillis()-now)+"ms]",e);
		}
    	//调用扣费接口
		
    	return orderId;
    }
	
	public String cardSaving(Passport passport, String cardtype, int mianzhi, String cardno, String cardpass, String otherDesp,
			 String server, String channel, String orderID,String[]others) {
		
		long now = System.currentTimeMillis();
		
		OrderFormManager orderFormManager = OrderFormManager.getInstance();

   	//先生成一个订单
   	java.util.Date cdate = new java.util.Date();
   	OrderForm order = new OrderForm();
   	order.setUserChannel(channel);
   	order.setServerName(server);
   	order.setCreateTime(now);
   	order.setPassportId(passport.getId());
   	order.setMemo1(others[0]);
   	order.setSavingPlatform("华为SDK");
   	order.setSavingMedium(cardtype);
	order.setHandleResult(OrderForm.HANDLE_RESULT_NOBACK);
   	order.setMediumInfo("华为SDK充值");
	if(others.length > 1){
		order.setChargeValue(others[1]);
	}
	if(others.length > 2){
		order.setChargeType(others[2]);
	}
   	orderFormManager.createOrderForm(order);
   	long id = order.getId();
   	String orderId = "hw"+DateUtil.formatDate(cdate,"yyyyMMdd") + "-" + id;
   	order.setOrderId(orderId);
   	try {
			orderFormManager.update(order);
			if(PlatformSavingCenter.logger.isInfoEnabled())
				PlatformSavingCenter.logger.info("[华为SDK充值调用] [成功] [OK] [账号:"+passport.getUserName()+"] [orderId:"+order.getOrderId()+"] [username:"+passport.getUserName()+"] [channel:"+channel+"] [server:"+server+"] [角色id:"+others[0]+"] [others:"+(others==null?"nul":Arrays.toString(others))+"] ["+(System.currentTimeMillis()-now)+"ms]");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			PlatformSavingCenter.logger.warn("[华为SDK充值调用] [生成订单失败] [更新订单失败] [账号:"+passport.getUserName()+"] [orderId:"+order.getOrderId()+"] [username:"+passport.getUserName()+"] [channel:"+channel+"] [server:"+server+"] [角色id:"+others[0]+"] [others:"+(others==null?"nul":Arrays.toString(others))+"] ["+(System.currentTimeMillis()-now)+"ms]",e);
		}
   	//调用扣费接口
		
   	return orderId;
   }
	
	
    public static HuaweiYeepaySavingManager getInstance() {
    	if(m_self == null)
    	{
    		m_self = new HuaweiYeepaySavingManager();
    	}
		return m_self;
	}
    

}
