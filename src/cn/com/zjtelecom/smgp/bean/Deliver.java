package cn.com.zjtelecom.smgp.bean;

import cn.com.zjtelecom.smgp.message.DeliverMessage;
import cn.com.zjtelecom.smgp.protocol.Tlv;
import cn.com.zjtelecom.util.DateUtil;

public class Deliver {
	public String MsgID;
	public int IsReport;
	public int MsgFormat;
	public String RecvTime;
	public String SrcTermID;
	public String DestTermID;
	public int MsgLength;
	public byte[] MsgContent;
	public byte[] Reserve;
	public String LinkID;
	public String ReportMsgID;
	public int TP_udhi;
	public Tlv[] OtherTlv;
	
	public Deliver(){
		this.IsReport =0;
		this.RecvTime =DateUtil.GetTimeString2();
		
	}

	public Deliver(DeliverMessage dm) {
		this.MsgID = dm.MsgID;
		this.IsReport = dm.IsReport;
		this.MsgFormat = dm.MsgFormat;
		this.RecvTime = dm.RecvTime;
		this.SrcTermID = dm.SrcTermID;
		this.DestTermID = dm.DestTermID;
		this.MsgLength = dm.MsgLength;
		this.MsgContent = dm.MsgContent;
		this.Reserve = dm.Reserve;
		this.LinkID = dm.LinkID;
		this.OtherTlv = dm.OtherTlv;
		this.ReportMsgID = dm.ReportMsgID;
		this.TP_udhi=dm.TP_udhi;
	}
}
