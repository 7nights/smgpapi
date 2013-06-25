package cn.com.zjtelecom.smgp.server.util;

import cn.com.zjtelecom.util.DateUtil;

public class GenerateNum {
	private int Emsnum=571062;
	private int MsgNum=0;
	private int LinkIDNum=0;
	
	public GenerateNum(){
	}
	
	public GenerateNum(int emsnum){
		this.Emsnum = emsnum;
	}
	public  String GenerateMsgID(){
		return this.Emsnum+DateUtil.GetTimeString() +  getFourNum(); 
	}
	 
	public  String GenerateLinkID(){
		return DateUtil.GetTimeString() + getTTenNum(); 
	}
	private  String getFourNum(){
		if ((this.MsgNum++) >= 10000) this.MsgNum=0;
		String returnnum ="000"+ String.valueOf(this.MsgNum);
		
		return returnnum.substring(returnnum.length()-4);
	}
	
	private  String getTTenNum(){
		if ((this.LinkIDNum++) >= 10000000) this.LinkIDNum=0;
		String returnnum ="0000000000"+ String.valueOf(this.LinkIDNum);
		return returnnum.substring(returnnum.length()-10);
	}
}
