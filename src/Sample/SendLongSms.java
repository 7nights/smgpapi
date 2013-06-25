package Sample;

import java.io.IOException;

import cn.com.zjtelecom.smgp.Client;
import cn.com.zjtelecom.smgp.bean.Result;
import cn.com.zjtelecom.smgp.bean.Submit;

public class SendLongSms {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// ��ȡ�����ļ�����
		Config config = new Config();

		// ��ȡ���ò���
		String host = config.get("smgwip");
		String account = config.get("smgwaccount");
		String passwd = config.get("smgwpasswd");
		String spid = config.get("smgwspid");
		String spnum = config.get("smgpspnum");
		int port = Integer.parseInt(config.get("smgwport"));
		String productid=config.get("productid");

		// Ŀ����룺18918911891
		String destnum = config.get("destnum");
		String content = config.get("longconent");
		

		// ��ʼ��client
		Client client = new Client(host, port, 2, account, passwd, spid, 0);

		// ����submit
		Submit submit = new Submit();
		submit.setSrcTermid(spnum);
		submit.setDestTermid(destnum);
		submit.setMsgContent(content.getBytes("iso-10646-ucs-2"));
		submit.setMsgFormat(8);
		if (productid!=null) submit.setProductID(productid);

		// ���Ͷ���
		Result[] result = client.SendLong(submit);
		for (int i = 0; i < result.length; i++) {
			System.out.println("--------------------------------");
			System.out.println("Message "+i+"");
			System.out.println("Status:" + result[i].ErrorCode);
			System.out.println("MsgID:" + result[i].ErrorDescription);
			System.out.println("--------------------------------");
		}

		// �˳�
		client.Close();

	}

}
