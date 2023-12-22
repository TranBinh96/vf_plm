package resource.test;

import org.hibernate.Session;

import resource.model.MAutoRecord;
import resource.util.RHibernateUtil;

public class Test {

	public static void main(String[] args) {
		Session ss = RHibernateUtil.getSessionFactory().openSession();
		ss.beginTransaction();
		
		//add object
		MAutoRecord mar = new MAutoRecord();
		mar.hostname = "THIS-TEST";
		mar.cpu_usage = 0.12;
		
		ss.persist(mar);
	
		ss.getTransaction().commit();
		
		//
		RHibernateUtil.shutdown();
		
	}
}
