package edu.zuo.setree.intergraph;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by wangyifei on 2018/5/1.
 */
class pair{
    private int left;
    private int right;
    public pair(int left, int right){
        this.left = left;
        this.right = right;
    }
    public void setLeft(int left){ this.left = left; }
    public void setRight(int right){ this.right = right; }
    public void set(int left, int right){
        this.left = left;
        this.right = right;
    }
    public String toString(){
        return "("+left+","+right+")";
    }
}

public class interGraph {
    public static final File consEdgeGraphFile = new File("consEdgeGraph");
    public static final File var2indexMapFile = new File("var2indexMap");
    public static int funcIndex = -1;

    private static Map<pair, String> pair2varMap = new LinkedHashMap<>();
    private static Map<String, Integer> func2indexMap = new LinkedHashMap<>();
    private static Map<Integer, Map<String, Integer>> funcParamReturn = new LinkedHashMap<>();

    public static final File pair2varMapFile = new File("pair2varMap");
    public static final File func2indexMapFile = new File("func2indexMap");
    public static final File interGraphFile = new File("interGraph");

    public static void printMap(){
        PrintWriter pair2varMapOut = null;
        PrintWriter func2indexMapOut = null;
        try {
            if (!pair2varMapFile.exists()) {
                pair2varMapFile.createNewFile();
            }
            if (!func2indexMapFile.exists()) {
                func2indexMapFile.createNewFile();
            }

            pair2varMapOut = new PrintWriter(new BufferedWriter(new FileWriter(pair2varMapFile,true)));
            func2indexMapOut = new PrintWriter(new BufferedWriter(new FileWriter(func2indexMapFile,true)));

            for (pair p: pair2varMap.keySet()) {
                pair2varMapOut.println(p.toString()+" : "+pair2varMap.get(p));
            }
            for (String f: func2indexMap.keySet()) {
                func2indexMapOut.println(func2indexMap.get(f).toString()+" : "+f);
            }
            pair2varMapOut.close();
            func2indexMapOut.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]){

        try {
            if (!consEdgeGraphFile.exists()) {
                System.out.println("Error: consEdgeGraph file not exists.");
                return;
            }
            if (! var2indexMapFile.exists()) {
                System.out.println("Error: var2indexMap file not exists.");
                return;
            }
            Scanner consEdgeGraphInput = new Scanner(consEdgeGraphFile);
            Scanner var2indexMapInput = new Scanner(var2indexMapFile);
            while(var2indexMapInput.hasNextLine()){
                String line = var2indexMapInput.nextLine();
                //System.out.println("#"+line+"#");
                if(line.length()==0){

                }else if(line.startsWith("<")){
                    ++funcIndex;
                    func2indexMap.put(line,funcIndex);
                }else {
                    String[] tokens = line.split(" : ");
                    int right = Integer.parseInt(tokens[0]);
                    pair2varMap.put(new pair(funcIndex,right), tokens[1]);

                }

            }

            funcIndex=-1;
            while(consEdgeGraphInput.hasNextLine()) {
                String line = consEdgeGraphInput.nextLine();
                if (line.length() == 0) {

                } else if (line.startsWith("<")) {
                    ++funcIndex;
                    funcParamReturn.put(funcIndex,new LinkedHashMap<String, Integer>());
                } else {
                    String[] tokens = line.split(", ");
                    if (tokens.length == 2) {
                        int i = Integer.parseInt(tokens[0]);
                        funcParamReturn.get(funcIndex).put(tokens[1], i);
                    }
                }
            }
            consEdgeGraphInput.close();
            consEdgeGraphInput = new Scanner(consEdgeGraphFile);
            funcIndex=-1;
            PrintWriter interGraphOut = null;
            if(!interGraphFile.exists()){
                interGraphFile.createNewFile();
            }
            interGraphOut = new PrintWriter(new BufferedWriter(new FileWriter(interGraphFile, true)));
            while(consEdgeGraphInput.hasNextLine()){
                String line = consEdgeGraphInput.nextLine();
                System.out.println("#"+line+"#");
                if(line.length()==0){

                }else if(line.startsWith("<")){
                    ++funcIndex;
                }else {
                    String[] tokens = line.split(", ");
                    if(tokens.length==2){
                        System.out.println(2);
                    }else if(tokens.length==3){
                        System.out.println(3);
                        int first_right = Integer.parseInt(tokens[0]);
                        int second_right = Integer.parseInt(tokens[1]);
                        pair p1 = new pair(funcIndex, first_right);
                        pair p2 = new pair(funcIndex, second_right);
                        interGraphOut.println(p1.toString()+", "+p2.toString()+", "+tokens[2]);
                    }else if(tokens.length==4){
                        System.out.println(4);
                        int first_right = Integer.parseInt(tokens[0]);
                        int second_right = Integer.parseInt(tokens[1]);
                        pair p1 = new pair(funcIndex, first_right);
                        pair p2 = new pair(funcIndex, second_right);
                        int n1_rignt = Integer.parseInt(tokens[2].substring(1));
                        int n2_right = Integer.parseInt(tokens[3].substring(0,tokens[3].length()-1));
                        pair p3 = new pair(funcIndex, n1_rignt);
                        pair p4 = new pair(funcIndex, n2_right);
                        interGraphOut.println(p1.toString()+", "+p2.toString()+", ["+p3.toString()+", "+p4.toString()+"]");
                    }else{
                        assert(tokens[3]=="[Call]");
                        // Do not handle system call
                        if(! func2indexMap.containsKey(tokens[2])) continue;
                        int funcNodeIndex = Integer.parseInt(tokens[1]);
                        int callFuncIndex = func2indexMap.get(tokens[2]);
                        // callee
                        if(tokens[4].length()>2){
                            int first_right = Integer.parseInt(tokens[4].substring(1,tokens[4].length()-1));
                            int second_right = funcParamReturn.get(callFuncIndex).get("[Callee]");
                            pair p1 = new pair(funcIndex, first_right);
                            pair p2 = new pair(callFuncIndex, second_right);
                            pair p3 = new pair(funcIndex, funcNodeIndex);
                            pair p4 = new pair(callFuncIndex, 0);
                            interGraphOut.println(p1.toString()+", "+p2.toString()+", ["+p3.toString()+", "+p4.toString()+"]");
                        }
                        //params
                        int paraN = tokens.length - 5;
                        if(paraN == 1){
                            if(tokens[5].length()==2)continue;
                            int first_right = Integer.parseInt(tokens[5].substring(1,tokens[5].length()-1));
                            int second_right = funcParamReturn.get(callFuncIndex).get("[Para0]");
                            pair p1 = new pair(funcIndex, first_right);
                            pair p2 = new pair(callFuncIndex, second_right);
                            pair p3 = new pair(funcIndex, funcNodeIndex);
                            pair p4 = new pair(callFuncIndex, 0);
                            interGraphOut.println(p1.toString()+", "+p2.toString()+", ["+p3.toString()+", "+p4.toString()+"]");
                        }else {
                            int first_right = Integer.parseInt(tokens[5].substring(1,tokens[5].length()));
                            int second_right = funcParamReturn.get(callFuncIndex).get("[Para0]");
                            pair p1 = new pair(funcIndex, first_right);
                            pair p2 = new pair(callFuncIndex, second_right);
                            pair p3 = new pair(funcIndex, funcNodeIndex);
                            pair p4 = new pair(callFuncIndex, 0);
                            interGraphOut.println(p1.toString()+", "+p2.toString()+", ["+p3.toString()+", "+p4.toString()+"]");
                            for(int i = 1;i<paraN-1;i++){
                                first_right = Integer.parseInt(tokens[5+i].substring(0,tokens[5+i].length()));
                                second_right = funcParamReturn.get(callFuncIndex).get("[Para"+i+"]");
                                p1 = new pair(funcIndex, first_right);
                                p2 = new pair(callFuncIndex, second_right);
                                p3 = new pair(funcIndex, funcNodeIndex);
                                p4 = new pair(callFuncIndex, 0);
                                interGraphOut.println(p1.toString()+", "+p2.toString()+", ["+p3.toString()+", "+p4.toString()+"]");
                            }

                            int paraIndex = paraN-1;
                            first_right = Integer.parseInt(tokens[5+paraIndex].substring(0,tokens[5+paraIndex].length()-1));
                            second_right = funcParamReturn.get(callFuncIndex).get("[Para"+paraIndex+"]");
                            p1 = new pair(funcIndex, first_right);
                            p2 = new pair(callFuncIndex, second_right);
                            p3 = new pair(funcIndex, funcNodeIndex);
                            p4 = new pair(callFuncIndex, 0);
                            interGraphOut.println(p1.toString()+", "+p2.toString()+", ["+p3.toString()+", "+p4.toString()+"]");
                        }
                        //return
                        if(tokens[0].length()>2){
                            int second_right = Integer.parseInt(tokens[0].substring(1,tokens[0].length()-1));
                            pair p2 = new pair(funcIndex, second_right);
                            pair p4 = new pair(funcIndex, funcNodeIndex);
                            Map<String, Integer> t = funcParamReturn.get(callFuncIndex);
                            for(String s:t.keySet()){
                                if(s.startsWith("[Return")){
                                    int first_right = t.get(s);
                                    int callFuncRetNode = Integer.parseInt(s.substring(7,s.length()-1));
                                    pair p1 = new pair(callFuncIndex, first_right);
                                    pair p3 = new pair(callFuncIndex, callFuncRetNode);
                                    interGraphOut.println(p1.toString()+", "+p2.toString()+", ["+p3.toString()+", "+p4.toString()+"]");
                                }
                            }

                        }
                    }
                }
            }


            consEdgeGraphInput.close();
            var2indexMapInput.close();
            interGraphOut.close();
            printMap();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

    }
}
