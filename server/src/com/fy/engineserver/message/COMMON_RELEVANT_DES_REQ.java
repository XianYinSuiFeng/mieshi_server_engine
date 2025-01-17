package com.fy.engineserver.message;

import com.xuanzhi.tools.transport.*;
import java.nio.ByteBuffer;



/**
 * 网络数据包，此数据包是由MessageComplier自动生成，请不要手动修改。<br>
 * 版本号：null<br>
 * 请求家族界面相关描述<br>
 * 数据包的格式如下：<br><br>
 * <table border="0" cellpadding="0" cellspacing="1" width="100%" bgcolor="#000000" align="center">
 * <tr bgcolor="#00FFFF" align="center"><td>字段名</td><td>数据类型</td><td>长度（字节数）</td><td>说明</td></tr> * <tr bgcolor="#FFFFFF" align="center"><td>length</td><td>int</td><td>getNumOfByteForMessageLength()个字节</td><td>包的整体长度，包头的一部分</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>type</td><td>int</td><td>4个字节</td><td>包的类型，包头的一部分</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>seqNum</td><td>int</td><td>4个字节</td><td>包的序列号，包头的一部分</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>windowId</td><td>int</td><td>4个字节</td><td>配置的长度</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>windowName.length</td><td>short</td><td>2个字节</td><td>字符串实际长度</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>windowName</td><td>String</td><td>windowName.length</td><td>字符串对应的byte数组</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>btnName.length</td><td>short</td><td>2个字节</td><td>字符串实际长度</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>btnName</td><td>String</td><td>btnName.length</td><td>字符串对应的byte数组</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>targetName.length</td><td>short</td><td>2个字节</td><td>字符串实际长度</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>targetName</td><td>String</td><td>targetName.length</td><td>字符串对应的byte数组</td></tr>
 * </table>
 */
public class COMMON_RELEVANT_DES_REQ implements RequestMessage{

	static GameMessageFactory mf = GameMessageFactory.getInstance();

	long seqNum;
	int windowId;
	String windowName;
	String btnName;
	String targetName;

	public COMMON_RELEVANT_DES_REQ(){
	}

	public COMMON_RELEVANT_DES_REQ(long seqNum,int windowId,String windowName,String btnName,String targetName){
		this.seqNum = seqNum;
		this.windowId = windowId;
		this.windowName = windowName;
		this.btnName = btnName;
		this.targetName = targetName;
	}

	public COMMON_RELEVANT_DES_REQ(long seqNum,byte[] content,int offset,int size) throws Exception{
		this.seqNum = seqNum;
		windowId = (int)mf.byteArrayToNumber(content,offset,4);
		offset += 4;
		int len = 0;
		len = (int)mf.byteArrayToNumber(content,offset,2);
		offset += 2;
		if(len < 0 || len > 16384) throw new Exception("string length ["+len+"] big than the max length [16384]");
		windowName = new String(content,offset,len);
		offset += len;
		len = (int)mf.byteArrayToNumber(content,offset,2);
		offset += 2;
		if(len < 0 || len > 16384) throw new Exception("string length ["+len+"] big than the max length [16384]");
		btnName = new String(content,offset,len);
		offset += len;
		len = (int)mf.byteArrayToNumber(content,offset,2);
		offset += 2;
		if(len < 0 || len > 16384) throw new Exception("string length ["+len+"] big than the max length [16384]");
		targetName = new String(content,offset,len);
		offset += len;
	}

	public int getType() {
		return 0x00FF0064;
	}

	public String getTypeDescription() {
		return "COMMON_RELEVANT_DES_REQ";
	}

	public String getSequenceNumAsString() {
		return String.valueOf(seqNum);
	}

	public long getSequnceNum(){
		return seqNum;
	}

	private int packet_length = 0;

	public int getLength() {
		if(packet_length > 0) return packet_length;
		int len =  mf.getNumOfByteForMessageLength() + 4 + 4;
		len += 4;
		len += 2;
		len +=windowName.getBytes().length;
		len += 2;
		len +=btnName.getBytes().length;
		len += 2;
		len +=targetName.getBytes().length;
		packet_length = len;
		return len;
	}

	public int writeTo(ByteBuffer buffer) {
		int messageLength = getLength();
		if(buffer.remaining() < messageLength) return 0;
		int oldPos = buffer.position();
		buffer.mark();
		try{
			buffer.put(mf.numberToByteArray(messageLength,mf.getNumOfByteForMessageLength()));
			buffer.putInt(getType());
			buffer.putInt((int)seqNum);

			buffer.putInt(windowId);
			byte[] tmpBytes1;
			tmpBytes1 = windowName.getBytes();
			buffer.putShort((short)tmpBytes1.length);
			buffer.put(tmpBytes1);
			tmpBytes1 = btnName.getBytes();
			buffer.putShort((short)tmpBytes1.length);
			buffer.put(tmpBytes1);
			tmpBytes1 = targetName.getBytes();
			buffer.putShort((short)tmpBytes1.length);
			buffer.put(tmpBytes1);
		}catch(Exception e){
		 e.printStackTrace();
			buffer.reset();
			throw new RuntimeException("in writeTo method catch exception :",e);
		}
		int newPos = buffer.position();
		buffer.position(oldPos);
		buffer.put(mf.numberToByteArray(newPos-oldPos,mf.getNumOfByteForMessageLength()));
		buffer.position(newPos);
		return newPos-oldPos;
	}

	/**
	 * 获取属性：
	 *	窗口id，用以区分  1:家族面板
	 */
	public int getWindowId(){
		return windowId;
	}

	/**
	 * 设置属性：
	 *	窗口id，用以区分  1:家族面板
	 */
	public void setWindowId(int windowId){
		this.windowId = windowId;
	}

	/**
	 * 获取属性：
	 *	窗口名
	 */
	public String getWindowName(){
		return windowName;
	}

	/**
	 * 设置属性：
	 *	窗口名
	 */
	public void setWindowName(String windowName){
		this.windowName = windowName;
	}

	/**
	 * 获取属性：
	 *	控件名
	 */
	public String getBtnName(){
		return btnName;
	}

	/**
	 * 设置属性：
	 *	控件名
	 */
	public void setBtnName(String btnName){
		this.btnName = btnName;
	}

	/**
	 * 获取属性：
	 *	按钮名
	 */
	public String getTargetName(){
		return targetName;
	}

	/**
	 * 设置属性：
	 *	按钮名
	 */
	public void setTargetName(String targetName){
		this.targetName = targetName;
	}

}