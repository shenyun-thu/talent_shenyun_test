package com.beisen.bigdata.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class getTitle {
    public static void main(String[] args){
        File file = new File("C:\\Users\\shenyun\\Desktop\\test.txt");  
        StringBuilder result = new StringBuilder();
        int count = 0;
        String ans = "";
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s = null;
            while((s = br.readLine())!= null){
                ans +=  "\""+ s.substring(38,s.length()).toUpperCase() + "\"" + ",";
                count ++;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println(ans);
        System.out.println(count);
    }   
}
