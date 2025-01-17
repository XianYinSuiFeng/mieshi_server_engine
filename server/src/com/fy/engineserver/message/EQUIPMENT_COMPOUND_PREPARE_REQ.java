package com.fy.engineserver.message;

import com.xuanzhi.tools.transport.*;
import java.nio.ByteBuffer;



/**
 * 网络数据包，此数据包是由MessageComplier自动生成，请不要手动修改。<br>
 * 版本号：null<br>
 * 服务器通知客户端，准备装备的合成<br>
 * 数据包的格式如下：<br><br>
 * <table border="0" cellpadding="0" cellspacing="1" width="100%" bgcolor="#000000" align="center">
 * <tr bgcolor="#00FFFF" align="center"><td>字段名</td><td>数据类型</td><td>长度（字节数）</td><td>说明</td></tr> * <tr bgcolor="#FFFFFF" align="center"><td>length</td><td>int</td><td>getNumOfByteForMessageLength()个字节</td><td>包的整体长度，包头的一部分</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>type</td><td>int</td><td>4个字节</td><td>包的类型，包头的一部分</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>seqNum</td><td>int</td><td>4个字节</td><td>包的序列号，包头的一部分</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>fomulaId</td><td>long</td><td>8个字节</td><td>配置的长度</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>descriptionInUUB.length</td><td>short</td><td>2个字节</td><td>字符串实际长度</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>descriptionInUUB</td><td>String</td><td>descriptionInUUB.length</td><td>字符串对应的byte数组</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>needArticleIds.length</td><td>int</td><td>4</td><td>数组长度</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>needArticleIds</td><td>long[]</td><td>needArticleIds.length</td><td>*</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>needArticleCounts.length</td><td>int</td><td>4</td><td>数组长度</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>needArticleCounts</td><td>int[]</td><td>needArticleCounts.length</td><td>*</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>hasArticleCounts.length</td><td>int</td><td>4</td><td>数组长度</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>hasArticleCounts</td><td>int[]</td><td>hasArticleCounts.length</td><td>*</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>price</td><td>int</td><td>4个字节</td><td>配置的长度</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>compound</td><td>boolean</td><td>1个字节</td><td>布尔型长度,0==false，其他为true</td></tr>
 * </table>
 */
public class EQUIPMENT_COMPOUND_PREPARE_REQ implements RequestMessage{

	static GameMessageFactory mf = GameMessageFactory.getInstance();

	long seqNum;
	long fomulaId;
	String descriptionInUUB;
	long[] needArticleIds;
	int[] needArticleCounts;
	int[] hasArticleCounts;
	int price;
	boolean compound;

	public EQUIPMENT_COMPOUND_PREPARE_REQ(){
	}

	public EQUIPMENT_COMPOUND_PREPARE_REQ(long seqNum,long fomulaId,String descriptionInUUB,long[] needArticleIds,int[] needArticleCounts,int[] hasArticleCounts,int price,boolean compound){
		this.seqNum = seqNum;
		this.fomulaId = fomulaId;
		this.descriptionInUUB = descriptionInUUB;
		this.needArticleIds = needArticleIds;
		this.needArticleCounts = needArticleCounts;
		this.hasArticleCounts = hasArticleCounts;
		this.price = price;
		this.compound = compound;
	}

	public EQUIPMENT_COMPOUND_PREPARE_REQ(long seqNum,byte[] content,int offset,int size) throws Exception{
		this.seqNum = seqNum;
		fomulaId = (long)mf.byteArrayToNumber(content,offset,8);
		offset += 8;
		int len = 0;
		len = (int)mf.byteArrayToNumber(content,offset,2);
		offset += 2;
		if(len < 0 || len > 16384) throw new Exception("string length ["+len+"] big than the max length [16384]");
		descriptionInUUB = new String(content,offset,len,"UTF-8");
		offset += len;
		len = (int)mf.byteArrayToNumber(content,offset,4);
		offset += 4;
		if(len < 0 || len > 4096) throw new Exception("array length ["+len+"] big than the max length [4096]");
		needArticleIds = new long[len];
		for(int i = 0 ; i < needArticleIds.length ; i++){
			needArticleIds[i] = (long)mf.byteArrayToNumber(content,offset,8);
			offset += 8;
		}
		len = (int)mf.byteArrayToNumber(content,offset,4);
		offset += 4;
		if(len < 0 || len > 4096) throw new Exception("array length ["+len+"] big than the max length [4096]");
		needArticleCounts = new int[len];
		for(int i = 0 ; i < needArticleCounts.length ; i++){
			needArticleCounts[i] = (int)mf.byteArrayToNumber(content,offset,4);
			offset += 4;
		}
		len = (int)mf.byteArrayToNumber(content,offset,4);
		offset += 4;
		if(len < 0 || len > 4096) throw new Exception("array length ["+len+"] big than the max length [4096]");
		hasArticleCounts = new int[len];
		for(int i = 0 ; i < hasArticleCounts.length ; i++){
			hasArticleCounts[i] = (int)mf.byteArrayToNumber(content,offset,4);
			offset += 4;
		}
		price = (int)mf.byteArrayToNumber(content,offset,4);
		offset += 4;
		compound = mf.byteArrayToNumber(content,offset,1) != 0;
		offset += 1;
	}

	public int getType() {
		return 0x00F1EEF0;
	}

	public String getTypeDescription() {
		return "EQUIPMENT_COMPOUND_PREPARE_REQ";
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
		len += 8;
		len += 2;
		try{
			len +=descriptionInUUB.getBytes("UTF-8").length;
		}catch(java.io.UnsupportedEncodingException e){
		 e.printStackTrace();
			throw new RuntimeException("unsupported encoding [UTF-8]",e);
		}
		len += 4;
		len += needArticleIds.length * 8;
		len += 4;
		len += needArticleCounts.length * 4;
		len += 4;
		len += hasArticleCounts.length * 4;
		len += 4;
		len += 1;
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

			buffer.putLong(fomulaId);
			byte[] tmpBytes1;
				try{
			 tmpBytes1 = descriptionInUUB.getBytes("UTF-8");
				}catch(java.io.UnsupportedEncodingException e){
			 e.printStackTrace();
					throw new RuntimeException("unsupported encoding [UTF-8]",e);
				}
			buffer.putShort((short)tmpBytes1.length);
			buffer.put(tmpBytes1);
			buffer.putInt(needArticleIds.length);
			for(int i = 0 ; i < needArticleIds.length; i++){
				buffer.putLong(needArticleIds[i]);
			}
			buffer.putInt(needArticleCounts.length);
			for(int i = 0 ; i < needArticleCounts.length; i++){
				buffer.putInt(needArticleCounts[i]);
			}
			buffer.putInt(hasArticleCounts.length);
			for(int i = 0 ; i < hasArticleCounts.length; i++){
				buffer.putInt(hasArticleCounts[i]);
			}
			buffer.putInt(price);
			buffer.put((byte)(compound==false?0:1));
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
	 *	合成图纸的Id，此图纸必在玩家的背包中
	 */
	public long getFomulaId(){
		return fomulaId;
	}

	/**
	 * 设置属性：
	 *	合成图纸的Id，此图纸必在玩家的背包中
	 */
	public void setFomulaId(long fomulaId){
		this.fomulaId = fomulaId;
	}

	/**
	 * 获取属性：
	 *	合成装备的描述
	 */
	public String getDescriptionInUUB(){
		return descriptionInUUB;
	}

	/**
	 * 设置属性：
	 *	合成装备的描述
	 */
	public void setDescriptionInUUB(String descriptionInUUB){
		this.descriptionInUUB = descriptionInUUB;
	}

	/**
	 * 获取属性：
	 *	需要的各个材料Id
	 */
	public long[] getNeedArticleIds(){
		return needArticleIds;
	}

	/**
	 * 设置属性：
	 *	需要的各个材料Id
	 */
	public void setNeedArticleIds(long[] needArticleIds){
		this.needArticleIds = needArticleIds;
	}

	/**
	 * 获取属性：
	 *	需要的各个材料的个数
	 */
	public int[] getNeedArticleCounts(){
		return needArticleCounts;
	}

	/**
	 * 设置属性：
	 *	需要的各个材料的个数
	 */
	public void setNeedArticleCounts(int[] needArticleCounts){
		this.needArticleCounts = needArticleCounts;
	}

	/**
	 * 获取属性：
	 *	玩家已经拥有需要的各个材料的个数
	 */
	public int[] getHasArticleCounts(){
		return hasArticleCounts;
	}

	/**
	 * 设置属性：
	 *	玩家已经拥有需要的各个材料的个数
	 */
	public void setHasArticleCounts(int[] hasArticleCounts){
		this.hasArticleCounts = hasArticleCounts;
	}

	/**
	 * 获取属性：
	 *	合成所需的手续费
	 */
	public int getPrice(){
		return price;
	}

	/**
	 * 设置属性：
	 *	合成所需的手续费
	 */
	public void setPrice(int price){
		this.price = price;
	}

	/**
	 * 获取属性：
	 *	true标识材料齐备，可以合成，false标识不能合成
	 */
	public boolean getCompound(){
		return compound;
	}

	/**
	 * 设置属性：
	 *	true标识材料齐备，可以合成，false标识不能合成
	 */
	public void setCompound(boolean compound){
		this.compound = compound;
	}

}