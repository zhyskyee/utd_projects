package edu.utd.db.zhySql.Page;
/***********************************************
* @author hxz174130@utdallas.edu
* 
* @date Apr 8, 2018 1:26:54 AM
* 
***********************************************/
public class payLoad {

	long plOffset; //absolute offset
	byte columnsNumber; 
	byte[] dataType;
	String[] data;
	
	public byte getColumnsNumber() {
		return columnsNumber;
	}
	public void setColumnsNumber(byte columnsNumber) {
		this.columnsNumber = columnsNumber;
	}

	public byte[] getDataType() {
		return dataType;
	}
	public void setDataType(byte[] dataType) {
		this.dataType = dataType;
	}
	public String[] getData() {
		return data;
	}
	public void setData(String[] data) {
		this.data = data;
	}
	public long getPlOffset() {
		return plOffset;
	}
	public void setPlOffset(long plOffset) {
		this.plOffset = plOffset;
	}
}
