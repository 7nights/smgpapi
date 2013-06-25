package Sample;
import java.math.BigInteger;
import java.security.AlgorithmParameterGenerator;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class test {
  

  public static void main(String[] args) {
	  String type = "end";
	  Integer testArr[] = new Integer[100];
	  try {
		System.out.println(decrypt("somepassword", "67a0fd39e75e37bc0743147d43b9487bde7db1f5ef798da4e570d6595190dd682a9d4491b47ad26c94e11e1e464e8bb1"));
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   }

  public static String decrypt(String seed, String encrypted) throws Exception {
	  byte[] keyb = seed.getBytes("UTF-8");
	  MessageDigest md = MessageDigest.getInstance("MD5");
	  byte[] thedigest = md.digest(keyb);
	  SecretKeySpec skey = new SecretKeySpec(thedigest, "AES");
	  Cipher dcipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
	  dcipher.init(Cipher.DECRYPT_MODE, skey);

	  byte[] clearbyte = dcipher.doFinal(toByte(encrypted));
	  return new String(clearbyte);
  }

	public static byte[] toByte(String hexString) {
	  int len = hexString.length()/2;
	  byte[] result = new byte[len];
	  for (int i = 0; i < len; i++)
	    result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
	  return result;
	}
}