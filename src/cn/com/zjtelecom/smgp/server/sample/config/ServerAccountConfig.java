package cn.com.zjtelecom.smgp.server.sample.config;

import java.util.HashMap;

public class ServerAccountConfig {

	protected  HashMap<String, String> passwordHash = new HashMap<String, String>();
	protected  HashMap<String, String> ipaddressHash = new HashMap<String, String>();
	protected  HashMap<String, String> spnumHash = new HashMap<String, String>();
	protected  HashMap<String, String> spidHash = new HashMap<String, String>();
    
	public String getPassword(String account){
		return this.passwordHash.get(account);
	}
	public String getIpAddress(String account){
		return this.ipaddressHash.get(account);
	}
	
	public String getSPNum(String account){
		return this.spnumHash.get(account);
	}
	public String getSPId(String account){
		return this.spidHash.get(account);
	}
}
