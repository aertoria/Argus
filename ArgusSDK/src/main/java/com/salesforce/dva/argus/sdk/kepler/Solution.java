package com.salesforce.dva.argus.sdk.kepler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author ethan.wang
 *
 */
public class Solution {
	 private int[] nums1;
	 private int[] nums2;
	
	 /**
	  * @param nums1
	  * @param nums2
	  * @return
	  */
	 public double findMedianSortedArrays(int[] nums1, int[] nums2) {
		this.nums1=nums1;
		this.nums2=nums2;
		List<Integer> merged=mergeTwoList();
		if (merged.isEmpty()){
			return 0;
		}
		return medianOf(merged);
	 }
	 
	 /**
	  * 
	  * @param list
	  * @return
	  */
	 private double medianOf(List<Integer> list){
		 assert(!list.isEmpty()):"list can not be empty";
		 
		 if(list.size()%2==0){
			 return (list.get((list.size()-1)/2)+list.get(list.size()/2))/2.0d;
		 }else{
			 return list.get((list.size()-1)/2);
		 }
	 }
	 
	 
	 
	 /**
	  * 
	  * @return
	  */
	 private List<Integer> mergeTwoList(){
		return mergeHelper(0,0);
	 }
	 
	 /**
	  * 
	  * @param i
	  * @param j
	  * @return
	  */
	 private List<Integer> mergeHelper(int i,int j){
		if (i==nums1.length){
			return tillEnd(j,nums2);
		}
		
		if (j==nums2.length){
			return tillEnd(i,nums1);
		}
		
		if (nums1[i]>=nums2[j]){
			List<Integer> result=new ArrayList<Integer>(Arrays.asList(nums2[j]));
			result.addAll(mergeHelper(i, j+1));
			return result;
		}else{
			List<Integer> result=new ArrayList<Integer>(Arrays.asList(nums1[i]));
			result.addAll(mergeHelper(i+1, j));
			return result;
		}
	 }
	 
	 /**
	  * 
	  * @param start
	  * @param nums
	  * @return
	  */
	 private List<Integer> tillEnd(int start,int[] nums){		 
		 List<Integer> result=new ArrayList<Integer>();
		 for(int idx=start;idx<nums.length;idx++){
			 result.add(nums[idx]);
		 }
		 return result;
	 }
	 
}
