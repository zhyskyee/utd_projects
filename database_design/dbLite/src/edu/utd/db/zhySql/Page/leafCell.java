package edu.utd.db.zhySql.Page;
/***********************************************
* @author hxz174130@utdallas.edu
* 
* @date Apr 8, 2018 2:37:22 PM
* 
***********************************************/
public class leafCell extends cell {
	short payLoadSize;
	payLoad payload;//table leaf cell
	
	public payLoad getPayload() {
		return payload;
	}
	public void setPayload(payLoad payload) {
		this.payload = payload;
	}
	public short getPayLoadSize() {
		return payLoadSize;
	}
	public void setPayLoadSize(short payLoadSize) {
		this.payLoadSize = payLoadSize;
	}
}
