package cn.com.zjtelecom.smgp.server.sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cn.com.zjtelecom.smgp.bean.Deliver;
import cn.com.zjtelecom.smgp.server.inf.ServerEventInterface;

public class ServerConsole extends Thread {
	private ServerEventInterface serverEventInterface;

	public void run() {
		do {
			String command = readComand();
			if (command.equals("")) {
				continue;
			} else if (command.equalsIgnoreCase("help")
					|| command.equalsIgnoreCase("h")) {
				Help();
				continue;
			} else if (command.equalsIgnoreCase("exit")){
				this.serverEventInterface.Exit();
				continue;
			} else if (command.indexOf("L") == 0 || command.indexOf("l") == 0) {
				this.serverEventInterface.ListConnected();
				continue;
			} else if (command.indexOf("D") == 0 || command.indexOf("d") == 0) {
				String[] para = command.split(" ");
				if (para.length < 4) {
					System.out.println("Error:Not enough arguments!");
					continue;
				} else {
					
					if (checkNum(para[1]) == false || checkNum(para[2]) == false ){
						System.out.println("Error:SrcNum or DestNum arguments must be number!");
						continue;
					}
					if (para.length>3){
						for (int i=4;i<para.length;i++){
							para[3]=para[3]+" "+para[i];
						}
					}
					System.out.println("--------------------------");
					System.out.println("Send Deliver  ");
					System.out.println("--------------------------");					
					System.out.println("SrcNum  : " + para[1]);
					System.out.println("DestNum : " + para[2]);
					System.out.println("Content : " + para[3]);
					System.out.println("--------------------------");
					Deliver deliver = new Deliver();
					deliver.IsReport = 0;
					deliver.MsgFormat = 15;
					deliver.SrcTermID = para[1];
					deliver.DestTermID = para[2];
					deliver.MsgContent = para[3].getBytes();
					deliver.MsgLength = deliver.MsgContent.length;
					

					this.serverEventInterface.SendDeliver(deliver);

					continue;
				}
			}
			System.out.println("Unknow Command!");
		} while (true);
	}

	public ServerConsole(ServerEventInterface serverinf) {
		this.serverEventInterface = serverinf;
	}

	private void Help() {
		System.out.println("--------------------------");
		System.out.println("Help  ");
		System.out.println("--------------------------");
		System.out.println("Help        £ºH[elp]");
		System.out.println("Send Deliver£ºD SrcNum DestNum Content");
		System.out.println("List Client £ºL[ist]");
		System.out.println("Close Server£ºExit");
		System.out.println("              [] is option");
		System.out.println("------------------------------------");
	}

	private boolean  checkNum(String checkstring) {
		for (int i = 0; i < checkstring.length(); i++) {
		  if (!Character.isDigit(checkstring.charAt(i))){
			  return false;
		  }
		}
		return true;
	}

	private String readComand() {
		String com = "";

		try {
			BufferedReader lineOfText = new BufferedReader(
					new InputStreamReader(System.in));
			
			com = lineOfText.readLine();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return com;
	}
}
