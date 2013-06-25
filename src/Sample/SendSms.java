package Sample;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.com.zjtelecom.smgp.Client;
import cn.com.zjtelecom.smgp.bean.Result;
import cn.com.zjtelecom.smgp.bean.Submit;

public class SendSms {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		//读取配置文件对象
		//Config config = new Config();
		Map<String, String> map = new HashMap<String, String>();
		for(int i = 0, length = args.length; i < length; i++){
			if(args[i].charAt(0) == '-'){
				map.put(args[i].substring(1), args[i + 1]);
				i++;
			}
		}
		
		//获取配置参数
		String host = map.get("smgwip");
		String account = map.get("smgwaccount");
		String passwd = map.get("smgwpasswd");
		String spid = map.get("smgwspid");
		String spnum = map.get("smgpspnum");
		int port = Integer.parseInt(map.get("smgwport"));
		
		String destnum = map.get("destnum");
		String content = map.get("content");
		String productid= map.get("productid");
		
		//初始化client
		Client client = new Client(host, port, 2,account, passwd,spid, 0);
		
		//设置submit
		Submit submit =new Submit();
		submit.setSrcTermid(spnum);
		submit.setDestTermid(destnum);
		submit.setMsgContent(content.getBytes("iso-10646-ucs-2"));
		submit.setMsgFormat(8);
		//if (productid!=null) submit.setProductID(productid);
		
		//发送短信
		Result  result =client.Send(submit);
		System.out.println("Status:"+result.ErrorCode);
		System.out.println("MsgID:"+result.ErrorDescription);
		
		//退出
		client.Close();

	}

}
