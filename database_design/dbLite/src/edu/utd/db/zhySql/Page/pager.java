package edu.utd.db.zhySql.Page;

import java.io.IOException;
import java.io.RandomAccessFile;

import com.sun.org.apache.regexp.internal.recompile;

import jdk.management.resource.internal.UnassignedContext;

/***********************************************
* @author hxz174130@utdallas.edu
* 
* @date Apr 7, 2018 6:59:22 PM
* 
***********************************************/
public class pager {
	RandomAccessFile file;
	public static int pageSize = 512; //Byte
	public static int pageHeaderStaticLength = 8; //8bytes
	public static byte[] pageTypeCode = {0x0D, 0x05}; //index 0 = leaf page, index 1 = interior page.
	int pageIndex = 0;  //Default 0
	//int cellNumber = 0;
	//int cellStartOffset = 0;

	long pageBaseAddr;
	//long cellNumberAddr;
	//long cellCntStartAddr;
	
	/* caller open it before  */
	public pager(RandomAccessFile f) {
		this.file = f;
		checkFile(file);
	}
	
	private void checkFile(RandomAccessFile f) {
		if ( null == f ) {
			throw new NullPointerException();
		}
	}
	
	public static boolean addPage(RandomAccessFile table, byte pageType) {
		try {
			long curTableLen = table.length();
			table.setLength(curTableLen + pager.pageSize);
			/* Set file pointer to the beginnning of the page */
			table.seek(curTableLen);
			table.write(pageType);
			table.write(0x00);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	//PageNumber = PageIndex + 1; 
	public static byte getBtreePageType(RandomAccessFile table, int pageNumber) {
		if(null == table) {
			return 0;
		}
		
		int pageOffset = (pageNumber - 1) * pageSize;
		
		try {
			table.seek(pageOffset);
			return table.readByte();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public static short getSerialCodeTypeByteLength(byte serialTypeCode) {
		switch (serialTypeCode) {
			case 0x00:
			case 0x04:
				return 1;
			case 0x01:
			case 0x05:
				return 2;
			case 0x02:
			case 0x06:
			case 0x08:
				return 4;
			case 0x03:
			case 0x07:
			case 0x09:
			case 0x0a:
			case 0x0b:
				return 8;
			default:
				return (short) (serialTypeCode - 0x0c);
		}
	} 
	
	/*
	 * columnValue should be filtered, attribute comes from table_column.
	 * */
	public static byte getSerialTypeCodeContent(String columnValue, String columnTypeAttr) {
		if (columnValue == null) {
			//It means user insert nothing specific
			switch (columnTypeAttr) {
			case "TINYINT":
				return 0x00;
			case "SMALLINT":
				return 0x01;
			case "INT":
				return 0x02;
			case "BIGINT":
				return 0x03;
			case "REAL":
				return 0x02;
			case "DOUBLE":
				return 0x03;
			case "DATETIME":
				return 0x03;
			case "DATE":
				return 0x03;
			case "TEXT":
			default:
				return 0x0c;
			}
		} else {
			switch (columnTypeAttr) {
			case "TINYINT":
				return 0x04;
			case "SMALLINT":
				return 0x05;
			case "INT":
				return 0x06;
			case "BIGINT":
				return 0x07;
			case "REAL":
				return 0x08;
			case "DOUBLE":
				return 0x09;
			case "DATETIME":
				return 0x0a;
			case "DATE":
				return 0x0b;
			case "TEXT":
			default:
				return (byte) (columnValue.length() + 0x0c);
			}
		}
	}

	public void updatePageBaseAddr() {
		this.pageBaseAddr = this.pageIndex * pager.pageSize;
		//this.cellNumberAddr = this.pageBaseAddr + 0x01;
		//this.cellCntStartAddr = this.pageBaseAddr + 0x02;
	}
	
	public byte getCellNumber() {
		byte cno = 0;
		try {
			file.seek(this.pageBaseAddr+0x01);
			cno = file.readByte();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cno;
	} 
	
	public short getCellsStartOfs() {
		short ofs = 0;
		try {
			file.seek(this.pageBaseAddr+0x02);
			ofs = file.readShort();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return ofs;
	}
	
	public int getRightMostPageNumber() {
		int pageNumber = 0;
		try {
			file.seek(this.pageBaseAddr+0x04);
			pageNumber = file.readInt();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return pageNumber;
	}
	
	public long getCellOffsetByIndex(byte idx, byte cellNumber) {
		
		if(idx < 0 || idx >= cellNumber) {
			System.out.println("Illegal cell index:" + idx +" cellTotalNumber:"+ cellNumber);
			return 0;
		}
		
		long cellOfs = 0; 
		try {
			file.seek(this.pageBaseAddr + pager.pageHeaderStaticLength + idx * 2);
			cellOfs = file.readShort() + this.pageBaseAddr;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return cellOfs;
	}
	
	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}
	
	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		pager.pageSize = pageSize;
	}
}
