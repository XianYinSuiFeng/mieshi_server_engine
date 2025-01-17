package com.fy.engineserver.tool.message;

import java.nio.ByteBuffer;

import com.xuanzhi.tools.transport.RequestMessage;



/**
 * 网络数据包，此数据包是由MessageComplier自动生成，请不要手动修改。<br>
 * 版本号：null<br>
 * 工具客户端发送验证信息给服务器，等待验证结果<br>
 * 数据包的格式如下：<br><br>
 * <table border="0" cellpadding="0" cellspacing="1" width="100%" bgcolor="#000000" align="center">
 * <tr bgcolor="#00FFFF" align="center"><td>字段名</td><td>数据类型</td><td>长度（字节数）</td><td>说明</td></tr> * <tr bgcolor="#FFFFFF" align="center"><td>length</td><td>int</td><td>getNumOfByteForMessageLength()个字节</td><td>包的整体长度，包头的一部分</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>type</td><td>int</td><td>4个字节</td><td>包的类型，包头的一部分</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>seqNum</td><td>int</td><td>4个字节</td><td>包的序列号，包头的一部分</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>username.length</td><td>short</td><td>2个字节</td><td>字符串实际长度</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>username</td><td>String</td><td>username.length</td><td>字符串对应的byte数组</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>password.length</td><td>short</td><td>2个字节</td><td>字符串实际长度</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>password</td><td>String</td><td>password.length</td><td>字符串对应的byte数组</td></tr>
 * <tr bgcolor="#FAFAFA" align="center"><td>product.length</td><td>short</td><td>2个字节</td><td>字符串实际长度</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>product</td><td>String</td><td>product.length</td><td>字符串对应的byte数组</td></tr>
 * <tr bgcolor="#FFFFFF" align="center"><td>verification code</td><td>校验码</td><td>16个字节</td><td>发送包的校验码，16个字节固定长</td></tr>
 * </table>
 */
public class VERIFY_USER_REQ implements RequestMessage{

	static DefaultMessageFactory mf = DefaultMessageFactory.getInstance();
	static String verificationKeyword = "POIUYH564RFbhd";


	long seqNum;
	String username;
	String password;
	String product;

	public VERIFY_USER_REQ(long seqNum,String username,String password,String product){
		this.seqNum = seqNum;
		this.username = username;
		this.password = password;
		this.product = product;
	}

	public VERIFY_USER_REQ(long seqNum,byte[] content,int offset,int size) throws Exception{
		this.seqNum = seqNum;
		byte verification[] = new byte[16];
		System.arraycopy(content, offset+size-16, verification, 0, 16);
		Verification md5 = new Verification();
		md5.update(content,offset,size-16);
		md5.update(verificationKeyword.getBytes(),0,verificationKeyword.getBytes().length);
		if(!Verification.equals(verification,md5.digest())) throw new Exception("verification failed!");
		int len = 0;
		len = (int)mf.byteArrayToNumber(content,offset,2);
		offset += 2;
		if(len < 0 || len > 16384) throw new Exception("string length ["+len+"] big than the max length [16384]");
		username = new String(content,offset,len,"UTF-8");
		offset += len;
		len = (int)mf.byteArrayToNumber(content,offset,2);
		offset += 2;
		if(len < 0 || len > 16384) throw new Exception("string length ["+len+"] big than the max length [16384]");
		password = new String(content,offset,len);
		offset += len;
		len = (int)mf.byteArrayToNumber(content,offset,2);
		offset += 2;
		if(len < 0 || len > 16384) throw new Exception("string length ["+len+"] big than the max length [16384]");
		product = new String(content,offset,len);
		offset += len;
	}

	public int getType() {
		return 0x00000040;
	}

	public String getTypeDescription() {
		return "VERIFY_USER_REQ";
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
			len +=username.getBytes("UTF-8").length;
		}catch(java.io.UnsupportedEncodingException e){
		 e.printStackTrace();
			throw new RuntimeException("unsupported encoding [UTF-8]",e);
		}
		len += 2;
		len +=password.getBytes().length;
		len += 2;
		len +=product.getBytes().length;
		len += 16;
		packet_length = len;
		return len;
	}

	public int writeTo(ByteBuffer buffer) {
		int messageLength = getLength();
		if(buffer.remaining() < messageLength) return 0;
		buffer.mark();
		try{
			buffer.put(mf.numberToByteArray(messageLength,mf.getNumOfByteForMessageLength()));
			buffer.putInt(getType());
			buffer.putInt((int)seqNum);
			int position = buffer.position();

			byte[] tmpBytes1 = username.getBytes("UTF-8");
			buffer.putShort((short)tmpBytes1.length);
			buffer.put(tmpBytes1);
			tmpBytes1 = password.getBytes();
			buffer.putShort((short)tmpBytes1.length);
			buffer.put(tmpBytes1);
			tmpBytes1 = product.getBytes();
			buffer.putShort((short)tmpBytes1.length);
			buffer.put(tmpBytes1);
			tmpBytes1 = new byte[buffer.position() - position];
			buffer.position(position);
			buffer.get(tmpBytes1);
			Verification md5 = new Verification();
			md5.update(tmpBytes1,0,tmpBytes1.length);
			md5.update(verificationKeyword.getBytes(),0,verificationKeyword.getBytes().length);
			buffer.put(md5.digest());
		}catch(Exception e){
		 e.printStackTrace();
			buffer.reset();
			throw new RuntimeException("in writeTo method catch exception :",e);
		}
		return messageLength;
	}

	/**
	 * 获取属性：
	 *	用户名
	 */
	public String getUsername(){
		return username;
	}

	/**
	 * 设置属性：
	 *	用户名
	 */
	public void setUsername(String username){
		this.username = username;
	}

	/**
	 * 获取属性：
	 *	密码
	 */
	public String getPassword(){
		return password;
	}

	/**
	 * 设置属性：
	 *	密码
	 */
	public void setPassword(String password){
		this.password = password;
	}

	/**
	 * 获取属性：
	 *	验证的工具名称
	 */
	public String getProduct(){
		return product;
	}

	/**
	 * 设置属性：
	 *	验证的工具名称
	 */
	public void setProduct(String product){
		this.product = product;
	}

}
