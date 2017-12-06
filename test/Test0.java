
public class Test0 {
//	int f;
	
	public Test0(){
//		this.f = 1;
	}

	private void t1(int x, int y){
		int a = x * x;
		int c = a + x;
		if(c > 0){
			int b = y - c;			
		}
		else{
			c-=2;
		}
		a++;
		while(a < 0){
			a--;
		}
		c+=2;
//		int d = b + this.f;
		
		switch(a) {
		case 2:
			c++;
			break;
		case 100:
			c--;
			break;
			
		default:
			System.out.println("default!");	
		}
		
		do {
			a+=3;
			c-=4;
		}
		while(c != 0);
		
		for(int i = 0; i < a; i++) {
			a = 2 * c;
			a -= 5;
			for(int j = 0; j < 10; j++) {
				if(a < 100) {
					return;
				}
				a++;
			}
		}
		
		a+=7;
		
		while(true) {
			a++;
			if(a == 0) {
				break;
			}
			a--;
		}
	}
	
	
	private void t2(int x){
		int a = 19 / x;
	}
	
	
	public static void main(String[] args) {
		
	}
}
