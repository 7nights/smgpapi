package cn.com.zjtelecom.smgp.server.result;

public class SubmitResult {
	private int sequence_Id;
	private int Status;
	private String msgID;
	
	public int getSequence_Id() {
		return sequence_Id;
	}
	public void setSequence_Id(int sequenceId) {
		sequence_Id = sequenceId;
	}
	public int getStatus() {
		return Status;
	}
	public void setStatus(int status) {
		Status = status;
	}
	public String getMsgID() {
		return msgID;
	}
	public void setMsgID(String msgID) {
		this.msgID = msgID;
	}

	public SubmitResult(int status,String msgid){
		this.Status = status;
		this.msgID = msgid;
		
	}
	public SubmitResult(int status) {
		this.Status = status;
	}

}
