package com.hyls.utils;

import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;

/* ====================================================================
    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 ==================================================================== */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.SQLNonTransientConnectionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.eventusermodel.FormatTrackingHSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.eventusermodel.MissingRecordAwareHSSFListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.eventusermodel.EventWorkbookBuilder.SheetRecordCollectingListener;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.BoolErrRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.RKRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;


import com.hyls.db.DbConnection;
import com.hyls.model.ColumnInfo;
import com.hyls.model.DictInfo;
import com.hyls.model.SysModule;
import com.hyls.model.TableInfo;

/**
 * A XLS -> CSV processor, that uses the MissingRecordAware EventModel code to
 * ensure it outputs all columns and rows.
 * 
 * @author Nick Burch
 */
public class XLS2CSVmra implements HSSFListener {
	private int minColumns;
	private POIFSFileSystem fs;
	private PrintStream output;

	private int lastRowNumber;
	private int lastColumnNumber;

	/** Should we output the formula, or the value it has? */
	private boolean outputFormulaValues = true;

	/** For parsing Formulas */
	private SheetRecordCollectingListener workbookBuildingListener;
	private HSSFWorkbook stubWorkbook;

	// Records we pick up as we process
	private SSTRecord sstRecord;
	private FormatTrackingHSSFListener formatListener;

	/** So we known which sheet we're on */
	private int sheetIndex = -1;
	private BoundSheetRecord[] orderedBSRs;
	private ArrayList boundSheetRecords = new ArrayList();

	// For handling formulas with string results
	private int nextRow;
	private int nextColumn;
	private boolean outputNextStringRecord;

	/**
	 * Creates a new XLS -> CSV converter
	 * 
	 * @param fs         The POIFSFileSystem to process
	 * @param output     The PrintStream to output the CSV to
	 * @param minColumns The minimum number of columns to output, or -1 for no
	 *                   minimum
	 */
	public XLS2CSVmra(POIFSFileSystem fs, PrintStream output, int minColumns) {
		this.fs = fs;
		this.output = output;
		this.minColumns = minColumns;
	}

	/**
	 * Creates a new XLS -> CSV converter
	 * 
	 * @param filename   The file to process
	 * @param minColumns The minimum number of columns to output, or -1 for no
	 *                   minimum
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public XLS2CSVmra(String filename, int minColumns) throws IOException, FileNotFoundException {
		this(new POIFSFileSystem(new FileInputStream(filename)), System.out, minColumns);
	}

	/**
	 * Initiates the processing of the XLS file to CSV
	 */
	public void process() throws IOException {
		MissingRecordAwareHSSFListener listener = new MissingRecordAwareHSSFListener(this);
		formatListener = new FormatTrackingHSSFListener(listener);

		HSSFEventFactory factory = new HSSFEventFactory();
		HSSFRequest request = new HSSFRequest();

		if (outputFormulaValues) {
			request.addListenerForAllRecords(formatListener);
		} else {
			workbookBuildingListener = new SheetRecordCollectingListener(formatListener);
			request.addListenerForAllRecords(workbookBuildingListener);
		}

		factory.processWorkbookEvents(request, fs);
	}

	/**
	 * Main HSSFListener method, processes events, and outputs the CSV as the file
	 * is processed.
	 */
	public void processRecord(Record record) {
		int thisRow = -1;
		int thisColumn = -1;
		String thisStr = null;
		switch (record.getSid()) {
		case BoundSheetRecord.sid:
			boundSheetRecords.add(record);
			break;
		case BOFRecord.sid:
			BOFRecord br = (BOFRecord) record;
			if (br.getType() == BOFRecord.TYPE_WORKSHEET) {
				// Create sub workbook if required
				if (workbookBuildingListener != null && stubWorkbook == null) {
					stubWorkbook = workbookBuildingListener.getStubHSSFWorkbook();
				}

				// Output the worksheet name
				// Works by ordering the BSRs by the location of
				// their BOFRecords, and then knowing that we
				// process BOFRecords in byte offset order
				sheetIndex++;
				if (orderedBSRs == null) {
					orderedBSRs = BoundSheetRecord.orderByBofPosition(boundSheetRecords);
				}
				// output.println();
				/*
				 * output.println( orderedBSRs[sheetIndex].getSheetname() + " [" +
				 * (sheetIndex+1) + "]:" );
				 */
				int del = 31;
				output.println("sheetName" + String.valueOf((char) del) + orderedBSRs[sheetIndex].getSheetname());
			}
			break;

		case SSTRecord.sid:
			sstRecord = (SSTRecord) record;
			break;

		case BlankRecord.sid:
			BlankRecord brec = (BlankRecord) record;

			thisRow = brec.getRow();
			thisColumn = brec.getColumn();
			thisStr = " ";
			break;
		case BoolErrRecord.sid:
			BoolErrRecord berec = (BoolErrRecord) record;

			thisRow = berec.getRow();
			thisColumn = berec.getColumn();
			thisStr = " ";
			break;

		case FormulaRecord.sid:
			FormulaRecord frec = (FormulaRecord) record;

			thisRow = frec.getRow();
			thisColumn = frec.getColumn();

			if (outputFormulaValues) {
				if (Double.isNaN(frec.getValue())) {
					// Formula result is a string
					// This is stored in the next record
					outputNextStringRecord = true;
					nextRow = frec.getRow();
					nextColumn = frec.getColumn();
				} else {
					thisStr = formatListener.formatNumberDateCell(frec);
				}
			} else {
				thisStr = HSSFFormulaParser.toFormulaString(stubWorkbook, frec.getParsedExpression());
			}
			if (StringUtils.isEmpty(thisStr)) {
				thisStr = " ";
			}
			break;
		case StringRecord.sid:
			if (outputNextStringRecord) {
				// String for formula
				StringRecord srec = (StringRecord) record;
				thisStr = srec.getString();
				// thisStr =replaceExcelBlank(srec.getString()) ;
				thisRow = nextRow;
				thisColumn = nextColumn;
				outputNextStringRecord = false;
			}
			if (StringUtils.isEmpty(thisStr)) {
				thisStr = " ";
			}
			break;

		case LabelRecord.sid:
			LabelRecord lrec = (LabelRecord) record;

			thisRow = lrec.getRow();
			thisColumn = lrec.getColumn();
			thisStr = lrec.getValue();
			if (StringUtils.isEmpty(thisStr)) {
				thisStr = " ";
			}
			break;
		case LabelSSTRecord.sid:
			LabelSSTRecord lsrec = (LabelSSTRecord) record;

			thisRow = lsrec.getRow();
			thisColumn = lsrec.getColumn();
			if (sstRecord == null) {
				thisStr = '"' + "(No SST Record, can't identify string)" + '"';
			} else {
				thisStr = sstRecord.getString(lsrec.getSSTIndex()).toString();
			}
			if (StringUtils.isEmpty(thisStr)) {
				thisStr = " ";
			}
			break;
		case NoteRecord.sid:
			NoteRecord nrec = (NoteRecord) record;

			thisRow = nrec.getRow();
			thisColumn = nrec.getColumn();
			// TODO: Find object to match nrec.getShapeId()
			// thisStr = '"' + "(TODO)" + '"';
			if (StringUtils.isEmpty(thisStr)) {
				thisStr = " ";
			}
			break;
		case NumberRecord.sid:
			NumberRecord numrec = (NumberRecord) record;

			thisRow = numrec.getRow();
			thisColumn = numrec.getColumn();

			// Format
			thisStr = formatListener.formatNumberDateCell(numrec);
			if (StringUtils.isEmpty(thisStr)) {
				thisStr = " ";
			}
			break;
		case RKRecord.sid:
			RKRecord rkrec = (RKRecord) record;

			thisRow = rkrec.getRow();
			thisColumn = rkrec.getColumn();
			if (StringUtils.isEmpty(thisStr)) {
				thisStr = " ";
			}
			// thisStr = '"' + "(TODO)" + '"';
			break;
		default:
			break;
		}

		// Handle new row
		if (thisRow != -1 && thisRow != lastRowNumber) {
			lastColumnNumber = -1;
		}

		// Handle missing column
		if (record instanceof MissingCellDummyRecord) {
			MissingCellDummyRecord mc = (MissingCellDummyRecord) record;
			thisRow = mc.getRow();
			thisColumn = mc.getColumn();
			if (StringUtils.isEmpty(thisStr)) {
				thisStr = " ";
			}
		}

		// If we got something to print out, do so
		if (thisStr != null) {
			if (thisColumn > 0) {
				// output.print(',');
				int del = 31;
				output.print(String.valueOf((char) del));
			}
			thisStr = replaceExcelBlank(thisStr);
			output.print(thisStr);
		}

		// Update column and row count
		if (thisRow > -1)
			lastRowNumber = thisRow;
		if (thisColumn > -1)
			lastColumnNumber = thisColumn;

		// Handle end of row
		if (record instanceof LastCellOfRowDummyRecord) {
			// Print out any missing commas if needed
			if (minColumns > 0) {
				// Columns are 0 based
				if (lastColumnNumber == -1) {
					lastColumnNumber = 0;
				}
				for (int i = lastColumnNumber; i < (minColumns); i++) {
					// output.print(',');
					int del = 31;
					output.print(String.valueOf((char) del));
				}
			}

			// We're onto a new row
			lastColumnNumber = -1;

			// End the row
			output.println();
		}
	}

	private static void processExcels(String folder) throws Exception {
		File folderFile = new File(folder);
		if (folderFile.isDirectory()) {
			File[] files = folderFile.listFiles();
			for (File file : files) {
				System.out.println("handling " + file.getName());
				String fileName = file.getName();
				String[] strs = StringUtils.split(fileName, ".");
				XLS2CSVmra xls2csv = new XLS2CSVmra(new POIFSFileSystem(new FileInputStream(file.getAbsolutePath())),
						new PrintStream(new File("F:\\work\\开发文档\\bzhcsv\\" + strs[0] + ".txt")), 15);
				xls2csv.process();
			}
		}
	}
	
	private static void processTxt(String folder) throws Exception {
		// read txt file
		PrintStream ps = new PrintStream(new File("F://data.log"));
		File txtFolder = new File(folder);
		Map<String, String> tabMap = new HashMap<String, String>();
		Map<String, Map<String,String>> midMap = new HashMap<String, Map<String,String>>();
		Map<String, String> moduleMap = new HashMap<String, String>();
		List<SysModule> moduleList = new ArrayList<SysModule>();
		List<TableInfo> tableList = new ArrayList<TableInfo>();
		List<ColumnInfo> colList = new ArrayList<ColumnInfo>();
		long start = System.currentTimeMillis();
		try {

			if (txtFolder.isDirectory()) {
				File[] files = txtFolder.listFiles();
				SysModule module;
				int t = 31;
				String del = String.valueOf((char) t);
				for (File file : files) {
					InputStreamReader reader = new InputStreamReader(new FileInputStream(file)); // 建立一个输入流对象reader
					BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言

					String fileName = file.getName();
					if("全量数据过滤表清单.txt".equals(fileName) || "数据标准化－码表.txt".equals(fileName)) {
						continue;
					}
					String[] strs = StringUtils.split(fileName, ".")[0].split("-");
					System.out.println("处理文件：" + fileName);
					module = new SysModule();
					module.setPk(UuidUtils.getUUID());
					module.setModuleName(strs[1]);

					String line = br.readLine();
					int index = 1;
					String preTabName="";
					String currTabName="";
					String sheetName = "";
					int orderNo = 1;
					while (line != null) {
						index++;
						String[] strings = StringUtils.split(line, del);
						if (strings.length > 0) {
							if ("sheetName".equals(strings[0])) {
								sheetName = strings[1];
								line = br.readLine();
								continue;
							}
						} else {
							line = br.readLine();
							continue;
						}
						// System.out.println("line no:"+ index+ " line info："+line+" length :
						// "+strings.length);
						// remove title info ; remove the null info
						if (strings.length >= 2) {
							// 如果无系统名，则该字段无属性；如果字段代码为all，则该字段无需处理；
							if ("源系统ID".equals(strings[0]) || StringUtils.isEmpty(strings[1].trim())
									|| "ALL".equals(strings[3])) {
								line = br.readLine();
								continue;
							}
							// module pk
							// a file refers to a module
							if (!moduleMap.containsKey(strings[0].trim())) {
								String moduleId = UuidUtils.getUUID();
								moduleMap.put(strings[0].trim(), moduleId);
								module.setSysid(strings[0].trim());
								module.setPk(moduleId);
								moduleList.add(module);
							}
							// tab pk
							if("中间业务".equals(module.getModuleName())) {
								//如果有该sheet页
								String tabId = "";
								if(midMap.containsKey(sheetName)) {
									if(!midMap.get(sheetName).containsKey(strings[1].trim())){
										tabId = UuidUtils.getUUID();
										midMap.get(sheetName).put(strings[1].trim(),tabId );
										
										TableInfo tableInfo = new TableInfo();
										tableInfo.setPk(tabId);
										tableInfo.setSourceSyspk(moduleMap.get(strings[0].trim()));
										tableInfo.setTableCode(strings[1].trim());
										if (strings[2].length() > 32) {
											tableInfo.setTableName(strings[2].trim().substring(0, 32));
										} else {
											tableInfo.setTableName(strings[2].trim());
										}
		
										tableInfo.setDistrict(sheetName);
										tableList.add(tableInfo);
									}
								}else {
									Map<String, String> map = new HashMap<String, String>();
									tabId = UuidUtils.getUUID();
									map.put(strings[1].trim(),  tabId);
									midMap.put(sheetName, map);
									
									TableInfo tableInfo = new TableInfo();
									tableInfo.setPk(tabId);
									tableInfo.setSourceSyspk(moduleMap.get(strings[0].trim()));
									tableInfo.setTableCode(strings[1].trim());
									if (strings[2].length() > 32) {
										tableInfo.setTableName(strings[2].trim().substring(0, 32));
									} else {
										tableInfo.setTableName(strings[2].trim());
									}
	
									tableInfo.setDistrict(sheetName);
									tableList.add(tableInfo);
								}
							}else {
								if (!tabMap.containsKey(strings[1].trim())) {
									String tabId = UuidUtils.getUUID();
									tabMap.put(strings[1].trim(), tabId);
									TableInfo tableInfo = new TableInfo();
									tableInfo.setPk(tabId);
									tableInfo.setSourceSyspk(moduleMap.get(strings[0].trim()));
									tableInfo.setTableCode(strings[1].trim());
									if (strings[2].length() > 32) {
										tableInfo.setTableName(strings[2].trim().substring(0, 32));
									} else {
										tableInfo.setTableName(strings[2].trim());
									}
	
									tableInfo.setDistrict(sheetName);
									tableList.add(tableInfo);
								}
							}

							// column info
							if (StringUtils.isNotEmpty(strings[0].trim())) {
								ColumnInfo ci = new ColumnInfo();
								ci.setPk(UuidUtils.getUUID());
								if("中间业务".equals(module.getModuleName())) {
									ci.setTbpk(midMap.get(sheetName).get(strings[1].trim()));
								}else {
									ci.setTbpk(tabMap.get(strings[1].trim()));
								}
								currTabName=strings[1];
								//pre为空，代表该表字段第一次进入循环，index不需要处理
								if(StringUtils.isEmpty(preTabName)) {
									orderNo=1;
									preTabName = currTabName;
								}else {
									//如果不为空，则代表有赋值；
									//相等代表是同一张表
									if(preTabName.equals(currTabName)) {
										preTabName= currTabName;
										orderNo++;
									}else {
										//如果不相等，则代表进入了另外一张表
										orderNo=1;
										preTabName= currTabName;
									}
								}
								
								
								ci.setColCode(strings[3]);
								if (strings[4].length() > 32) {
									ci.setColName(strings[4].substring(0, 32));
								} else {
									ci.setColName(strings[4]);
								}
								ci.setOrderNo(orderNo);
								ci.setColDataType(strings[5]);
								ci.setColLen(parseIntSafe(strings[6]));
								ci.setColPrecision(parseIntSafe(strings[7]));
								ci.setPrimaryFlag(strings[10]);
								if (StringUtils.isEmpty(strings[11].trim())) {
									ci.setColDecl(strings[4]);
								} else
									ci.setColDecl(strings[11]);
								colList.add(ci);
								/*
								 * ci.setIsInUseNow(strings[9]); ci.setModiAdv(strings[13]);
								 * ci.setIsStandard(strings[8]); ci.setModiDes(modiDes);
								 * ci.setColDecl(strings[11]);
								 */
							}
						}
						line = br.readLine();
					}
					br.close();
					System.out.println(fileName + " Module amount:" + moduleList.size() + " table amount："
							+ tableList.size() + " column amount: " + colList.size());
					for (SysModule sModule : moduleList) {
						// System.out.println(sModule.getInsSql());
						DbConnection.insertSysModule(sModule, DbConnection.getConn());
						//ps.println(sModule.getInsSql());
					}
					for (TableInfo ti : tableList) {
						// System.out.println(ti.getInsSql());
						DbConnection.insertTableInfo(ti, DbConnection.getConn());
						//ps.println(ti.getInsSql());
					}
					/*for(ColumnInfo col: colList) {
						System.out.println(col.toString());
					}*/
					DbConnection.addColumnInfo(colList, DbConnection.getConn());
					moduleList.clear();
					tableList.clear();
					colList.clear();
				}
				/*
				 * for(ColumnInfo cInfo :colList) { ps.println(cInfo.getInsSql()); }
				 */
				long end = System.currentTimeMillis();
				System.out.println("time cost :" + (end - start) / 1000 + "秒");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ps.flush();
			ps.close();
		}

	}

	public static void main(String[] args) throws Exception {
		beforeProcess();
		// step 1： convert excel to txt files
		processExcels("F:\\work\\开发文档\\标准化文档\\");

		// step 2:
		processTxt("F:\\work\\开发文档\\bzhcsv\\");

		// step 3: 处理码表
		 processDict("F:\\work\\开发文档\\bzhcsv\\数据标准化－码表.txt");
		 // 手工创建index on tb_column_info 和tb_dict 
		 //update tb_column_info t1 set reserve = '1' where exists (select pk from tb_dict t2 where t1.pk = t2.col_pk);
		 //alter table tb_column_info add index ciindex(pk);
	}

	private static void beforeProcess() {
		String sql= "delete from tb_sysmodule";
		DbConnection.exeSql(sql, DbConnection.getConn());
		sql = "delete from tb_column_info";
		DbConnection.exeSql(sql, DbConnection.getConn());
		sql = "delete from tb_info";
		DbConnection.exeSql(sql, DbConnection.getConn());
		sql = "delete from tb_dict";
		DbConnection.exeSql(sql, DbConnection.getConn());
	}

	private static void processDict(String filepath) throws Exception{
		// read txt file
		File file = new File(filepath);
		List<DictInfo> colList = new ArrayList<DictInfo>();
		
		InputStreamReader reader;
		reader = new InputStreamReader(new FileInputStream(file));
		BufferedReader br = new BufferedReader(reader); 

		String fileName = file.getName();
		
		String line = br.readLine();
		String sheetName = "";
		int t = 31;
		String del = String.valueOf((char) t);
		DictInfo dictInfo = null;
		while (line != null) {
			System.out.println(line);
			String[] strings = StringUtils.split(line, del);
			if (strings.length > 0) {
				//line starts with sheetName ,it brings the info of the sheet name.
				if ("sheetName".equals(strings[0])) {
					sheetName = strings[1];
					line = br.readLine();
					continue;
				}else if("系统名".equals(strings[0])) {
					line = br.readLine();
					continue;
				}
			} else {
				line = br.readLine();
				continue;
			}
			
			//正常处理
			if(strings.length>=5) {
				dictInfo = new DictInfo();
				dictInfo.setDictKey(strings[3]);
				dictInfo.setDictValue(strings[4]);
				dictInfo.setPk(UuidUtils.getUUID());
				dictInfo.setColPk(DbConnection.getColpkByCodeAndTabPk(strings[2], DbConnection.getTabPk(strings[1])));
				dictInfo.setModule(DbConnection.getModulePk(strings[0]));
				dictInfo.setTabCode(strings[1]);
				dictInfo.setTabPk(DbConnection.getTabPk(strings[1]));
				dictInfo.setColCode(strings[2]);
				dictInfo.setModCode(strings[0]);
				System.out.println(dictInfo);
				colList.add(dictInfo);
			}
			line = br.readLine();
		}
		DbConnection.addDictInfo(colList,DbConnection.getConn());
		
	}

	public static int parseIntSafe(String str) {
		if (StringUtils.isEmpty(str.trim())) {
			return 0;
		}
		return Integer.parseInt(str);
	}

	public static String replaceBlank(String str) {
		String dest = "";
		if (str != null) {
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;
	}

	public static String replaceExcelBlank(String content) {
		for (int j = 10; j < 14; j++) {
			content = content.replaceAll(String.valueOf((char) j), "");
		}
		return content;
	}

}
