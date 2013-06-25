package cn.com.zjtelecom.smgp.server.inf;

import cn.com.zjtelecom.smgp.bean.Deliver;
import cn.com.zjtelecom.smgp.bean.Login;
import cn.com.zjtelecom.smgp.bean.Submit;
import cn.com.zjtelecom.smgp.server.result.LoginResult;
import cn.com.zjtelecom.smgp.server.result.SubmitResult;

public interface ServerEventInterface {
     public SubmitResult onSumit(Submit submit,String account);
     public LoginResult  onLogin(Login login);
     public void SendDeliver(Deliver deliver);
     public void ListConnected();
     public void Exit();
}
