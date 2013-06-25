package cn.com.zjtelecom.smgp.bean;

import cn.com.zjtelecom.smgp.message.DeliverMessage;
import cn.com.zjtelecom.util.Hex;

public class LongDeliver {
	private DeliverMessage[] GDeliver;
	private long lastactivetime;
	private int Total = 0;
	private int Count = 0;
	private int TotalMsgLengh = 0;

	public LongDeliver(DeliverMessage delivermsg) {
		this.lastactivetime = System.currentTimeMillis();
		byte tmp[] = new byte[6];
		System.arraycopy(delivermsg.MsgContent, 0, tmp, 0, 6);
		//System.out.println(Hex.rhex(tmp));
		this.Total = delivermsg.MsgContent[4];
		//System.out.println("总共条数：" + this.Total);
		GDeliver = new DeliverMessage[this.Total];
		int curnum = delivermsg.MsgContent[5];
		if (curnum <= this.Total) {
			GDeliver[curnum - 1] = delivermsg;
			this.TotalMsgLengh = delivermsg.MsgContent.length - 6;
		}
		this.Count = 1;

	}

	public int AddDeliver(DeliverMessage delivermsg) {
		// 0表示正常
		// 1表示长短信已经满了
		// -1表示长短信出错了
		// this.Count++;
		this.lastactivetime = System.currentTimeMillis();
		if (delivermsg.MsgContent[4] != this.Total
				|| GDeliver[delivermsg.MsgContent[5] - 1] != null) {
			// 长短信出错
			return -1;
		} else {
			GDeliver[delivermsg.MsgContent[5] - 1] = delivermsg;
			this.TotalMsgLengh = this.TotalMsgLengh
					+ delivermsg.MsgContent.length - 6;
			this.Count++;
			//System.out.println("当前条数：" + this.Count);
			if (this.Count == this.Total) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	public DeliverMessage MergeDeliver() {
		byte[] mergeMsgContent = new byte[this.TotalMsgLengh];
		//System.out.println("总长度：" + this.TotalMsgLengh);
		int nav = 0;
		for (int i = 0; i < this.Total; i++) {
			System.arraycopy(this.GDeliver[i].MsgContent, 6, mergeMsgContent,
					nav, this.GDeliver[i].MsgContent.length - 6);
			nav = nav + this.GDeliver[i].MsgContent.length - 6;
		}
		GDeliver[0].MsgContent = mergeMsgContent;
		GDeliver[0].MsgLength = GDeliver[0].MsgContent.length;
		return GDeliver[0];
	}

	private static byte[] changeLongHeader(byte[] header) {
		return ("(" + header[5] + "/" + header[4] + ")").getBytes();
	}

	private static byte[] mergeByte(byte[] newhead, byte[] srcmsg) {
		byte[] tmpbyte = new byte[newhead.length + srcmsg.length];
		System.arraycopy(newhead, 0, tmpbyte, 0, newhead.length);
		System.arraycopy(srcmsg, 6, tmpbyte, newhead.length, srcmsg.length - 6);
		return tmpbyte;
	}

	public DeliverMessage[] popDeliver() {
		DeliverMessage[] returnDeliver = new DeliverMessage[Count];
		int j = 0;
		for (int i = 0; i < this.Total; i++) {
			if (GDeliver[i] != null) {
				returnDeliver[j] = GDeliver[i];
				returnDeliver[j].MsgContent = mergeByte(
						changeLongHeader(returnDeliver[j].MsgContent),
						returnDeliver[j].MsgContent);
				returnDeliver[j].MsgLength = returnDeliver[j].MsgContent.length;
				j++;
			}
		}
		return returnDeliver;
	}

	public DeliverMessage[] CheckIfOverTime(int second) {
		//System.out.println("超时:"+(System.currentTimeMillis() - this.lastactivetime)+"毫秒,"+"需要:"+second*1000);
		if ((System.currentTimeMillis() - this.lastactivetime) > second * 1000) {
			return popDeliver();
		} else {
			return null;
		}
	}
}
