package com.hyls.model;

/**
 * @author Administrator
 *
 */
/**
 * @author Administrator
 *
 */
public class ColumnInfo {
	private String pk;
	private String colCode;
	private String tbpk;
	private String colName;
	private String colDataType;
	private int colLen;
	private int colPrecision;
	private String isStandard;
	private String isInUseNow;
	private String primaryFlag;
	private String colDecl;
	private String modiAdv;
	private String modiHis; 
	private String modiDes;
	private String isReleased;
	private int orderNo;
	public String getPk() {
		return pk;
	}
	public void setPk(String pk) {
		this.pk = pk;
	}
	public String getColCode() {
		return colCode;
	}
	public void setColCode(String colCode) {
		this.colCode = colCode;
	}
	public String getTbpk() {
		return tbpk;
	}
	public void setTbpk(String tbpk) {
		this.tbpk = tbpk;
	}
	public String getColName() {
		return colName;
	}
	public void setColName(String colName) {
		this.colName = colName;
	}
	public String getColDataType() {
		return colDataType;
	}
	public void setColDataType(String colDataType) {
		this.colDataType = colDataType;
	}
	public int getColLen() {
		return colLen;
	}
	public void setColLen(int colLen) {
		this.colLen = colLen;
	}
	public int getColPrecision() {
		return colPrecision;
	}
	public void setColPrecision(int colPrecision) {
		this.colPrecision = colPrecision;
	}
	public String getIsStandard() {
		return isStandard;
	}
	public void setIsStandard(String isStandard) {
		this.isStandard = isStandard;
	}
	public String getIsInUseNow() {
		return isInUseNow;
	}
	public void setIsInUseNow(String isInUseNow) {
		this.isInUseNow = isInUseNow;
	}
	public String getPrimaryFlag() {
		return primaryFlag;
	}
	public void setPrimaryFlag(String primaryFlag) {
		this.primaryFlag = primaryFlag;
	}
	public String getColDecl() {
		return colDecl;
	}
	public void setColDecl(String colDecl) {
		this.colDecl = colDecl;
	}
	public String getModiAdv() {
		return modiAdv;
	}
	public void setModiAdv(String modiAdv) {
		this.modiAdv = modiAdv;
	}
	public String getModiHis() {
		return modiHis;
	}
	public void setModiHis(String modiHis) {
		this.modiHis = modiHis;
	}
	public String getModiDes() {
		return modiDes;
	}
	public void setModiDes(String modiDes) {
		this.modiDes = modiDes;
	}
	public String getIsReleased() {
		return isReleased;
	}
	public void setIsReleased(String isReleased) {
		this.isReleased = isReleased;
	}
	
	public int getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(int orderNo) {
		this.orderNo = orderNo;
	}
	@Override
	public String toString() {
		return "顺序："+this.orderNo+" 列序号："+this.colCode+" 列名称"+this.colName+" 列对应表："+this.tbpk+" 字段数据类型："+this.colDataType+" 字段长度："+ this.colLen+" 字段精度:"+this.colPrecision;
	}
	
	public String getInsSql() {
		return "insert into tb_column_info (pk,tbpk,col_code,col_name,col_data_type,col_length,col_precision,primary_flag) values("
					+"'"+pk+"','"+tbpk+"','"+colCode+"','"+colName+"','"+colDataType+"',"+colLen+","+colPrecision+",'"+primaryFlag+"');";
		
	}
	
	
}
