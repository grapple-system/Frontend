class TestArray{
    public static void main(String []args){
        TestArray ta=new TestArray();

        if(args[0]=="hello"){
            ta.print(args[0]);
        }
        else{
            ta.print(args[1]);
        }
    }

    public void print(String s){
        s="he";
    }
}