package edu.utd.db.zhySql.Page;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.List;
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
	
	public Map<Integer, interiorCell> getCells() {
		return cells;
	}

	public interiorPager(RandomAccessFile f) {
		super(f);
		// TODO Auto-generated constructor stub
	}

	public void retrieveInteriorCells() {
		Map<Integer, interiorCell> cells = new LinkedHashMap<Integer, interiorCell>();
		interiorCell lc;
		int rowId;
		for (int i = 0; i < Byte.toUnsignedInt(getCellNumber()); i++) {
			lc = getInteriorCellByIndex(i);
			rowId = lc.getRowId();
			cells.put(rowId, lc);
		}
		
		if(cells.isEmpty()) {this.cells = null;}
		else {this.cells = cells;}
	} 
	
	public interiorCell getInteriorCellByIndex(int idx) {
		interiorCell ic = new interiorCell();
		ic.setLocation(getCellOffsetByIndex(idx, Byte.toUnsignedInt(getCellNumber())));
		
		if(ic.getLocation() == 0) {
			return null;
		} else {
			try {
				file.seek(Short.toUnsignedLong(ic.getLocation())+this.pageBaseAddr);
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
	/*make sure the space is enough*/
	public boolean writeInteriorCell(interiorCell ic, short OfsStartAddr, short cntStartAddr) {
		try {
			this.file.seek(this.pageBaseAddr+OfsStartAddr);
			file.writeShort(cntStartAddr);
			
			this.file.seek(this.pageBaseAddr+cntStartAddr);
			file.writeInt(ic.getLeftChildPageNumber());
			file.writeInt(ic.getRowId());
			byte cno = getCellNumber();
			cno++;
			this.file.seek(this.pageBaseAddr+0x01);
			file.writeByte(cno);
			file.writeShort(cntStartAddr);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
	}
	
	/* Return the point the record insert, the value of the found index is bigger than the target.
	 * return -1 means it need to insert to the tail
	 * */
	public int binarySearch(int keyTarget, Integer[] keyArray) {
		int n = keyArray.length;
		int low = 0;
		int hi = n - 1;
		int mid = 0;
		
	    while (low < hi) {
		    mid = (low + hi) / 2;
		    if (keyArray[mid].intValue() < keyTarget) {
		      low = mid + 1;
		    } else {
		    	  hi = mid;
		    }
	    }
	    
	    if (keyArray[low].intValue() < keyTarget) {
	    		return -1;  
	    } else {
	    		return low;
	    }
	}
}
