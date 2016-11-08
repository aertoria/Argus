package com.salesforce.dva.argus.sdk.kepler;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SolutionTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() {
		Solution s=new Solution();
		
		int[] nums1={};
		int[] nums2={};
		double r=s.findMedianSortedArrays(nums1, nums2);
		System.out.println(r);
	}

}
