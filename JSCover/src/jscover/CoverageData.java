package jscover;

import java.util.ArrayList;
import java.util.HashMap;

public class CoverageData {
	 // Coverage matrix for all test cases
    // ArrayList<String> = <t1, t2...>; ArrayList<ArrayList<String>> = <t1, t2..>, <t2, t3>....; HashMap... = <script1, [<t1, t2..>, <t2, t3>....]..>
    public static HashMap<String, HashMap<Integer, ArrayList<String>>> linesCoverage = new  HashMap<String, HashMap<Integer, ArrayList<String>>>();
    // ArrayList<String> = <t1, t2...>; HashMap<String, ArrayList<String>> = [1T, <t1, t2>..]; HashMap ... = <script, [1T, <t1, t2>]>..
    public static HashMap<String, HashMap<String, ArrayList<String>>> branchesCoverage = new HashMap<String, HashMap<String, ArrayList<String>>>();
    
    //script -> 1,1-> test1
    public static HashMap<String, HashMap<String, ArrayList<String>>> switchCoverage = new HashMap<String, HashMap<String, ArrayList<String>>>();

}
