package resource.services;

import java.util.LinkedList;

import org.hibernate.Session;


import qweb.qWeb;
import qweb.comps.SimpleVolumeApp;
import qweb.comps.SimpleVolumeApp.Volume;
import resource.model.MAutoRecord;
import resource.util.RHibernateUtil;
import resource.util.qConfigCloned;

public class MainRServer {

	
	public static class Config {
		public static int CFG_RUNNING_PORT = 10380;
		public static String CFG_ROOT_APP_PATH = "/msr";
		public static String CFG_APP_VOL_STORAGE_PATH = "/mvol";
		public static LinkedList<String> CFG_VOLUMES_INFO ;//= "MSR_VOL|C:\\temp\\MSR_VOL";
		public static String CFG_HIBERNATE_FILE_PATH="D:/E/workspace_1301/SVN_REPO_NEW/monsys/server/MsResourcesLayer/src/hibernate.cfg.xml";
		
	}
	
	
	SimpleVolumeApp sva = new SimpleVolumeApp();
	public boolean init() {
		SimpleVolumeApp.Config sva_cfg = new SimpleVolumeApp.Config();
		sva_cfg.CFG_VOLUMES_INFO = Config.CFG_VOLUMES_INFO;
		sva.setConfig(sva_cfg);
		return true;
	}
	
	public static void main(String[] args) {
		
		MainRServer mr = new MainRServer();
		if(!qConfigCloned.simpleLoad(new Config(), "msr.properties")) {
			System.out.println("ERROR: can't load msr.properties!");
			return;
		}
		
		mr.init();
		mr.start();
	}

	private void start() {
		qWeb.CFG_ROOT_APP_PATH = Config.CFG_ROOT_APP_PATH;
		//volume storage service
		qWeb.on(Config.CFG_RUNNING_PORT)
		.add_handler(Config.CFG_APP_VOL_STORAGE_PATH, sva)
		.start()
		;
		
	}
	
}
