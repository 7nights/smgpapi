package cn.com.zjtelecom.smgp.server.result;

public class LoginResult {
	private int sequence_Id;
	private int Status;
    private String shareKey;
    private String serverVersion;
    private byte[] authenticatorClient;
    private String clientID;
    private String spNum;
    
    
	public int getSequence_Id() {
		return sequence_Id;
	}
	public void setSequence_Id(int sequenceId) {
		sequence_Id = sequenceId;
	}
	public String getServerVersion() {
		return serverVersion;
	}
	public void setServerVersion(String serverVersion) {
		this.serverVersion = serverVersion;
	}

    
    public LoginResult(int status,String sharekey,String version,String spnum){
        this.Status = status;
        this.shareKey = sharekey;
        this.serverVersion = version;
        this.setSpNum(spnum);
    }
      public int getStatus() {
		return Status;
	}
	public void setStatus(int status) {
		this.Status = status;
	}
	public String getShareKey() {
		return shareKey;
	}
	public void setShareKey(String shareKey) {
		this.shareKey = shareKey;
	}
	public String getServer() {
		return serverVersion;
	}
	public void setServer(String server) {
		serverVersion = server;
	}
	public void setAuthenticatorClient(byte[] authenticatorClient) {
		this.authenticatorClient = authenticatorClient;
	}
	public byte[] getAuthenticatorClient() {
		return authenticatorClient;
	}
	public void setClientID(String clientID) {
		this.clientID = clientID;
	}
	public String getClientID() {
		return clientID;
	}
	public void setSpNum(String spNum) {
		this.spNum = spNum;
	}
	public String getSpNum() {
		return spNum;
	}

}
