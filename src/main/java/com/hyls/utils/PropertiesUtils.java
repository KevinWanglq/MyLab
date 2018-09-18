package com.hyls.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtils {
	
	private final static Properties properties = new Properties();
	static {
		readProperties();
	}
	
	public static void readProperties() {
	    try {
	        // ʹ��ClassLoader����properties�����ļ����ɶ�Ӧ��������
	        InputStream in = PropertiesUtils.class.getClassLoader().getResourceAsStream("config.properties");
	        // ʹ��properties�������������
	        properties.load(in);
	        //��ȡkey��Ӧ��valueֵ
	        //properties.getProperty(String key);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getProperty(String key) {
		return properties.getProperty(key);
	}
}
