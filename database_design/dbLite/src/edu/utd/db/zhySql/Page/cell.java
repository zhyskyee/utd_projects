package edu.utd.db.zhySql.Page;

/***********************************************
* @author hxz174130@utdallas.edu
* 
* @date Apr 8, 2018 12:27:31 AM
* 
***********************************************/
public class cell {
	//int pageNumber;
	int rowId;
	short location; //absolute address
	
	public short getLocation() {
		return location;
	}
	public void setLocation(short location) {
		this.location = location;
	}
	
	public int getRowId() {
		return rowId;
	}
	public void setRowId(int rowId) {
		this.rowId = rowId;
	}	
}
