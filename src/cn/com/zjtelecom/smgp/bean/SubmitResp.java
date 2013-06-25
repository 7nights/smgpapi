package cn.com.zjtelecom.smgp.bean;

public class SubmitResp {
	private String msgID = "";
	private int resultCode = 0;
	private int sequenceID = 0;
	private Submit submit=new Submit();
	private long sendTime;

	public SubmitResp(){
		
	}
	
	public SubmitResp(Submit submit){
		this.submit=submit;
		this.sendTime = (new java.util.Date()).getTime();
	}
	
	public long getSendTime() {
		return sendTime;
	}

	public void setSendTime(long sendTime) {
		this.sendTime = sendTime;
	}

	public String getMsgID() {
		return msgID;
	}

	public void setMsgID(String msgID) {
		this.msgID = msgID;
	}

	public int getResultCode() {
		return resultCode;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}

	public int getSequenceID() {
		return this.sequenceID;
	}

	public void setSequenceID(int sequenceID) {
		this.sequenceID = sequenceID;
	}

	public Submit getSubmit() {
		return submit;
	}

	public void setSubmit(Submit submit) {
		this.submit = submit;
	}


}
