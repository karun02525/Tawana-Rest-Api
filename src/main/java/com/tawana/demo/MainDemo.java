package com.tawana.demo;

public class MainDemo {
	
	public static void main(String[] args) {
		
		ReadPropertiesSingalton s=	ReadPropertiesSingalton.getInstance();
		System.out.println(s.getProperties().getProperty("user"));
		System.out.println(s.getProperties().getProperty("pass"));
		
		
	

	}

}
