package cn.com.zjtelecom.util;

public class Hex2 {
      public static byte[] rByte(String packagestr){
    	  byte[] tmp = new byte[packagestr.length()/2];
    	  for (int i=0;i<packagestr.length()/2;i++){
    		  tmp[i]=Byte.parseByte(packagestr.substring(i*2,i*2+2));
    		  //int j = (String.valueOf(("0x"+packagestr.substring(i*2,i*2+2)).;
    	  }
    	  return tmp;
      }
      
      private Integer getInt(String hex){
    	 return (Integer.parseInt(hex.substring(0, 1))*16+Integer.parseInt(hex.substring(1, 2)));
      }
}
