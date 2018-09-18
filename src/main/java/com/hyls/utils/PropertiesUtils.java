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
	        // 使用ClassLoader加载properties配置文件生成对应的输入流
	        InputStream in = PropertiesUtils.class.getClassLoader().getResourceAsStream("config.properties");
	        // 使用properties对象加载输入流
	        properties.load(in);
	        //获取key对应的value值
	        //properties.getProperty(String key);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getProperty(String key) {
		return properties.getProperty(key);
	}
}
