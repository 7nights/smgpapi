package cn.com.zjtelecom.smgp.message;

import java.util.Vector;

import cn.com.zjtelecom.smgp.bean.Submit;
import cn.com.zjtelecom.smgp.protocol.RequestId;
import cn.com.zjtelecom.smgp.protocol.Tlv;
import cn.com.zjtelecom.smgp.protocol.TlvId;
import cn.com.zjtelecom.smgp.protocol.TlvUtil;
import cn.com.zjtelecom.util.Hex;
import cn.com.zjtelecom.util.TypeConvert;

public class SubmitMessage extends Message {

	private Submit submit;

	public SubmitMessage(byte[] buffer) {
		this.sequence_Id = TypeConvert.byte2int(buffer, 4);
		this.submit = new Submit();
		submit.setMsgType(buffer[8]);
		submit.setNeedReport(buffer[9]);
		submit.setPriority(buffer[10]);
		submit.setServiceID(TypeConvert.getString(buffer, 11, 0, 10));
		submit.setFeetype(TypeConvert.getString(buffer, 21, 0, 2));
		submit.setFeeCode(TypeConvert.getString(buffer, 23, 0, 6));
		submit.setFixedFee(TypeConvert.getString(buffer, 29, 0, 6));
		submit.setMsgFormat(buffer[35]);
		submit.setValidTime(TypeConvert.getString(buffer, 36, 0, 17));
		submit.setAtTime(TypeConvert.getString(buffer, 53, 0, 17));
		submit.setSrcTermid(TypeConvert.getString(buffer, 70, 0, 21));
		submit.setChargeTermid(TypeConvert.getString(buffer, 91, 0, 21));
		submit.setDestTermIDCountOnly4Server(buffer[112]);

		int cur = 113;
		if (submit.getDestTermIDCount() == 1) {
			submit.setDestTermid(TypeConvert.getString(buffer, cur, 0, 21));
			cur = cur + 21;
		} else {
			String[] destTermidArray = new String[submit.getDestTermIDCount()];
			for (int i = 0; i < submit.getDestTermIDCount(); i++) {
				destTermidArray[i] = TypeConvert.getString(buffer, cur, 0, 21);
				cur = cur + 21;
			}
			submit.setDestTermIDArrayOnly4Server(destTermidArray);
		}
		submit.setMsgLength(buffer[cur] & 0xFF);
		cur++;

		byte[] message = new byte[submit.getMsgLength()];
		System.arraycopy(buffer, cur, message, 0, message.length);
		submit.setMsgContent(message);
		cur = cur + message.length;
		byte[] reserv = new byte[8];
		System.arraycopy(buffer, cur, reserv, 0, reserv.length);
		cur = cur + 8;
		// ¼ÌÐøÐ´tlv
		if (cur < buffer.length) {
			//System.out.println("»¹ÓÐtlv×Ö¶Î");
			byte[] tlv = new byte[buffer.length - cur];
			System.arraycopy(buffer, cur, tlv, 0, tlv.length);
			Tlv[] otherTlv = TlvUtil.TlvAnalysis(tlv);
			submit.setOtherTlvArray(otherTlv);
			for (int i = 0; i < otherTlv.length; i++) {
				if (otherTlv[i].Tag == TlvId.LinkID) {
					submit.setLinkID(otherTlv[i].Value);
				} else if (otherTlv[i].Tag == TlvId.Mserviceid) {
					submit.setProductID(otherTlv[i].Value);
				}else if (otherTlv[i].Tag == TlvId.TP_udhi) {
					submit.setTP_udhi(Integer.parseInt(otherTlv[i].Value));
				}
			}

		}

	}

	public SubmitMessage(int msgtype, int needreport, int priority,
			String serviceid, String feetype, String feecode, String fixedfee,
			int msgformat, String validtime, String attime, String srctermid,
			String chargetermid, String[] desttermid, int msglength,
			byte[] msgcontent, byte[] reserve, Tlv[] tlvarray, int SequenceId) {
		this.sequence_Id = SequenceId;
		int tlvlength = 0;
		/*
		 * for (int i = 0; i < tlvarray.length; i++) { tlvlength +=
		 * tlvarray[i].Length + 4; }
		 */
		for (int i = 0; i < tlvarray.length; i++) {
			tlvlength += tlvarray[i].TlvBuf.length;
		}

		int len = 126 + 21 * desttermid.length + msgcontent.length + tlvlength;
		buf = new byte[len];
		// System.out.println("len:"+len);
		TypeConvert.int2byte(len, buf, 0); // PacketLength
		TypeConvert.int2byte(RequestId.Submit, buf, 4); // RequestID
		TypeConvert.int2byte(this.sequence_Id, buf, 8); // sequence_Id
		buf[12] = (byte) msgtype;
		buf[13] = (byte) needreport;
		buf[14] = (byte) priority;
		System.arraycopy(serviceid.getBytes(), 0, buf, 15, serviceid.length());
		System.arraycopy(feetype.getBytes(), 0, buf, 25, feetype.length());
		System.arraycopy(feecode.getBytes(), 0, buf, 27, feecode.length());
		System.arraycopy(fixedfee.getBytes(), 0, buf, 33, fixedfee.length());
		buf[39] = (byte) msgformat;
		if (validtime != null && validtime.length() > 0)
			System.arraycopy(validtime.getBytes(), 0, buf, 40, 16);
		if (attime != null && attime.length() > 0)
			System.arraycopy(attime.getBytes(), 0, buf, 57, 16);
		System.arraycopy(srctermid.getBytes(), 0, buf, 74, srctermid.length());
		System.arraycopy(chargetermid.getBytes(), 0, buf, 95, chargetermid
				.length());
		buf[116] = (byte) desttermid.length;
		int i = 0;
		for (i = 0; i < desttermid.length; i++)
			System.arraycopy(desttermid[i].getBytes(), 0, buf, 117 + i * 21,
					desttermid[i].length());

		int loc = 117 + i * 21;
		// System.out.println("DEbug msglen:"+msgcontent.length);
		// System.out.println("msg:"+new String(msgcontent));
		buf[loc] = (byte) msgcontent.length;
		System.arraycopy(msgcontent, 0, buf, loc + 1, msgcontent.length);
		loc = loc + msgcontent.length + 1;
		if (reserve != null)
			System.arraycopy(reserve, 0, buf, loc, reserve.length);
		loc = loc + 8;
		// System.out.println("loc:"+loc);
		// System.out.print("tlv:");
		for (i = 0; i < tlvarray.length; i++) {
			// System.out.println("Tag:"+(tlvarray[i].Tag));
			// System.out.print("Tag:"+Hex.rhex(tlvarray[i].TlvBuf));
			System.arraycopy(tlvarray[i].TlvBuf, 0, buf, loc,
					tlvarray[i].TlvBuf.length);
			loc = loc + tlvarray[i].TlvBuf.length;
			// System.out.println("loc:" + loc);
		}
		// System.out.println();

	}

	public Submit getSubmit() {
		return this.submit;
	}

}
