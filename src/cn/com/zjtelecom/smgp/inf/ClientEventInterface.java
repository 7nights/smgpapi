package cn.com.zjtelecom.smgp.inf;

import cn.com.zjtelecom.smgp.bean.Deliver;
import cn.com.zjtelecom.smgp.bean.SubmitResp;

public interface ClientEventInterface {
    public void onDeliver(Deliver deliver);
    public void OnSubmitResp(SubmitResp submitResp);
}
