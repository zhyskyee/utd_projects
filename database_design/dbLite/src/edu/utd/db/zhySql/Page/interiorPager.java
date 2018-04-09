package edu.utd.db.zhySql.Page;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Map;

/***********************************************
* @author hxz174130@utdallas.edu
* 
* @date Apr 8, 2018 11:54:07 AM
* 
***********************************************/
public class interiorPager extends pager {
	//<Integer,interiorCell> = <rowId, interiorCell>  
	Map<Integer, interiorCell> cells;
	
	public interiorPager(RandomAccessFile f) {
		super(f);
		// TODO Auto-generated constructor stub
	}

	public void retrieveInteriorCells() {
		Map<Integer, interiorCell> cells = new LinkedHashMap<Integer, interiorCell>();
		interiorCell lc;
		int rowId;
		for (byte i = 0; i < getCellNumber(); i++) {
			lc = getInteriorCellByIndex(i);
			rowId = lc.getRowId();
			cells.put(rowId, lc);
		}
		
		if(cells.isEmpty()) {this.cells = null;}
		else {this.cells = cells;}
	} 
	
	public interiorCell getInteriorCellByIndex(byte idx) {
		interiorCell ic = new interiorCell();
		ic.setLocation(getCellOffsetByIndex(idx, getCellNumber()));
		
		if(ic.getLocation() == 0) {
			return null;
		} else {
			try {
				file.seek(ic.getLocation());
				ic.setLeftChildPageNumber(file.readInt());
				ic.setRowId(file.readInt());
				return ic;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} 
		}
		
	}

}
