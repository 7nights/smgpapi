package cn.com.zjtelecom.smgp.message;

import cn.com.zjtelecom.smgp.protocol.RequestId;
import cn.com.zjtelecom.util.TypeConvert;

public class ExitRespMessage  extends Message {
	public ExitRespMessage(int seq) {
        int len = 12;
        buf = new byte[len];
        TypeConvert.int2byte(len, buf, 0);
        TypeConvert.int2byte(RequestId.Exit_Resp, buf, 4);
        TypeConvert.int2byte(seq, buf, 8);
	}
}
