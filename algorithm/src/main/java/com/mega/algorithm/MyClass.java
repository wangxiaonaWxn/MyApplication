package com.mega.algorithm;

public class MyClass {
    private static int[] num = new int[]{1,31,5,2,9,10,8};
    public static void main(String arg[]) {
        sort();
    }

    private static void sort() {
        int test = 8;
        test = test << 1;
        test = test >> 2;
        System.out.println(test);
        int len = num.length;
        for (int i=0;i<len;i++) {
            for (int j = i+1;j<len;j++) {
                if (num[i] > num[j]) {
                    int temp = num[i];
                    num[i] = num[j];
                    num[j] = temp;
                }
            }
        }
        for (int nu : num) {
            System.out.println(nu);
        }
    }
}