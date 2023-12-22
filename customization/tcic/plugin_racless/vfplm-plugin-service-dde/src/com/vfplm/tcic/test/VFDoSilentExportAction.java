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

import com.ebsolutions.soacore.process.DoSilentExportAction;

public class VFDoSilentExportAction extends DoSilentExportAction {
	
	
	@Override
	protected int initProcessConfiguration() {
		// TODO Auto-generated method stub
		int x = super.initProcessConfiguration();
		
		MySampleSilentExport.vfex_execute_cat_list_export_file();

		return x;
	}
	
	@Override
	protected int coreProcess() {
		
		
		int x =  super.coreProcess();


		return x;
	}
	
	
	/////////////////////////////////////////////////


}
