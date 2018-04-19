package edu.utd.db.zhySql.Page;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import edu.utd.db.zhySql.Table.table;
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
		cells = new LinkedHashMap<Integer, leafCell>();
		tuples = new LinkedHashMap<Integer, payLoad>();
		// TODO Auto-generated constructor stub
	}
	public Map<Integer, leafCell> getLeafCells(){
		return cells;
	} 
	public void retrieveLeafCells() {
		Map<Integer, leafCell> cells = new LinkedHashMap<Integer, leafCell>();
		leafCell lc;
		int rowId;
		for (byte i = 0; i < Byte.toUnsignedInt(getCellNumber()); i++) {
			lc = getLeafCellByIndex(i); 
			rowId = lc.getRowId();
			cells.put(rowId, lc);
		}
		
		if(!cells.isEmpty()) {this.cells = cells;}
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
		lc.setLocation(getCellOffsetByIndex(Byte.toUnsignedInt(idx), Byte.toUnsignedInt(getCellNumber())));
		long addrLong = Short.toUnsignedLong(lc.getLocation());
		if(addrLong == 0) {
			return null;
		} else {
			try {
				file.seek(addrLong+this.pageBaseAddr);
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
		
		pl.setPlOffset((short) (Short.toUnsignedLong(lc.getLocation())+0x06)); //cell location + 6byte
		
		try {
			file.seek(Short.toUnsignedLong(pl.getPlOffset())+this.pageBaseAddr);// read one byte for columns length
			pl.setColumnsNumber(file.readByte());
			byte[] dataTypeCode = new byte[Byte.toUnsignedInt(pl.getColumnsNumber())];
			file.read(dataTypeCode, 0, Byte.toUnsignedInt(pl.getColumnsNumber()));
			pl.setDataType(dataTypeCode);
			
			//file.seek(anchor); //skip n bytes 
			String[] dataStr = new String[Byte.toUnsignedInt(pl.getColumnsNumber())];
			int contentLen = 0;
			for (int i = 0; i < Byte.toUnsignedInt(pl.getColumnsNumber()); i++) {
				int byteLen = getSerialCodeTypeByteLength(dataTypeCode[i]);
				contentLen += byteLen;
				int dataTypeCodeTmp = Byte.toUnsignedInt(dataTypeCode[i]);
				switch (dataTypeCodeTmp) {
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
					dataStr[i] = Float.toString(file.readFloat()); 
					break;

				case 0x09:
					dataStr[i] = Double.toString(file.readDouble());
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
					SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
					dataStr[i] = sdf1.format(date);
					break;

				default:
					int len = new Integer(dataTypeCodeTmp - 0x0c);
					byte[] bytes = new byte[len];
					
					for (int j = 0; j < len; j++) {
						bytes[j] = file.readByte();
					}
					
					dataStr[i] = new String(bytes);
					break;
				}
			}
			
			//double check the content length
			if(contentLen + Byte.toUnsignedInt(pl.getColumnsNumber()) + 1 != Short.toUnsignedInt(lc.getPayLoadSize())) {
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
	
	public static short calcPayLoadSize(String[] values, byte[] defineDataType) {
		short size = 1; //1 byte for column number
		short n = (short) defineDataType.length; //n byte
		size += n;
		short maxByteNumber;
		for (int i = 0; i < defineDataType.length; i++) {
			maxByteNumber = pager.getSerialCodeTypeByteLength(defineDataType[i]);
			int dataType = Byte.toUnsignedInt(defineDataType[i]);
			if (dataType < 0x0c) {
				size += maxByteNumber;
			} else {
				//not text, not variable length
				if (values[i] == null || values[i].isEmpty()) {
					size+=0;
				} else {
					//if it is a text, it should be no more than the limit
					maxByteNumber = (short) (maxByteNumber < values[i].length() ? maxByteNumber : values[i].length());
					size += maxByteNumber;
				}
			}
		}

		return size;
	}
	
	private byte[] leafCell2Bytes(leafCell lc) {
		byte[] b = new byte[lc.getPayLoadSize()+6];
		int ofs = 0;
		try {
			pager.shortToByte(lc.getPayLoadSize(), b, ofs);
			ofs += 2;
			pager.intToByte(lc.getRowId(), b, ofs);
			ofs += 4;
			b[ofs] = lc.getPayload().getColumnsNumber();
			ofs++;
			for (int i = 0, cntOfs = Byte.toUnsignedInt(lc.getPayload().getColumnsNumber()) + ofs;
					i < Byte.toUnsignedInt(lc.getPayload().getColumnsNumber()); i++) {
				boolean isContentNull = false;
				byte dataType = lc.getPayload().getDataType()[i];
				int dataTypeUnsigned = Byte.toUnsignedInt(dataType);
				
				if (lc.getPayload().getData()[i] == null || lc.getPayload().getData()[i].isEmpty() ||
						(dataTypeUnsigned < 0x0c && lc.getPayload().getData()[i].equalsIgnoreCase("null"))) {
					isContentNull = true;
				}
				
				switch (dataTypeUnsigned) {
				case 0x04:
					lc.getPayload().getDataType()[i] = isContentNull ? 0x00 : dataType;
					b[cntOfs] = isContentNull ? 0 : Byte.parseByte(lc.getPayload().getData()[i]);
					cntOfs++;
					break;
				case 0x05:
					lc.getPayload().getDataType()[i] = isContentNull ? 0x01 : dataType;
					short c = isContentNull ? 0 : Short.parseShort(lc.getPayload().getData()[i]);
					pager.shortToByte(c, b, cntOfs);
					cntOfs += 2; //2 bytes
					break;
				case 0x06:
					lc.getPayload().getDataType()[i] = isContentNull ? 0x02 : dataType;
					int c1 = isContentNull ? 0 : Integer.parseInt(lc.getPayload().getData()[i]);
					pager.intToByte(c1, b, cntOfs);
					cntOfs += 4;
					break;
				case 0x07:
					lc.getPayload().getDataType()[i] = isContentNull ? 0x03 : dataType;
					long c2 = isContentNull ? 0 : Long.parseLong(lc.getPayload().getData()[i]);
					pager.longToByte(c2, b, cntOfs);
					cntOfs += 8;
					break;
				case 0x08:
					lc.getPayload().getDataType()[i] = isContentNull ? 0x02 : dataType;
					float c3 = isContentNull ? 0 : Float.parseFloat(lc.getPayload().getData()[i]);
					pager.floatToByte(c3, b, cntOfs);
					cntOfs += 4;
					break;
				case 0x09:
					lc.getPayload().getDataType()[i] = isContentNull ? 0x03 : dataType;
					double c4 = isContentNull ? 0 : Double.parseDouble(lc.getPayload().getData()[i]);
					pager.doubleToByte(c4, b, cntOfs);
					cntOfs += 8;
					break;
				case 0x0a:
					lc.getPayload().getDataType()[i] = isContentNull ? 0x03 : dataType;
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
					if (isContentNull) {
						pager.longToByte(0, b, cntOfs);
					} else {
						Date c5 = sdf.parse(lc.getPayload().getData()[i]);
						pager.longToByte(c5.getTime(), b, cntOfs);
					}
					cntOfs += 8;
					break;
				case 0x0b:
					lc.getPayload().getDataType()[i] = isContentNull ? 0x03 : dataType;
					SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
					if (isContentNull) {
						pager.longToByte(0, b, cntOfs);
					} else {
						Date c6 = sdf1.parse(lc.getPayload().getData()[i]);
						pager.longToByte(c6.getTime(), b, cntOfs);
					}
					cntOfs += 8;
					break;
				default:
					int realCharNumber = lc.getPayload().getData()[i].length();
					int charCounter;
					for (charCounter = 0; charCounter < realCharNumber && charCounter < (dataTypeUnsigned-0x0c); charCounter++) {
						b[cntOfs++] = (byte) lc.getPayload().getData()[i].charAt(charCounter);
					}
					lc.getPayload().getDataType()[i] = (byte) (charCounter + 0x0c); //null string
					if(charCounter < realCharNumber) {
						System.out.println("ATTENTION: The text is too long, only the front part is stored!");
					}
					break;
				} //endOfSwitch
				b[ofs++] = lc.getPayload().getDataType()[i];//write down the actual dataType after the convertion.
			}//endOfFor
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
		return b;
	}
	
	public byte[] modifyLeafCellContent2Bytes(leafCell lc, int colIdx, String value, Byte[] defineDataType) {
		byte targetColDataType = lc.getPayload().getDataType()[colIdx-1];
		int targetColDataTypeUn = Byte.toUnsignedInt(targetColDataType);
		if (targetColDataTypeUn >= 0x0c) { //this column is a text, variable.
			if (value.length() > targetColDataTypeUn - 0x0c) {
				System.out.println("ERROR: PROHIBIT MODIFYING VALUE TO LONGER. ONLY "+(targetColDataTypeUn - 0x0c)+" CHARS USABLE.");
				return null;
			}
		}
		
		byte[] b = new byte[lc.getPayLoadSize()+6];
		int ofs = 0;
		try {
			pager.shortToByte(lc.getPayLoadSize(), b, ofs);
			ofs += 2;
			pager.intToByte(lc.getRowId(), b, ofs);
			ofs += 4;
			int columnNumber = Byte.toUnsignedInt(lc.getPayload().getColumnsNumber());
			b[ofs] = lc.getPayload().getColumnsNumber();
			ofs++;
			
			for (int i = 0, cntOfs = columnNumber + ofs; i < columnNumber; i++) {
				boolean isContentNull = false;
				lc.getPayload().getData()[i] = (i == colIdx-1 ? value : lc.getPayload().getData()[i]);
				
				if (lc.getPayload().getData()[i] == null 
					|| lc.getPayload().getData()[i].isEmpty() 
					|| (lc.getPayload().getData()[i].contains("null") && Byte.toUnsignedInt(lc.getPayload().getDataType()[i]) < 0x0c)) {
					isContentNull = true;
				}
				
				byte dataType = defineDataType[i+1].byteValue(); //defineDataType including the RowID at the first place.
				int dataTypeUn = Byte.toUnsignedInt(dataType);
				//String data =lc.getPayload().getData()[i];
				switch (dataTypeUn) {
				case 0x04:
					b[cntOfs] = isContentNull ? 0 : Byte.parseByte(lc.getPayload().getData()[i]);
					lc.getPayload().getDataType()[i] = (byte) (isContentNull ? 0x00 : dataType);
					cntOfs++;
					break;
				case 0x05:
					short c = isContentNull ? 0 : Short.parseShort(lc.getPayload().getData()[i]);
					pager.shortToByte(c, b, cntOfs);
					lc.getPayload().getDataType()[i] = (byte) (isContentNull ? 0x01 : dataType);
					cntOfs += 2; //2 bytes
					break;
				case 0x06:
					int c1 = isContentNull ? 0 : Integer.parseInt(lc.getPayload().getData()[i]);
					pager.intToByte(c1, b, cntOfs);
					lc.getPayload().getDataType()[i] = (byte) (isContentNull ? 0x02 : dataType);
					cntOfs += 4;
					break;
				case 0x07:
					long c2 = isContentNull ? 0 : Long.parseLong(lc.getPayload().getData()[i]);
					pager.longToByte(c2, b, cntOfs);
					lc.getPayload().getDataType()[i] = (byte) (isContentNull ? 0x03 : dataType);
					cntOfs += 8;
					break;
				case 0x08:
					float c3 = isContentNull ? 0 : Float.parseFloat(lc.getPayload().getData()[i]);
					pager.floatToByte(c3, b, cntOfs);
					lc.getPayload().getDataType()[i] = (byte) (isContentNull ? 0x02 : dataType);
					cntOfs += 4;
					break;
				case 0x09:
					double c4 = isContentNull ? 0 : Double.parseDouble(lc.getPayload().getData()[i]);
					pager.doubleToByte(c4, b, cntOfs);
					lc.getPayload().getDataType()[i] = (byte) (isContentNull ? 0x03 : dataType);
					cntOfs += 8;
					break;
				case 0x0a:
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
					Date c5 = isContentNull ? sdf.parse("0") : sdf.parse(lc.getPayload().getData()[i]);
					pager.longToByte(c5.getTime(), b, cntOfs);
					lc.getPayload().getDataType()[i] = (byte) (isContentNull ? 0x03 : dataType);
					cntOfs += 8;
					break;
				case 0x0b:
					SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
					Date c6 = isContentNull ? sdf1.parse("0") : sdf1.parse(lc.getPayload().getData()[i]);
					pager.longToByte(c6.getTime(), b, cntOfs);
					lc.getPayload().getDataType()[i] = (byte) (isContentNull ? 0x03 : dataType);
					cntOfs += 8;
					break;
				default:
					int maxCharNumber = Byte.toUnsignedInt(lc.getPayload().getDataType()[i]) - 0x0c;
					int charCounter;
					for (charCounter = 0; charCounter < maxCharNumber; charCounter++) {
						if(charCounter >= lc.getPayload().getData()[i].length()) {
							b[cntOfs++] = 0;
						} else {
							b[cntOfs++] = (byte) lc.getPayload().getData()[i].charAt(charCounter);
						}
					}
					break;
				}
				b[ofs++] = lc.getPayload().getDataType()[i];
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
		return b;
	}
	
	private int getIndexOfKey(Integer[] intSeq, Integer key) {
		for (int i = 0; i < intSeq.length; i++) {
			if (intSeq[i].intValue() == key.intValue()) {
				return i;
			}
		}
		return -1;
	}
	
	public leafCell createLeafCell(int pri, byte[] datatype, String[] values) {
		leafCell lc = new leafCell();
		payLoad p = new payLoad();
		
		String[] SkipKeyValues = new String[values.length-1];
		System.arraycopy(values, 1, SkipKeyValues, 0, values.length-1);
		byte[] SkipKeyDataType = new byte[datatype.length-1];
		System.arraycopy(datatype, 1, SkipKeyDataType, 0, datatype.length-1);
		
		p.setColumnsNumber((byte) (datatype.length - 1));
		p.setData(SkipKeyValues);
		p.setDataType(SkipKeyDataType);
		
		lc.setRowId(pri);
		lc.setPayLoadSize(calcPayLoadSize(SkipKeyValues, SkipKeyDataType));
		lc.setPayload(p);
		
		return lc;
	}
	public boolean insertLeafCell(int[] rootpage, leafCell lc) {
		//1.calc lc's offset, pageNo.
		List<Integer> parentPagePath = new ArrayList<Integer>();
		
		if(!pager.lookUpNewLeafPagePath(this.file, rootpage[0], parentPagePath)) {
			System.err.println("Fatal: Unable to find the target page.");
			return false;
		}
		
		int pageNumber = parentPagePath.get(parentPagePath.size() - 1);
		//open the PageNumber
		setPageIndex(pageNumber - 1);
		updatePageBaseAddr();
		retrieveLeafCells();
		
		if(pageNumber == -1) {
			System.out.println("Error: Failed to locate the page!");
			return false;
		}
		
		/* perfect strategy:
		 * 1. put data into the tail(check if space is enough)
		 * 2. put data into the deleted bytes(iff the space is enough)
		 * 3. pack up all data to reserve room for the new one if the sum-up of bytes is enough 
		 * 4. if 1,2,3 fail, then remove the couples of record, form new leafCells to insert again*/
		/*
		 * Due to the rowId is automatically increased, so the record would not actually deleted.  
		 * */
		int unallocatedSpaceEnd = getCellsStartOfs();
		int cellNumber = Byte.toUnsignedInt(getCellNumber());
		int unallocatedSpaceStart = (pager.pageHeaderStaticLength + cellNumber * 2);
		int unallocateSpaceLength = unallocatedSpaceEnd - (unallocatedSpaceStart+2); //1B for the tuple itself
		byte[] wbytes = leafCell2Bytes(lc);
		int lcSize = wbytes.length;
		
		TreeSet<Integer> sortedKeySet = new TreeSet<Integer>(cells.keySet());
		Integer[] KeySerialBefore = sortedKeySet.toArray(new Integer[sortedKeySet.size()]);
		
		if(lcSize <= unallocateSpaceLength) {
			//situation 1:
			short lcOfs = (short) (unallocatedSpaceEnd - lcSize);
			short ofsStartAddr = (short) (unallocatedSpaceStart);
			return writeLeafCell(ofsStartAddr, lcOfs, wbytes);
		} else {
			//it is over flow
			int newLeafPageNo = requestNewPage(file, pageTypeCode[0]);
			leafPager newLpReader = new leafPager(file);
			newLpReader.setPageIndex(newLeafPageNo-1);
			newLpReader.updatePageBaseAddr();
			short lcOfs = (short) (pageSize - lcSize);
			short ofsStartAddr = (short) pageHeaderStaticLength;
			newLpReader.writeLeafCell(ofsStartAddr, lcOfs, wbytes);
			this.writeRightMostPageNumber(newLeafPageNo);
			
			interiorCell ic = new interiorCell();
			ic.setRowId(KeySerialBefore[KeySerialBefore.length - 1]);
			ic.setLeftChildPageNumber(pageNumber);
			int parentInteriorRootPageNo;
			if(parentPagePath.size() == 1) {
				//there are no interior page
				parentInteriorRootPageNo = requestNewPage(file, pageTypeCode[1]);
				rootpage[0] = parentInteriorRootPageNo;
				parentPagePath.add(0, new Integer(parentInteriorRootPageNo));
			}
			
			interiorPager parentIpReader = new interiorPager(file);
			//locate leaf's parent page;
			parentInteriorRootPageNo = parentPagePath.get(parentPagePath.size() - 2);
			parentIpReader.setPageIndex(parentInteriorRootPageNo - 1);
			parentIpReader.updatePageBaseAddr();
			parentIpReader.writeRightMostPageNumber(newLeafPageNo);
			
			return insertInteriorCell(parentPagePath, parentPagePath.size() - 2, ic, rootpage);
			//inserted tuple has the biggest key in the page, only insert this tuple into the new page.
			//create a new interiorCell and insert into the current page's parent interior page.
		}
	}
	public boolean writeLeafCell(short OfsStartAddr, short cntStartAddr, byte[] lcCnt) {
		byte cno = getCellNumber();
		int cnoInt = Byte.toUnsignedInt(cno);
		cnoInt++;
		try {
			this.file.seek(this.pageBaseAddr+0x01);
			file.writeByte((byte)cnoInt);
			file.writeShort(cntStartAddr);
			
			long AddrLong= this.pageBaseAddr+ Short.toUnsignedLong(OfsStartAddr);
			this.file.seek(AddrLong);
			this.file.writeShort(cntStartAddr);
			
			AddrLong = this.pageBaseAddr+ Short.toUnsignedLong(cntStartAddr);
			this.file.seek(AddrLong);
			this.file.write(lcCnt);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	public boolean updateLeafCell(leafCell lCell, byte[] updateInfo) {
		try {
			this.file.seek(this.pageBaseAddr+Short.toUnsignedLong(lCell.getLocation()));
			this.file.write(updateInfo);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	public boolean deleteLeafCell(List<leafCell> lCellList) {
		
		try {
			byte cno = getCellNumber();
			if(cno == 0 || lCellList.isEmpty()) 
				return true;
			
			int cnoInt = Byte.toUnsignedInt(cno);
			short[] cellOfsSeq = new short[cnoInt];

			List<Integer> delIndexList = new ArrayList<Integer>(); 
			leafCell lc;
			//skip the lCell
			for (int i = 0; i < cnoInt; i++) {
				short s = getCellOffsetByIndex(i, cnoInt);

				for (int j = 0; j < lCellList.size(); j++) {
					lc = lCellList.get(j);
					if(s == lc.getLocation()) {
						delIndexList.add(Integer.valueOf(i));
						break;
					}
				}
				
				cellOfsSeq[i] = s;
			}
			
			if(delIndexList.size() == 0) 
				return false;
			byte[] cellOfsSeqByte = new byte[2*(cnoInt-delIndexList.size())];
			int cnter = 0;
			for (int i = 0; i < cnoInt; i++) {
				if(!delIndexList.contains(Integer.valueOf(i))) {
					shortToByte(cellOfsSeq[i], cellOfsSeqByte, 2*(cnter++));
				}
			}
			
			if(cellOfsSeqByte != null && cellOfsSeqByte.length != 0) {
				this.file.seek(this.pageBaseAddr+pager.pageHeaderStaticLength);
				this.file.write(cellOfsSeqByte);
			}
			
			cnoInt-=delIndexList.size();
			this.file.seek(this.pageBaseAddr+0x01);
			file.writeByte((byte)cnoInt);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	public boolean insertInteriorCell(List<Integer> parentPagePath, int index, interiorCell ic, int[] rootPageNo) {
		interiorPager ipReader = new interiorPager(file);
		//start from the bottom node.
		int tmpPageNo = parentPagePath.get(index);
		ipReader.setPageIndex(tmpPageNo - 1);
		ipReader.updatePageBaseAddr();
		
		int ipUnallocatedCntEnd = ipReader.getCellsStartOfs();
		int cellNo = Byte.toUnsignedInt(ipReader.getCellNumber());
		int ipUnallocatedCntStart =  cellNo * 2 + pageHeaderStaticLength;
		
		if (ipUnallocatedCntEnd - ipUnallocatedCntStart < 10) {
			//interior page is not enough to insert a new interiorCell
			int newInteriorPageNo = requestNewPage(file, pageTypeCode[1]);
			interiorPager newIpReader = new interiorPager(file);
			newIpReader.setPageIndex(newInteriorPageNo - 1);
			newIpReader.updatePageBaseAddr();
			newIpReader.writeRightMostPageNumber(ipReader.getRightMostPageNumber());
			newIpReader.writeInteriorCell(ic, (short)pageHeaderStaticLength, (short)(pageSize - 8));
			ipReader.writeRightMostPageNumber(newInteriorPageNo);
			
			//pack up a new ic to insert the grandparent.
			ipReader.retrieveInteriorCells();
			TreeSet<Integer> leftSiblingParentKey = new TreeSet<Integer>(ipReader.cells.keySet());
			Integer lastKey = leftSiblingParentKey.last();
			interiorCell newIc = new interiorCell();
			newIc.setRowId(lastKey.intValue());
			newIc.setLeftChildPageNumber(tmpPageNo);
			boolean isParentExist = index - 1 >= 0 ? true : false;
			interiorPager parentIpReader = new interiorPager(file);
			int parentPageNo;
			int idx;
			if(isParentExist) {
				parentPageNo = parentPagePath.get(index - 1);
				idx = index - 1;
			} else {
				int newInteriorRootPageNo = requestNewPage(file, pageTypeCode[1]);
				rootPageNo[0] = newInteriorRootPageNo;
				parentPagePath.add(0, new Integer(newInteriorRootPageNo));
				parentPageNo = newInteriorRootPageNo;
				idx = 0;//it should be the root page.
			}
			parentIpReader.setPageIndex(parentPageNo - 1);
			parentIpReader.updatePageBaseAddr();
			parentIpReader.writeRightMostPageNumber(newInteriorPageNo);
			
			return insertInteriorCell(parentPagePath, idx, newIc, rootPageNo);
		} else {
			ic.setLocation((short)(ipUnallocatedCntEnd - 8));
			return ipReader.writeInteriorCell(ic, (short)ipUnallocatedCntStart, (short)(ipUnallocatedCntEnd - 8));
		}
	}
}
