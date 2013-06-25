package cn.com.zjtelecom.smgp.bean;

import cn.com.zjtelecom.smgp.message.LoginMessage;

public class Login {
	public int sequence_Id;
    public String Account;
    public String ShareKey;
    public String Version;
    public int LoginMode;
    public String ipaddress;
    public int timestamp;
    public byte[] AuthenticatorClient;

    public Login(LoginMessage loginMessage,String ipaddress){
    	this.sequence_Id =loginMessage.getSequence_Id();
    	this.Account = loginMessage.getClientID();
    	this.Version = loginMessage.getClientVersion();
    	this.LoginMode = loginMessage.getLoginMode();
    	this.ipaddress = ipaddress;
    	this.timestamp = loginMessage.getTimeStamp();
    	this.AuthenticatorClient = loginMessage.getAuthenticatorClient();
    	
    	
    }
}
