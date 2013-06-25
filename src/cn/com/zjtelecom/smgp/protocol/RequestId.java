package cn.com.zjtelecom.smgp.protocol;

public class RequestId {
	public static int Login =          0x00000001; // 客户端登录
	public static int Login_Resp =     0x80000001; // 客户端登录应答
	public static int Submit =         0x00000002; // 提交短消息
	public static int Submit_Resp=     0x80000002;// 提交短消息应答
	public static int Deliver =        0x00000003;
	public static int Deliver_Resp =   0x80000003;
	public static int ActiveTest =     0x00000004;
	public static int ActiveTest_Resp= 0x80000004;
	public static int Exit =           0x00000006;
	public static int Exit_Resp =      0x80000006;
}
