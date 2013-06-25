package cn.com.zjtelecom.smgp.server.sample;

import java.io.IOException;
import cn.com.zjtelecom.smgp.bean.Deliver;
import cn.com.zjtelecom.smgp.bean.Login;
import cn.com.zjtelecom.smgp.bean.Submit;
import cn.com.zjtelecom.smgp.protocol.Tlv;
import cn.com.zjtelecom.smgp.protocol.TlvId;
import cn.com.zjtelecom.smgp.server.Server;
import cn.com.zjtelecom.smgp.server.inf.ServerEventInterface;
import cn.com.zjtelecom.smgp.server.result.LoginResult;
import cn.com.zjtelecom.smgp.server.result.SubmitResult;
import cn.com.zjtelecom.smgp.server.sample.config.ServerAccountConfFromFile;
import cn.com.zjtelecom.smgp.server.sample.config.ServerAccountConfig;
import cn.com.zjtelecom.smgp.server.util.CheckValid;
import cn.com.zjtelecom.smgp.server.util.Display;
import cn.com.zjtelecom.util.Key;

public class SampleServer {

	private static ServerAccountConfig accountconfig;
	private static String ServerVersion = "3.0";

	private static class server implements ServerEventInterface {
		private Server serverSimulate;
		private ServerConsole serverConsole;

		public server(int port) {
			this.serverSimulate = new Server(this, port);
			this.serverSimulate.start();
			this.serverConsole = new ServerConsole(this);
			this.serverConsole.start();

		}

		public void SendDeliver(Deliver deliver) {
			this.serverSimulate.SendDeliver(deliver);
		}

		public LoginResult onLogin(Login login) {
			Display.DisplayLogin(login, accountconfig);
			if (accountconfig.getPassword(login.Account) == null) {
				return (new LoginResult(52, "", ServerVersion, ""));
			} else if (Key.checkAuth(login.AuthenticatorClient, login.Account,
					accountconfig.getPassword(login.Account), String
							.valueOf(login.timestamp)) == false) {
				return (new LoginResult(21, "", ServerVersion, ""));
			} else if (!accountconfig.getIpAddress(login.Account).equals(
					login.ipaddress)) {
				return (new LoginResult(20, "", ServerVersion, ""));
			}

			return (new LoginResult(0,
					accountconfig.getPassword(login.Account), ServerVersion,
					accountconfig.getSPNum(login.Account)));
		}

		public SubmitResult onSumit(Submit submit, String account) {
			int checkvalue = 0;
			Display.DisplaySubmit(submit);

			// check SPId valid
			if (accountconfig.getSPId(account) != null) {
				int findTlv = 0;
				Tlv[] otherTlv = submit.getOtherTlvArray();
				for (int i = 0; i < otherTlv.length; i++) {
					if (otherTlv[i].Tag == TlvId.MsgSrc) {
						if (otherTlv[i].Value.equals(accountconfig
								.getSPId(account))) {
							findTlv = 1;
						} else {
							checkvalue = 8200;
						}
					}
				}
				if (findTlv == 0)
					checkvalue = 8200;

			}

			// check SrcTermid valid
			if ((submit.getSrcTermid())
					.indexOf(accountconfig.getSPNum(account)) != 0)
				checkvalue = 46;
			// check Other Valid
			if (checkvalue == 0)
				checkvalue = CheckValid.CheckSubmit(submit);
			System.out.println("SubmitResult:" + checkvalue);
			
			// ·¢ËÍÏûÏ¢
			Deliver deliver = new Deliver();
			deliver.IsReport = 0;
			deliver.MsgFormat = submit.getMsgFormat();
			deliver.SrcTermID = submit.getSrcTermid();
			deliver.DestTermID = submit.getDestTermid();
			deliver.MsgContent = submit.getMsgContent();
			deliver.MsgLength = deliver.MsgContent.length;
			
			this.SendDeliver(deliver);
			System.out.println("Message Deliverd");
			
			return new SubmitResult(checkvalue);
		}

		public void ListConnected() {
			Display.DisplayClientList(this.serverSimulate.getClientlist(),
					accountconfig);

		}

		public void Exit() {
			this.serverSimulate.stop();
			this.serverConsole.stop();
			
			
		}

	}

	public static void main(String[] args) {
		try {
			accountconfig = new ServerAccountConfFromFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		server sv = new server(8890);
	}

}
