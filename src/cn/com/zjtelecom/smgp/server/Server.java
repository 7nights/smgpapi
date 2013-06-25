package cn.com.zjtelecom.smgp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import cn.com.zjtelecom.smgp.bean.Deliver;
import cn.com.zjtelecom.smgp.bean.Login;
import cn.com.zjtelecom.smgp.bean.Submit;
import cn.com.zjtelecom.smgp.server.inf.ServerEventInterface;
import cn.com.zjtelecom.smgp.server.result.LoginResult;
import cn.com.zjtelecom.smgp.server.result.SubmitResult;
import cn.com.zjtelecom.smgp.server.util.CheckValid;
import cn.com.zjtelecom.smgp.server.util.ClientStatus;
import cn.com.zjtelecom.smgp.server.util.GenerateNum;

public class Server extends Thread {
	private int serverPort = 8890;
	private ServerEventInterface serverEventInterface;
	private ServerSocket server;
	private int connectCount = 0;
	private int TimeOut = 60 * 15; // ȱʡ15�볬ʱ
	private GenerateNum generateNum = new GenerateNum();

	private HashMap<String, ClientStatus> clientlist = new HashMap<String, ClientStatus>();
	private HashMap<String, String> spnum2Account = new HashMap<String, String>();

	public int getTimeOut() {
		return TimeOut;
	}

	public void setTimeOut(int timeOut) {
		TimeOut = timeOut;
	}

	public HashMap<String, ClientStatus> getClientlist() {
		return clientlist;
	}

	public void setClientlist(HashMap<String, ClientStatus> clientlist) {
		this.clientlist = clientlist;
	}

	public Server(ServerEventInterface serverEventInterface, int port) {
		this.serverEventInterface = serverEventInterface;
		this.serverPort = port;
	}

	public void run() {
		try {
			server = new ServerSocket(this.serverPort);
			
			ActiveTestThread activeTestThread = new ActiveTestThread(this);
			activeTestThread.start();
			
			while (true) {
				Socket clientsocket = server.accept();
				ServerHandleConnect serverHandleConnect = new ServerHandleConnect(
						this, clientsocket, this.TimeOut);
				serverHandleConnect.start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private synchronized void connected(String account, String ipaddress,
			ServerHandleConnect serverHandleConnect) {
		String key = account + "$" + ipaddress;
		System.out.println("IpAddress:" + ipaddress + "," + "Account:"
				+ account + " has connected!");
		if (this.clientlist.get(key) == null) {
			this.clientlist.put(key, new ClientStatus(account, ipaddress,
					serverHandleConnect));
		} else {
			this.clientlist.get(key).AddNew(serverHandleConnect);
		}
		connectCount++;
	}

	public void SendDeliver(Deliver deliver) {
        boolean havefind = false;
		deliver.MsgID = this.generateNum.GenerateMsgID();
		if (deliver.IsReport == 0)
			deliver.LinkID = this.generateNum.GenerateLinkID();
		//System.out.println("addmsgid:" + deliver.MsgID);
		Iterator iterator = this.spnum2Account.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			//System.out.println("key:" + key+"|");
			//System.out.println("DestTermID:" + deliver.DestTermID+"|");
            //System.out.println(deliver.DestTermID.indexOf(key));
			if (deliver.DestTermID.indexOf(key) >= 0) {
				// System.out.println("Find Client");
				havefind =true;
				ClientStatus clientStatus = this.clientlist
						.get(this.spnum2Account.get(key));
				if (clientStatus == null)
					continue;
				Vector<ServerHandleConnect> clientv = clientStatus
						.getServerHandleConnectList();
				ServerHandleConnect tmphanlde = null;
				for (int i = 0; i < clientv.size(); i++) {
					if (tmphanlde == null)
						tmphanlde = clientv.get(i);

					if (tmphanlde.getLoginMode() == 1) {
						tmphanlde.SendDeliver(deliver);
						break;
					} else if (tmphanlde.getLoginMode() == 2) {
						tmphanlde = clientv.get(i);
					}
				}
				tmphanlde.SendDeliver(deliver);
			} 
		}
		if (havefind == false) {
			System.out.println(deliver.DestTermID + " is not Connected");
		} else {
			System.out.println("Deliver has Sended to Client!");
		}
	}

	public void disconnect(String account, String ipaddress,
			ServerHandleConnect serverHandleConnect) {
		String key = account + "$" + ipaddress;
		if (this.clientlist.get(key).removeClientConnected(serverHandleConnect)) {
			System.out.println("IpAddress:" + ipaddress + "," + "Account:"
					+ account + " all has disconnected!");
			this.clientlist.remove(key);
		} else {
			System.out.println("IpAddress:" + ipaddress + "," + "Account:"
					+ account + "  has disconnected!");
		}

	}

	public void disconnect(ServerHandleConnect serverHandleConnect) {
		System.out.println("other disconnected");
		serverHandleConnect.stop();
		serverHandleConnect.destroy();

	}

	public LoginResult onLogin(Login login,
			ServerHandleConnect serverHandleConnect) {
		LoginResult loginresult = this.serverEventInterface.onLogin(login);
		if (loginresult.getStatus() == 0) {
			this.spnum2Account.put(loginresult.getSpNum(), login.Account + "$"
					+ login.ipaddress);
			this.connected(login.Account, login.ipaddress, serverHandleConnect);
		}

		return loginresult;
	}

	public SubmitResult onSumit(Submit submit,String account) {

		SubmitResult submitResult = this.serverEventInterface.onSumit(submit,account);
		submitResult.setMsgID(this.generateNum.GenerateMsgID());
		return submitResult;
	}

}
