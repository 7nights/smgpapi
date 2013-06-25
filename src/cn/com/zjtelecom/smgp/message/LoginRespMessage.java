package cn.com.zjtelecom.smgp.message;

import java.security.NoSuchAlgorithmException;

import cn.com.zjtelecom.smgp.protocol.RequestId;
import cn.com.zjtelecom.util.Key;
import cn.com.zjtelecom.util.TypeConvert;

public class LoginRespMessage extends Message {
	private int Status = 0;
	private byte[] AuthenticatorServer = null;
	private String Version = "";

	public LoginRespMessage(int sequence_Id, int status,
			byte[] authenticatorsclient, String sharekey, String serverversion) {

		try {
			int len = 33;
			this.buf = new byte[len];
			TypeConvert.int2byte(len, this.buf, 0); // PacketLength
			TypeConvert.int2byte(RequestId.Login_Resp, this.buf, 4); // RequestID
			TypeConvert.int2byte(sequence_Id, this.buf, 8); // sequence_Id
			TypeConvert.int2byte(status, this.buf, 12); // status
			
			byte[] authorityserver = Key.GenerateAuthenticatorServer(status,
					authenticatorsclient, sharekey);
			System.arraycopy(authorityserver, 0, this.buf, 12+4, authorityserver.length);
			//System.arraycopy(authorityserver, 0, this.buf, 12+4, authorityserver.length);
			//System.out.println("indexof:"+(12+4+ authorityserver.length));
			TypeConvert.int2byte3(getVersion(serverversion), this.buf, 12+4+ authorityserver.length);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private int getVersion(String version){
		return Integer.parseInt(version.substring(0,version.indexOf(".")))*16+Integer.parseInt(version.substring(version.indexOf(".")+1));
	}
	public LoginRespMessage(byte[] buffer) {
		if (buffer.length != 25) {
			throw new IllegalArgumentException("LoginResp Package resolv Error");
		} else {
			// this.buf = new byte[25];
			// System.arraycopy(buffer, 0, buffer, 0, buffer.length);
			this.sequence_Id = TypeConvert.byte2int(buffer, 0);
			this.Status = TypeConvert.byte2int(buffer, 4);
			AuthenticatorServer = new byte[16];
			System.arraycopy(buffer, 8, AuthenticatorServer, 0,
					AuthenticatorServer.length);
			// this.AuthenticatorServer = new String(tmp);
			this.Version = (int) (buffer[24] / 16) + "."
					+ ((int) (buffer[24]) % 16);
		}
	}

	public String getServerVersion() {
		return this.Version;
	}

	public int getSequenceId() {
		return this.sequence_Id;
	}

	public int getStatus() {
		return this.Status;
	}

	public byte[] getAuthenticatorServer() {
		return this.AuthenticatorServer;
	}
}
