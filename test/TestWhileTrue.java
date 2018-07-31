public class TestWhileTrue {
    public void main(String []args){

        /* Exception in thread "main" java.lang.RuntimeException: unexpected infinite loop!!! */
        /* throw manually in LoopTransformer.java */
        /* compare to case below */
        while(true){
            System.out.println("Hello");
        }

        /* Exception in thread "main" java.lang.StackOverflowError */
        /* this case exists */
//        while(true){
//            try {
//                System.out.println("Hello");
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
    }
}
