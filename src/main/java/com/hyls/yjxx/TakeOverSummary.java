package com.hyls.yjxx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hyls.sql.ExcelUtils;
import com.hyls.utils.PropertiesUtils;

public class TakeOverSummary {
	public static void main(String[] args) {
		File inputFile = new File(PropertiesUtils.getProperty("filePath"));
		String sheetNo = PropertiesUtils.getProperty("sheetNo");
		String colIndex = PropertiesUtils.getProperty("colIndex");
		List<String> list =ExcelUtils.getContentByIndex(inputFile, Integer.parseInt(sheetNo), Integer.parseInt(colIndex));
		generateResultFile(list);
	}
	
	private static void generateResultFile(List<String> list) {
		Map<String ,Integer> resultMap= new HashMap<String, Integer>();
		int count = 0;
		for(String name: list) {
			if(resultMap.containsKey(name)) {
				count = resultMap.get(name);
				count = count+1;
				resultMap.put(name, count);
			}else {
				resultMap.put(name, 1);
			}
		}
		File outputFile  = new File(PropertiesUtils.getProperty("outputFilePath"));
		BufferedWriter out = null; 
		try {
			//pWriter = new PrintWriter(outputFile);
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "gbk"));
			for(Entry<String,Integer> entry: resultMap.entrySet()) {
				out.write(entry.getKey()+"\t" + entry.getValue());
				out.newLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
}
