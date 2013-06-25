package cn.com.zjtelecom.smgp.protocol;

import java.util.Vector;

import cn.com.zjtelecom.util.TypeConvert;

public class TlvUtil {
	public static Tlv[] TlvAnalysis(byte[] buffer) {
		int cur = 0;
		byte[] tlv = new byte[buffer.length - cur];
		System.arraycopy(buffer, cur, tlv, 0, tlv.length);
		//System.out.println(Hex.rhex(tlv));
		Vector<Tlv> tmptlv = new Vector<Tlv>();
		for (int loc = 0; loc < tlv.length;) {
			int tlv_Tag = TypeConvert.byte2short(tlv, loc + 0);
			int tlv_Length = TypeConvert.byte2short(tlv, loc + 2);
			String tlv_Value = "";
			//System.out.println("Tag:" + tlv_Tag);
			//System.out.println("tlv_Length:" + tlv_Length);
			if (tlv_Tag == TlvId.Mserviceid || tlv_Tag == TlvId.SrcTermPseudo
					|| tlv_Tag == TlvId.DestTermPseudo
					|| tlv_Tag == TlvId.ChargeTermPseudo
					|| tlv_Tag == TlvId.LinkID || tlv_Tag == TlvId.MsgSrc) {
				tlv_Value = TypeConvert.getString(tlv, loc + 4, 0, tlv_Length);
				//System.out.println("tlv_Value:" + tlv_Value);
				loc = loc + 4 + tlv_Length;
			} else {
				tlv_Value = String.valueOf(TypeConvert.byte2tinyint(tlv,
						loc + 4));
				//System.out.println("tlv_ValueInt:" + tlv_Value);
				loc = loc + 4 + 1;
			}

			tmptlv.add(new Tlv(tlv_Tag, tlv_Value));

		}
		Tlv[] tmptlvarray = new Tlv[tmptlv.size()];
		for (int i = 0; i < tmptlv.size(); i++) {
			tmptlvarray[i] = (Tlv) tmptlv.get(i);
		}
		return tmptlvarray;
	}
}
