package com.fy.engineserver.message;

import com.xuanzhi.tools.transport.*;
import java.nio.ByteBuffer;



/**
 * 网络数据包，此数据包是由MessageComplier自动生成，请不要手动修改。<br>
 * 版本号：null<br>
 * 进行家族修炼<br>
 * 数据包的格式如下：<br><br>
 * <table border="0" cellpadding="0" cellspacing="1" width="100%" bgcolor="#000000" align="center">
 * <tr bgcolor="#00FFFF" align="center"><td>字段名</td><td>数据类型</td><td>长度（字节数）</td><td>说明</td></tr> * <tr bgcolor="#FFFFFF" align="center"><td>length</td><td>int</td><td>getNumOfByteForMessageLength()个字节</td><td>包的整体长度，包头的一部分</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>type</td><td>int</td><td>4个字节</td><td>包的类型，包头的一部分</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>seqNum</td><td>int</td><td>4个字节</td><td>包的序列号，包头的一部分</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>shangxiangId</td><td>int</td><td>4个字节</td><td>配置的长度</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>gongzi</td><td>long</td><td>8个字节</td><td>配置的长度</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>xiulianZhi</td><td>long</td><td>8个字节</td><td>配置的长度</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>maxShangxiangNums</td><td>int</td><td>4个字节</td><td>配置的长度</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>currentShangxiang</td><td>int</td><td>4个字节</td><td>配置的长度</td></tr>
 * </table>
 */
public class JIAZU_XIULIAN_RES implements ResponseMessage{

	static GameMessageFactory mf = GameMessageFactory.getInstance();

	long seqNum;
	int shangxiangId;
	long gongzi;
	long xiulianZhi;
	int maxShangxiangNums;
	int currentShangxiang;

	public JIAZU_XIULIAN_RES(){
	}

	public JIAZU_XIULIAN_RES(long seqNum,int shangxiangId,long gongzi,long xiulianZhi,int maxShangxiangNums,int currentShangxiang){
		this.seqNum = seqNum;
		this.shangxiangId = shangxiangId;
		this.gongzi = gongzi;
		this.xiulianZhi = xiulianZhi;
		this.maxShangxiangNums = maxShangxiangNums;
		this.currentShangxiang = currentShangxiang;
	}

	public JIAZU_XIULIAN_RES(long seqNum,byte[] content,int offset,int size) throws Exception{
		this.seqNum = seqNum;
		shangxiangId = (int)mf.byteArrayToNumber(content,offset,4);
		offset += 4;
		gongzi = (long)mf.byteArrayToNumber(content,offset,8);
		offset += 8;
		xiulianZhi = (long)mf.byteArrayToNumber(content,offset,8);
		offset += 8;
		maxShangxiangNums = (int)mf.byteArrayToNumber(content,offset,4);
		offset += 4;
		currentShangxiang = (int)mf.byteArrayToNumber(content,offset,4);
		offset += 4;
	}

	public int getType() {
		return 0x70FF0036;
	}

	public String getTypeDescription() {
		return "JIAZU_XIULIAN_RES";
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
		len += 8;
		len += 8;
		len += 4;
		len += 4;
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

			buffer.putInt(shangxiangId);
			buffer.putLong(gongzi);
			buffer.putLong(xiulianZhi);
			buffer.putInt(maxShangxiangNums);
			buffer.putInt(currentShangxiang);
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
	 *	对应id
	 */
	public int getShangxiangId(){
		return shangxiangId;
	}

	/**
	 * 设置属性：
	 *	对应id
	 */
	public void setShangxiangId(int shangxiangId){
		this.shangxiangId = shangxiangId;
	}

	/**
	 * 获取属性：
	 *	工资
	 */
	public long getGongzi(){
		return gongzi;
	}

	/**
	 * 设置属性：
	 *	工资
	 */
	public void setGongzi(long gongzi){
		this.gongzi = gongzi;
	}

	/**
	 * 获取属性：
	 *	修炼值 
	 */
	public long getXiulianZhi(){
		return xiulianZhi;
	}

	/**
	 * 设置属性：
	 *	修炼值 
	 */
	public void setXiulianZhi(long xiulianZhi){
		this.xiulianZhi = xiulianZhi;
	}

	/**
	 * 获取属性：
	 *	最多可上香次数 
	 */
	public int getMaxShangxiangNums(){
		return maxShangxiangNums;
	}

	/**
	 * 设置属性：
	 *	最多可上香次数 
	 */
	public void setMaxShangxiangNums(int maxShangxiangNums){
		this.maxShangxiangNums = maxShangxiangNums;
	}

	/**
	 * 获取属性：
	 *	已经上香次数
	 */
	public int getCurrentShangxiang(){
		return currentShangxiang;
	}

	/**
	 * 设置属性：
	 *	已经上香次数
	 */
	public void setCurrentShangxiang(int currentShangxiang){
		this.currentShangxiang = currentShangxiang;
	}

}