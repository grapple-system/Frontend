public class TestSynchronized {
    public void main(String []args){
        while(true){
            synchronized (this){
                System.out.println("Hello");
            }
        }
    }
}
