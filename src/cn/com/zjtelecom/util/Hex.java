package cn.com.zjtelecom.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class Hex {
	  public static byte[] rstr(String hex) {
	        int length = hex.length();
	        byte[] bHex = new byte[length/2];
	        String temp = null;
	        int t = 0;
	        for (int i=0; i<length; i++) {
	            temp = "" + hex.charAt(i) + hex.charAt(++i);
	            bHex[t++] = (byte)Integer.parseInt(temp, 16);
	        }
	        return bHex;
	    }
	  
	public static String rhex(byte[] in) {
			DataInputStream data = new DataInputStream(new ByteArrayInputStream(in));
			String str = "";
			try {
				for (int j = 0; j < in.length; j++) {
					String tmp = Integer.toHexString(data.readUnsignedByte());
					if (tmp.length() == 1) {
						tmp = "0" + tmp;
					}
					str = str + tmp;
				}
			} catch (Exception ex) {
			}
			return str;
		}
}
