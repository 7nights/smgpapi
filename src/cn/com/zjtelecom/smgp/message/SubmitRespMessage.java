package cn.com.zjtelecom.smgp.message;

import cn.com.zjtelecom.smgp.protocol.RequestId;
import cn.com.zjtelecom.util.Hex;
import cn.com.zjtelecom.util.TypeConvert;

public class SubmitRespMessage extends Message {
	private String MsgID = "";
	private int Status = 0;
	
	public SubmitRespMessage(int seq,int status,String msgid){
		int len = 26;
		this.buf = new byte[len];
		TypeConvert.int2byte(len, this.buf, 0); // PacketLength
		TypeConvert.int2byte(RequestId.Submit_Resp, this.buf, 4); // RequestID
		TypeConvert.int2byte(seq, this.buf, 8); // sequence_Id
		System.arraycopy(Hex.rstr(msgid), 0, this.buf, 12, 10);
		TypeConvert.int2byte(status,this.buf,22);

	}

	public SubmitRespMessage(byte[] buffer) {
		
		if (buffer.length != 18) {
			System.out.println("SubmitResp Package resolv Error:"+Hex.rhex(buffer));

			return;
		}
		
		
		byte[] msgid = new byte[10];
		this.sequence_Id = TypeConvert.byte2int(buffer, 0);
		System.arraycopy(buffer, 4, msgid, 0, msgid.length);
		this.MsgID = Hex.rhex(msgid);
		//byte[] status =new byte[4];
		//System.arraycopy(this.buf, 8, msgid, 0, status.length);
		this.Status = TypeConvert.byte2int(buffer, 14);
	}


	public String getMsgID() {
		return MsgID;
	}

	public void setMsgID(String msgID) {
		MsgID = msgID;
	}

	public int getStatus() {
		return Status;
	}

	public void setStatus(int status) {
		Status = status;
	}
}
