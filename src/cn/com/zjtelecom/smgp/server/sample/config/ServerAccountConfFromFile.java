package cn.com.zjtelecom.smgp.server.sample.config;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerAccountConfFromFile extends ServerAccountConfig{
	private  String accountfile = "account.conf";
	
	public ServerAccountConfFromFile() throws IOException{
		this.readAccount();
	}
	
	public ServerAccountConfFromFile(String configfile) throws IOException{
		this.accountfile = configfile;
		this.readAccount();
	}
	private  void readAccount() throws IOException {

			FileInputStream fileinputstream = new FileInputStream(accountfile);
			BufferedReader bufferedreader = new BufferedReader(
					new InputStreamReader(fileinputstream));
			String line;
			String curAccount = "";
			do {

				if ((line = bufferedreader.readLine()) == null)
					break;
				if (line.indexOf("#") == 0)
					continue;
				if (line.indexOf("[") == 0 && line.indexOf("]") > 0) {
					curAccount = line.substring(1, line.indexOf("]"));
					// System.out.println("ReadAccount:" + curAccount);
					continue;
				}
				if (line.indexOf("=") > 0 && !curAccount.equals("")) {
					if (line.indexOf("Password=") == 0)
						this.passwordHash.put(curAccount, line.substring(line
								.indexOf("=") + 1));
					else if (line.indexOf("IPAddress=") == 0)
						this.ipaddressHash.put(curAccount, line.substring(line
								.indexOf("=") + 1));
					else if (line.indexOf("SPNum=") == 0)
						this.spnumHash.put(curAccount, line.substring(line
								.indexOf("=") + 1));
					else if (line.indexOf("SPId=") == 0)
						this.spidHash.put(curAccount, line.substring(line
								.indexOf("=") + 1));

				}
			} while (true);
	

	}
}
