package edu.utd.db.zhySql.Page;
/***********************************************
* @author hxz174130@utdallas.edu
* 
* @date Apr 8, 2018 2:40:13 PM
* 
***********************************************/
public class interiorCell extends cell {
	int leftChildPageNumber; //table interior cell
	
	public int getLeftChildPageNumber() {
		return leftChildPageNumber;
	}
	public void setLeftChildPageNumber(int leftChildPageNumber) {
		this.leftChildPageNumber = leftChildPageNumber;
	}
}
