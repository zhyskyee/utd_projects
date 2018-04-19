package edu.utd.db.zhySql.Page;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;

import javax.swing.plaf.InsetsUIResource;

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
	long pageBaseAddr;
	
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
	public boolean isPageEmpty(int pageNo) {
		byte cno = 0;
		long addr = (pageNo - 1) * pager.pageSize;
		try {
			file.seek(addr+0x01);
			cno = file.readByte();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return cno==0?true:false;
	}
	
	public static int locatePageNumberOfRowId(RandomAccessFile table, int rootpage, int rowId) { // List<Integer> parentPagePath
		int pageNo = rootpage;
		interiorPager ipReader = new interiorPager(table);
		//parentPagePath.add(Integer.valueOf(rootpage));
		boolean isPageFound = false;
		byte pageBtreeType;
		
		while (!isPageFound) {
			pageBtreeType = getBtreePageType(table, pageNo);
			
			switch (pageBtreeType) {
			case 0x05:
				//interior BTree
				ipReader.setPageIndex(pageNo-1);
				ipReader.updatePageBaseAddr();
				ipReader.retrieveInteriorCells();
				Map<Integer, interiorCell> tMap = ipReader.getCells();
				TreeSet<Integer> tSet = new TreeSet<Integer>(tMap.keySet());
				Integer[] keyArray = tSet.toArray(new Integer[tSet.size()]);
				int insertPos = ipReader.binarySearch(rowId, keyArray);
				if (insertPos < 0) {
					//it means the key is underlying at the last
					pageNo = ipReader.getRightMostPageNumber();
				} else {
					pageNo =tMap.get(keyArray[insertPos]).getLeftChildPageNumber(); 
				}
				break;
			case 0x0d:
				//Table Btree
				isPageFound = true;
				break;
			default:
				System.out.println("Failed to locate the page for unknown page.");
				return 0;
			}
		}
		return pageNo;
	}
	public static boolean lookUpNewLeafPagePath(RandomAccessFile table, int rootpage, List<Integer> parentPagePath) {
		int pageNo = rootpage;
		byte pageBtreeType;
		boolean isPageFound = false;
		interiorPager ipReader = new interiorPager(table);
		parentPagePath.add(Integer.valueOf(rootpage));
		while(!isPageFound) {
			pageBtreeType = getBtreePageType(table, pageNo);
			
			switch (pageBtreeType) {
			case 0x05:
				//interior BTree
				ipReader.setPageIndex(pageNo-1);
				ipReader.updatePageBaseAddr();
				ipReader.retrieveInteriorCells();
				int rightMostPageNumber = ipReader.getRightMostPageNumber();
				//boolean b = ipReader.isPageEmpty(rightMostPageNumber);
				if(rightMostPageNumber == 0) {
					//this node has no right branch which means this is the latest branch.
					//lookup the last leaf.
					Map<Integer, interiorCell> cells = ipReader.getCells();
					Integer[] keys = cells.keySet().toArray(new Integer[ipReader.getCellNumber()]);
					//get the last cell to locate the leaf page, since key is bigger than the existing keys.
					pageNo = cells.get(keys[keys.length - 1]).getLeftChildPageNumber();
					parentPagePath.add(Integer.valueOf(pageNo));
				} else {
					pageNo = rightMostPageNumber;
					parentPagePath.add(Integer.valueOf(pageNo));
				}
				break;
			case 0x0d:
				//Table Btree
				isPageFound = true;
				break;
			default:
				System.out.println("Failed to locate the page for unknown page.");
				return false;
			}
		}
		
		if (isPageFound) {
			return true;
		} else {
			return false;
		}
	}
	public static int requestNewPage(RandomAccessFile table, byte pageType) {
		try {
			int curPageNumber = (int) (table.length() / pager.pageSize);
			for (int i = 0; i < curPageNumber; i++) {
				long addr = i * pager.pageSize;
				table.seek(addr);
				byte pt = table.readByte();
				if(pt != pageTypeCode[0] && pt != pageTypeCode[1]) {
					//it means this page is discarded, so use this page.
					table.seek(addr);
					//clear the content
					table.writeByte(pageType);
					table.writeByte(0x00); //zero cell
					table.writeShort(0x0000);//represent 65536
					table.writeInt(0x00000000);
					//Now this page is blank, good to go.
					return i+1;
				}
			}
			//run to here that means no discarded page, so add new blank page.
			if(!addPage(table, pageType)) {
				System.out.println("Fatal: Failed to allocate a new page.");
				return -1;
			} else {
				return curPageNumber+1;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}
	public static boolean addPage(RandomAccessFile table, byte pageType) {
		try {
			long curTableLen = table.length();
			table.setLength(curTableLen + pager.pageSize);
			/* Set file pointer to the beginnning of the page */
			table.seek(curTableLen);
			table.writeByte(pageType);
			table.writeByte(0x00);;
			table.writeShort(0x0000);
			table.writeInt(0x00000000);
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
		int unsignedByteCode = Byte.toUnsignedInt(serialTypeCode);
		switch (unsignedByteCode) {
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
				return (short) (unsignedByteCode - 0x0c);
		}
	} 
	public static Byte getSerialTypeCode(String dataTypeStr) {
		switch (dataTypeStr) {
		case "TINYINT":
			return Byte.valueOf((byte) 0x04);
		case "SMALLINT":
			return Byte.valueOf((byte) 0x05);
		case "INT":
			return Byte.valueOf((byte) 0x06);
		case "BIGINT":
			return Byte.valueOf((byte) 0x07);
		case "REAL":
			return Byte.valueOf((byte) 0x08);
		case "DOUBLE":
			return Byte.valueOf((byte) 0x09);
		case "DATETIME":
			return Byte.valueOf((byte) 0x0a);
		case "DATE":
			return Byte.valueOf((byte) 0x0b);
		case "TEXT":
		default:
			return Byte.valueOf((byte) 0xff);
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
	
	public boolean writeBytes(short ofs, byte[] bytes) {
		try {
			
			this.file.seek(this.pageBaseAddr+Short.toUnsignedLong(ofs));
			this.file.write(bytes);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
	}
	/** 
     * write 2 byte from the bytes start
     */ 
    public static void shortToByte(short number, byte[] bytes, int begin) { 
        int temp = number; 
        for (int i = begin+1; i >= begin; --i) { 
        		bytes[i] = new Integer(temp & 0xff).byteValue();
        		temp = temp >> 8; 
        } 
    } 
    public static void intToByte(int number, byte[] bytes, int begin) { 
        int temp = number;  
        for (int i = begin+3; i >= begin; --i) { 
        		bytes[i] = new Integer(temp & 0xff).byteValue();
        		temp = temp >> 8;
        }
    } 
    public static void longToByte(long number, byte[] bytes, int begin) { 
        long temp = number;  
        for (int i = begin+7; i >= begin; --i) { 
        		bytes[i] = new Long(temp & 0xff).byteValue();
        		temp = temp >> 8;
        }
    } 
    public static void floatToByte(float number, byte[] bytes, int begin) { 
        int temp = Float.floatToIntBits(number);
        for (int i = begin+3; i >= begin; --i) { 
        		bytes[i] = new Integer(temp & 0xff).byteValue();
        		temp = temp >> 8;
        }
    } 
    public static void doubleToByte(double number, byte[] bytes, int begin) { 
        long temp = Double.doubleToLongBits(number);  
        for (int i = begin+7; i >= begin; --i) { 
        		bytes[i] = new Long(temp & 0xff).byteValue();
        		temp = temp >> 8;
        }
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
	
	public int getCellsStartOfs() {
		short ofs = 0;
		try {
			file.seek(this.pageBaseAddr+0x02);
			ofs = file.readShort();
			if (ofs == 0) {
				return pager.pageSize;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return Short.toUnsignedInt(ofs);
	}
	
	public int getRightMostPageNumber() {
		int pageNumber = 0;
		try {
			file.seek(this.pageBaseAddr+0x04);
			pageNumber = file.readInt();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return 0;
		}
		return pageNumber;
	}
	public boolean writeRightMostPageNumber(int no) {
		try {
			file.seek(this.pageBaseAddr+0x04);
			file.writeInt(no);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public short getCellOffsetByIndex(int idx, int cellNumber) {
		
		if(idx < 0 || idx >= cellNumber) {
			System.out.println("Illegal cell index:" + idx +" cellTotalNumber:"+ cellNumber);
			return 0;
		}
		
		short cellOfs = 0; 
		try {
			file.seek(this.pageBaseAddr + pager.pageHeaderStaticLength + idx * 2);
			cellOfs = file.readShort();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return cellOfs;
	}
	
	public short[] getRelativeCellOffsetSeq(short[] cellOfs, byte cellNumber) {
		try {
			file.seek(this.pageBaseAddr + pager.pageHeaderStaticLength);
			for (int i = 0; i < cellNumber; i++) {
				cellOfs[i] = file.readShort();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
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
