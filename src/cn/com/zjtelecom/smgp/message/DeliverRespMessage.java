package cn.com.zjtelecom.smgp.message;

import cn.com.zjtelecom.smgp.protocol.RequestId;
import cn.com.zjtelecom.util.TypeConvert;

public class DeliverRespMessage extends Message {
    public DeliverRespMessage(byte msg_Id[],int sequence_id, int status){
    	int len=26;
    	this.sequence_Id = sequence_id;
    	buf =new byte[len];
        TypeConvert.int2byte(len, buf, 0);
        TypeConvert.int2byte(RequestId.Deliver_Resp, buf, 4);
        TypeConvert.int2byte(sequence_id, buf, 8);
        System.arraycopy(msg_Id, 0, buf, 12, msg_Id.length);
        TypeConvert.int2byte(status, buf, 22);
    }
}
