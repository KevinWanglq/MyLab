package com.hyls.solution;

public class Solution {
    public static int[] twoSum(int[] nums, int target) {
        int s,t;
        int[] result={0,0};
        for(int i=0;i<nums.length;i++){
            for(int j=i+1;j<nums.length;j++){
                if(nums[i]+nums[j]==target){
                    result[0]=i;
                    result[1]=j;
                }
            }
            
        }
        return result;
    }
    
    /**
     *	 Êý×Ö·´×ª   428748 
     *
     * @param x
     * @return
     */
    public int reverse(int x) {
        int rev = 0;
        while (x != 0) {
            int pop = x % 10;
            x /= 10;
            if (rev > Integer.MAX_VALUE/10 || (rev == Integer.MAX_VALUE / 10 && pop > 7)) return 0;
            if (rev < Integer.MIN_VALUE/10 || (rev == Integer.MIN_VALUE / 10 && pop < -8)) return 0;
            rev = rev * 10 + pop;
        }
        return rev;
    }
    
    public static void main(String[] args) {
		/*int[] nums= {2, 7, 11, 15};
		int target= 9;
		int[] result=twoSum(nums, target);*/
    	
	}
}