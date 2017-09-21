package com.beisen.bigdata.test;

public class makeKey {
    public static void main(String[] args){
        for(int i = 0;i<=255;i++){
            String temp = Integer.toString(i);
            while(temp.length()!=3){
                temp = "0" + temp;
            }
            System.out.print("\""+temp+"|\""+ ",");
        }
    }
}
