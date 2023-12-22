package resource.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class qConfigCloned {


	static class qError {

		public static boolean logi(int status, String module, String func, String msg, boolean showMsgOK) {
			boolean ret =false;
			String log=null;
			if(status < 0){
				log = "["+module+"],["+func+"],"  +",ERROR,\t"+ msg + ", ecode="+status;
				System.out.println(log);
				ret = false;
			} else {
				if(showMsgOK) {
					log = "["+module+"],["+func+"]," + ",OK,\t"+ msg ;
					System.out.println(log);
				}
				ret = true;
			}
			
			
			return ret;
		}
		
	}
	public static boolean CFG_PARSE_ENV = true;
	
	
	public static <T> boolean autoMap(Object forClass, String cfgFile) throws Exception {
		return autoMap_selected(forClass, cfgFile, null);
	}
	
	public static <T> boolean autoMap_selected(Object forClass, InputStream cfg_stream, String[] selected_fields) throws Exception {

		
		boolean ret = true;
		
		List<String> fields_selected=null;
		///
		if(selected_fields!=null && selected_fields.length>0) {
			fields_selected = Arrays.asList(selected_fields);
		}
		///
		
		Class<?> clz;
		
			Properties toolProps = new Properties();
			toolProps.load(new InputStreamReader(cfg_stream, Charset.forName("UTF-8")));
			
			///////////
			clz = Class.forName(forClass.getClass().getName());
//			System.out.println(forClass.getClass().getName());
			
			Field[] f = clz.getDeclaredFields();
			
			for(int i = 0; i < f.length; i++) {
				Class<?> t = f[i].getType();
//	            System.out.println("field name : " + f[i].getName() + " , type : " + t.getName());
	            //TODO: classify, extract and set property type here!!!
				
				String type = t.getName();
				String prop_name = f[i].getName();
	            String prop_value_string = toolProps.getProperty(f[i].getName());
	            
	            if((fields_selected!=null) && !qString.is_in_list_of(prop_name, fields_selected, "ONE")){
	            	continue;
	            }
	            
	            if(prop_value_string ==null) {
	            	qError.logi(-1, "qConfig", "autoMap", 
	            			"missing tag "+f[i].getName()+" in properties file" , true);
	            	ret = false;
					throw new Exception("TODO: no tag in config file " + f[i].getName());
	            }
	            
	            prop_value_string = prop_value_string.trim();
	            
	            if(CFG_PARSE_ENV)
	            	prop_value_string = resolveEnvVars(prop_value_string);
	            
	            if("int".equals(type)) {
					set(forClass, prop_name, new Integer(prop_value_string)); 
				}
	            else if("long".equals(type)) {
					set(forClass, prop_name, new Long(prop_value_string)); 
				} 
				else if("java.lang.String".equals(type)) {
					set(forClass, prop_name, prop_value_string); 
				}
				else if("boolean".equals(type)) {
					set(forClass, prop_name, new Boolean(prop_value_string)); 
				}
				else if("double".equals(type)) {
					set(forClass, prop_name, new Double(prop_value_string)); 
				}
				else if("float".equals(type)) {
					set(forClass, prop_name, new Float(prop_value_string)); 
				}
				else if("com.vfplm.quick.qConfig$qConfigObjects".equals(type)) {
					set(forClass, prop_name, qConfigCloned.parseConfigObjects(prop_value_string)); 
				}
				else if("java.util.LinkedList".equals(type)) {
					set(forClass, prop_name, qConfigCloned.parseAlist(prop_value_string)); 
				}
	            
				else {
					qError.logi(-1, "qConfig", "serialize", "TODO: not support field type " +type+"/"+ prop_name, true);
					ret = false;
					throw new Exception("TODO: not support field type " +type+"/"+ prop_name);
				}
				
	            //set(forClass, f[i].getName(), "10"); 
			}
			
		return ret;
	
	}
	/**
	 * Auto map properties of object to property file. Support parsing for int/bool/string/double...<br>
	 * ie: <pre>
	 * qConfig.autoMap(new MyConfig(), "c:\cfg.propperties"); <br>
	 * print(MyConfig.CFG_ABC);
	 * </pre>
	 * @param forClass
	 * @param cfgFile
	 * @return
	 * @throws Exception
	 */
	public static <T> boolean autoMap_selected(Object forClass, String cfgFile, String[] selected_fields) throws Exception{
		FileInputStream in = new FileInputStream( cfgFile );
		return autoMap_selected(forClass, in, selected_fields);
	}	
	
	private static LinkedList<String> parseAlist(String prop_value_string) {
		
		String[] als = prop_value_string.split(";");
		LinkedList<String> ls = new LinkedList<String>();
		for(int i=0; i< als.length; i++) {
			ls.add(als[i]);
		}
		
		return ls;
	}

	/*
	 * Returns input string with environment variable references expanded, e.g. $SOME_VAR or ${SOME_VAR}
	 * https://stackoverflow.com/questions/2263929/regarding-application-properties-file-and-environment-variable
	 */
	private static String resolveEnvVars(String input)
	{
	    if (null == input)
	    {
	        return null;
	    }
	    // match ${ENV_VAR_NAME} or %ENV_VAR_NAME%
	    Pattern p = Pattern.compile("\\$\\{(\\w+)\\}|%(\\w+)%");
	    Matcher m = p.matcher(input); // get a matcher object
	    StringBuffer sb = new StringBuffer();
	    while(m.find()){
	        String envVarName = null == m.group(1) ? m.group(2) : m.group(1);
	        String envVarValue = System.getenv(envVarName);
	        m.appendReplacement(sb, null == envVarValue ? "" : Matcher.quoteReplacement(envVarValue));
	    }
	    m.appendTail(sb);
	    return sb.toString();
	}
	
	private static boolean set(Object object, String fieldName, Object fieldValue) {
	    Class<?> clazz = object.getClass();
	    while (clazz != null) {
	        try {
	            Field field = clazz.getDeclaredField(fieldName);
	            field.setAccessible(true);
	            field.set(object, fieldValue);
	            return true;
	        } catch (NoSuchFieldException e) {
	            clazz = clazz.getSuperclass();
	        } catch (Exception e) {
	            throw new IllegalStateException(e);
	        }
	    }
	    return false;
	}
	
	/**
	 * Quick map properties to Config class. Support parsing for int/bool/string/double...<br>
	 * ie: <pre>
	 * qConfig.simpleLoad(new MyConfig(), "my.properties"); <br>
	 * print(MyConfig.CFG_ABC);
	 * 
	 * From command: java -Dmy.properties="c:\cfg.propperties" app.Main
	 * </pre>
	 * @param cfClass
	 * @param javaD - java parameter point to config file
	 * @return
	 */
	public static boolean simpleLoad(Object cfClass, String javaD){
		try {
			autoMap(cfClass, System.getProperty(javaD));
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	////////////////////////////////////////////////////////////
	// C F G   O B J E C T
	////////////////////////////////////////////////////////////
	
	/**
	 * first element is header settings with format:
	 * <code> {"@header@": "option1=value1;option2=value2;..."} </code><br>
	 * Options supported: <br>
	 * <code>key</code> : declare property name where its value is unique among configuration objects to index them
	 * <pre>
	 * Input: 
	 * //cfg.properties file
	 * matrix_mg=[ \
	 * 		     {"@header@": "key=id"           }\
	 * 		    ,{"id": "^BODY_IN_WHITE$"        	,"user_id": "facanha"  ,"email": "v.facanha@vinfast.vn"           }\
	 * 		    ,{"id": "^DOORS_AND_CLOSURES$"       ,"user_id": "trungdd"  ,"email": "v.trungdd4@vinfast.vn"           }\
	 * 			]
	 * Call:
	 *  qConfigObjects cfg = parseConfigObjects((String) matrix_mg)		
	 * Access:
	 *  usr1_email = cfg.obj("^BODY_IN_WHITE$").prop("email");
	 *  	
	 * </pre>
	 * @param ls_str_objs
	 * @return
	 */
	public static qConfigObjects parseConfigObjects (String ls_str_objs) {
		
		qConfigObjects cfg_objs = new qConfigObjects();
		
		LinkedHashMap<String, String> parseCfg_settings = new LinkedHashMap<String, String>();
		
		
		JSONArray ja;
		try {
			ja = new JSONArray(ls_str_objs);
			String key_prop = null;
			JSONObject jheader_settings = null;
			
			//read Parser settings
			if(ja.length() > 0) {
				jheader_settings = ja.getJSONObject(0);
				String settings_str = jheader_settings.getString("@header@");
				String[] settings = settings_str.split(";");
				
				for(int i=0; i< settings.length;i++) {
					String s = settings[i];
					String[] pv = s.split("=");
					parseCfg_settings.put(pv[0], pv[1]);
				}
				
				//extract Settings
				key_prop = parseCfg_settings.get("key"); //name of "key" properties of all bellowing objects
			}
			
			if(key_prop ==null) return cfg_objs;
			
			for(int i = 1; i < ja.length(); i++) {
				JSONObject jo = ja.getJSONObject(i);
				String[] props_ = JSONObject.getNames(jo);
				
				String _key_obj = jo.getString(key_prop);
				_qConfigObject _obj = new _qConfigObject();
				
				for(int j=0; j< props_.length; j++)
					_obj.prop_val.put(props_[j], jo.getString(props_[j]));
				
				cfg_objs.ls_objects.put(_key_obj, _obj);
			}
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return cfg_objs;
	}
	
	public static class qConfigObjects {
		static _qConfigObject __emptyCfgObj = new _qConfigObject();
		
		//<Object Unique key, object>
		@Deprecated
		public  LinkedHashMap<String, _qConfigObject> ls_objects = null;
		
		public qConfigObjects () {
			ls_objects = new LinkedHashMap<String, _qConfigObject>();
		}
		/**
		 * Return configuration object by key
		 * @param x
		 * @return
		 */
		public _qConfigObject obj(String x) {
			
			if(ls_objects.containsKey(x))
				return ls_objects.get(x);
			
			return __emptyCfgObj;
		}
		
	}
	
	@Deprecated
	public static class _qConfigObject {
		
		@Deprecated public LinkedHashMap<String, String> prop_val = null;
		
		public _qConfigObject() {
			prop_val = new LinkedHashMap<String, String>();
		}
		
		/**
		 * return value of a property
		 * @param prop_name
		 * @return
		 */
		public String prop(String prop_name) {
			if(prop_val.containsKey(prop_name))
				return prop_val.get(prop_name);
			
			return null;
		}
	}
}
