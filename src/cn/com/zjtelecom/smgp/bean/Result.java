package cn.com.zjtelecom.smgp.bean;

public class Result {
     public int ErrorCode=0;
     public String ErrorDescription="";
     public Result(){
     }
     public Result(int errorcode,String errordescription){
    	 this.ErrorCode =errorcode;
    	 this.ErrorDescription = errordescription;
     }
}
