package com.salesforce.dva.argus.service.metric.transform.plus;


import java.io.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.util.regex.*;

import scala.collection.mutable.HashMap;


//public interface Matrix{
//    int getLength();
//    int getWidth();
//}



public class Solution {
    /*
 * Complete the function below.
 */
	
	
	static int targetX;
	static int targetY;
	static int[][] a;
	static Map<String, Integer> cache;
    
    static int numberOfPaths(int[][] a) {
        assert(a!=null):"a is not valid";

        targetX=a.length-1;
        targetY=(targetX>0)?a[0].length-1:0;
        Solution.a=a;
        
        //List location=Arrays.asList(targetX,targetY);
        Solution.cache=(Map<String, Integer>) new HashMap<String,Integer>();
        return helper(targetX,targetY);
    }
    
    
    /*
    * recursively calling itself untill reach the target, and passing along the optimum value
    */
    final private static int helper(int x,int y){
    		final String currentKey=x+":"+y;
    		if (Solution.cache.containsKey(currentKey)){
    			return cache.get(currentKey);
    		}
    	
            // if X,Y out boudery: return Max
            if (x<0||x>Solution.targetX||y<0||y>Solution.targetY){
                return 0;
            }
            
            if (Solution.a[x][y]==0){
                return 0;
            }
            
            if (x==0&&y==0){
                return 1;
            }
            
            int option1=helper(x,y-1);
            int option2=helper(x-1,y);
            Solution.cache.put(currentKey, option1+option2);
            return cache.get(currentKey);
    } 
     
    
    public static void main(String[] args) throws IOException{
        Scanner in = new Scanner(System.in);
        final String fileName = System.getenv("OUTPUT_PATH");
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
        int res;
        
        int _a_rows = 0;
        int _a_cols = 0;
        _a_rows = Integer.parseInt(in.nextLine().trim());
        _a_cols = Integer.parseInt(in.nextLine().trim());
        
        int[][] _a = new int[_a_rows][_a_cols];
        for(int _a_i=0; _a_i<_a_rows; _a_i++) {
            for(int _a_j=0; _a_j<_a_cols; _a_j++) {
                _a[_a_i][_a_j] = in.nextInt();
                
            }
        }
        
        if(in.hasNextLine()) {
          in.nextLine();
        }
        
        res = numberOfPaths(_a);
        bw.write(String.valueOf(res));
        bw.newLine();
        
        bw.close();
        
    }
}