package cn.com.zjtelecom.smgp.server.util;

import java.util.Vector;

import cn.com.zjtelecom.smgp.server.ServerHandleConnect;

public class ClientStatus {
	private String account = "";
	private String ipaddress;
	private int curconnected = 0;
	private Vector<ServerHandleConnect> serverHandleConnectList = new Vector<ServerHandleConnect>();

	
	public Vector<ServerHandleConnect> getServerHandleConnectList() {
		return serverHandleConnectList;
	}

	public void setServerHandleConnectList(
			Vector<ServerHandleConnect> serverHandleConnectList) {
		this.serverHandleConnectList = serverHandleConnectList;
	}


	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getIpaddress() {
		return ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}

	public int getCurconnected() {
		return curconnected;
	}

	public void setCurconnected(int curconnected) {
		this.curconnected = curconnected;
	}

	public ClientStatus(String account, String ipaddress,
			ServerHandleConnect serverHandleConnect) {
		this.account = account;
		this.ipaddress = ipaddress;
		this.curconnected = 1;
		this.serverHandleConnectList.add(serverHandleConnect);
	}

	public void AddNew(ServerHandleConnect serverHandleConnect) {
		this.curconnected++;
		this.serverHandleConnectList.add(serverHandleConnect);
	}

	public boolean removeClientConnected(ServerHandleConnect serverHandleConnect) {
		for (int i = 0; i < this.serverHandleConnectList.size(); i++) {
			if (serverHandleConnectList.get(i) == serverHandleConnect) {
				serverHandleConnectList.remove(i);
				this.curconnected--;
			}
		}
		if (this.curconnected <= 0) {
			return true;
		} else {
			return false;
		}

	}

}
