package cn.com.zjtelecom.smgp.message;

import cn.com.zjtelecom.smgp.protocol.RequestId;
import cn.com.zjtelecom.util.TypeConvert;

public class ActiveTestRespMessage extends Message{
    public ActiveTestRespMessage(){
        int len = 12;
        this.buf = new byte[len];
        TypeConvert.int2byte(len, this.buf, 0);
        TypeConvert.int2byte(RequestId.ActiveTest_Resp, this.buf, 4);
    } 
    public ActiveTestRespMessage(int seq){
        int len = 12;
        this.buf = new byte[len];
        TypeConvert.int2byte(len, this.buf, 0);
        TypeConvert.int2byte(RequestId.ActiveTest_Resp, this.buf, 4);//requestID
        TypeConvert.int2byte(this.sequence_Id, this.buf, 8); // sequence_Id
    } 
    
}
