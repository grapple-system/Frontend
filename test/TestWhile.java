import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestWhile {
    private List<String> list = new ArrayList<>();

    TestWhile(){};

    public void main(String []args){
        Iterator ite = list.iterator();

        while(ite.hasNext()){
            String str = (String) ite.next();
            if(str == "Hello"){
                System.out.println(str);
            }
        }
    }
}
