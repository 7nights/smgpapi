package cn.com.zjtelecom.util;

public class TypeConvert
{

    public TypeConvert()
    {
    }
    
    public static String getString(byte [] src, int srcPos,int destPos,int length){
    	byte[] tmp=new byte[length];
    	System.arraycopy(src, srcPos, tmp, destPos, length);
    	return (new String(tmp).trim());
    	
    }
    
    public static String getHexString(byte [] src, int srcPos,int destPos,int length){
    	byte[] tmp=new byte[length];
    	System.arraycopy(src, srcPos, tmp, destPos, length);
    	return (Hex.rhex(tmp));
    	
    }


    public static String byte2String(byte buf[], int offset)
    {
        int pos = offset;
        for(; offset < buf.length; offset++)
            if(buf[offset] == 0)
                break;

        if(offset > pos)
            offset--;
        else
        if(offset == pos)
            return "";
        int len = (offset - pos) + 1;
        byte bb[] = new byte[len];
        System.arraycopy(buf, pos, bb, 0, len);
        String str = new String(bb);
        return str;
    }

    public static int byte2int(byte b[])
    {
        return b[3] & 0xff | (b[2] & 0xff) << 8 | (b[1] & 0xff) << 16 | (b[0] & 0xff) << 24;
    }

    public static int byte2int(byte b[], int offset)
    {
        return b[offset + 3] & 0xff | (b[offset + 2] & 0xff) << 8 | (b[offset + 1] & 0xff) << 16 | (b[offset] & 0xff) << 24;
    }

    public static long byte2long(byte b[], int offset)
    {
        return (long)b[offset + 7] & 255L | ((long)b[offset + 6] & 255L) << 8 | ((long)b[offset + 5] & 255L) << 16 | ((long)b[offset + 4] & 255L) << 24 | ((long)b[offset + 3] & 255L) << 32 | ((long)b[offset + 2] & 255L) << 40 | ((long)b[offset + 1] & 255L) << 48 | (long)b[offset] << 56;
    }

    public static long byte2long(byte b[])
    {
        return (long)b[7] & 255L | ((long)b[6] & 255L) << 8 | ((long)b[5] & 255L) << 16 | ((long)b[4] & 255L) << 24 | ((long)b[3] & 255L) << 32 | ((long)b[2] & 255L) << 40 | ((long)b[1] & 255L) << 48 | (long)b[0] << 56;
    }

    public static short byte2short(byte b[], int offset)
    {
        return (short)(b[offset + 1] & 0xff | (b[offset] & 0xff) << 8);
    }

    public static short byte2short(byte b[])
    {
        return (short)(b[1] & 0xff | (b[0] & 0xff) << 8);
    }

    public static short byte2tinyint(byte b[], int offset)
    {
        return (short)(b[offset] & 0xff );
    }

    
    public static void int2byte(int n, byte buf[], int offset)
    {
        buf[offset] = (byte)(n >> 24);
        buf[offset + 1] = (byte)(n >> 16);
        buf[offset + 2] = (byte)(n >> 8);
        buf[offset + 3] = (byte)n;
    }
    
    public static void int2byte2(int n, byte buf[], int offset)
    {
        buf[offset] = (byte)(n >> 8);
        buf[offset + 1] = (byte)n;
    }
    
    public static void int2byte3(int n, byte buf[], int offset)
    {
        //buf[offset] = (byte)(n >> 8);
        buf[offset] = (byte)n;
    }

    public static byte[] int2byte(int n)
    {
        byte b[] = new byte[4];
        b[0] = (byte)(n >> 24);
        b[1] = (byte)(n >> 16);
        b[2] = (byte)(n >> 8);
        b[3] = (byte)n;
        return b;
    }

    public static void long2byte(long n, byte buf[], int offset)
    {
        buf[offset] = (byte)(int)(n >> 56);
        buf[offset + 1] = (byte)(int)(n >> 48);
        buf[offset + 2] = (byte)(int)(n >> 40);
        buf[offset + 3] = (byte)(int)(n >> 32);
        buf[offset + 4] = (byte)(int)(n >> 24);
        buf[offset + 5] = (byte)(int)(n >> 16);
        buf[offset + 6] = (byte)(int)(n >> 8);
        buf[offset + 7] = (byte)(int)n;
    }

    public static byte[] long2byte(long n)
    {
        byte b[] = new byte[8];
        b[0] = (byte)(int)(n >> 56);
        b[1] = (byte)(int)(n >> 48);
        b[2] = (byte)(int)(n >> 40);
        b[3] = (byte)(int)(n >> 32);
        b[4] = (byte)(int)(n >> 24);
        b[5] = (byte)(int)(n >> 16);
        b[6] = (byte)(int)(n >> 8);
        b[7] = (byte)(int)n;
        return b;
    }

    public static void short2byte(int n, byte buf[], int offset)
    {
        buf[offset] = (byte)(n >> 8);
        buf[offset + 1] = (byte)n;
    }

    public static byte[] short2byte(int n)
    {
        byte b[] = new byte[2];
        b[0] = (byte)(n >> 8);
        b[1] = (byte)n;
        return b;
    }
}