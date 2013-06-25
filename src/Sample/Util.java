package Sample;

public class Util {
	public static class Bitmap{
		public int[] datamap;
		public Bitmap(int length){
			datamap = new int [1 + length/32];
		}
		public void set(int i){
			datamap[i>>5] |= (1<<(i & 0x1F));
		}
		public void clear(int i){
			datamap[i>>5] &= ~(1<<(i & 0x1F));
		}
		public boolean test(int i){
			return (datamap[i>>5] & (1<<(i & 0x1F))) != 0;
		}
	}
	public static void main(String[] args){
		Bitmap b = new Bitmap(32);
		b.set(3);
		System.out.println("2: " + b.test(2));
		System.out.println("3: " + b.test(3));
		b.clear(3);
		System.out.println("3: " + b.test(3));
		b.set(4);
		System.out.println("4: " + b.test(4));
		b.set(31);
		System.out.println("34: " + b.test(34));

		System.out.println(b.datamap.length);
	}
}