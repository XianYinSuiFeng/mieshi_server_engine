package com.fy.engineserver.message;

import com.xuanzhi.tools.transport.*;
import java.nio.ByteBuffer;



/**
 * 网络数据包，此数据包是由MessageComplier自动生成，请不要手动修改。<br>
 * 版本号：null<br>
 * 更改条件，增加物品到交易栏<br>
 * 数据包的格式如下：<br><br>
 * <table border="0" cellpadding="0" cellspacing="1" width="100%" bgcolor="#000000" align="center">
 * <tr bgcolor="#00FFFF" align="center"><td>字段名</td><td>数据类型</td><td>长度（字节数）</td><td>说明</td></tr> * <tr bgcolor="#FFFFFF" align="center"><td>length</td><td>int</td><td>getNumOfByteForMessageLength()个字节</td><td>包的整体长度，包头的一部分</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>type</td><td>int</td><td>4个字节</td><td>包的类型，包头的一部分</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>seqNum</td><td>int</td><td>4个字节</td><td>包的序列号，包头的一部分</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>packageType</td><td>byte</td><td>1个字节</td><td>配置的长度</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>index</td><td>int</td><td>4个字节</td><td>配置的长度</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>entityId</td><td>long</td><td>8个字节</td><td>配置的长度</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>count</td><td>int</td><td>4个字节</td><td>配置的长度</td></tr>
 * </table>
 */
public class DEAL_ADD_ARTICLE_REQ implements RequestMessage{

	static GameMessageFactory mf = GameMessageFactory.getInstance();

	long seqNum;
	byte packageType;
	int index;
	long entityId;
	int count;

	public DEAL_ADD_ARTICLE_REQ(){
	}

	public DEAL_ADD_ARTICLE_REQ(long seqNum,byte packageType,int index,long entityId,int count){
		this.seqNum = seqNum;
		this.packageType = packageType;
		this.index = index;
		this.entityId = entityId;
		this.count = count;
	}

	public DEAL_ADD_ARTICLE_REQ(long seqNum,byte[] content,int offset,int size) throws Exception{
		this.seqNum = seqNum;
		packageType = (byte)mf.byteArrayToNumber(content,offset,1);
		offset += 1;
		index = (int)mf.byteArrayToNumber(content,offset,4);
		offset += 4;
		entityId = (long)mf.byteArrayToNumber(content,offset,8);
		offset += 8;
		count = (int)mf.byteArrayToNumber(content,offset,4);
		offset += 4;
	}

	public int getType() {
		return 0x0000A003;
	}

	public String getTypeDescription() {
		return "DEAL_ADD_ARTICLE_REQ";
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
		len += 4;
		len += 8;
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

			buffer.put(packageType);
			buffer.putInt(index);
			buffer.putLong(entityId);
			buffer.putInt(count);
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
	 *	交易背包的类型
	 */
	public byte getPackageType(){
		return packageType;
	}

	/**
	 * 设置属性：
	 *	交易背包的类型
	 */
	public void setPackageType(byte packageType){
		this.packageType = packageType;
	}

	/**
	 * 获取属性：
	 *	交易背包的格子下标
	 */
	public int getIndex(){
		return index;
	}

	/**
	 * 设置属性：
	 *	交易背包的格子下标
	 */
	public void setIndex(int index){
		this.index = index;
	}

	/**
	 * 获取属性：
	 *	交易物品
	 */
	public long getEntityId(){
		return entityId;
	}

	/**
	 * 设置属性：
	 *	交易物品
	 */
	public void setEntityId(long entityId){
		this.entityId = entityId;
	}

	/**
	 * 获取属性：
	 *	交易物品的数量
	 */
	public int getCount(){
		return count;
	}

	/**
	 * 设置属性：
	 *	交易物品的数量
	 */
	public void setCount(int count){
		this.count = count;
	}

}