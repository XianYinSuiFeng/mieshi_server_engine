package com.fy.engineserver.message;

import com.xuanzhi.tools.transport.*;
import java.nio.ByteBuffer;



/**
 * 网络数据包，此数据包是由MessageComplier自动生成，请不要手动修改。<br>
 * 版本号：null<br>
 * 查询某一地图的动态位置数据<br>
 * 数据包的格式如下：<br><br>
 * <table border="0" cellpadding="0" cellspacing="1" width="100%" bgcolor="#000000" align="center">
 * <tr bgcolor="#00FFFF" align="center"><td>字段名</td><td>数据类型</td><td>长度（字节数）</td><td>说明</td></tr> * <tr bgcolor="#FFFFFF" align="center"><td>length</td><td>int</td><td>getNumOfByteForMessageLength()个字节</td><td>包的整体长度，包头的一部分</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>type</td><td>int</td><td>4个字节</td><td>包的类型，包头的一部分</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>seqNum</td><td>int</td><td>4个字节</td><td>包的序列号，包头的一部分</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>mapname.length</td><td>short</td><td>2个字节</td><td>字符串实际长度</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>mapname</td><td>String</td><td>mapname.length</td><td>字符串对应的byte数组</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>opens.length</td><td>int</td><td>4</td><td>数组长度</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>opens</td><td>boolean[]</td><td>opens.length</td><td>*</td></tr>
 * </table>
 */
public class QUERY_GAMEMAP_MOVE_LIVINGOBJECT_REQ implements RequestMessage{

	static GameMessageFactory mf = GameMessageFactory.getInstance();

	long seqNum;
	String mapname;
	boolean[] opens;

	public QUERY_GAMEMAP_MOVE_LIVINGOBJECT_REQ(){
	}

	public QUERY_GAMEMAP_MOVE_LIVINGOBJECT_REQ(long seqNum,String mapname,boolean[] opens){
		this.seqNum = seqNum;
		this.mapname = mapname;
		this.opens = opens;
	}

	public QUERY_GAMEMAP_MOVE_LIVINGOBJECT_REQ(long seqNum,byte[] content,int offset,int size) throws Exception{
		this.seqNum = seqNum;
		int len = 0;
		len = (int)mf.byteArrayToNumber(content,offset,2);
		offset += 2;
		if(len < 0 || len > 16384) throw new Exception("string length ["+len+"] big than the max length [16384]");
		mapname = new String(content,offset,len,"UTF-8");
		offset += len;
		len = (int)mf.byteArrayToNumber(content,offset,4);
		offset += 4;
		if(len < 0 || len > 4096) throw new Exception("array length ["+len+"] big than the max length [4096]");
		opens = new boolean[len];
		for(int i = 0 ; i < opens.length ; i++){
			opens[i] = mf.byteArrayToNumber(content,offset,1) != 0;
			offset += 1;
		}
	}

	public int getType() {
		return 0x0000F043;
	}

	public String getTypeDescription() {
		return "QUERY_GAMEMAP_MOVE_LIVINGOBJECT_REQ";
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
		len += 2;
		try{
			len +=mapname.getBytes("UTF-8").length;
		}catch(java.io.UnsupportedEncodingException e){
		 e.printStackTrace();
			throw new RuntimeException("unsupported encoding [UTF-8]",e);
		}
		len += 4;
		len += opens.length;
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

			byte[] tmpBytes1;
				try{
			 tmpBytes1 = mapname.getBytes("UTF-8");
				}catch(java.io.UnsupportedEncodingException e){
			 e.printStackTrace();
					throw new RuntimeException("unsupported encoding [UTF-8]",e);
				}
			buffer.putShort((short)tmpBytes1.length);
			buffer.put(tmpBytes1);
			buffer.putInt(opens.length);
			for(int i = 0 ; i < opens.length; i++){
				buffer.put((byte)(opens[i]==false?0:1));
			}
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
	 *	地图名
	 */
	public String getMapname(){
		return mapname;
	}

	/**
	 * 设置属性：
	 *	地图名
	 */
	public void setMapname(String mapname){
		this.mapname = mapname;
	}

	/**
	 * 获取属性：
	 *	开启需要显示的内容数组，true为显示，false为不显示
	 */
	public boolean[] getOpens(){
		return opens;
	}

	/**
	 * 设置属性：
	 *	开启需要显示的内容数组，true为显示，false为不显示
	 */
	public void setOpens(boolean[] opens){
		this.opens = opens;
	}

}