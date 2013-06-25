package cn.com.zjtelecom.smgp.protocol;

import cn.com.zjtelecom.util.TypeConvert;

public class Tlv {
	public int Tag;
	public int Length;
	public String Value;
	public byte[] TlvBuf;

	public Tlv(int tag, String value) {
		this.Tag = tag;
		this.Length = value.length();
		this.Value = value;
        //System.out.println("tag:"+tag+" value:"+value);
		if (tag == TlvId.Mserviceid || tag == TlvId.MsgSrc
				|| tag == TlvId.SrcTermPseudo || tag == TlvId.DestTermPseudo
				|| tag == TlvId.ChargeTermPseudo || tag == TlvId.LinkID ) {
			//²ÎÊýÎªoct string
			this.TlvBuf = new byte[4 + this.Length];
			TypeConvert.int2byte2(Tag, TlvBuf, 0);
			TypeConvert.int2byte2(this.Length, TlvBuf, 2);
			System.arraycopy(this.Value.getBytes(), 0, TlvBuf, 4, this.Length);
			//System.out.println("tlv:" + Hex.rhex(TlvBuf));
		} else {
			this.TlvBuf = new byte[4 + 1];
			TypeConvert.int2byte2(Tag, TlvBuf, 0);
			TypeConvert.int2byte2(1, TlvBuf, 2);
			TypeConvert.int2byte3(Integer.parseInt(value), TlvBuf, 4);
			//System.out.println("tlv:" + Hex.rhex(TlvBuf));

		}

	}
}
