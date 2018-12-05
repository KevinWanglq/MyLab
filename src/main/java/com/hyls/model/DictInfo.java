package com.hyls.model;

public class DictInfo {
	private String pk;
	private String dictKey;
	private String dictValue;
	private String tabCode;
	private String tabPk;
	private String colPk;
	private String colCode;
	private String module;
	private String modCode;
	private String reserve;
	public String getPk() {
		return pk;
	}
	public void setPk(String pk) {
		this.pk = pk;
	}
	public String getDictKey() {
		return dictKey;
	}
	public void setDictKey(String dictKey) {
		this.dictKey = dictKey;
	}
	public String getDictValue() {
		return dictValue;
	}
	public void setDictValue(String dictValue) {
		this.dictValue = dictValue;
	}
	public String getTabCode() {
		return tabCode;
	}
	public void setTabCode(String tabCode) {
		this.tabCode = tabCode;
	}
	public String getTabPk() {
		return tabPk;
	}
	public void setTabPk(String tabPk) {
		this.tabPk = tabPk;
	}
	public String getColPk() {
		return colPk;
	}
	public void setColPk(String colPk) {
		this.colPk = colPk;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getReserve() {
		return reserve;
	}
	public void setReserve(String reserve) {
		this.reserve = reserve;
	}
	
	public String getColCode() {
		return colCode;
	}
	public void setColCode(String colCode) {
		this.colCode = colCode;
	}
	public String getModCode() {
		return modCode;
	}
	public void setModCode(String modCode) {
		this.modCode = modCode;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "pk; "+ this.pk+" dict_key: "+dictKey+" dict_value: "+dictValue+" col code"+colCode+" col_pk: "+colPk+" module:"+module+" module code:"+ modCode;
	}
	
}
