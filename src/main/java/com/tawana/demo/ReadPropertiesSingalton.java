package com.tawana.demo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ReadPropertiesSingalton {
	
	private static ReadPropertiesSingalton instance=null;
	private static Properties properties=null;
	
	private ReadPropertiesSingalton() {
		System.out.println("Const.....call");
		try {
			FileInputStream fis=new FileInputStream("data.properties");
		    properties=new Properties();
			properties.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
				
	}
	
	public static synchronized ReadPropertiesSingalton getInstance() {
		 if(instance==null) {
			return instance=new ReadPropertiesSingalton();
		 }
		 return instance;
	}
	
	public Properties getProperties() {
		return properties;
	}

}
