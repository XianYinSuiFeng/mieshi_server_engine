package com.fy.engineserver.message;

import com.xuanzhi.tools.transport.*;
import java.nio.ByteBuffer;



/**
 * 网络数据包，此数据包是由MessageComplier自动生成，请不要手动修改。<br>
 * 版本号：null<br>
 * 服务器通知客户端，精灵立刻移动到目的地；或者玩家中止移动时发送此指令给服务器<br>
 * 数据包的格式如下：<br><br>
 * <table border="0" cellpadding="0" cellspacing="1" width="100%" bgcolor="#000000" align="center">
 * <tr bgcolor="#00FFFF" align="center"><td>字段名</td><td>数据类型</td><td>长度（字节数）</td><td>说明</td></tr> * <tr bgcolor="#FFFFFF" align="center"><td>length</td><td>int</td><td>getNumOfByteForMessageLength()个字节</td><td>包的整体长度，包头的一部分</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>type</td><td>int</td><td>4个字节</td><td>包的类型，包头的一部分</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>seqNum</td><td>int</td><td>4个字节</td><td>包的序列号，包头的一部分</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>reason</td><td>byte</td><td>1个字节</td><td>配置的长度</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>objectType</td><td>byte</td><td>1个字节</td><td>配置的长度</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>objectId</td><td>long</td><td>8个字节</td><td>配置的长度</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>x</td><td>short</td><td>2个字节</td><td>配置的长度</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>y</td><td>short</td><td>2个字节</td><td>配置的长度</td></tr>
 * </table>
 */
public class SET_POSITION_REQ implements RequestMessage{

	static GameMessageFactory mf = GameMessageFactory.getInstance();

	long seqNum;
	byte reason;
	byte objectType;
	long objectId;
	short x;
	short y;

	public SET_POSITION_REQ(){
	}

	public SET_POSITION_REQ(long seqNum,byte reason,byte objectType,long objectId,short x,short y){
		this.seqNum = seqNum;
		this.reason = reason;
		this.objectType = objectType;
		this.objectId = objectId;
		this.x = x;
		this.y = y;
	}

	public SET_POSITION_REQ(long seqNum,byte[] content,int offset,int size) throws Exception{
		this.seqNum = seqNum;
		reason = (byte)mf.byteArrayToNumber(content,offset,1);
		offset += 1;
		objectType = (byte)mf.byteArrayToNumber(content,offset,1);
		offset += 1;
		objectId = (long)mf.byteArrayToNumber(content,offset,8);
		offset += 8;
		x = (short)mf.byteArrayToNumber(content,offset,2);
		offset += 2;
		y = (short)mf.byteArrayToNumber(content,offset,2);
		offset += 2;
	}

	public int getType() {
		return 0x000000C2;
	}

	public String getTypeDescription() {
		return "SET_POSITION_REQ";
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
		len += 1;
		len += 1;
		len += 8;
		len += 2;
		len += 2;
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

			buffer.put(reason);
			buffer.put(objectType);
			buffer.putLong(objectId);
			buffer.putShort(x);
			buffer.putShort(y);
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
	 *	移动的原因
	 */
	public byte getReason(){
		return reason;
	}

	/**
	 * 设置属性：
	 *	移动的原因
	 */
	public void setReason(byte reason){
		this.reason = reason;
	}

	/**
	 * 获取属性：
	 *	移动生物的物种
	 */
	public byte getObjectType(){
		return objectType;
	}

	/**
	 * 设置属性：
	 *	移动生物的物种
	 */
	public void setObjectType(byte objectType){
		this.objectType = objectType;
	}

	/**
	 * 获取属性：
	 *	移动生物的编号
	 */
	public long getObjectId(){
		return objectId;
	}

	/**
	 * 设置属性：
	 *	移动生物的编号
	 */
	public void setObjectId(long objectId){
		this.objectId = objectId;
	}

	/**
	 * 获取属性：
	 *	目的地x坐标
	 */
	public short getX(){
		return x;
	}

	/**
	 * 设置属性：
	 *	目的地x坐标
	 */
	public void setX(short x){
		this.x = x;
	}

	/**
	 * 获取属性：
	 *	目的地y坐标
	 */
	public short getY(){
		return y;
	}

	/**
	 * 设置属性：
	 *	目的地y坐标
	 */
	public void setY(short y){
		this.y = y;
	}

}