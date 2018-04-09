package edu.utd.db.zhySql.Page;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/***********************************************
* @author hxz174130@utdallas.edu
* 
* @date Apr 7, 2018 11:52:40 PM
* 
***********************************************/
public class leafPager extends pager {

	Map<Integer, leafCell> cells;
	Map<Integer, payLoad> tuples;
	
	public leafPager(RandomAccessFile f) {
		super(f);
		// TODO Auto-generated constructor stub
	}
	
	public void retrieveLeafCells() {
		Map<Integer, leafCell> cells = new LinkedHashMap<Integer, leafCell>();
		leafCell lc;
		int rowId;
		for (byte i = 0; i < getCellNumber(); i++) {
			lc = getLeafCellByIndex(i);
			rowId = lc.getRowId();
			cells.put(rowId, lc);
		}
		
		if(cells.isEmpty()) {this.cells = null;}
		else {this.cells = cells;}
	} 
	
	public void retrieveTuples() {
		Map<Integer, payLoad> tuples = new LinkedHashMap<Integer, payLoad>();
		int rowId;
		payLoad pLoad;
		leafCell lc;
		if (this.cells != null) {
			//HINTS: cells has been retrieve
			for (Map.Entry<Integer, leafCell> entry : this.cells.entrySet()) {
				rowId = entry.getKey();
				lc = entry.getValue();
				pLoad = getPayLoadByLeafCell(lc);
				tuples.put(rowId, pLoad);
			}
			
			if (tuples.isEmpty()) {
				this.tuples = null;
			} else {
				this.tuples = tuples;
			}
		} else {
			this.tuples = null;
		}
	}
	
	public leafCell getLeafCellByIndex(byte idx) {
		leafCell lc = new leafCell();
		lc.setLocation(getCellOffsetByIndex(idx, getCellNumber()));
		
		if(lc.getLocation() == 0) {
			return null;
		} else {
			try {
				file.seek(lc.getLocation());
				lc.setPayLoadSize(file.readShort());
				//move to read the key aka row_id
			
				lc.setRowId(file.readInt());
				//payload is read by another funtion
				return lc;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} 
		}
		
	}

	public payLoad getPayLoadByLeafCell(leafCell lc) {
		if (lc == null) {
			return null;
		}
		
		payLoad pl = new payLoad();
		
		pl.setPlOffset(lc.getLocation()+0x01); //cell location + 1byte
		
		//long anchor = pl.getPlOffset();
		
		try {
			file.seek(pl.getPlOffset());// read one byte for columns length
			pl.setColumnsNumber(file.readByte());
			
			//anchor += 1;
			//file.seek(anchor); //skip 1-byte
			byte[] dataTypeCode = new byte[pl.getColumnsNumber()];
			file.read(dataTypeCode, 0, pl.getColumnsNumber());
			pl.setDataType(dataTypeCode);
			
			//anchor += pl.getColumnsNumber();
			//file.seek(anchor); //skip n bytes 
			String[] dataStr = new String[pl.getColumnsNumber()];
			int contentLen = 0;
			for (int i = 0; i < pl.getColumnsNumber(); i++) {
				int byteLen = getSerialCodeTypeByteLength(dataTypeCode[i]);
				contentLen += byteLen;
				
				switch (dataTypeCode[i]) {
				case 0x00:
					dataStr[i] = Integer.toString(file.readByte());
					dataStr[i] = "null";
					break;

				case 0x01:
					dataStr[i] = Integer.toString(file.readShort());
					dataStr[i] = "null";
					break;

				case 0x02:
					dataStr[i] = Integer.toString(file.readInt());
					dataStr[i] = "null";
					break;

				case 0x03:
					dataStr[i] = Long.toString(file.readLong());
					dataStr[i] = "null";
					break;

				case 0x04:
					dataStr[i] = Integer.toString(file.readByte());
					break;

				case 0x05:
					dataStr[i] = Integer.toString(file.readShort());
					break;

				case 0x06:
					dataStr[i] = Integer.toString(file.readInt());
					break;

				case 0x07:
					dataStr[i] = Long.toString(file.readLong());
					break;

				case 0x08:
					dataStr[i] = String.valueOf(file.readFloat());
					break;

				case 0x09:
					dataStr[i] = String.valueOf(file.readDouble());
					break;

				case 0x0a:
					long tmp = file.readLong();
					Date dateTime = new Date(tmp);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
					dataStr[i] = sdf.format(dateTime);
					break;

				case 0x0b:
					long tmp1 = file.readLong();
					Date date = new Date(tmp1);
					SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
					dataStr[i] = sdf1.format(date);
					break;

				default:
					int len = new Integer(dataTypeCode[i] - 0x0c);
					byte[] bytes = new byte[len];
					
					for (int j = 0; j < len; j++) {
						bytes[j] = file.readByte();
					}
					
					dataStr[i] = new String(bytes);
					break;
				}
			}
			
			//double check the content length
			if(contentLen + pl.getColumnsNumber() + 1 != lc.getPayLoadSize()) {
				System.out.println("Error! payloadSize is not equals to the actual length!!!");
				return null;
			}
			
			pl.setData(dataStr);
			lc.setPayload(pl);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}		
		return pl;
	}
	
}
