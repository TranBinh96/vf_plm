package resource.util;

import java.io.File;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import resource.services.MainRServer.Config;

public class RHibernateUtil {

	private static SessionFactory ssFact = null;//buildSessionFactory();
	
	private static SessionFactory buildSessionFactory() {
		try {
			if(ssFact == null) {
				File cfg = new File(Config.CFG_HIBERNATE_FILE_PATH);
				StandardServiceRegistry  standardRegistry = null;
				
				if(cfg.exists()) {
					System.out.println("loading external config! hibernate.cfg.xml");
					standardRegistry  = new StandardServiceRegistryBuilder()
							.configure(cfg).build();
				} else {
					System.out.println("loading embedded config! hibernate.cfg.xml");

				  standardRegistry  = new StandardServiceRegistryBuilder()
						.configure("/resource/hibernate.cfg.xml").build();
				}
				
				Metadata metaData = new MetadataSources (standardRegistry)
						.getMetadataBuilder()
						.build();
				ssFact = metaData.getSessionFactoryBuilder().build();
			}
			
			return ssFact;
		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}
	
	public static SessionFactory getSessionFactory() {
		if(ssFact==null) ssFact = buildSessionFactory();
		return ssFact;
	}

	public static void shutdown() {
		getSessionFactory().close();
	}
	
}
