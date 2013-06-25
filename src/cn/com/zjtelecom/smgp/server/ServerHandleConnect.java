package cn.com.zjtelecom.smgp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import cn.com.zjtelecom.smgp.bean.Deliver;
import cn.com.zjtelecom.smgp.bean.Login;
import cn.com.zjtelecom.smgp.bean.Submit;
import cn.com.zjtelecom.smgp.message.ActiveTestMessage;
import cn.com.zjtelecom.smgp.message.ActiveTestRespMessage;
import cn.com.zjtelecom.smgp.message.DeliverMessage;
import cn.com.zjtelecom.smgp.message.ExitRespMessage;
import cn.com.zjtelecom.smgp.message.LoginMessage;
import cn.com.zjtelecom.smgp.message.LoginRespMessage;
import cn.com.zjtelecom.smgp.message.Package;
import cn.com.zjtelecom.smgp.message.SubmitMessage;
import cn.com.zjtelecom.smgp.message.SubmitRespMessage;
import cn.com.zjtelecom.smgp.protocol.RequestId;
import cn.com.zjtelecom.smgp.server.result.LoginResult;
import cn.com.zjtelecom.smgp.server.result.SubmitResult;
import cn.com.zjtelecom.util.DateUtil;
import cn.com.zjtelecom.util.Hex;

public class ServerHandleConnect extends Thread {
	private Socket clientsocket;

	private String ipaddress;
	private String account;
	private int loginMode = 0;

	private DataInputStream in;
	private DataOutputStream out;
	private Server serversim;
	private Long LastActiveTime;
	private int TimeOut;

	private boolean hasLogin = false;

	public ServerHandleConnect(Server server, Socket socket, int timeout) {

		this.serversim = server;
		this.clientsocket = socket;
		this.TimeOut = timeout;
	}

	public void run() {
		try {
			this.clientsocket.setSoTimeout(this.TimeOut * 1000);
			this.in = new DataInputStream(this.clientsocket.getInputStream());
			this.out = new DataOutputStream(this.clientsocket.getOutputStream());
			do {
				int PackLen = in.readInt();

				if (PackLen > 2500)
					exit(); // ������Ȳ���

				byte[] Message = new byte[PackLen - 4];
				in.read(Message);

				this.LastActiveTime = DateUtil.getTimeStampL();
				// debug
				// System.out.println(Hex.rhex(Message));
				Package mpackage = new Package(Message);

				// ���ò���
				int SequenceId = mpackage.SequenceId;

				if (this.hasLogin || mpackage.ReqestId == RequestId.ActiveTest
						|| mpackage.ReqestId == RequestId.ActiveTest_Resp
						|| mpackage.ReqestId == RequestId.Login) {
					switch (mpackage.ReqestId) {
					case (0x00000001):
						// login
						LoginMessage loginMessage = new LoginMessage(Message);
						// login��Ҫ���������صĲ���
						this.ipaddress = this.clientsocket
								.getRemoteSocketAddress().toString();
						if (this.ipaddress.indexOf("/") == 0) {
							this.ipaddress = this.ipaddress.substring(1);
						}
						if (this.ipaddress.indexOf(":") > 0) {
							this.ipaddress = this.ipaddress.substring(0,
									this.ipaddress.indexOf(":"));
						}

						byte[] AuthenticatorClient = loginMessage
								.getAuthenticatorClient();
						Login login = new Login(loginMessage, this.ipaddress);

						LoginResult loginresult = this.serversim.onLogin(login,
								this);
						LoginRespMessage loginRespMessage = new LoginRespMessage(
								SequenceId, loginresult.getStatus(),
								AuthenticatorClient, loginresult.getShareKey(),
								loginresult.getServerVersion());
						SendBuf(loginRespMessage.getBuf());

						// �����Ѿ�����
						if (loginresult.getStatus() == 0) {

							this.account = login.Account;
							this.hasLogin = true;
							this.setLoginMode(login.LoginMode);
						} else {
							// ��¼ʧ��
							this.exit();
						}
						break;
					case (0x00000002):
						// submit
						SubmitMessage submitMessage = new SubmitMessage(Message);
						Submit submit = submitMessage.getSubmit();
						SubmitResult submitResult = this.serversim.onSumit(
								submit, this.account);
						SubmitRespMessage submitRespMessage = new SubmitRespMessage(
								SequenceId, submitResult.getStatus(),
								submitResult.getMsgID());
						SendBuf(submitRespMessage.getBuf());
						break;
					case (0x00000004):
						// ActiveTest
						ActiveTestRespMessage activeTestRespMessage = new ActiveTestRespMessage(
								SequenceId);
						SendBuf(activeTestRespMessage.getBuf());
						break;
					case (0x00000006):
						// Exit
						ExitRespMessage exitRespMessage = new ExitRespMessage(
								SequenceId);
						SendBuf(exitRespMessage.getBuf());
						break;
					case (0x80000003):
						// Deliver_Resp,����Ҫ����
						break;
					case (0x80000004):
						// Active_Resp,����Ҫ����
						break;
					}
				} else {
					// �Ƿ�����
					System.out.println("Package Error!");
					exit();
				}

			} while (true);
		} catch (SocketException e) {
			System.out.println("Client has close socket connect!");
			exit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Client Exit !");
			exit();
		}

	}

	public void SendDeliver(Deliver deliver) {
		DeliverMessage dlm = new DeliverMessage(deliver);
		try {
			SendBuf(dlm.getBuf());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			exit();
		}
	}

	public void ActiveTest() {
		this.checkConnect();
		if (this.hasLogin) {
			ActiveTestMessage activeTestMessage = new ActiveTestMessage();
			try {
				SendBuf(activeTestMessage.getBuf());
			} catch (IOException e) {
				// System.out.println("Active Test Error!");
				exit();
			}
		}
	}

	private void checkConnect() {
		if ((DateUtil.getTimeStampL() - this.LastActiveTime) > this.TimeOut * 1000) {
			exit();
		}

	}

	private void exit() {
		if (this.hasLogin) {
			this.serversim.disconnect(this.account, this.ipaddress, this);
		} else {
			this.serversim.disconnect(this);
		}
	}

	private synchronized void SendBuf(byte[] buf) throws IOException {
		this.out.write(buf);
	}

	public void setLoginMode(int loginMode) {
		this.loginMode = loginMode;
	}

	public int getLoginMode() {
		return loginMode;
	}

	public String getIpaddress() {
		return ipaddress;
	}

	public String getAccount() {
		return account;
	}
}
