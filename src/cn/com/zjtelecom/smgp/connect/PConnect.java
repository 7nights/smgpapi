package cn.com.zjtelecom.smgp.connect;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import cn.com.zjtelecom.smgp.bean.Deliver;
import cn.com.zjtelecom.smgp.bean.LongDeliver;
import cn.com.zjtelecom.smgp.bean.Result;
import cn.com.zjtelecom.smgp.bean.Submit;
import cn.com.zjtelecom.smgp.bean.SubmitBatch;
import cn.com.zjtelecom.smgp.message.ActiveTestMessage;
import cn.com.zjtelecom.smgp.message.ActiveTestRespMessage;
import cn.com.zjtelecom.smgp.message.DeliverMessage;
import cn.com.zjtelecom.smgp.message.DeliverRespMessage;
import cn.com.zjtelecom.smgp.message.ExitMessage;
import cn.com.zjtelecom.smgp.message.LoginMessage;
import cn.com.zjtelecom.smgp.message.LoginRespMessage;
import cn.com.zjtelecom.smgp.message.Package;
import cn.com.zjtelecom.smgp.message.SubmitMessage;
import cn.com.zjtelecom.smgp.message.SubmitRespMessage;
import cn.com.zjtelecom.smgp.pdu.WapPushPdu;
import cn.com.zjtelecom.smgp.protocol.RequestId;
import cn.com.zjtelecom.smgp.protocol.Tlv;
import cn.com.zjtelecom.smgp.protocol.TlvId;
import cn.com.zjtelecom.util.Hex;
import cn.com.zjtelecom.util.TypeConvert;

public class PConnect extends Thread {
	/*
	 * parameter
	 */
	private String Host;
	private int Port;
	/*
	 * socket
	 */
	private Socket GwSocket;
	private DataInputStream in;
	private DataOutputStream out;
	private boolean HasConnect=false;
	/*
	 * smgp para
	 */
	private int LoginMode;
	private String ClientID;
	private String ClientPasswd;
	private String SPID;
	/*
	 * status
	 */
	private int DisplayMode = 1;
	private int FirstLogin = 0;
	
	private int HasLogin = 0;
	private int Logout = 0;

	/*
	 * 日志文件
	 */
	private FileWriter LogFile;

	/*
	 * 临时数据存放变量
	 */
	private Package CurPack = new Package();
	private Vector<Package> undoPack = new Vector<Package>();
	private Vector deliverbuffer = new Vector();
	private int SequenceId = 0;
	private int SockTimeOut=60000;

	public int getSockTimeOut() {
		return SockTimeOut;
	}

	public void setSockTimeOut(int sockTimeOut) {
		SockTimeOut = sockTimeOut;
	}

	/*
	 * 临时存放长短信
	 */
	private HashMap<String, LongDeliver> longsmsbuffer = new HashMap<String, LongDeliver>();
	private int LongSmsOverTime = 60;

	public PConnect(String host, int port, int loginmode, String clientid,
			String clientpasswd, String spid, int displaymode) {
		this.Host = host;
		this.Port = port;
		this.LoginMode = loginmode;
		this.ClientID = clientid;
		this.ClientPasswd = clientpasswd;
		this.SPID = spid;
		this.DisplayMode = displaymode;
		this.SequenceId = GetStartSeq();
		this.HasConnect = this.Connect();
		// this.Login();
	}

	public PConnect(String host, int port, int loginmode, String clientid,
			String clientpasswd, String spid) {
		this.Host = host;
		this.Port = port;
		this.LoginMode = loginmode;
		this.ClientID = clientid;
		this.ClientPasswd = clientpasswd;
		this.SPID = spid;
		this.SequenceId = GetStartSeq();
		this.HasConnect = this.Connect();
		// this.Login();
	}

	public void setDisplayMode(int displaymode) {
		this.DisplayMode = displaymode;
	}

	public void SetLogFile(String LogFile) throws IOException {
		this.LogFile = new FileWriter(LogFile);
	}

	private boolean Connect() {
		try {
			this.GwSocket = new Socket(this.Host, this.Port);
			
			//if (this.GwSocket==null){System.out.println("can not create socket!");}
		
			this.GwSocket.setSoTimeout(this.SockTimeOut);
			//if (this.out==null){System.out.println("can not create socket!");}
			
			this.in = new DataInputStream(this.GwSocket.getInputStream());
			this.out = new DataOutputStream(this.GwSocket.getOutputStream());
			this.FirstLogin++;
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			if (this.FirstLogin > 0) {
				try {
					Thread.sleep(5000);
					if (this.DisplayMode >= 2) {
						System.out.println("Connect Fail!Reconneted");
					}
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return this.Connect();
			} else {
				//System.out.println("Can't Connected!");
				return false;
			}
		}
		
		return true;
	}

	private void ReConnect(int ms) {
		if (this.Logout == 0) {
			try {
				Thread.sleep(ms);
				System.out.println("Try Reconnect!");
				this.Connect();
				System.out.println("Try Login!");
				// this.resume();
				// Result result = this.Login();
				LoginMessage lm = new LoginMessage(this.ClientID,
						this.ClientPasswd, this.LoginMode);
				SendBuf(lm.getBuf());
				
				// out.write(lm.getBuf());
				// System.out.println("ErrorCode:"+result.ErrorCode);
				// System.out.println("ErrorDescription:"+result.ErrorDescription);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void run() {
		do {
			try {
				int PackLen = in.readInt();
				byte[] Message = new byte[PackLen - 4];

				in.read(Message);

				// 写日志
				if (this.LogFile != null) {
					this.LogFile.write("[" + GetTime() + "]" + "Recv:"
							+ Hex.rhex(TypeConvert.int2byte((PackLen)))
							+ Hex.rhex(Message) + "\n");
					this.LogFile.flush();
				}

				// System.out.println("Recevice:"+Hex.rhex(Message));

				this.CurPack = new Package(Message);
				if (this.DisplayMode >= 2)
					DisplayPackage(PackLen, Message, 0);
				switch (this.CurPack.ReqestId) {
				case 0x00000004: // ActiveTest
					SendBuf((new ActiveTestRespMessage()).getBuf());
					// out.write((new ActiveTestRespMessage()).getBuf());
					if (this.DisplayMode >= 3) {
						DisplayPackage((new ActiveTestRespMessage()).getBuf(),
								1);
					}
					break;
				case 0x00000003: // Deliver
					DeliverMessage dm = new DeliverMessage(this.CurPack.Message);
					DeliverRespMessage drm = new DeliverRespMessage(
							dm.MsgID_BCD, dm.getSequence_Id(), 0);
					SendBuf(drm.getBuf());
					// out.write(drm.getBuf());
					if (this.DisplayMode >= 2) {
						DisplayPackage(drm.getBuf(), 1);
					}

					// 检查是否长短信
					if (dm.TP_udhi == 1) {
						// 长短信
						AddLongSms(dm);
						// System.out.println("收到长短信:" + dm.TP_udhi);
					} else {
						// 否
						// System.out.println("收到putong短信" + dm.TP_udhi);
						this.deliverbuffer.add(dm);

					}
					break;
				case 0x80000001: // Login_Resp
					break;
				case 0x80000002: // Submit_Resp
					break;
				case 0x80000006: // Exit_Resp
					break;
				default:
					break;

				}

				if (this.CurPack.ReqestId == RequestId.Deliver
						|| this.CurPack.ReqestId == RequestId.Login_Resp
						|| this.CurPack.ReqestId == RequestId.Submit_Resp
						|| this.CurPack.ReqestId == RequestId.Exit_Resp
						|| CheckLongSmsOverTime(this.LongSmsOverTime)) {
					if (this.CurPack.ReqestId == RequestId.Submit_Resp
							|| this.CurPack.ReqestId == RequestId.Login_Resp
							|| this.CurPack.ReqestId == RequestId.Exit_Resp)
						this.undoPack.add(this.CurPack);
					synchronized (this) {
						notify();
					}
				}
				// if (CheckLongSmsOverTime(this.LongSmsOverTime)) notify();
			} catch (SocketTimeoutException e) {
				SendActive();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				if (this.DisplayMode >= 1 && this.Logout == 0) {
					System.out.println("Lost Connect,ReConnect");
				}
				if (this.Logout == 0)
					this.ReConnect(1000);
				// this.resume();
				continue;
			}

		} while (true);
	}

	public void setLSmsOverTime(int second) {
		this.LongSmsOverTime = second;
	}
	
	private void SendActive(){
		ActiveTestMessage activeTestMessage =new ActiveTestMessage();
		try {
			SendBuf(activeTestMessage.getBuf());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void AddLongSms(DeliverMessage dm) {
		// int curstat=0;
		if (this.longsmsbuffer.get(dm.SrcTermID) != null) {
			int curstat = this.longsmsbuffer.get(dm.SrcTermID).AddDeliver(dm);
			// 0表示正常
			// 1表示长短信已经满了
			// -1表示长短信出错了

			if (curstat == 0) {
				return;
			} else if (curstat == 1) {
				this.deliverbuffer.add(this.longsmsbuffer.get(dm.SrcTermID)
						.MergeDeliver());
				this.longsmsbuffer.remove(dm.SrcTermID);
			} else if (curstat == -1) {
				DeliverMessage[] tmpdeliver = this.longsmsbuffer.get(
						dm.SrcTermID).popDeliver();
				for (int i = 0; i < tmpdeliver.length; i++) {
					this.deliverbuffer.add(tmpdeliver[i]);
				}
				this.longsmsbuffer.put(dm.SrcTermID, new LongDeliver(dm));
			}
		} else {
			this.longsmsbuffer.put(dm.SrcTermID, new LongDeliver(dm));
		}
		return;
	}

	public synchronized Deliver OnDeliver() {
		while (this.deliverbuffer.size() == 0) {
			try {
				synchronized (this) {
					// System.out.println("Wait Deliver!");
					wait();
				}

			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// System.out.println("Size:"+this.deliverbuffer.size());
		Deliver dm = new Deliver((DeliverMessage) (this.deliverbuffer.get(0)));
		this.deliverbuffer.remove(0);
		// System.out.println("Return Deliver");
		return dm;
	}

	private boolean CheckLongSmsOverTime(int second) {
		Iterator spit = this.longsmsbuffer.keySet().iterator();
		// System.out.println("检查是否有长短信被执行");
		while (spit.hasNext()) {
			DeliverMessage[] tmpDeliverMsg;
			String key = "";
			if ((tmpDeliverMsg = this.longsmsbuffer.get(
					(key = (String) spit.next())).CheckIfOverTime(second)) != null) {
				for (int i = 0; i < tmpDeliverMsg.length; i++) {
					this.deliverbuffer.add(tmpDeliverMsg[i]);
				}

				this.longsmsbuffer.remove(key);
				return true;
			}

		}
		return false;
	}

	public synchronized void LogOut() {
		ExitMessage em = new ExitMessage();
		this.Logout = 1;
		try {
			SendBuf(em.getBuf());

			long sendTime = getTimeStamp();
			Package tmppack = new Package();
			while ((getTimeStamp() - sendTime) < 60000
					&& ((tmppack = checkPackage(0, RequestId.Exit_Resp)) != null)) {
				try {
					synchronized (this) {
						wait(60000);
					}

				} catch (RuntimeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			this.GwSocket.close();
			if (this.LogFile != null) {
				this.LogFile.close();
			}
			this.stop();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public synchronized Deliver getDeliver() {
		if (this.deliverbuffer.size() == 0) {
			return null;
		} else {
			Deliver dm = new Deliver((DeliverMessage) (this.deliverbuffer
					.get(0)));
			this.deliverbuffer.remove(0);
			// System.out.println("Return Deliver");
			return dm;
		}
	}

	public synchronized Result Login() {
       if (this.HasConnect == false) {
    	   return new Result(-2,"Can not creat socket!");
       }
		Result result = new Result();
		try {
			LoginMessage lm = new LoginMessage(this.ClientID,
					this.ClientPasswd, this.LoginMode);
			SendBuf(lm.getBuf());
			// out.write(lm.getBuf());

			while (this.CurPack == null
					|| this.CurPack.ReqestId != RequestId.Login_Resp) {
				try {
					synchronized (this) {
						wait();
					}

				} catch (RuntimeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			LoginRespMessage lrm = new LoginRespMessage(this.CurPack.Message);
			result.ErrorCode = lrm.getStatus();
			result.ErrorDescription = "SerVersion:" + lrm.getServerVersion()
					+ " ShareKey:" + Hex.rhex(lrm.getAuthenticatorServer());

			if (lrm.getStatus() == 0)
				this.HasLogin = 1;

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public synchronized Result[] SendLong(Submit submit) {

		Vector<byte[]> splitContent = SplitContent(submit.getMsgContent());
		Result[] result = new Result[splitContent.size()];

		for (int i = 0; i < splitContent.size(); i++) {
			Submit tmpSubmit = submit;
			tmpSubmit.setMsgContent(addContentHeader(splitContent.get(i),
					splitContent.size(), i + 1));
			// System.out.println("msglen:"+tmpSubmit.getMsgLength());
			//tmpSubmit.AddTlv(TlvId.TP_udhi, "1");
			tmpSubmit.setTP_udhi(1);
			tmpSubmit.AddTlv(TlvId.PkNumber, String.valueOf(i + 1));
			tmpSubmit
					.AddTlv(TlvId.PkTotal, String.valueOf(splitContent.size()));
			result[i] = this.Send(tmpSubmit);

		}

		return result;
	}

	public synchronized Result SendWapPush(String desc, String url,
			String srcTermId, String destTermid, String productID) {

		Submit[] submitarray = WapPushPdu.getWapPushSubmit(desc, url,
				srcTermId, destTermid, destTermid, productID);
		Result result = null;
		Result resulttmp = null;
		for (int i = 0; i < submitarray.length; i++) {
			resulttmp = this.Send(submitarray[i]);
			// System.out.println(Hex.rhex(submitarray[i].getMsgContent()));
			if (result == null || resulttmp.ErrorCode != 0) {
				result = resulttmp;
			}
		}
		return result;
	}

	public synchronized Result SendWapPush(String desc, String url,
			Submit submit) {

		Submit[] submitarray = WapPushPdu.getWapPushSubmit(desc, url, submit);
		Result result = null;
		Result resulttmp = null;
		for (int i = 0; i < submitarray.length; i++) {
			resulttmp = this.Send(submitarray[i]);
			// System.out.println(Hex.rhex(submitarray[i].getMsgContent()));
			if (result == null || resulttmp.ErrorCode != 0) {
				result = resulttmp;
			}
		}
		return result;
	}

	/*
	 * public synchronized Result SendWapPush(String disc, String url, String
	 * srcTermId, String destTermid, String productID) {
	 * 
	 * Submit submit = new Submit(); try {
	 * submit.setMsgContent(getWapPushPdu(disc, url)); } catch
	 * (UnsupportedEncodingException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } submit.setMsgFormat(4);
	 * submit.AddTlv(TlvId.TP_udhi, "1"); submit.setDestTermid(destTermid);
	 * submit.setSrcTermid(srcTermId); submit.setProductID(productID);
	 * submit.setMsgType(7);
	 * 
	 * System.out.println("msg len:" + submit.getMsgLength());
	 * 
	 * return this.Send(submit); }
	 */

	/*
	 * private static byte[] getWapPushPdu(String disc, String url)
	 * 
	 * throws UnsupportedEncodingException {
	 * 
	 * byte[] WapPushDisc = disc.getBytes("UTF-8"); byte[] WapPushUrl =
	 * url.getBytes("UTF-8");
	 * 
	 * byte WapPushHeader1[] = { 0x0B, // WAP PUSH 头部的总长度 // 长短信 0x00, //
	 * 标志这是个分拆短信 0x03, // 分拆数据元素的长度 0x03, // 长短信seq序号 0x01, // 总共1条 0x01, // 第1条
	 * // WAP PUSH 0x05, // WAP Push 0x04, // 分拆数据元素的长度 0x0B, // (byte) 0x84,//
	 * 0x23, // (byte) 0xF0 // }; byte WapPushHeader2[] = { 0x29, 0x06, 0x06,
	 * 0x03, (byte) 0xAE, (byte) 0x81, (byte) 0xEA, (byte) 0x8D, (byte) 0xCA };
	 * // WSP
	 * 
	 * byte WapPushIndicator[] = { 0x02, // 标记位 0x05, // WAPFORUM//DTD SI
	 * 1.0//EN 0x6A, // UTF-8 0x00, // 标记开始 0x45, // <si> (byte) 0xC6,//
	 * <indication 0x0C, // href="http:// 0x03 }; // 字符串开始 byte
	 * WapPushDisplayTextHeader[] = { 0x00, // URL 字符串结束 0x01, // > 0x03 //
	 * 内容描述字符串开始 };
	 * 
	 * byte EndOfWapPush[] = { 0x00, //内容描述字符串结束 0x01, //</si> 0x01
	 * //</indication> };
	 * 
	 * byte returnbyte[] = new byte[35 + WapPushDisc.length +
	 * WapPushUrl.length]; int nav = 0; System.arraycopy(WapPushHeader1, 0,
	 * returnbyte, nav, WapPushHeader1.length); nav = nav +
	 * WapPushHeader1.length;
	 * 
	 * System.arraycopy(WapPushHeader2, 0, returnbyte, nav,
	 * WapPushHeader2.length); nav = nav + WapPushHeader2.length;
	 * 
	 * System.arraycopy(WapPushIndicator, 0, returnbyte, nav,
	 * WapPushIndicator.length); nav = nav + WapPushIndicator.length;
	 * 
	 * System.arraycopy(WapPushUrl, 0, returnbyte, nav, WapPushUrl.length); nav
	 * = nav + WapPushUrl.length;
	 * 
	 * System.arraycopy(WapPushDisplayTextHeader, 0, returnbyte, nav,
	 * WapPushDisplayTextHeader.length); nav = nav +
	 * WapPushDisplayTextHeader.length;
	 * 
	 * System.arraycopy(WapPushDisc, 0, returnbyte, nav, WapPushDisc.length);
	 * nav = nav + WapPushDisc.length;
	 * 
	 * System.arraycopy(EndOfWapPush, 0, returnbyte, nav, EndOfWapPush.length);
	 * nav = nav + EndOfWapPush.length;
	 * 
	 * return returnbyte;
	 * 
	 * }
	 */
/*
	public synchronized Result SendFlashSms(Submit submit) {
		submit.setMsgContent(addFlashSmsHeader(submit.getMsgContent()));
		submit.AddTlv(TlvId.TP_udhi, "1");
		// submit.setMsgFormat(0x18);
		System.out.println("内容：" + Hex.rhex(submit.getMsgContent()));
		return this.Send(submit);
	}*/
/*
	private static byte[] addFlashSmsHeader(byte[] content) {
		// int curlong = content.length;
		byte[] newcontent = new byte[content.length + 2];
		newcontent[0] = 0x00;
		newcontent[1] = 0x01;
		System.arraycopy(content, 0, newcontent, 2, content.length);
		return newcontent;
	}
*/
	private static byte[] addContentHeader(byte[] content, int total, int num) { // 为了加消息头参数为（原数据,总条数,当前条数）
		// int curlong = content.length;
		byte[] newcontent = new byte[content.length + 6];
		newcontent[0] = 0x05;
		newcontent[1] = 0x00;
		newcontent[2] = 0x03;
		newcontent[4] = (byte) total;
		newcontent[5] = (byte) num;
		System.arraycopy(content, 0, newcontent, 6, content.length);

		return newcontent;
	}
	
	private static Vector<byte[]> SplitContent(byte[] content) {
		ByteArrayInputStream buf = new ByteArrayInputStream(content);
		Vector<byte[]> tmpv = new Vector<byte[]>();

		int msgCount = (int) (content.length / (140 - 6) + 1);
		int LeftLen = content.length;
		for (int i = 0; i < msgCount; i++) {
			byte[] tmp = new byte[140 - 6];
			if (LeftLen < (140 - 6))
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

	public synchronized Result Send(Submit submit) {

		/*
		 * if (submit.isLong()) { // 长短信 // submit.getMsgContent().length; }
		 * else { }
		 */

		if (submit.getMsgContent().length > 200) {
			return (new Result(4, "Message too Long"));
		}
		Vector<Tlv> tlv = new Vector<Tlv>();
		if (this.SPID != null && !this.SPID.equals("")) {
			tlv.add(new Tlv(TlvId.MsgSrc, this.SPID));
		}
		if (submit.getProductID() != null && !submit.getProductID().equals("")) {
			tlv.add(new Tlv(TlvId.Mserviceid, submit.getProductID()));
		}
		
		if (submit.getTP_udhi()==1){
			tlv.add(new Tlv(TlvId.TP_udhi,String.valueOf(1)));
		}

		if (submit.getLinkID() != null && !submit.getLinkID().equals("")) {
			tlv.add(new Tlv(TlvId.LinkID, submit.getLinkID()));
		}

		if (submit.getOtherTlvArray() != null) {
			for (int i = 0; i < submit.getOtherTlvArray().length; i++) {
				tlv.add(submit.getOtherTlvArray()[i]);
			}
		}
		Tlv[] tlvarray = new Tlv[tlv.size()];
		// System.out.println("tlvlen:"+tlv.size());
		for (int i = 0; i < tlv.size(); i++) {
			// System.out.println(((Tlv)tlv.get(i)).Value);
			tlvarray[i] = (Tlv) tlv.get(i);
		}

		String[] desttermidarray = new String[1];
		desttermidarray[0] = submit.getDestTermid();
		if (SequenceId++ == 0x7FFFFF) {
			SequenceId = 0;
		}

		int tmpseq = SequenceId;
		SubmitMessage sm = new SubmitMessage(submit.getMsgType(), submit
				.getNeedReport(), submit.getPriority(), submit.getServiceID(),
				submit.getFeetype(), submit.getFeeCode(), submit.getFixedFee(),
				submit.getMsgFormat(), submit.getValidTime(), submit
						.getAtTime(), submit.getSrcTermid(), submit
						.getChargeTermid(), desttermidarray, submit
						.getMsgLength(), submit.getMsgContent(), submit
						.getReserve(), tlvarray, tmpseq);
		try {
			// System.out.println(Hex.rhex(sm.getBuf()));
			SendBuf(sm.getBuf());
			// out.write(sm.getBuf());
			if (this.DisplayMode >= 2) {
				DisplayPackage(sm.getBuf(), 1);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return (new Result(-1, "Socket Error!"));

		}

		long sendTime = getTimeStamp();

		/*
		 * while ((getTimeStamp() - sendTime) < 60000 && (this.CurPack == null
		 * || this.CurPack.ReqestId != RequestId.Submit_Resp ||
		 * this.CurPack.SequenceId != tmpseq)) {
		 */
		Package tmppack = new Package();
		while ((getTimeStamp() - sendTime) < 60000
				&& ((tmppack = checkPackage(tmpseq, RequestId.Submit_Resp)) == null)) {
			// checkPackage(int, int)
			try {
				synchronized (this) {
					wait(60000);
				}

			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (tmppack != null) {
			SubmitRespMessage srm = new SubmitRespMessage(tmppack.Message);
			return (new Result(srm.getStatus(), srm.getMsgID()));
		} else {
			System.out.println("SubmitResp超时！" + "Seq:" + tmpseq);
			return (new Result(-1, "00000000000000000000"));

		}

		// if (srm==null) {
		// return (new Result(-1,"Login Fail"));
		// } else {

		// }

	}

	private static Long getTimeStamp() {
		return (new java.util.Date()).getTime();
	}

	public synchronized Result SendBatch(SubmitBatch submit) {

		Vector tlv = new Vector();
		if (this.SPID != null && !this.SPID.equals("")) {
			tlv.add(new Tlv(TlvId.MsgSrc, this.SPID));
		}
		if (submit.getProductID() != null && !submit.getProductID().equals("")) {
			tlv.add(new Tlv(TlvId.Mserviceid, submit.getProductID()));
		}

		if (submit.getLinkID() != null && !submit.getLinkID().equals("")) {
			tlv.add(new Tlv(TlvId.LinkID, submit.getLinkID()));
		}

		if (submit.getOtherTlvArray() != null) {
			for (int i = 0; i < submit.getOtherTlvArray().length; i++) {
				tlv.add(submit.getOtherTlvArray()[i]);
			}
		}
		Tlv[] tlvarray = new Tlv[tlv.size()];
		// System.out.println("tlvlen:"+tlv.size());
		for (int i = 0; i < tlv.size(); i++) {
			// System.out.println(((Tlv)tlv.get(i)).Value);
			tlvarray[i] = (Tlv) tlv.get(i);
		}

		// String[] desttermidarray = new String[1];
		// desttermidarray[0] = submit.getDestTermid();
		if (SequenceId++ == 0x7FFFFF) {
			SequenceId = 0;
		}
		SubmitMessage sm = new SubmitMessage(submit.getMsgType(), submit
				.getNeedReport(), submit.getPriority(), submit.getServiceID(),
				submit.getFeetype(), submit.getFeeCode(), submit.getFixedFee(),
				submit.getMsgFormat(), submit.getValidTime(), submit
						.getAtTime(), submit.getSrcTermid(), submit
						.getChargeTermid(), submit.getDestTermid(), submit
						.getMsgLength(), submit.getMsgContent(), submit
						.getReserve(), tlvarray, this.SequenceId);
		try {
			// System.out.println(Hex.rhex(sm.getBuf()));
			SendBuf(sm.getBuf());
			// out.write(sm.getBuf());
			if (this.DisplayMode >= 2) {
				DisplayPackage(sm.getBuf(), 1);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return (new Result(-1, "Socket Error!"));

		}

		while (this.CurPack == null
				|| this.CurPack.ReqestId != RequestId.Submit_Resp) {
			try {
				synchronized (this) {
					wait();
				}

			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Result result = new Result();
		SubmitRespMessage srm = new SubmitRespMessage(this.CurPack.Message);
		// if (srm==null) {
		// return (new Result(-1,"Login Fail"));
		// } else {
		return (new Result(srm.getStatus(), srm.getMsgID()));
		// }

	}

	private void DisplayPackage(int len, byte[] message, int sendOrReceive) {
		if (sendOrReceive == 0) {
			System.out.println("-------------Receive Package-------------");
		} else {
			System.out.println("--------------Send  Package--------------");
		}
		System.out.println("Length:" + len);
		System.out.println("Pack:" + Hex.rhex(message));
	}

	private void DisplayPackage(byte[] message, int sendOrReceive) {
		int len = TypeConvert.byte2int(message, 0);
		byte[] messagetmp = new byte[message.length - 4];
		// System.arraycopy(src, srcPos, dest, destPos, length)
		System.arraycopy(message, 4, messagetmp, 0, messagetmp.length);
		// System.out.println("message:"+Hex.rhex(message));
		// System.out.println("messagetmp:"+Hex.rhex(messagetmp));
		DisplayPackage(len, messagetmp, sendOrReceive);
	}

	private synchronized void SendBuf(byte[] buf) throws IOException {
		
		this.out.write(buf);
		if (this.LogFile != null) {
			this.LogFile.write("[" + GetTime() + "]" + "Send:" + Hex.rhex(buf)
					+ "\n");
			this.LogFile.flush();
		}
	}

	private Package checkPackage(int seq, int reqid) {
		// System.out.println("PackNum:"+ this.undoPack.size());
		for (int i = 0; i < this.undoPack.size(); i++) {
			Package pk = (Package) this.undoPack.get(i);

			if (pk.SequenceId == seq && pk.ReqestId == reqid) {
				this.undoPack.remove(i);
				return pk;
			}
			if (((new java.util.Date()).getTime() - pk.timestamp) > 60000) {
				// 超时移除
				this.undoPack.remove(i);
			}
		}

		return null;
	}

	private static String GetTime() {
		String TimeStamp = "";
		Calendar now = Calendar.getInstance();
		// now.getTime();
		SimpleDateFormat bartDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		return (bartDateFormat.format(now.getTime()));
		/*
		 * TimeStamp =FormatInt(Integer.toString(now.YEAR)) +"年" +
		 * FormatInt(Integer.toString(now.MONTH + 1))+"月" +
		 * FormatInt(Integer.toString(now.DAY_OF_MONTH))+"日 " +
		 * FormatInt(Integer.toString(now.HOUR))+":" +
		 * FormatInt(Integer.toString(now.MINUTE ))+":" +
		 * FormatInt(Integer.toString(now.SECOND)); return TimeStamp;
		 */
	}

	private static int GetStartSeq() {
		SimpleDateFormat bartDateFormat = new SimpleDateFormat("HHmmss");
		Calendar now = Calendar.getInstance();
		// now.getTime();
		return Integer.parseInt(bartDateFormat.format(now.getTime()));
	}
}
