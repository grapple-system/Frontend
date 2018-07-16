import java.util.ArrayList;

public class Testloop {

	private static ArrayList tlist;
	
	public static void main(String args[]) {
		
		int a = 0;
		a+=7;
		
		while(a < 0){
			a--;
		}
		
		while(true) {
			a++;
			if(a == 0) {
				break;
			}
			if(a == 1) {
				return;
			}
			a--;
		}
		
		for (int i = 0; i < tlist.size(); i++) { 
			if (tlist.size() == a)
				return;
		}
		return;
		
		
		
	}
	
	
	
}
