package com.hyls.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.hyls.db.DbConnection;
import com.hyls.model.ImageInfo;
import com.mysql.fabric.xmlrpc.base.Array;

public class FileUtils {
	
	/** 
     * ���ֽ�Ϊ��λ��ȡ�ļ��������ڶ��������ļ�����ͼƬ��������Ӱ����ļ��� 
     */  
    public static void readFileByBytes(String fileName) {  
        File file = new File(fileName);  
        InputStream in = null;  
        try {  
            System.out.println("���ֽ�Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���ֽڣ�");  
            // һ�ζ�һ���ֽ�  
            in = new FileInputStream(file);  
            int tempbyte;  
            while ((tempbyte = in.read()) != -1) {  
                System.out.write(tempbyte);  
            }  
            in.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
            return;  
        }  
        try {  
            System.out.println("���ֽ�Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�����ֽڣ�");  
            // һ�ζ�����ֽ�  
            byte[] tempbytes = new byte[100];  
            int byteread = 0;  
            in = new FileInputStream(fileName);  
            showAvailableBytes(in);  
            // �������ֽڵ��ֽ������У�bytereadΪһ�ζ�����ֽ���  
            while ((byteread = in.read(tempbytes)) != -1) {  
                System.out.write(tempbytes, 0, byteread);  
            }  
        } catch (Exception e1) {  
            e1.printStackTrace();  
        } finally {  
            if (in != null) {  
                try {  
                    in.close();  
                } catch (IOException e1) {  
                }  
            }  
        }  
    }  
  
    /** 
     * ���ַ�Ϊ��λ��ȡ�ļ��������ڶ��ı������ֵ����͵��ļ� 
     */  
    public static void readFileByChars(String fileName) {  
        File file = new File(fileName);  
        Reader reader = null;  
        try {  
            System.out.println("���ַ�Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���ֽڣ�");  
            // һ�ζ�һ���ַ�  
            reader = new InputStreamReader(new FileInputStream(file));  
            int tempchar;  
            while ((tempchar = reader.read()) != -1) {  
                // ����windows�£�\r\n�������ַ���һ��ʱ����ʾһ�����С�  
                // ������������ַ��ֿ���ʾʱ���ỻ�����С�  
                // ��ˣ����ε�\r����������\n�����򣬽������ܶ���С�  
                if (((char) tempchar) != '\r') {  
                    System.out.print((char) tempchar);  
                }  
            }  
            reader.close();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        try {  
            System.out.println("���ַ�Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�����ֽڣ�");  
            // һ�ζ�����ַ�  
            char[] tempchars = new char[30];  
            int charread = 0;  
            reader = new InputStreamReader(new FileInputStream(fileName));  
            // �������ַ����ַ������У�charreadΪһ�ζ�ȡ�ַ���  
            while ((charread = reader.read(tempchars)) != -1) {  
                // ͬ�����ε�\r����ʾ  
                if ((charread == tempchars.length)  
                        && (tempchars[tempchars.length - 1] != '\r')) {  
                    System.out.print(tempchars);  
                } else {  
                    for (int i = 0; i < charread; i++) {  
                        if (tempchars[i] == '\r') {  
                            continue;  
                        } else {  
                            System.out.print(tempchars[i]);  
                        }  
                    }  
                }  
            }  
  
        } catch (Exception e1) {  
            e1.printStackTrace();  
        } finally {  
            if (reader != null) {  
                try {  
                    reader.close();  
                } catch (IOException e1) {  
                }  
            }  
        }  
    }  
  
    /** 
     * ����Ϊ��λ��ȡ�ļ��������ڶ������еĸ�ʽ���ļ� 
     * ���������������
     */  
    public static List<ImageInfo> readFileByLines(String fileName) {  
        File file = new File(fileName);  
        BufferedReader reader = null;  
        List<ImageInfo> list = new ArrayList<>();
        try {  
        	InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");
            System.out.println("����Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���У�");  
            reader = new BufferedReader(read);  
            String tempString = null;  
            // һ�ζ���һ�У�ֱ������nullΪ�ļ�����  
            ImageInfo image = null;
            
            while ((tempString = reader.readLine()) != null) {  
            	image = new ImageInfo();
            	image.setPk(UuidUtils.getUUID());
            	String[] strings = tempString.split("_");
            	if(strings.length>1) {
            		image.setName(strings[1]);
                	image.setCode(strings[0]);
            	}else {
            		image.setName(tempString);
            		image.setCode("");
            	}
            	list.add(image);
                // ��ʾ�к� 
            	// solution but failed to solve the chinese characters problem;
            	//tempString = new String(tempString.getBytes("UTF-8"),"GBK");
                System.out.println(tempString);  
            }  
            reader.close();  
            
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            if (reader != null) {  
                try {  
                    reader.close();  
                } catch (IOException e1) {  
                }  
            }  
        }  
        return list;
    }  
  
    /** 
     * �����ȡ�ļ����� 
     */  
    public static void readFileByRandomAccess(String fileName) {  
        RandomAccessFile randomFile = null;  
        try {  
            System.out.println("�����ȡһ���ļ����ݣ�");  
            // ��һ����������ļ�������ֻ����ʽ  
            randomFile = new RandomAccessFile(fileName, "r");  
            // �ļ����ȣ��ֽ���  
            long fileLength = randomFile.length();  
            // ���ļ�����ʼλ��  
            int beginIndex = (fileLength > 4) ? 4 : 0;  
            // �����ļ��Ŀ�ʼλ���Ƶ�beginIndexλ�á�  
            randomFile.seek(beginIndex);  
            byte[] bytes = new byte[10];  
            int byteread = 0;  
            // һ�ζ�10���ֽڣ�����ļ����ݲ���10���ֽڣ����ʣ�µ��ֽڡ�  
            // ��һ�ζ�ȡ���ֽ�������byteread  
            while ((byteread = randomFile.read(bytes)) != -1) {  
                System.out.write(bytes, 0, byteread);  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            if (randomFile != null) {  
                try {  
                    randomFile.close();  
                } catch (IOException e1) {  
                }  
            }  
        }  
    }  
  
    /** 
     * ��ʾ�������л�ʣ���ֽ��� 
     */  
    private static void showAvailableBytes(InputStream in) {  
        try {  
            System.out.println("��ǰ�ֽ��������е��ֽ���Ϊ:" + in.available());  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
  
    public static void main(String[] args) {
		DbConnection.addImageInfo(readFileByLines("C:\\Users\\Administrator\\Desktop\\1.txt"), DbConnection.getConn());
	}
	  
	  
	
}	