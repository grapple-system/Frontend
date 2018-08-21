public class TestTypeCast {
    int retInteger(int i){
        return i;
    }

    long retLong(long l){
        return l;
    }

    float retFloat(float f){
        return f;
    }

     /*right*/
//    public void symbolicInteger2Integer(){
//        int i = 2 * retInteger(2);
//    }

    /*right*/
//    public void symbolicInteger2Long(){
//        Long l = 2l * retInteger(2);
//    }

    /*error*/
    public void symbolicInteger2Float(){
        float f = 2.3f * retInteger(2);
    }

    /*error*/
//    public void symbolicInteger2Double(){
//        double d = 2.3 * retInteger(2);
//    }

    /*error*/
//    public void symbolicFloat2Double(){
//        double d = 2.3 * retFloat(2.3f);
//    }

}
