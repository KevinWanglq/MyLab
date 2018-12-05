package com.hyls.model;

import org.apache.xmlbeans.impl.xb.xsdschema.impl.PublicImpl;

public class SysModule {
	private String pk ;
	private String sysid;
	private String moduleName;
	public String getPk() {
		return pk;
	}
	public void setPk(String pk) {
		this.pk = pk;
	}
	public String getSysid() {
		return sysid;
	}
	public void setSysid(String sysid) {
		this.sysid = sysid;
	}
	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "table pk"+ pk+" sysid:"+ sysid+" moduleName:"+ moduleName;
	}
	
	public String getInsSql() {
		return "insert into tb_sysmodule (pk,sysid,module_name) values('"+pk+"','"+sysid+"','"+moduleName+"')";
	}
	
}
