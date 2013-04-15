package edu.gatech.aristotle.jsretest;

import org.mozilla.javascript.*;
import org.mozilla.javascript.ast.*;
import java.io.*;

public class TryParser {
	
	public static void main(String[] args) throws FileNotFoundException, IOException{
		Parser parser=new Parser();
		
		AstNode root=parser.parse(new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\Shoreray\\Dropbox\\WALA\\tests\\hello.js"))),"C:\\Users\\Shoreray\\Dropbox\\WALA\\tests\\hello.js",1);
		System.out.println(root.debugPrint());
		//System.out.println(root.toSource());
	}

}
