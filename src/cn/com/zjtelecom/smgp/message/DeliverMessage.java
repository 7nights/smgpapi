package cn.com.zjtelecom.smgp.message;

import java.util.Vector;

import cn.com.zjtelecom.smgp.bean.Deliver;
import cn.com.zjtelecom.smgp.protocol.RequestId;
import cn.com.zjtelecom.smgp.protocol.Tlv;
import cn.com.zjtelecom.smgp.protocol.TlvId;
import cn.com.zjtelecom.smgp.protocol.TlvUtil;
import cn.com.zjtelecom.util.Hex;
import cn.com.zjtelecom.util.TypeConvert;

public class DeliverMessage extends Message {
	public byte[] MsgID_BCD;
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
	public int TP_udhi;
	public String ReportMsgID;
	public Tlv[] OtherTlv;

	public DeliverMessage(Deliver deliver) {

		this.MsgID = deliver.MsgID;
		this.IsReport = deliver.IsReport;
		this.MsgFormat = deliver.MsgFormat;
		this.RecvTime = deliver.RecvTime;
		this.SrcTermID = deliver.SrcTermID;
		this.DestTermID = deliver.DestTermID;
		this.MsgLength = deliver.MsgLength;
		this.MsgContent = deliver.MsgContent;
		this.Reserve = deliver.Reserve;
		this.LinkID = deliver.LinkID;
		this.TP_udhi = deliver.TP_udhi;
		this.OtherTlv = deliver.OtherTlv;

		// 12是header,77是固定长度，还需包含tlv和msgconent
		int len = 12 + 77 + deliver.MsgLength;
		// get tlv length

		// 处理tlv
		int tlvlength = 0;
		Vector<Tlv> tlvV = new Vector<Tlv>();
		if (this.LinkID != null)
			tlvV.add(new Tlv(TlvId.LinkID, this.LinkID));
		if (this.TP_udhi != 0)
			tlvV.add(new Tlv(TlvId.TP_udhi, String.valueOf(this.TP_udhi)));
		if (this.OtherTlv != null) {
			for (int i = 0; i < this.OtherTlv.length; i++) {
				tlvV.add(this.OtherTlv[i]);
			}
		}
		Tlv[] tlvarray = new Tlv[tlvV.size()];
		for (int i = 0; i < tlvV.size(); i++) {
			tlvarray[i] = tlvV.get(i);
			tlvlength = tlvlength + tlvarray[i].TlvBuf.length;
		}

		len = len + tlvlength;
		this.buf = new byte[len];
		TypeConvert.int2byte(len, buf, 0); // PacketLength
		TypeConvert.int2byte(RequestId.Deliver, this.buf, 4); // RequestID
		TypeConvert.int2byte(this.sequence_Id, this.buf, 8); // sequence_Id

		System.arraycopy(Hex.rstr(this.MsgID), 0, this.buf, 12, 10); // msgid
		this.buf[22] = (byte) this.IsReport; // IsReport
		this.buf[23] = (byte) this.MsgFormat; // MsgFormat
		System.arraycopy(this.RecvTime.getBytes(), 0, this.buf, 24,
				this.RecvTime.length());
		System.arraycopy(this.SrcTermID.getBytes(), 0, this.buf, 38,
				this.SrcTermID.length());
		System.arraycopy(this.DestTermID.getBytes(), 0, this.buf, 59,
				this.DestTermID.length());
		this.buf[80] = (byte) this.MsgLength;
		System.arraycopy(this.MsgContent, 0, this.buf, 81, this.MsgLength);
		if (this.Reserve != null)
			System.arraycopy(this.Reserve, 0, this.buf, 81 + this.MsgLength,
					this.Reserve.length);

		int cur = 81 + this.MsgLength + 8;
		for (int i = 0; i < tlvarray.length; i++) {
			System.arraycopy(tlvarray[i].TlvBuf, 0, this.buf, cur,
					tlvarray[i].TlvBuf.length);
			cur = cur + tlvarray[i].TlvBuf.length;
		}

	}

	public DeliverMessage(byte[] buffer) {

		this.sequence_Id = TypeConvert.byte2int(buffer, 0);
		this.MsgID_BCD = new byte[10];
		System.arraycopy(buffer, 4, this.MsgID_BCD, 0, this.MsgID_BCD.length);
		this.MsgID = TypeConvert.getHexString(buffer, 4, 0, 10);
		this.IsReport = buffer[14];
		this.MsgFormat = buffer[15];
		this.RecvTime = TypeConvert.getString(buffer, 16, 0, 14);
		this.SrcTermID = TypeConvert.getString(buffer, 30, 0, 21);
		this.DestTermID = TypeConvert.getString(buffer, 51, 0, 21);
		this.MsgLength = buffer[72] & 0xFF;
		// System.out.println("len:"+this.MsgLength);
		this.MsgContent = new byte[this.MsgLength];
		System.arraycopy(buffer, 73, this.MsgContent, 0, this.MsgLength);

		if (this.IsReport == 1) {
			byte[] tmpmsgid = new byte[10];
			System.arraycopy(this.MsgContent, 3, tmpmsgid, 0, 10);
			this.ReportMsgID = Hex.rhex(tmpmsgid);
			// System.out.println("DeliverID:"+this.ReportMsgID);
		}

		this.Reserve = new byte[8];
		System.arraycopy(buffer, 73 + this.MsgLength, this.Reserve, 0,
				this.Reserve.length);

		byte[] tlv = new byte[buffer.length - 73 - this.MsgLength - 8];
		System.arraycopy(buffer, this.MsgLength + 73 + 8, tlv, 0, tlv.length);
	
		Tlv[] otherTlv = TlvUtil.TlvAnalysis(tlv);
		this.OtherTlv = otherTlv;

		for (int i = 0; i < otherTlv.length; i++) {
			if (otherTlv[i].Tag == TlvId.LinkID) {
				this.LinkID = otherTlv[i].Value ;
			} else if (otherTlv[i].Tag == TlvId.TP_udhi) {
				this.TP_udhi = Integer.parseInt(otherTlv[i].Value);
			}
		}
		// System.out.println("tlv:" + Hex.rhex(tlv));

	}
}
