package cn.com.zjtelecom.smgp.bean;

import java.io.UnsupportedEncodingException;

import cn.com.zjtelecom.smgp.protocol.Tlv;
import cn.com.zjtelecom.smgp.protocol.TlvId;

public class Submit {
	/*
	 * 6, //msgtype 0, //int needreport, 0, //int priority, "",//String
	 * serviceid, "02",//String feetype "000",//String feecode "000",//String
	 * fixedfee, 15,//int msgformat "",//String validtime, "",//String attime,
	 * "10620068",//String srctermid, "",//String chargetermid,
	 * desttermid,//String[] desttermid, msgcontent.length,//int msglength,
	 * msgcontent,//byte[] msgcontent, null,//String reserve tlvarray //Tlv[]
	 * tlvarray);
	 */

	private int MsgType = 6;
	private int NeedReport = 0;
	private int Priority = 0;
	private String ServiceID = "";
	private String Feetype = "00";
	private String FeeCode = "000";
	private String FixedFee = "000";
	private int MsgFormat = 15;
	private String ValidTime = "";
	private String AtTime = "";
	private String SrcTermid = "";
	private String DestTermid = "";
	private String ChargeTermid = "";
	private int MsgLength = 0;
	private byte[] MsgContent = null;
	private byte[] Reserve = null;
	private int TP_udhi=0;

	// tlv
	private String ProductID = "";
	private String LinkID = "";
	private Tlv[] OtherTlvArray = null;
	
	private int isLongMsg =0;
	
	private int DestTermIDCount=1;
	private String[] DestTermIDArray;
	

	public Submit(){
		
	}
/*	public Submit(Submit submit) {
		this.MsgType = submit.MsgType;
		this.NeedReport = submit.NeedReport;
		this.Priority = submit.Priority;
		this.ServiceID = submit.ServiceID;
		this.Feetype = submit.Feetype;
		this.FeeCode = submit.FeeCode;
		this.FixedFee = submit.FixedFee;
		this.MsgFormat = submit.MsgFormat;
		this.ValidTime = submit.ValidTime;
		this.AtTime = submit.AtTime;
		this.SrcTermid = submit.SrcTermid;
		this.DestTermid = submit.DestTermid;
		this.ChargeTermid = submit.ChargeTermid;
		this.MsgLength = submit.MsgLength;
		this.MsgContent = submit.MsgContent;
		this.Reserve = submit.Reserve;
		this.ProductID = submit.ProductID;
		this.LinkID = submit.LinkID;
		this.OtherTlvArray =submit.OtherTlvArray;
		
	}
	*/

	public Submit(int msgtype, int needreport, int priority, String serviceid,
			String feetype, String feecode, String fixedfee, int msgformat,
			String validtime, String attime, String srctermid,
			String desttermid, String chargetermid, int msglength,
			byte[] msgcontent, byte[] reserve, String productid, String Linkid) {
		this.MsgType = msgtype;
		this.NeedReport = needreport;
		this.Priority = priority;
		this.ServiceID = serviceid;
		this.Feetype = feetype;
		this.FeeCode = feecode;
		this.FixedFee = fixedfee;
		this.MsgFormat = msgformat;
		this.ValidTime = validtime;
		this.AtTime = attime;
		this.SrcTermid = srctermid;
		this.DestTermid = desttermid;
		this.ChargeTermid = chargetermid;
		this.MsgLength = msglength;
		this.MsgContent = msgcontent;
		this.Reserve = reserve;
		this.ProductID = productid;
		this.LinkID = Linkid;
	}

	public Submit(int msgtype, int needreport, int priority, String serviceid,
			String feetype, String feecode, String fixedfee, int msgformat,
			String validtime, String attime, String srctermid,
			String desttermid, String chargetermid, int msglength,
			byte[] msgcontent, byte[] reserve, String productid, String Linkid,
			Tlv[] othertlvarray) {
		this.MsgType = msgtype;
		this.NeedReport = needreport;
		this.Priority = priority;
		this.ServiceID = serviceid;
		this.Feetype = feetype;
		this.FeeCode = feecode;
		this.FixedFee = fixedfee;
		this.MsgFormat = msgformat;
		this.ValidTime = validtime;
		this.AtTime = attime;
		this.SrcTermid = srctermid;
		this.DestTermid = desttermid;
		this.ChargeTermid = chargetermid;
		this.MsgLength = msglength;
		this.MsgContent = msgcontent;
		this.Reserve = reserve;
		this.ProductID = productid;
		this.LinkID = Linkid;
		this.OtherTlvArray = othertlvarray;
	}

	public void AddTlv(int tag, String value) {
		if (tag == TlvId.LinkID) {
			this.LinkID = value;
		} else if (tag == TlvId.TP_udhi) {
			this.TP_udhi = Integer.parseInt(value);
		} else if (tag == TlvId.Mserviceid) {
			this.ProductID = value;
		} else {
			if (this.OtherTlvArray == null) {
				Tlv[] tmp = new Tlv[1];
				tmp[0] = new Tlv(tag, value);
				this.OtherTlvArray = tmp;
			} else {
				
				int find=0;
				for (int i=0;i<OtherTlvArray.length;i++){
					if (OtherTlvArray[i].Tag == tag) {
						find=1;
						//System.out.println("ÒÑ¾­´æÔÚtag:"+tag);
						OtherTlvArray[i]= new Tlv(tag, value);
					}
				}
				if (find==0) {
				Tlv[] tmp = new Tlv[OtherTlvArray.length + 1];
				System
						.arraycopy(OtherTlvArray, 0, tmp, 0,
								OtherTlvArray.length);
				tmp[OtherTlvArray.length] = new Tlv(tag, value);
				this.OtherTlvArray = tmp;
				}
			}
		}
	}
	
	public void RemoveTlv(int tag){
		int newlen=0;
		if (this.OtherTlvArray != null) {
			Tlv[] tmp = new Tlv[OtherTlvArray.length];
			for(int i=0;i<OtherTlvArray.length;i++){
				if (OtherTlvArray[i].Tag != tag){
					tmp[newlen]=OtherTlvArray[i];
					newlen++;
				} 
			}
			//System.out.println("newlen:"+newlen);
			if (newlen<OtherTlvArray.length){
				Tlv[] tmp2=new Tlv[newlen];
				System
				.arraycopy(tmp, 0, tmp2, 0,
						newlen);
				this.OtherTlvArray=tmp2;
			}
		}
	}

	public Submit(String srctermid, String desttermid, byte[] msgconent) {
		this.SrcTermid = srctermid;
		this.DestTermid = desttermid;
		this.ChargeTermid = desttermid;
		this.MsgContent = msgconent;
	}

	public Submit(String srctermid, String desttermid, String msgconent) {
		this.SrcTermid = srctermid;
		this.DestTermid = desttermid;
		this.ChargeTermid = desttermid;
		this.MsgContent = msgconent.getBytes();
	}

	public Submit(String srctermid, String desttermid, String msgconent,
			String productid) {
		this.SrcTermid = srctermid;
		this.DestTermid = desttermid;
		this.ChargeTermid = desttermid;
		this.MsgContent = msgconent.getBytes();
		this.ProductID = productid;
	}

	public Submit(String srctermid, String desttermid, byte[] msgconent,
			String productid) {
		this.SrcTermid = srctermid;
		this.DestTermid = desttermid;
		this.ChargeTermid = desttermid;
		this.MsgContent = msgconent;
		this.ProductID = productid;
	}

	public int getMsgType() {
		return MsgType;
	}

	public void setMsgType(int msgType) {
		MsgType = msgType;
	}

	public int getNeedReport() {
		return NeedReport;
	}

	public void setNeedReport(int needReport) {
		NeedReport = needReport;
	}

	public int getPriority() {
		return Priority;
	}

	public void setPriority(int priority) {
		Priority = priority;
	}

	public String getServiceID() {
		return ServiceID;
	}

	public void setServiceID(String serviceID) {
		ServiceID = serviceID;
	}

	public String getFeetype() {
		return Feetype;
	}

	public void setFeetype(String feetype) {
		Feetype = feetype;
	}

	public String getFeeCode() {
		return FeeCode;
	}

	public void setFeeCode(String feeCode) {
		FeeCode = feeCode;
	}

	public String getFixedFee() {
		return FixedFee;
	}

	public void setFixedFee(String fixedFee) {
		FixedFee = fixedFee;
	}

	public int getMsgFormat() {
		return MsgFormat;
	}

	public void setMsgFormat(int msgFormat) {
		MsgFormat = msgFormat;
	}

	public String getValidTime() {
		return ValidTime;
	}

	public void setValidTime(String validTime) {
		ValidTime = validTime;
	}

	public String getSrcTermid() {
		return SrcTermid;
	}

	public void setSrcTermid(String srcTermid) {
		SrcTermid = srcTermid;
	}

	public int getMsgLength() {
		return MsgLength;
	}

	public void setMsgLength(int msgLength) {
		MsgLength = msgLength;
	}

	public byte[] getMsgContent() {
		return MsgContent;
	}

	public void setMsgContent(byte[] msgContent) {
		MsgContent = msgContent;
		this.MsgLength=msgContent.length;
	}
	
	public void setMsgContent(String msgcontent,int msgformat) {
		if (msgformat==15){
			try {
				this.MsgContent = msgcontent.getBytes("GBK");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (msgformat==8){
			try {
				this.MsgContent = msgcontent.getBytes("iso-10646-ucs-2");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			this.MsgContent = msgcontent.getBytes();
		}
	    this.MsgFormat =msgformat;
		this.MsgLength=this.MsgContent.length;
	}


	public byte[] getReserve() {
		return Reserve;
	}

	public void setReserve(byte[] reserve) {
		Reserve = reserve;
	}

	public String getAtTime() {
		return AtTime;
	}

	public void setAtTime(String atTime) {
		AtTime = atTime;
	}

	public String getDestTermid() {
		return DestTermid;
	}

	public void setDestTermid(String destTermid) {
		DestTermid = destTermid;
	}

	public String getProductID() {
		return ProductID;
	}

	public void setProductID(String productID) {
		ProductID = productID;
	}

	public String getLinkID() {
		return LinkID;
	}

	public void setLinkID(String linkID) {
		LinkID = linkID;
	}

	public Tlv[] getOtherTlvArray() {
		return OtherTlvArray;
	}

	public void setOtherTlvArray(Tlv[] otherTlvArray) {
		OtherTlvArray = otherTlvArray;
	}

	public String getChargeTermid() {
		return ChargeTermid;
	}

	public void setChargeTermid(String chargeTermid) {
		ChargeTermid = chargeTermid;
	}
	
	public void setLong(int longmsg){
		isLongMsg = longmsg;
	}
	
	public boolean isLong(){
		if (isLongMsg==1) {
			return true;
		}else {
			return false;
		}
	}

	public void setDestTermIDCountOnly4Server(int destTermIDCount) {
		DestTermIDCount = destTermIDCount;
	}

	public int getDestTermIDCount() {
		return DestTermIDCount;
	}

	public void setDestTermIDArrayOnly4Server(String[] destTermIDArray) {
		DestTermIDArray = destTermIDArray;
	}

	public String[] getDestTermIDArray() {
		return DestTermIDArray;
	}

	public void setTP_udhi(int tP_udhi) {
		TP_udhi = tP_udhi;
	}

	public int getTP_udhi() {
		return TP_udhi;
	}
	
}
