package cn.com.zjtelecom.smgp.message;

import cn.com.zjtelecom.util.TypeConvert;

public class Package {
    public int SequenceId = 0;
	public int Len;
    public int ReqestId;
    public byte[] Message;
    public long timestamp;
    public Package(byte [] buf){
    	this.Len=buf.length+4;
    	this.ReqestId = TypeConvert.byte2int(buf, 0);
    	this.Message = new byte[buf.length-4];
    	System.arraycopy(buf, 4, this.Message, 0, this.Message.length);
    	this.SequenceId=TypeConvert.byte2int(buf, 4);
    	this.timestamp = (new java.util.Date()).getTime();
    	
    }
	public Package() {
		// TODO Auto-generated constructor stub
	}
}
