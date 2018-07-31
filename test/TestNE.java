public class TestNE {
    boolean isTrue(){
        return true;
    }
    public void main(String []args){
        boolean a = isTrue();
        boolean b = isTrue();
        if(a==b){
            System.out.println("Hello");
        }
    }
}
