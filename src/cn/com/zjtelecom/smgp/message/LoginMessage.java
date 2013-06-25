package cn.com.zjtelecom.smgp.message;

import java.security.NoSuchAlgorithmException;

import cn.com.zjtelecom.smgp.protocol.RequestId;
import cn.com.zjtelecom.util.DateUtil;
import cn.com.zjtelecom.util.Key;
import cn.com.zjtelecom.util.TypeConvert;

public class LoginMessage extends Message {
	private String clientID;
	private byte[] authenticatorClient;
	private String sharedKey;
	private int loginMode;
	private String clientVersion;
	private int timeStamp;

	public LoginMessage(byte[] buffer) {
       //传进出长度以外的封包
		int len = buffer.length;

		//恢复login前面的长度4个字节
		this.buf = new byte[len+4];
		TypeConvert.int2byte(len, buf, 0); // PacketLength
		System.arraycopy(buffer, 0, this.buf, 4, len);
		
		this.sequence_Id = TypeConvert.byte2int(buffer, 4);
		this.clientID=TypeConvert.getString(buffer, 8, 0, 8);
		this.authenticatorClient=new byte[16];
		System.arraycopy(buffer, 16, this.authenticatorClient, 0, 16);
		this.loginMode = buffer[32];
		this.clientVersion=getVersion(buffer[37]);
		this.setTimeStamp(TypeConvert.byte2int(buffer,33));
		
	}

	private static String getVersion(int version){
		return (version/16)+"."+(version-((int)version/16)*16);
		
	}
	public LoginMessage(String ClientID, String shared_Secret, int LoginMode)
			throws IllegalArgumentException, NoSuchAlgorithmException {
		this.clientID = ClientID;
		this.loginMode = LoginMode;
		this.sharedKey = shared_Secret;
		// 判断数据合法性
		if (ClientID == null)
			throw new IllegalArgumentException("ClientID isNull");
		if (ClientID.length() > 8)
			throw new IllegalArgumentException(
					"ClientID is length is great then 8");
		if (ClientID.length() > 8)
			throw new IllegalArgumentException(
					"ClientID is length is great then 8");
		int len = 42;
		buf = new byte[len];
		TypeConvert.int2byte(len, buf, 0); // PacketLength
		TypeConvert.int2byte(RequestId.Login, buf, 4); // RequestID
		System.arraycopy(ClientID.getBytes(), 0, buf, 12, ClientID.length()); // ClientID

		// 生成sharekey
		String timeStamp = DateUtil.GetTimeString();
		byte[] sharekey = Key.GenerateAuthenticatorClient(ClientID, shared_Secret,
				timeStamp);
		System.arraycopy(sharekey, 0, buf, 20, sharekey.length); // sharekey
		// System.out.println("LoginMode:"+LoginMode);
		buf[36] = (byte) LoginMode; // LoginMode
		//bebug
		//System.out.println("LoginTimeStampStr:"+timeStamp);
		//System.out.println("LoginTimeStampInt:"+Integer.parseInt(timeStamp));
		TypeConvert.int2byte(Integer.parseInt(timeStamp), buf, 37); // TimeStamp
		// System.out.println("login:"+Hex.rhex(buf));
		buf[41] = 0x30;
	}

	public String getClientID() {
		return clientID;
	}

	public byte[] getAuthenticatorClient() {
		return authenticatorClient;
	}

	public String getSharedKey() {
		return sharedKey;
	}

	public int getLoginMode() {
		return loginMode;
	}
	public String getClientVersion() {
		return clientVersion;
	}

	public void setTimeStamp(int timeStamp) {
		this.timeStamp = timeStamp;
	}

	public int getTimeStamp() {
		return timeStamp;
	}


}
