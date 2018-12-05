package com.hyls.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.hyls.sql.ExcelUtils.ColType;

public class ExcelUtils {
	//类型列表  int  = 0  ， String = 1
	public enum ColType{
		String{
			@Override
			public StringBuffer buildSql(StringBuffer sql, String value) {
				sql.append("'").append(value).append("',");
				return sql ;
			}
		},Integer{
			@Override
			public StringBuffer buildSql(StringBuffer sql, String value) {
				// TODO Auto-generated method stub
				sql.append(value).append(",");
				return sql;
			}
			
		};

		public abstract StringBuffer buildSql(StringBuffer sql, String value);
	}
	
	public static void main(String[] args) {
		File f = new File("F:\\work\\Routine\\1.xlsx");
		File outputFile = new File("F:\\work\\Routine\\out.sql");
		ColType[] clt= {ColType.Integer,ColType.String,ColType.String};
		String[] colNames = {"code","name","type"};
		SqlPart sp = new SqlPart();
		sp.setSheetNo(4);
		sp.setTableName("test");
		sp.setTypeArray(clt);
		sp.setColNames(colNames);
		
		generateInsertSql(f, outputFile, sp);
	}
	
	public static List<String> getContentByIndex(File inputFile, int sheetNo,int colIndex){
		List<String> list = new ArrayList<String>();
		HSSFWorkbook xwb = null;
		try {
		    xwb = new HSSFWorkbook(new FileInputStream(inputFile));
			HSSFSheet sheet = xwb.getSheetAt(sheetNo);
			HSSFRow row = null;
			HSSFCell cell = null;
			int rowStart =sheet.getFirstRowNum();
			int rowEnd = sheet.getPhysicalNumberOfRows();
			
			/*for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
		        CellRangeAddress region = sheet.getMergedRegion(i); // 
		        int colIndex1 = region.getFirstColumn();             // 合并区域首列位置
		        int rowNum = region.getFirstRow();                     // 合并区域首行位置
		        System.out.println("第[" + i + "]个合并区域：" +"rowNum :"+rowNum+ "colIndex1"+colIndex1+" " +sheet.getRow(rowNum).getCell(colIndex1).getStringCellValue());
		    }*/
			for (int i = rowStart+1; i <rowEnd; i++) {
				row = sheet.getRow(i);
				if(row==null) {
					continue;
				}
				cell = row.getCell(colIndex);
				if(cell ==null) {
					continue;
				}
				cell.setCellType(CellType.STRING);
				list.add(cell.getStringCellValue());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(xwb!=null) {
				try {
					xwb.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return list;
	}
	
	public static void generateInsertSql(File inputFile,File outputFile,SqlPart sp) {
		PrintWriter pw  = null; 
		XSSFWorkbook xwb = null;
		try {
		    pw = new PrintWriter(outputFile);
		    xwb = new XSSFWorkbook(new FileInputStream(inputFile));
			XSSFSheet sheet = xwb.getSheetAt(sp.getSheetNo());
			XSSFRow row = null;
			XSSFCell cell = null;
			StringBuffer basicSql =new StringBuffer("insert into "+ sp.getTableName() +"(");
			for(String col : sp.getColNames()) {
				basicSql.append(col).append(",");
			}
			basicSql =new StringBuffer(basicSql.substring(0, basicSql.length()-1));
			basicSql.append(") values (");
			int rowStart =sheet.getFirstRowNum();
			int rowEnd = sheet.getPhysicalNumberOfRows();
			if(sp.getRowStartIndex() != 0) {
				rowStart = sp.getRowStartIndex();
			}
			if(sp.getRowEndIndex()!=0) {
				rowEnd = sp.getRowEndIndex();
			}
			for (int i = rowStart; i <rowEnd; i++) {
				row = sheet.getRow(i);
				int colStart =row.getFirstCellNum();
				int colEnd = row.getPhysicalNumberOfCells();
				if(sp.getColStartIndex() != 0) {
					colStart = sp.getColStartIndex();
				}
				if(sp.getColEndIndex()!=0) {
					colEnd = sp.getColEndIndex();
				}
				StringBuffer sql = new StringBuffer(basicSql);
				for(int j =colStart;j<colEnd;j++) {
					cell = row.getCell(j);
					cell.setCellType(CellType.STRING);
					sql= sp.getTypeArray()[j].buildSql(sql, cell.getStringCellValue());
					//sql = buildSql(sql,cell.getStringCellValue(),sp.getTypeArray()[j]);
					
				}
				sql = new StringBuffer(sql.substring(0, sql.length()-1));
				sql.append(");");
				pw.println(sql);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			pw.flush();
			if(pw!=null) {
				pw.close();
			}
			if(xwb!=null) {
				try {
					xwb.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**   
	* 获取单元格的值   
	* @param cell   
	* @return   
	*/    
	public String getCellValue(Cell cell){    
	    if(cell == null) return "";    
	    if(cell.getCellType() == Cell.CELL_TYPE_STRING){    
	        return cell.getStringCellValue();    
	    }else if(cell.getCellType() == Cell.CELL_TYPE_BOOLEAN){    
	        return String.valueOf(cell.getBooleanCellValue());    
	    }else if(cell.getCellType() == Cell.CELL_TYPE_FORMULA){    
	        return cell.getCellFormula() ;    
	    }else if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC){    
	        return String.valueOf(cell.getNumericCellValue());    
	    }
	    return "";    
	}

	/*private static StringBuffer buildSql(StringBuffer sql, String value,ColType type) {
		switch (type) {
		case String:
			sql.append("'").append(value).append("',");
			break;
		case Integer:
			sql.append(value).append(",");
		default:
			break;
		}
		return sql;
	}*/
	
}

/**
 * @author Administrator
 *
 */
class SqlPart{
	private String tableName ;
	private int sheetNo ;
	//sheet 从第几列开始 
	private int colStartIndex;
	//sheet 到第几列结束
	private int colEndIndex;
	// start from 1
	private int rowStartIndex;
	private int rowEndIndex;
	private String[] colNames;
	private ColType[] typeArray ;
	private int[] skipCol;
	public int getSheetNo() {
		return sheetNo;
	}
	public void setSheetNo(int sheetNo) {
		this.sheetNo = sheetNo;
	}
	public int getColStartIndex() {
		return colStartIndex;
	}
	public void setColStartIndex(int colStartIndex) {
		this.colStartIndex = colStartIndex;
	}
	public int getColEndIndex() {
		return colEndIndex;
	}
	public void setColEndIndex(int colEndIndex) {
		this.colEndIndex = colEndIndex;
	}
	public int getRowStartIndex() {
		return rowStartIndex;
	}
	public void setRowStartIndex(int rowStartIndex) {
		this.rowStartIndex = rowStartIndex;
	}
	public int getRowEndIndex() {
		return rowEndIndex;
	}
	public void setRowEndIndex(int rowEndIndex) {
		this.rowEndIndex = rowEndIndex;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String[] getColNames() {
		return colNames;
	}
	public void setColNames(String[] colNames) {
		this.colNames = colNames;
	}
	public ColType[] getTypeArray() {
		return typeArray;
	}
	public void setTypeArray(ColType[] typeArray) {
		this.typeArray = typeArray;
	}
	public int[] getSkipCol() {
		return skipCol;
	}
	public void setSkipCol(int[] skipCol) {
		this.skipCol = skipCol;
	}
	
}
