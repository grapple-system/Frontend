
public class Test3 {
//	int f;
	
	public Test3(){
//		this.f = 1;
	}

	private void t1(int x, int y) throws Exception {
		int a = x * x;
		int c = a + x;
		if(c > 0){
			int b = y - c;			
		}
		else{
			c--;
		}
		a++;
		
		if(a == 0)
			throw new Exception("a is zero!");
		
		if(a < 0){
			a--;
		}
		c++;
		
//		int d = b + this.f;
	}
	
	
	private void t2(int x){
		int a = 19 / x;
		try{
			t1(x, a);
			a++;
			if(x == 0){
				throw new Exception("divided by 0!");
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
	public static void main(String[] args) {
		
	}
}
