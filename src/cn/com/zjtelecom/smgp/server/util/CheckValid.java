package cn.com.zjtelecom.smgp.server.util;

import cn.com.zjtelecom.smgp.bean.Submit;

public class CheckValid {
   public  static int CheckSubmit(Submit submit){
	   if (submit.getMsgType()!=6) return 30;
	   if (!submit.getFeetype().equals("00") && !submit.getFeetype().equals("01") && submit.getFeetype().equals("02")) return 32;
	   if (submit.getMsgFormat()>15) return 34;
	   return 0;
   }
}
