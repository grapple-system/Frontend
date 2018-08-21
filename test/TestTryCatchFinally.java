import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestTryCatchFinally {

    public void main(String []args){
       try{
           System.out.println("START");
       }catch (Exception e){
           e.printStackTrace();
       }finally {
           System.out.println("END");
       }
    }
}
