package cn.com.zjtelecom.smgp.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import cn.com.zjtelecom.smgp.server.util.ClientStatus;

public class ActiveTestThread extends Thread {
   private Server server;
	public ActiveTestThread(Server serversim){
	   this.server = serversim;
   }
	public void run(){
	  do {
		  HashMap<String, ClientStatus> list = this.server.getClientlist();
		  Iterator iterator	 = list.keySet().iterator();
		  while(iterator.hasNext()) {
			  String key =(String) iterator.next();
			  ClientStatus clientStatus=list.get(key);
			  Vector<ServerHandleConnect> connectlist = clientStatus.getServerHandleConnectList();
			  for (int i=0;i<connectlist.size();i++){
				  connectlist.get(i).ActiveTest();
			  }
			  
		  }
		  try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
	  }while(true); 
   }
}
