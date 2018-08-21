import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Testloop {

	private static ArrayList tlist;

	public static void main(String args[]) {

		int a = 0;
		a += 7;

		while (a < 0) {
			a--;
		}

		while (true) {
			a++;
			if (a == 0) {
				break;
			}
			if (a == 1) {
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

	private List<String> list = new ArrayList<>();

	public void f() {
		Iterator ite = list.iterator();

		while (ite.hasNext()) {
			String str = (String) ite.next();
			if (str == "Hello") {
				System.out.println(str);
			}
		}

		int a = 9;

		do {
			a += 2;
			if (a == 0) {
				break;
			}
			a -= 2;
		} while (true);
	}

}
