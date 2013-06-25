package cn.com.zjtelecom.smgp.message;

import cn.com.zjtelecom.util.TypeConvert;

public class Message {
    public Message()
    {
    }
    public void setSequenceId(int sequence_Id)
    {
        this.sequence_Id = sequence_Id;
        TypeConvert.int2byte(sequence_Id, buf, 8);
    }
    
    protected byte buf[];
    protected int sequence_Id;
	public byte[] getBuf() {
		return buf;
	}
	public int getSequence_Id() {
		return sequence_Id;
	}
	public void setSequence_Id(int sequence_Id) {
		this.sequence_Id = sequence_Id;
	}
}
