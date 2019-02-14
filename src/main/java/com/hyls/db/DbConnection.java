package com.hyls.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import com.hyls.model.ColumnInfo;
import com.hyls.model.DictInfo;
import com.hyls.model.ImageInfo;
import com.hyls.model.SysModule;
import com.hyls.model.TableInfo;

public class DbConnection {
	private static boolean isInit = false;
	private static Connection conn = null;
	private static String driver = "com.mysql.jdbc.Driver";
	private static String url = "jdbc:mysql://localhost:3306/jfinal_demo?useSSL=true&useUnicode=true&characterEncoding=UTF-8&rewriteBatchedStatements=true";
	private static String username = "root";
	private static String password = "mystar?92";
	private static HashMap<String, String> moduleMap= new HashMap<String, String>(); 
	//private static HashMap<String, String> mnamePkMap = new HashMap<String, String>();
	
	private static HashMap<String, String> tabMap= new HashMap<String, String>(); 
	private static void initModuleCache() {
		 PreparedStatement pstmt = null;
		 ResultSet rs = null;
		try {
			pstmt=getConn().prepareStatement("select sysid,pk from tb_sysmodule");
			rs=pstmt.executeQuery();
			String pk= "";
			String sysid = "";
			while(rs.next()) {
				pk = rs.getString("pk");
				sysid = rs.getString("sysid");
				if(!moduleMap.containsKey(sysid)) {
					moduleMap.put(sysid, pk);
				}
				/*if(!mnamePkMap.containsKey(name)) {
					mnamePkMap.put(name, pk);
				}*/
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static void initTableCache() {
		 PreparedStatement pstmt = null;
		 ResultSet rs = null;
		try {
			pstmt=getConn().prepareStatement("select table_code,pk from tb_info");
			rs=pstmt.executeQuery();
			String pk= "";
			String tabCode = "";
			while(rs.next()) {
				pk = rs.getString("pk");
				tabCode = rs.getString("table_code");
				if(!tabMap.containsKey(tabCode)) {
					tabMap.put(tabCode, pk);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void exeSql(String sql,Connection conn) {
		 PreparedStatement pstmt = null;
		 try {
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*public static String getModulePkByName(String name) {
		if(!isInit) {
			initModuleCache();
			initTableCache();
			isInit = true;
		}
		return mnamePkMap.get(name);
	}*/
	
	public static String getModulePk(String sysid) {
		if(!isInit) {
			initModuleCache();
			initTableCache();
			isInit = true;
		}
		return moduleMap.get(sysid);
	}
	
	public static String getTabPk(String tabCode) {
		if(!isInit) {
			initModuleCache();
			initTableCache();
			isInit = true;
		}
		return tabMap.get(tabCode);
	}
	public static Connection getConn() {
	    try {
	      if(conn == null) {
	        Class.forName(driver); //classLoader,加载对应驱动
	        conn = (Connection) DriverManager.getConnection(url, username, password);
	      }
	    } catch (ClassNotFoundException e) {
	        e.printStackTrace();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return conn;
	}
	
	public static void main(String[] args) {
		 String name= "即远期标志(根据汇票天数判断  0即期 0天/1 短远期 <=90天 / 2 中远期 90天<X<=180天 / 3 长远期 180天<x<=360天 /4 超远期 >360天)";
		 System.out.println(name.substring(0,3));
	}
	
	public static String getColpkByCodeAndTabPk(String colCode, String tabPk) {
		String sql = "select pk from tb_column_info where col_code= ? and tbpk = ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String pk = "";
		try {
			pstmt= getConn().prepareStatement(sql);
			pstmt.setString(1, colCode);
			pstmt.setString(2, tabPk);
			rs= pstmt.executeQuery();
			if(rs.next())
			{
				pk = rs.getString("pk");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pk ;
	}
	
	
	public static int addImageInfo(List<ImageInfo> imageInfo, Connection conn) {
		 int j = 0;
		    PreparedStatement pstmt = null;
		    ImageInfo column = null;
		    String sql = "insert into imageinfo (pk,name,staff_no) values(?,?,?)";
		    try {
		    	pstmt = (PreparedStatement) conn.prepareStatement(sql);
		    	for(int i=0;i<imageInfo.size();i++) {
		    		column = imageInfo.get(i);
			        pstmt.setString(1, column.getPk());
			        pstmt.setString(2, column.getName());
			        pstmt.setString(3, column.getCode());
			        pstmt.addBatch();
			        //every thousand ;
			        if(i!=0&&i%1000==0) {
			        	long begin =System.currentTimeMillis();
			        	pstmt.executeBatch();
			        	long end = System.currentTimeMillis();
			        	System.out.println("1000 Batch cost "+(end-begin)/1000+" second");
			        	pstmt.clearParameters();
			        	pstmt.clearBatch();
			        }
		    	}
		    	long begin =System.currentTimeMillis();
		    	pstmt.executeBatch();
	        	long end = System.currentTimeMillis();
	        	System.out.println("Batch cost "+(end-begin)/1000+" second");
		        pstmt.close();
		    } catch (SQLException e) {
		        e.printStackTrace();
		    }finally {
		    	releasePrep(pstmt);
			}
		    return j;
		
	}
	
	public static int addColumnInfo(List<ColumnInfo> columnList,Connection conn) {
	    int j = 0;
	    PreparedStatement pstmt = null;
	    ColumnInfo column = null;
	    String sql = "insert into tb_column_info (pk,tbpk,col_code,col_name,col_data_type,col_length,col_precision,primary_flag,col_decl,order_no) values(?,?,?,?,?,?,?,?,?,?)";
	    try {
	    	pstmt = (PreparedStatement) conn.prepareStatement(sql);
	    	for(int i=0;i<columnList.size();i++) {
	    		column = columnList.get(i);
		        pstmt.setString(1, column.getPk());
		        pstmt.setString(2, column.getTbpk());
		        pstmt.setString(3, column.getColCode());
		        pstmt.setString(4, column.getColName());
		        pstmt.setString(5, column.getColDataType());
		        pstmt.setInt(6, column.getColLen());
		        pstmt.setInt(7, column.getColPrecision());
		        pstmt.setString(8, column.getPrimaryFlag());
		        pstmt.setString(9, column.getColDecl());
		        pstmt.setInt(10, column.getOrderNo());
		        pstmt.addBatch();
		        //every thousand ;
		        if(i!=0&&i%1000==0) {
		        	long begin =System.currentTimeMillis();
		        	pstmt.executeBatch();
		        	long end = System.currentTimeMillis();
		        	System.out.println("1000 Batch cost "+(end-begin)/1000+" second");
		        	pstmt.clearParameters();
		        	pstmt.clearBatch();
		        }
	    	}
	    	long begin =System.currentTimeMillis();
	    	pstmt.executeBatch();
        	long end = System.currentTimeMillis();
        	System.out.println("Batch cost "+(end-begin)/1000+" second");
	        pstmt.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }finally {
	    	releasePrep(pstmt);
		}
	    return j;
	}
	
	public static int addDictInfo(List<DictInfo> dictList, Connection conn) {
		int j = 0;
		PreparedStatement pstmt = null;
	    DictInfo dict = null;
	    String sql = "insert into tb_dict (pk,dict_key,dict_value,col_pk,module,col_code,mod_code,tab_code,tab_pk) values(?,?,?,?,?,?,?,?,?)";
	    try {
	    	pstmt = (PreparedStatement) conn.prepareStatement(sql);
	    	for(int i=0;i<dictList.size();i++) {
	    		dict = dictList.get(i);
		        pstmt.setString(1, dict.getPk());
		        pstmt.setString(2, dict.getDictKey());
		        pstmt.setString(3, dict.getDictValue());
		        pstmt.setString(4, dict.getColPk());
		        pstmt.setString(5, dict.getModule());
		        pstmt.setString(6, dict.getColCode());
		        pstmt.setString(7, dict.getModCode());
		        pstmt.setString(8, dict.getTabCode());
		        pstmt.setString(9, dict.getTabPk());
		        pstmt.addBatch();
		        //every one thousand ;
		        if(i!=0&&i%1000==0) {
		        	long begin =System.currentTimeMillis();
		        	pstmt.executeBatch();
		        	long end = System.currentTimeMillis();
		        	System.out.println("1000 Batch cost "+(end-begin)/1000+" second");
		        	pstmt.clearParameters();
		        	pstmt.clearBatch();
		        }
	    	}
	    	long begin =System.currentTimeMillis();
	    	pstmt.executeBatch();
        	long end = System.currentTimeMillis();
        	System.out.println("Batch cost "+(end-begin)/1000+" second");
	        pstmt.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }finally {
	    	releasePrep(pstmt);
		}
		return j;
	}
	
	public static int insertSysModule(SysModule module,Connection conn) {
	    int i = 0;
	    PreparedStatement pstmt = null;
	    String sql = "insert into tb_sysmodule (pk,sysid,module_name) values(?,?,?)";
	    try {
	        pstmt = (PreparedStatement) conn.prepareStatement(sql);
	        pstmt.setString(1, module.getPk());
	        pstmt.setString(2, module.getSysid());
	        pstmt.setString(3, module.getModuleName());
	        i = pstmt.executeUpdate();
	        pstmt.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }finally {
	    	releasePrep(pstmt);
		}
	    return i;
	}
	
	public static int insertTableInfo(TableInfo tableInfo,Connection conn) {
	    int i = 0;
	    PreparedStatement pstmt = null;
	    String sql = "insert into tb_info (pk,source_syspk,table_name,table_code,district) values(?,?,?,?,?)";
	    try {
	        pstmt = (PreparedStatement) conn.prepareStatement(sql);
	        pstmt.setString(1, tableInfo.getPk());
	        pstmt.setString(2, tableInfo.getSourceSyspk());
	        pstmt.setString(3, tableInfo.getTableName());
	        pstmt.setString(4, tableInfo.getTableCode());
	        pstmt.setString(5, tableInfo.getDistrict());
	        i = pstmt.executeUpdate();
	        pstmt.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }finally {
	    	releasePrep(pstmt);
		}
	    return i;
	}
	
	private static void releaseConn(Connection conn) {
		if(conn!=null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void releasePrep(PreparedStatement ps) {
		if(ps!=null) {
			try {
				ps.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
