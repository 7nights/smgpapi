package cn.com.zjtelecom.smgp;

import java.io.IOException;

import cn.com.zjtelecom.smgp.Exception.SubmitException;
import cn.com.zjtelecom.smgp.bean.Result;
import cn.com.zjtelecom.smgp.bean.Submit;
import cn.com.zjtelecom.smgp.bean.SubmitBatch;
import cn.com.zjtelecom.smgp.connect.PConnectEvent;
import cn.com.zjtelecom.smgp.inf.ClientEventInterface;

public class ClientEvent {
	private PConnectEvent pconnect;
	private Result LoginResult;
	private ClientEventInterface clientEventInterface;
	private Long LongLastSubmit=0L;
	private int sleeptime=100;  //»± °10Ãı/√Î

	public ClientEvent(ClientEventInterface clientparent,String host, int port, int loginmode, String clientid,
			String clientpasswd, String spid, int displaymode) {
		
		this.clientEventInterface=clientparent;
		this.pconnect = new PConnectEvent(this.clientEventInterface,host, port, loginmode, clientid,
				clientpasswd, spid, displaymode);
		this.pconnect.start();
		this.LoginResult = this.pconnect.Login();
		if (this.LoginResult.ErrorCode != 0) {
			this.pconnect.stop();
		}
	}

	public ClientEvent(ClientEventInterface clientparent,String host, int port, int loginmode, String clientid,
			String clientpasswd, String spid) {
		this.clientEventInterface=clientparent;
		this.pconnect = new PConnectEvent(this.clientEventInterface,host, port, loginmode, clientid,
				clientpasswd, spid);
		this.pconnect.start();
		this.LoginResult = this.pconnect.Login();
		if (this.LoginResult.ErrorCode != 0) {
			this.pconnect.stop();
		}
	}
	public void SetLogFile(String LogFile) throws IOException {
		this.pconnect.SetLogFile(LogFile);
	}
	
	public void Close() {
		this.pconnect.LogOut();
	}


    
    /*public synchronized Result SendWapPush(String disc, String url,String srcTermId,String destTermid,String productID){
    	return this.pconnect.SendWapPush(disc, url, srcTermId, destTermid, productID);
    }
    
	public synchronized Result SendWapPush(String desc, String url,
			Submit submit){
		return this.pconnect.SendWapPush(desc, url, submit);
	}*/
    
	public void setDisplayMode(int displaymode) {
		this.pconnect.setDisplayMode(displaymode);
	}

	public  int Send(Submit submit) throws SubmitException {
		
		Long cur=getTimeStamp();
		if ((cur-this.LongLastSubmit)<this.sleeptime){
			try {
				Thread.sleep(this.sleeptime-(cur-this.LongLastSubmit));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.LongLastSubmit = getTimeStamp();
		if (this.LoginResult.ErrorCode == 0) {
			return this.pconnect.Send(submit);
			
		} else {
			throw new SubmitException(this.LoginResult); 
		}
	}
	

	public  Integer[] SendLong(Submit submit) throws SubmitException {
		if (this.LoginResult.ErrorCode == 0) {
			return this.pconnect.SendLong(submit);
		} else {
			throw new SubmitException(this.LoginResult); 
		}
	}
	
	public void setLSmsOverTime(int second){
		this.pconnect.setLSmsOverTime(second);
	}
	
	public Result SendBatch(SubmitBatch submit) {
		if (this.LoginResult.ErrorCode == 0) {
			return this.pconnect.SendBatch(submit);
		} else {
			return this.LoginResult;
		}
	}


	public Result Login() {

		return this.LoginResult;
	}
	
	public void setSpeed(int persecond) {
		this.sleeptime =1000/persecond;
	}

	private static Long getTimeStamp() {
		return (new java.util.Date()).getTime();
	}

}
