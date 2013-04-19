package edu.gatech.aristotle.jsretest.coverage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CoverageData cov = new CoverageData("/Users/jielu/Dropbox/JSCover-0.2.6_updated/target");
		
		// Output line coverage
		System.out.println("############################# Line Coverage #############################");
		Iterator<Entry<String, HashMap<Integer, ArrayList<String>>>>it = cov.getLinesCoverage().entrySet().iterator();
		while(it.hasNext()){
			Entry<String, HashMap<Integer, ArrayList<String>>> fileCov = it.next();
			System.out.println("==================== " + fileCov.getKey() + " ========================");
			System.out.println("==================== Covered Lines: " + fileCov.getValue().size() + " ====================");
			Iterator<Entry<Integer, ArrayList<String>>> it2 = fileCov.getValue().entrySet().iterator();
			while(it2.hasNext()){
				Entry<Integer, ArrayList<String>> lineCov = it2.next();
				StringBuilder builder = new StringBuilder();
				builder.append("#### Line ");
				builder.append(lineCov.getKey());
				builder.append(": \n");
				for(int i=0; i< lineCov.getValue().size(); i++){
					builder.append(lineCov.getValue().get(i));
					builder.append("\n");
				}
				System.out.println(builder.toString());
			}
		}
		
		// Output branch coverage
		System.out.println();
		System.out.println("############################# Branch Coverage #############################");
		Iterator<Entry<String, HashMap<String, ArrayList<String>>>>it3 = cov.getBranchesCoverage().entrySet().iterator();
		while(it3.hasNext()){
			Entry<String, HashMap<String, ArrayList<String>>> fileCov = it3.next();
			System.out.println("==================== " + fileCov.getKey() + " ========================");
			System.out.println("==================== Covered Branches: " + fileCov.getValue().size() + " ====================");
			Iterator<Entry<String, ArrayList<String>>> it4 = fileCov.getValue().entrySet().iterator();
			while(it4.hasNext()){
				Entry<String, ArrayList<String>> lineCov = it4.next();
				StringBuilder builder = new StringBuilder();
				builder.append("#### Branch ");
				builder.append(lineCov.getKey());
				builder.append(": \n");
				for(int i=0; i< lineCov.getValue().size(); i++){
					builder.append(lineCov.getValue().get(i));
					builder.append("\n");
				}
				System.out.println(builder.toString());
			}
		}
		
		// Output switch coverage
		System.out.println();
		System.out.println("############################# Switch Coverage #############################");
		Iterator<Entry<String, HashMap<String, ArrayList<String>>>>it5 = cov.getSwitchCoverage().entrySet().iterator();
		while(it5.hasNext()){
			Entry<String, HashMap<String, ArrayList<String>>> fileCov = it5.next();
			System.out.println("==================== " + fileCov.getKey() + " ========================");
			System.out.println("==================== Covered Switches: " + fileCov.getValue().size() + " ====================");
			Iterator<Entry<String, ArrayList<String>>> it6 = fileCov.getValue().entrySet().iterator();
			while(it6.hasNext()){
				Entry<String, ArrayList<String>> lineCov = it6.next();
				StringBuilder builder = new StringBuilder();
				builder.append("#### Switch Case ");
				builder.append(lineCov.getKey());
				builder.append(": \n");
				for(int i=0; i< lineCov.getValue().size(); i++){
					builder.append(lineCov.getValue().get(i));
					builder.append("\n");
				}
				System.out.println(builder.toString());
			}
		}
	}
	
	

}
