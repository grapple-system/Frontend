/**
 * Created by wangyifei on 2018/7/10.
 */
class Int{
    int i;
    public String s;
    Int(){
        i=0;
    }
    Int(int i){
        this.i = i;
    }
    int value(){
        return i;
    }
}

public class test5 {
    public int random(){
        return ((int)System.nanoTime())%29;
    }

    public Int add(Int i1, Int i2){
        return new Int(i1.value()+i2.value());
    }

    public Int sub(Int i1, Int i2){
        int x = random();
        int y = random();
        if(x<y){
            return new Int(i1.value()-i2.value());
        }else {
            return new Int(i2.value()-i1.value());
        }
    }

    public void main(){
        int x = random();
        int y = random();
        Int a = new Int(5);
        Int b = new Int(6);
        Int c = new Int();
        c.s = "hello";
        if(x<y){
            x = x+y;
        }else{
            y=x+y;
        }
        c = new Int(x);

        if(x<y){
            y=y-x;
        }else{
            x=x-y;
        }
        c = sub(a,b);
        if(x<y){
            x = x*y;
        }else {
            y=x*y;
        }
        c =add(a,b);
    }
}
