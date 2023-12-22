package resource.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.hibernate.Session;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import resource.model.CfgColumnSet;
import resource.model.MAutoRecord;
import resource.services.MainRServer.Config;

/**
 * Monsys Resource util
 * @author truongdd3
 *
 */
public class MsrUtil {

	public static void main(String[] arg) {
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Enter command: 1 - init dB; ");
		String cmd =  scanner.nextLine();
		
			
			if(cmd.equals("1")) {
				System.out.println("Enter path to hibernate config:");
				String val =  scanner.nextLine();

				Config.CFG_HIBERNATE_FILE_PATH = val;
				//create db
				init();
				//create Tables
				CfgColumnSet cfgCol = new CfgColumnSet();
					Session ss = RHibernateUtil.getSessionFactory().openSession();
					ss.beginTransaction();
//					ss.persist(cfgCol);
//					ss.getTransaction().commit();
					ss.close();
			}
	}
	
	public static boolean init() {
		Session ss = RHibernateUtil.getSessionFactory().openSession();
		
		return ss==null? false: true;
	}
	
	///////////////////////////////////////////////////////
	
	public static void storeRecord(MAutoRecord mar) {
		Session ss = RHibernateUtil.getSessionFactory().openSession();
		ss.beginTransaction();
		
		//add object

		ss.persist(mar);
	
		ss.getTransaction().commit();
		
		//
		//https://stackoverflow.com/questions/42065877/entitymanagerfactory-is-closed-hibernate
		
		ss.close();
		
	}
	///////////////////////////////////////////////////////
	
	public static List<MAutoRecord> get_RecordsByID( int[] indexes) {

//		List<MAutoRecord> ls = new LinkedList<MAutoRecord>();
		
		Session ss = RHibernateUtil.getSessionFactory().openSession();

		Query query = ss.createNativeQuery("SELECT * FROM MAutoRecord as m WHERE ID in :IDs ", 
				MAutoRecord.class);
		
		List<Integer> list = new ArrayList<Integer>();
		Collections.addAll(list, Arrays.stream(indexes).boxed().toArray(Integer[]::new));
		
		query.setParameter( "IDs", list);

		List<MAutoRecord> ret = query.getResultList();
		
		
		return ret;
	
	}
	
	///////////////////////////////////////////////////////
	
	public static List<MAutoRecord>  getMeasureRecordsInTimeSequence(Date d_from, Date d_to, 
			String user, String machine, String[] colnames, String webh_01) {
//		List<MAutoRecord> ls = new LinkedList<MAutoRecord>();
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String fromDate= format.format(d_from);
		String toDate= format.format(d_to);

		Session ss = RHibernateUtil.getSessionFactory().openSession();

		Query query = ss.createNativeQuery("SELECT * FROM MAutoRecord as m WHERE m.soa_idUserTest = :user AND m.hostname = :machine AND m.soa_webServer = :webh_01 "
				+ "AND m.create_date BETWEEN :fromDate AND :toDate "
				+ "ORDER BY m.create_date ASC", 
				MAutoRecord.class);
		
		query.setParameter( "user", user);
		query.setParameter( "machine", machine);
		query.setParameter( "webh_01", webh_01);
		query.setParameter( "fromDate", fromDate);
		query.setParameter( "toDate", toDate );
		
		List<MAutoRecord> ret = query.getResultList();
		
		
		return ret;
	}

	///////////////////////////////////////////////////////
	
	public static List<MAutoRecord>  getMeasureRecordsInTimeSequenceLike(Date d_from, Date d_to, 
			String user, String machine, String[] colnames, String webh_01) {
//		List<MAutoRecord> ls = new LinkedList<MAutoRecord>();
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String fromDate= format.format(d_from);
		String toDate= format.format(d_to);

		Session ss = RHibernateUtil.getSessionFactory().openSession();

		Query query = ss.createNativeQuery("SELECT * FROM MAutoRecord as m WHERE m.soa_idUserTest like :user AND m.hostname like :machine AND m.soa_webServer like :webh_01 "
				+ "AND m.create_date BETWEEN :fromDate AND :toDate "
				+ "ORDER BY m.create_date ASC", 
				MAutoRecord.class);
		
		query.setParameter( "user", user);
		query.setParameter( "machine", machine);
		query.setParameter( "webh_01", webh_01);
		query.setParameter( "fromDate", fromDate);
		query.setParameter( "toDate", toDate );
		
		
		List<MAutoRecord> ret = query.getResultList();
		
		
		return ret;
	}
	///////////////////////////////////////////////////////
	
	
	
	
	public static CfgColumnSet getCfgColumnSet(String setName) {
		Session ss = RHibernateUtil.getSessionFactory().openSession();
		
		CfgColumnSet set = (CfgColumnSet) ss.get(CfgColumnSet.class, setName);
		
		return set;
	}
	
	public static List<String> getListCfgColumnSet() {
		Session ss = RHibernateUtil.getSessionFactory().openSession();
		EntityManager em = ss.getEntityManagerFactory().createEntityManager();
		
		List<String> ls = new LinkedList<String>();
		
		Query q = em.createNativeQuery("select DISTINCT  nameSet from CfgColumnSet");
		List<Object> x = q.getResultList();
		
		for(Object a: x) {
			if(!ls.contains(a)) {
				System.out.println(a);
				ls.add((String)a);
			}
		}
		
		return ls;
	}
	
	public static List<String> get_listUsers() {
		Session ss = RHibernateUtil.getSessionFactory().openSession();
		EntityManager em = ss.getEntityManagerFactory().createEntityManager();
		
		List<String> ls = new LinkedList<String>();
		
		Query q = em.createNativeQuery("select DISTINCT soa_idUserTest from MAutoRecord");
		List<Object> x = q.getResultList();
		
		for(Object a: x) {
			if(!ls.contains(a)) {

				System.out.println(a);
				ls.add((String)a);
			}
		}
		
		return ls;
	}

	public static List<String> get_listHosts() {
		Session ss = RHibernateUtil.getSessionFactory().openSession();
		EntityManager em = ss.getEntityManagerFactory().createEntityManager();
		
		List<String> ls = new LinkedList<String>();
		
		Query q = em.createNativeQuery("select DISTINCT hostname from MAutoRecord");
		List<Object> x = q.getResultList();
		
		for(Object a: x) {
			if(!ls.contains(a)) {
				System.out.println(a);
				ls.add((String)a);
			}
		}
		
		return ls;
	}
	
	//
	
	public static List<String> get_UniqueList4Select(String key) {
		Session ss = RHibernateUtil.getSessionFactory().openSession();
		EntityManager em = ss.getEntityManagerFactory().createEntityManager();
		
		List<String> ls = new LinkedList<String>();
		
		Query q = em.createNativeQuery("select DISTINCT "+ key +" from MAutoRecord");
		List<Object> x = q.getResultList();
		
		for(Object a: x) {
			if(!ls.contains(a)) {
				System.out.println(a);
				ls.add((String)a);
			}
		}
		
		return ls;
	}
	
}
