package Sample;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Config {
    private static String configFile="smgpc.ini";
	 private  HashMap <String,String> configproperty=new HashMap<String,String>();
	 
   public Config() throws IOException {
  	 this.ReadProperty(configFile);
   }
	 public  Config(String configFile2) throws IOException {
		// TODO Auto-generated method stub
		 this.ReadProperty(configFile2);
	}
	private  void ReadProperty(String cfile) throws IOException{
			String line = "";
			FileInputStream fileinputstream = new FileInputStream(cfile);
			BufferedReader bufferedreader = new BufferedReader(
					new InputStreamReader(fileinputstream));
			do {
				if ((line = bufferedreader.readLine()) == null)
					break;
				if (line.indexOf("#") == 0 || line.indexOf("=") < 0)
					continue;
				String key=line.substring(0,line.indexOf("="));
				String value=line.substring(line.indexOf("=")+1);
				
				//System.out.println("key:"+key);
				//System.out.println("value:"+value);
				configproperty.put(key,value);
				
			} while(true);
	 }
	public  String get(String key){
		return configproperty.get(key);
	}
}