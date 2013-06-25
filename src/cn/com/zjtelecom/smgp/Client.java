package cn.com.zjtelecom.smgp;

import java.io.IOException;

import cn.com.zjtelecom.smgp.bean.Deliver;
import cn.com.zjtelecom.smgp.bean.Result;
import cn.com.zjtelecom.smgp.bean.Submit;
import cn.com.zjtelecom.smgp.bean.SubmitBatch;
import cn.com.zjtelecom.smgp.connect.PConnect;

public class Client {
	private PConnect pconnect;
	private Result LoginResult;

	public Client(String host, int port, int loginmode, String clientid,
			String clientpasswd, String spid, int displaymode) {
		this.pconnect = new PConnect(host, port, loginmode, clientid,
				clientpasswd, spid, displaymode);
		this.pconnect.start();
		this.LoginResult = this.pconnect.Login();
		if (this.LoginResult.ErrorCode != 0) {
			this.pconnect.stop();
		}
	}

	public Client(String host, int port, int loginmode, String clientid,
			String clientpasswd, String spid) {
		this.pconnect = new PConnect(host, port, loginmode, clientid,
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

   /* public  Result SendFlashSms(Submit submit){
    	return this.pconnect.SendFlashSms(submit);
    }
    */
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

	public  Result Send(Submit submit) {
		if (this.LoginResult.ErrorCode == 0) {
			return this.pconnect.Send(submit);
		} else {
			return this.LoginResult;
		}
	}
	
	public  Result[] SendLong(Submit submit) {
		if (this.LoginResult.ErrorCode == 0) {
			return this.pconnect.SendLong(submit);
		} else {
			Result [] result=new Result[1];
			result[0]=this.LoginResult;
			return result;
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

	public Deliver OnDeliver() {
		if (this.LoginResult.ErrorCode == 0) {
			return this.pconnect.OnDeliver();
		} else {
			return null;
		}
	}

	public Deliver getDeliver() {
		if (this.LoginResult.ErrorCode == 0) {
			return this.pconnect.getDeliver();
		} else {
			return null;
		}
	}
	
	//public set

}
