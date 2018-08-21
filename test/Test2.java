import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Test2 {
	public int a;
	
	public Test2(int a){
		this.a = a;
	}
	
	public int getA(){
		return a;
	}
	
	public static void main(String[] args) throws IOException{

		new Test2(0).foo(1);
	}
	
	
	public void foo0(int x) throws IOException {
		FileWriter out = null, out2 = null;

		if(x >= 0) {
			out = new FileWriter("out.txt");
			out2 = new FileWriter("out2.txt");
		}
		
 		if(x > 0) {
 			out2.close();
			out2 = out;
			out.write(x);
			out2.close();
		}
		
		if(x == 0) {
			out2.close();
		}
		
		return;
	}
	
	//path-sensitive vs. path-insensitive typestate checking
	public void foo1(int x) throws IOException {
		FileWriter out = null;

		if(x >= 0) {
			out = new FileWriter("out.txt");
		}
		
 		if(x > 0) {
			out.write(x);
			out.close();
		}
		
 		return;
	}
	
	//under path-sensitive typestate checking: path-sensitive alias vs. path-insensitive alias 
	public void foo(int x) throws IOException {
		FileWriter out = null, out2 = null;

		if(x >= 0) {
			out = new FileWriter("out.txt");
		}
		
 		if(x > 0) {
 			out.write(x);
			out2 = out;
			out2.close();
		}
		
		if(x == 0) {
			out2.close();
		}
		
		return;
	}
	

	
}
