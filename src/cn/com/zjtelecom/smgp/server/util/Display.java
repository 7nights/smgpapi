package cn.com.zjtelecom.smgp.server.util;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import cn.com.zjtelecom.smgp.bean.Login;
import cn.com.zjtelecom.smgp.bean.Submit;
import cn.com.zjtelecom.smgp.protocol.Tlv;
import cn.com.zjtelecom.smgp.server.ServerHandleConnect;
import cn.com.zjtelecom.smgp.server.sample.config.ServerAccountConfig;
import cn.com.zjtelecom.util.Hex;

public class Display {
	
	public static void DisplayLogin(Login login,ServerAccountConfig accountconfig){
		System.out.println("--------------------------");
		System.out.println("New Client Login");
		System.out.println("--------------------------");
		System.out.println("Account   : " + login.Account);
		System.out.println("SPNum     : " + accountconfig.getSPNum(login.Account));
		System.out.println("Version   : " + login.Version);
		System.out.println("LoginMode : " + login.LoginMode);
		System.out.println("IpAddress : " + login.ipaddress);
		System.out.println("--------------------------");
	}
	public static void DisplayClientList(HashMap<String, ClientStatus> clientlist,ServerAccountConfig accountconfig){
		System.out.println("--------------------------");
		System.out.println("List All Client");
		System.out.println("--------------------------");
		Iterator iterator = clientlist.keySet().iterator();
		int clientnum = 0;
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			ClientStatus clientStatus = clientlist.get(key);
			Vector<ServerHandleConnect> connectlist = clientStatus
					.getServerHandleConnectList();
			for (int i = 0; i < connectlist.size(); i++) {
			
				System.out.println("Client "
						+ (++clientnum));
				System.out.println("----------------");
				
				
				System.out.println("Account   : "
						+ connectlist.get(i).getAccount());
				System.out.println("IPAddress : "
						+ connectlist.get(i).getIpaddress());
				System.out.println("SPNum     : "
						+ accountconfig.getSPNum(connectlist.get(i)
								.getAccount()));
				System.out.println("SPID      : "
						+ accountconfig.getSPId(connectlist.get(i)
								.getAccount()));
				System.out.println("----------------");
			}

		}
		System.out.println("\n--------------------------");
		if (clientnum==0) System.out.println("No Client Conneted"); 
		else System.out.println("Total "+clientnum+" Client Conneted");
		System.out.println("--------------------------");

	
	}
	public static void DisplaySubmit(Submit submit) {
		System.out.println("--------------------------");
		System.out.println("Got Submit");
		System.out.println("--------------------------");
		System.out.println("MsgType         : " + submit.getMsgType());
		System.out.println("NeedReport      : " + submit.getNeedReport());
		System.out.println("Priority        : " + submit.getPriority());
		System.out.println("ServiceID       : " + submit.getServiceID());
		System.out.println("Feetype         : " + submit.getFeetype());
		System.out.println("FeeCode         : " + submit.getFeeCode());
		System.out.println("FixedFee        : " + submit.getFixedFee());
		System.out.println("MsgFormat       : " + submit.getMsgFormat());
		System.out.println("ValidTime       : " + submit.getValidTime());
		System.out.println("AtTime          : " + submit.getAtTime());
		System.out.println("SrcTermid       : " + submit.getSrcTermid());
		if (submit.getDestTermIDCount() == 1) {
			System.out.println("DestTermid      : " + submit.getDestTermid());
		} else {
			System.out
					.println("DestTermIDCount : " + submit.getDestTermIDCount());
			String[] tmpTermID = submit.getDestTermIDArray();
			for (int i = 0; i < tmpTermID.length; i++) {
				System.out.println("[" + i + "]:" + tmpTermID[i]);
			}
		}
		System.out.println("ChargeTermid 	: " + submit.getChargeTermid());
		System.out.println("MsgLength       : " + submit.getMsgLength());
		System.out.println("MsgConentHex    : "+Hex.rhex(submit.getMsgContent()));
		try {
			if (submit.getMsgFormat() == 8) {
				System.out
						.println("MsgContent      : "
								+ new String(submit.getMsgContent(),
										"iso-10646-ucs-2"));
			} else if (submit.getMsgFormat() == 15) {
				System.out.println("MsgContent      : "
						+ new String(submit.getMsgContent(), "gbk"));
			} else {
				System.out.println("MsgContent      : "
						+ new String(submit.getMsgContent()));
			}
			if (!submit.getLinkID().equals("")) System.out.println("LinkID          : " + submit.getLinkID());
			if (!submit.getProductID().equals("")) System.out.println("MServiceid      : " + submit.getProductID());
			if (submit.getOtherTlvArray()!=null){
				Tlv[] tlvarray=submit.getOtherTlvArray();
				for (int i=0;i<tlvarray.length;i++){
					switch (tlvarray[i].Tag){
					case 0x0001:System.out.println("TP_pid          : "+tlvarray[i].Value);break;
					case 0x0002:System.out.println("TP_udhi         : "+tlvarray[i].Value);break;
					//case 0x0003:System.out.println("LinkID:	"+tlvarray[i].Value);break;
					case 0x0004:System.out.println("ChargeUserType  : "+tlvarray[i].Value);break;
					case 0x0005:System.out.println("ChargeTermType  : "+tlvarray[i].Value);break;
					case 0x0006:System.out.println("ChargeTermPseudo: "+tlvarray[i].Value);break;
					case 0x0007:System.out.println("DestTermType    : "+tlvarray[i].Value);break;
					case 0x0008:System.out.println("DestTermPseudo  : "+tlvarray[i].Value);break;
					case 0x0009:System.out.println("PkTotal         : "+tlvarray[i].Value);break;
					case 0x000A:System.out.println("PkNumber        : "+tlvarray[i].Value);break;
					case 0x000B:System.out.println("SubmitMsgType   : "+tlvarray[i].Value);break;
					case 0x000C:System.out.println("SPDealReslt     : "+tlvarray[i].Value);break;
					case 0x000D:System.out.println("SrcTermType     : "+tlvarray[i].Value);break;
					case 0x000E:System.out.println("SrcTermPseudo   : "+tlvarray[i].Value);break;
					case 0x000F:System.out.println("NodesCount      : "+tlvarray[i].Value);break;
					case 0x0010:System.out.println("MsgSrc          : "+tlvarray[i].Value);break;
					case 0x0011:System.out.println("SrcType         : "+tlvarray[i].Value);break;
					//case 0x0012:System.out.println("MServiceid      : "+tlvarray[i].Value);break;
					}
				}
			}
			System.out.println("-----------------------");
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}
}
