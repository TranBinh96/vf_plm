package com.vfplm.tcic.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestClass {


   	static boolean vfex_execute_cat_list_export_file() {
   		boolean ret = false;
   		
      	//1. check existence of file %WORK_DIR%\\tmp\\vfex_cat_list.txt
   		
   		String wrk_dir = System.getenv("WORK_DIR");
   		
   		File _f_catlist = new File(wrk_dir +"\\tmp\\vfex_cat_list.txt");
   		boolean exists = _f_catlist.exists();
   		
   		if(!exists) return true;
   		
   		//1.1 read %WORK_DIR%\\tmp\\tcic_selection.xml
   		
		String content = null;
		try {
			 content = new String(Files.readAllBytes(Paths.get(wrk_dir +"\\tmp\\tcic_selection.xml")), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(content==null) return true;
		
		
		String item_id="";
		String object_type="";
		String item_revision_id="";
		String revrule_name="";
		
		Matcher matcher = Pattern.compile("item_id=\"(\\w+?)\"").matcher(content);
		if (matcher.find()) item_id = matcher.group(1);
		
		matcher = Pattern.compile("object_type=\"(\\w+?)\"").matcher(content);
		if (matcher.find()) object_type = matcher.group(1);
		
		matcher = Pattern.compile("item_revision_id=\"(.+?)\"").matcher(content);
		if (matcher.find()) item_revision_id = matcher.group(1);
		
		matcher = Pattern.compile("revrule name=\"(.+)\"").matcher(content);
		if (matcher.find()) revrule_name = matcher.group(1);
		

		String _select_line = item_id+ "/"+ object_type+ "/"+item_revision_id+ "/"+revrule_name;
		
      	//2. check appropriate info inside
		
		//2.1 read content
		ArrayList<String> lines_required = new ArrayList<>();
		
   		
		try {
		    BufferedReader bufReader = new BufferedReader(new FileReader(_f_catlist));

		    String line = bufReader.readLine();
		    while (line != null) {
		    	lines_required.add(line);
		      line = bufReader.readLine();
		    }

		    bufReader.close();
		    
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
		
		String header_catlist = "";
		if(lines_required.size() >0) {
			header_catlist = lines_required.remove(0);
		}
		
		if(!header_catlist.equals(_select_line)) {
			System.out.println("selected_file="+_select_line );
			System.out.println("required="+header_catlist );
			return false;
		}
		
   		//2.2 file_idenfity - (item_id/item_type/item_rev_id/rev_rule)
		
		
      	//3. read %WORK_DIR%\\Export\\ExpSpreadSheet.txt
		ArrayList<String> lines_actual = new ArrayList<>();

		try {
		    BufferedReader bufReader = new BufferedReader(new FileReader(wrk_dir + "\\Export\\ExpSpreadSheet.txt"));

		    String line = bufReader.readLine();
		    while (line != null) {
		    	lines_actual.add(line.trim());
		      line = bufReader.readLine();
		    }

		    bufReader.close();
		    
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
		
		String org_head_line = null;
		if(lines_actual.size()>=0) org_head_line = lines_actual.remove(0);
		
      	//4. remove unlisted files not in vfex list
		LinkedHashSet<String> lines_final = new LinkedHashSet<String>();
		lines_final.add(org_head_line);
		
		for(String line: lines_actual) {
			
			if(line.contains(".CATDrawing")) continue; //ignore CATDRawing
			
			for(String required: lines_required) {
				if(line.contains(required)) {
					lines_final.add(line);
				}
			}
		}
		
      	//5. save back to ExpSpreadSheet.txt
		try {
			PrintWriter pw = new PrintWriter(wrk_dir + "\\Export\\ExpSpreadSheet.txt");
			pw.close(); //to clear old content
			
			FileWriter writer = new FileWriter(wrk_dir + "\\Export\\ExpSpreadSheet.txt"); 
			for(String str: lines_final) {
			  writer.write(str + System.lineSeparator());
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
   	
	public static void main(String...args) {

		vfex_execute_cat_list_export_file();
	}
	
	static void test1() {

		String content = null;
		try {
			 content = new String(Files.readAllBytes(Paths.get("C:/Siemens/TCIC_V5_tmp/tmp/tcic_selection.xml")), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String item_id="";
		String object_type="";
		String item_revision_id="";
		String revrule_name="";
		
		Matcher matcher = Pattern.compile("item_id=\"(\\w+?)\"").matcher(content);
		if (matcher.find()) item_id = matcher.group(1);
		
		matcher = Pattern.compile("object_type=\"(\\w+?)\"").matcher(content);
		if (matcher.find()) object_type = matcher.group(1);
		
		matcher = Pattern.compile("item_revision_id=\"(\\w+?)\"").matcher(content);
		if (matcher.find()) item_revision_id = matcher.group(1);
		
		matcher = Pattern.compile("revrule name=\"(.+)\"").matcher(content);
		if (matcher.find()) revrule_name = matcher.group(1);
		
		String _out = item_id+ "/"+ object_type+ "/"+item_revision_id+ "/"+revrule_name+ "/";
		
		System.out.println(_out); 
	}
}
