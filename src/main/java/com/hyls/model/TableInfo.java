package com.hyls.model;

public class TableInfo {
	private String pk;
	private String sourceSyspk;
	private String tableName;
	private String tableCode;
	private String district;
	public String getPk() {
		return pk;
	}
	public void setPk(String pk) {
		this.pk = pk;
	}
	public String getSourceSyspk() {
		return sourceSyspk;
	}
	public void setSourceSyspk(String sourceSyspk) {
		this.sourceSyspk = sourceSyspk;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getTableCode() {
		return tableCode;
	}
	public void setTableCode(String tableCode) {
		this.tableCode = tableCode;
	}
	
	public String getDistrict() {
		return district;
	}
	public void setDistrict(String district) {
		this.district = district;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "pk:" + pk + " soucesyspk:"+sourceSyspk+" tableName:"+tableName +" tableCode:"+tableCode;
	}
	
	public String getInsSql() {
		return "insert into tb_info (pk,source_syspk,table_name,table_code,district) values('"+pk+
	"','"+sourceSyspk+"','"+tableName+"','"+tableCode+"','"+district+"')";
	}
	
}
