package cn.com.zjtelecom.smgp.pdu;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import cn.com.zjtelecom.smgp.bean.Submit;
import cn.com.zjtelecom.smgp.protocol.TlvId;

/*
text/vnd.wap.si
<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE si PUBLIC "-//WAPFORUM//DTD SI 1.0//EN" "http://www.wapforum.org/DTD/si.dtd">
<si>
<indication href
="http://123.123.123.123/pushservice/actionmanager"
si-id="SomePushApp1013753552819"
action="signal-high"
created="2002-02-15T08:12:32Z">
Some Push Application
</indication>
</si>
*/
public class WapPushPdu {
	private static byte[] WapPushUdhi = { 0x05, // WAP Push
			0x04, // ·Ö²ðÊý¾ÝÔªËØµÄ³¤¶È
			0x0B, //
			(byte) 0x84,//
			0x23, //
			(byte) 0xF0 //
	};

	/*private static byte[] WapPushHeader2 = { 0x29, 
		   0x06,  //pdu type (06 = push) 
		   0x06,  // headers len 
		   0x03,(byte) 0xAE,(byte) 0x81, (byte) 0xEA, //application/vnd.wap.sic; charset=utf-8 
		  (byte) 0x8D, (byte) 0xCA //content-length:
		  }; //WSP 
*/
	private static byte[] WapPushHeader2 = {
		0x25, // - transaction id (connectionless WSP) 
		0x06,// - pdu type (06 = push) 
		0x0A,// - headers len 
		0x03,(byte) 0xAE,(byte) 0x81,(byte) 0xEA, //- content type: application/vnd.wap.sic; charset=utf-8 
		(byte)0xAF,(byte)0x82,// - x-wap-application-id: w2 (wap browser) 
		(byte)0x8D,(byte)0xD9,// - content-length: 89 
		(byte)0xB4,(byte)0x84 // - push-flag: 4 
	};

	private static byte WapPushIndicator[] = { 0x02, // ±ê¼ÇÎ»
			0x05, // WAPFORUM//DTD SI 1.0//EN
			0x6A, // UTF-8
			0x00, // ±ê¼Ç¿ªÊ¼
			0x45, // <si>
			(byte) 0xC6,// <indication
			0x08, // action=signal-high ÊÇ·ñ¸ßÁÁ
			0x0C, // href="http://
			0x03 }; // ×Ö·û´®¿ªÊ¼
	private static byte WapPushDisplayTextHeader[] = { 0x00, // URL ×Ö·û´®½áÊø
			0x01, // >
			0x03 // ÄÚÈÝÃèÊö×Ö·û´®¿ªÊ¼
	};

	private static byte EndOfWapPush[] = { 
		    0x00, // ÄÚÈÝÃèÊö×Ö·û´®½áÊø
			0x01, // </si>
			0x01 // </indication>
	};
	
	public static Submit[] getWapPushSubmit(String desc, String url,
			Submit submit) {
		Submit [] arraySubmit=null;
		byte[] PushBody;
		//byte[] PushHeader;
		try {
			PushBody = getWapPushBody(desc,url);
			//PushHeader = ;
			if (PushBody.length<(140-12)){
				arraySubmit=new Submit[1];
				arraySubmit[0]=submit;
				arraySubmit[0].AddTlv(TlvId.TP_udhi, "1");
				arraySubmit[0].AddTlv(TlvId.TP_pid, "0");
				//arraySubmit[0].setMsgType(0);
				arraySubmit[0].setMsgFormat(4);
				arraySubmit[0].setServiceID("WAP");
				arraySubmit[0].AddTlv(TlvId.SubmitMsgType, "1");
				arraySubmit[0].setMsgContent(mergeByteArray(getWapPushHeader(1,1),PushBody));
			}else {
				Vector<byte[]> contentArray=SplitContent(PushBody);
				arraySubmit=new Submit[contentArray.size()];
				for (int i=0;i<contentArray.size();i++){
					arraySubmit[i]=submit;
					arraySubmit[i].AddTlv(TlvId.TP_udhi, "1");
					arraySubmit[i].AddTlv(TlvId.TP_pid, "0");
					arraySubmit[i].setMsgFormat(4);
					arraySubmit[i].setServiceID("WAP");
					//arraySubmit[i].setMsgType(0);
					arraySubmit[i].AddTlv(TlvId.SubmitMsgType, "1");
					arraySubmit[i].setMsgContent(mergeByteArray(getWapPushHeader(contentArray.size(),i+1),contentArray.get(i)));
				}
			}
			
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return arraySubmit;
	}

	public static Submit [] getWapPushSubmit(String desc,String url,String srcTermid,String destTermid,String chargeTermid,String productID){
		Submit [] arraySubmit=null;
		byte[] PushBody;
		//byte[] PushHeader;
		try {
			PushBody = getWapPushBody(desc,url);
			//PushHeader = ;
			if (PushBody.length<(140-12)){
				arraySubmit=new Submit[1];
				arraySubmit[0]=new Submit();
				arraySubmit[0].AddTlv(TlvId.TP_udhi, "1");
				arraySubmit[0].setMsgFormat(4);
				arraySubmit[0].setSrcTermid(srcTermid);
				arraySubmit[0].setChargeTermid(chargeTermid);
				arraySubmit[0].setDestTermid(destTermid);
				arraySubmit[0].setMsgContent(mergeByteArray(getWapPushHeader(1,1),PushBody));
				arraySubmit[0].setProductID(productID);
				arraySubmit[0].setMsgType(7);
			}else {
				Vector<byte[]> contentArray=SplitContent(PushBody);
				arraySubmit=new Submit[contentArray.size()];
				for (int i=0;i<contentArray.size();i++){
					arraySubmit[i]=new Submit();
					arraySubmit[i].AddTlv(TlvId.TP_udhi, "1");
					arraySubmit[i].setMsgFormat(4);
					arraySubmit[i].setSrcTermid(srcTermid);
					arraySubmit[i].setChargeTermid(chargeTermid);
					arraySubmit[i].setDestTermid(destTermid);
					arraySubmit[i].setMsgContent(mergeByteArray(getWapPushHeader(contentArray.size(),i+1),contentArray.get(i)));
					arraySubmit[i].setProductID(productID);
					arraySubmit[i].setMsgType(7);
				}
			}
			
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return arraySubmit;
	}
	
	private static Vector<byte[]> SplitContent(byte[] content) {
		ByteArrayInputStream buf = new ByteArrayInputStream(content);
		Vector<byte[]> tmpv = new Vector<byte[]>();

		int msgCount = (int) (content.length / (140 - 12) + 1);
		int LeftLen = content.length;
		for (int i = 0; i < msgCount; i++) {
			byte[] tmp = new byte[140 - 12];
			if (LeftLen < (140 - 12))
				tmp = new byte[LeftLen];
			try {
				buf.read(tmp);
				tmpv.add(tmp);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LeftLen = LeftLen - tmp.length;
		}
		return tmpv;
	}
	
	public static byte[] getWapPushHeader(int total, int cur) {
		byte[] wappushUdhi = WapPushUdhi;
		byte[] longsmsUdhi = getLongSmsUdhi(total, cur);
		int udhiLength = wappushUdhi.length + longsmsUdhi.length;
		byte[] lenHeader = new byte[1];
		lenHeader[0] = (byte) udhiLength;
		return mergeByteArray(lenHeader, mergeByteArray(wappushUdhi,
				longsmsUdhi));
	}

	public static byte[] getWapPushBody(String desc, String url) throws UnsupportedEncodingException {
		return mergeByteArray(mergeByteArray(mergeByteArray(mergeByteArray(mergeByteArray(WapPushHeader2,WapPushIndicator),url.getBytes("UTF-8")),WapPushDisplayTextHeader),desc.getBytes("UTF-8")),EndOfWapPush);
	}

	private static byte[] mergeByteArray(byte[] by1, byte[] by2) {
		byte[] tmpbyteNew = new byte[by1.length + by2.length];
		System.arraycopy(by1, 0, tmpbyteNew, 0, by1.length);
		System.arraycopy(by2, 0, tmpbyteNew, by1.length, by2.length);
		return tmpbyteNew;
	}

	private static byte[] getLongSmsUdhi(int total, int cur) {
		byte[] longSmsUdhi = { 0x00, // ±êÖ¾ÕâÊÇ¸ö·Ö²ð¶ÌÐÅ
				0x03, // ·Ö²ðÊý¾ÝÔªËØµÄ³¤¶È
				0x00, // ³¤¶ÌÐÅseqÐòºÅ
				(byte) total, // ×Ü¹²1Ìõ
				(byte) cur // µÚ1Ìõ
		};
		return longSmsUdhi;
	}


}
