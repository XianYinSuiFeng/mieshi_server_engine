package com.fy.engineserver.message;

import com.xuanzhi.tools.transport.*;
import java.nio.ByteBuffer;



/**
 * 网络数据包，此数据包是由MessageComplier自动生成，请不要手动修改。<br>
 * 版本号：null<br>
 * <br>
 * 数据包的格式如下：<br><br>
 * <table border="0" cellpadding="0" cellspacing="1" width="100%" bgcolor="#000000" align="center">
 * <tr bgcolor="#00FFFF" align="center"><td>字段名</td><td>数据类型</td><td>长度（字节数）</td><td>说明</td></tr> * <tr bgcolor="#FFFFFF" align="center"><td>length</td><td>int</td><td>getNumOfByteForMessageLength()个字节</td><td>包的整体长度，包头的一部分</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>type</td><td>int</td><td>4个字节</td><td>包的类型，包头的一部分</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>seqNum</td><td>int</td><td>4个字节</td><td>包的序列号，包头的一部分</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>index</td><td>int</td><td>4个字节</td><td>配置的长度</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>point</td><td>int</td><td>4个字节</td><td>配置的长度</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>pointLeft</td><td>int</td><td>4个字节</td><td>配置的长度</td></tr>
 * </table>
 */
public class SkEnh_Add_point_RES implements ResponseMessage{

	static GameMessageFactory mf = GameMessageFactory.getInstance();

	long seqNum;
	int index;
	int point;
	int pointLeft;

	public SkEnh_Add_point_RES(){
	}

	public SkEnh_Add_point_RES(long seqNum,int index,int point,int pointLeft){
		this.seqNum = seqNum;
		this.index = index;
		this.point = point;
		this.pointLeft = pointLeft;
	}

	public SkEnh_Add_point_RES(long seqNum,byte[] content,int offset,int size) throws Exception{
		this.seqNum = seqNum;
		index = (int)mf.byteArrayToNumber(content,offset,4);
		offset += 4;
		point = (int)mf.byteArrayToNumber(content,offset,4);
		offset += 4;
		pointLeft = (int)mf.byteArrayToNumber(content,offset,4);
		offset += 4;
	}

	public int getType() {
		return 0x8E0EAA71;
	}

	public String getTypeDescription() {
		return "SkEnh_Add_point_RES";
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

			buffer.putInt(index);
			buffer.putInt(point);
			buffer.putInt(pointLeft);
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
	 *	技能下标
	 */
	public int getIndex(){
		return index;
	}

	/**
	 * 设置属性：
	 *	技能下标
	 */
	public void setIndex(int index){
		this.index = index;
	}

	/**
	 * 获取属性：
	 *	加完后的点数
	 */
	public int getPoint(){
		return point;
	}

	/**
	 * 设置属性：
	 *	加完后的点数
	 */
	public void setPoint(int point){
		this.point = point;
	}

	/**
	 * 获取属性：
	 *	剩余点数
	 */
	public int getPointLeft(){
		return pointLeft;
	}

	/**
	 * 设置属性：
	 *	剩余点数
	 */
	public void setPointLeft(int pointLeft){
		this.pointLeft = pointLeft;
	}

}