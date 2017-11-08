
public class Test1 {
	private Test2 t2;
	
	public Test1(){
		
	}
	
	public static int multiply(int x, int y){
		return x * y;
	}
	
	private int add(int x, int y){
		return x + y;
	}
	
	public static Test2 createTest2(Test2 o2, String s){
		return o2;
	}
	
	public static void main(String[] args) {
		Test1 t1 = new Test1();
		Test2 t2 = new Test2(1);
		t2.a = t1.add(2, t2.getA());
		
		t1.t2 = createTest2(new Test2(0), "hello");
	}

}
