
public class Test0 {
//	int f;
	
	public Test0(){
//		this.f = 1;
	}

	private void while_dowhile(int x, int y){
		int a = x * x;
		int c = a + x;
		if(c + a > 0 + x){
			int b = y - c;			
		}
		else{
			c-=2;
		}
		a++;
		
		double dd = 0d;
//		boolean bool = dd < 0.4f;
		if(dd < 0.4f) {
			a++;
		}
		
		boolean bool2 = x <= c == dd < 0.4d;
		if(!bool2) {
			a--;
		}
		
		long ll = 9;
		long l2 = ll - x;
		if(l2 < 10) {
			c++;
		}
		
//		while(a < 0){
//			a--;
//		}
//		c+=2;
//		int d = b + this.f;
//		
//		c = lookup_switch(a, c);
//		
//		do {
//			a+=3;
//			c-=4;
//		}
//		while(c != 0);
//		
//
//		
//		true_while(a, c);
	}
//
//	private int lookup_switch(int a, int c) {
//		switch(a) {
//		case 2:
//			c++;
//			break;
//		case 100:
//			c--;
//			break;
//			
//		default:
//			System.out.println("default!");	
//		}
//		return c;
//	}
//
//	private void true_while(int a, int c) {
//		a+=7;
//		
//		while(true) {
//			a++;
//			if(a == 0) {
//				break;
//			}
//			if(a == 1) {
//				return;
//			}
//			a--;
//		}
//		
//		do{
//			a+=2;
//			if(a == 0) {
//				break;
//			}
//			a-=2;
//		}
//		while(true);
//		
//		
//		while(true) {
//			a+=3;
////			if(a == 0) {
////				break;
////			}
//			
//			if(a == 1) {
//				break;
//			}
//			a-=3;
//		}
//		
//		c++;
//	}
	
	
	private void nest_for(int x){
		int a = 19 / x;
		for(int i = 0; i < 9; i++) {
//			a = 2 * x;
//			a -= 5;
			for(int j = 0; j < 10; j++) {
				if(a < 100) {
					return;
				}
				a++;
			}
		}
//		x++;
		
	}
	
	
	public static void main(String[] args) {
		Test0 t00 = new Test0();
		Test0 t01 = new Test0();
		if(t00 == t01) {
			System.out.println("Equal");
		}
	}
}
