package cn.com.zjtelecom.smgp.connect;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import cn.com.zjtelecom.smgp.Exception.SubmitException;
import cn.com.zjtelecom.smgp.bean.Deliver;
import cn.com.zjtelecom.smgp.bean.LongDeliver;
import cn.com.zjtelecom.smgp.bean.Result;
import cn.com.zjtelecom.smgp.bean.Submit;
import cn.com.zjtelecom.smgp.bean.SubmitBatch;
import cn.com.zjtelecom.smgp.bean.SubmitResp;
import cn.com.zjtelecom.smgp.inf.ClientEventInterface;
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
import cn.com.zjtelecom.util.DateUtil;
import cn.com.zjtelecom.util.Hex;
import cn.com.zjtelecom.util.TypeConvert;

public class PConnectEvent extends Thread {

	private ClientEventInterface clientEventInterface;

	/*
	 * 临时存放submit
	 */
	private HashMap<Integer, SubmitResp> tmpSubmitResp = new HashMap<Integer, SubmitResp>();
	private int submitRespTimeOut = 60000;
	private int CheckSubmitRespInterval = 600000;
	private Long LastCheckSubmitTime = 0L;

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
	// private Vector deliverbuffer = new Vector();
	private int SequenceId = 0;

	/*
	 * 临时存放长短信
	 */
	private HashMap<String, LongDeliver> longsmsbuffer = new HashMap<String, LongDeliver>();
	private int LongSmsOverTime = 60;

	public PConnectEvent(ClientEventInterface clientEventInterface,
			String host, int port, int loginmode, String clientid,
			String clientpasswd, String spid, int displaymode) {
		this.clientEventInterface = clientEventInterface;
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

	public PConnectEvent(ClientEventInterface clientEventInterface,
			String host, int port, int loginmode, String clientid,
			String clientpasswd, String spid) {
		this.clientEventInterface = clientEventInterface;
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
			this.GwSocket.setSoTimeout(60000);
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
						this.onDeliver(dm);

					}
					break;
				case 0x80000001: // Login_Resp
					break;
				case 0x80000002: // Submit_Resp
					SubmitRespMessage submitRespMessage = new SubmitRespMessage(
							this.CurPack.Message);
					this.onSubmitResp(submitRespMessage);
					break;
				case 0x80000006: // Exit_Resp
					break;
				default:
					break;

				}

				// 检查是否有超时的submitResp,防止内存占用
				this.checkSubmitResp();

				if (this.CurPack.ReqestId == RequestId.Deliver
						|| this.CurPack.ReqestId == RequestId.Login_Resp
						// || this.CurPack.ReqestId == RequestId.Submit_Resp
						|| this.CurPack.ReqestId == RequestId.Exit_Resp
						|| CheckLongSmsOverTime(this.LongSmsOverTime)) {
					if (this.CurPack.ReqestId == RequestId.Login_Resp
							|| this.CurPack.ReqestId == RequestId.Exit_Resp)
						this.undoPack.add(this.CurPack);
					synchronized (this) {
						notify();
					}
				}
				// if (CheckLongSmsOverTime(this.LongSmsOverTime)) notify();

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
				/*
				 * 原先增加到buffer
				 * this.deliverbuffer.add(this.longsmsbuffer.get(dm.SrcTermID)
				 * .MergeDeliver());
				 */
				// 这里需要处理
				this.onDeliver(this.longsmsbuffer.get(dm.SrcTermID)
						.MergeDeliver());
				this.longsmsbuffer.remove(dm.SrcTermID);
			} else if (curstat == -1) {
				DeliverMessage[] tmpdeliver = this.longsmsbuffer.get(
						dm.SrcTermID).popDeliver();
				for (int i = 0; i < tmpdeliver.length; i++) {
					this.onDeliver(tmpdeliver[i]);
					// this.deliverbuffer.add(tmpdeliver[i]);
				}
				this.longsmsbuffer.put(dm.SrcTermID, new LongDeliver(dm));
			}
		} else {
			this.longsmsbuffer.put(dm.SrcTermID, new LongDeliver(dm));
		}
		return;
	}

	/*
	 * public synchronized Deliver OnDeliver() { while
	 * (this.deliverbuffer.size() == 0) { try { synchronized (this) { //
	 * System.out.println("Wait Deliver!"); wait(); }
	 * 
	 * } catch (RuntimeException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (InterruptedException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } } //
	 * System.out.println("Size:"+this.deliverbuffer.size()); Deliver dm = new
	 * Deliver((DeliverMessage) (this.deliverbuffer.get(0)));
	 * this.deliverbuffer.remove(0); // System.out.println("Return Deliver");
	 * return dm; }
	 */
	private boolean CheckLongSmsOverTime(int second) {
		Iterator spit = this.longsmsbuffer.keySet().iterator();
		// System.out.println("检查是否有长短信被执行");
		while (spit.hasNext()) {
			DeliverMessage[] tmpDeliverMsg;
			String key = "";
			if ((tmpDeliverMsg = this.longsmsbuffer.get(
					(key = (String) spit.next())).CheckIfOverTime(second)) != null) {
				for (int i = 0; i < tmpDeliverMsg.length; i++) {
					// this.deliverbuffer.add(tmpDeliverMsg[i]);
					this.onDeliver(tmpDeliverMsg[i]);
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

	private synchronized void onDeliver(DeliverMessage deliverMessage) {
		Deliver dm = new Deliver(deliverMessage);
		this.clientEventInterface.onDeliver(dm);
	}

	private synchronized void onSubmitResp(SubmitRespMessage submitRespMessage) {
		int seq = submitRespMessage.getSequence_Id();
		SubmitResp submitResp = this.tmpSubmitResp.get(seq);
		if (submitResp != null) {
			this.tmpSubmitResp.remove(seq);
		} else {
			submitResp = new SubmitResp();
		}
		submitResp.setMsgID(submitRespMessage.getMsgID());
		submitResp.setResultCode(submitRespMessage.getStatus());
		submitResp.setSequenceID(submitRespMessage.getSequence_Id());

		this.clientEventInterface.OnSubmitResp(submitResp);
	}

	public synchronized Result Login() {

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

	public synchronized Integer[] SendLong(Submit submit)
			throws SubmitException {

		Vector<byte[]> splitContent = SplitContent(submit.getMsgContent());
		Integer[] result = new Integer[splitContent.size()];

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

	private static byte[] addFlashSmsHeader(byte[] content) {
		// int curlong = content.length;
		byte[] newcontent = new byte[content.length + 2];
		newcontent[0] = 0x00;
		newcontent[1] = 0x01;
		System.arraycopy(content, 0, newcontent, 2, content.length);
		return newcontent;
	}

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

	public synchronized int Send(Submit submit) throws SubmitException {

		if (submit.getMsgContent().length > 200) {

			throw new SubmitException((new Result(4, "Message too Long")));
		}

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
			throw new SubmitException(new Result(-1, "Socket Error!"));

		}

		tmpSubmitResp.put(tmpseq, new SubmitResp(submit));

		return tmpseq;

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

	private void checkSubmitResp() {
		if ((DateUtil.getTimeStampL() - this.LastCheckSubmitTime) < this.CheckSubmitRespInterval)
			return;
		this.LastCheckSubmitTime = DateUtil.getTimeStampL();
		Iterator iteratorSubmitResp = this.tmpSubmitResp.keySet().iterator();
		while (iteratorSubmitResp.hasNext()) {
			int key = (Integer) iteratorSubmitResp.next();
			SubmitResp submitResp = this.tmpSubmitResp.get(key);
			// 检查是否超时
			if ((DateUtil.getTimeStampL() - submitResp.getSendTime()) > this.submitRespTimeOut) {
				this.tmpSubmitResp.remove(key);
				System.out.println("SubmitResp超时");
			}

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
