package edu.utd.db.zhySql.Table;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.Set;
import java.util.TreeSet;

import edu.utd.db.zhySql.sqlBase;
import edu.utd.db.zhySql.Page.leafCell;
import edu.utd.db.zhySql.Page.leafPager;
import edu.utd.db.zhySql.Page.pager;
import edu.utd.db.zhySql.Page.payLoad;

/***********************************************
* @author hxz174130@utdallas.edu
* 
* @date Apr 8, 2018 3:18:57 PM
* 
***********************************************/
public class table {
	RandomAccessFile table;
	String directory;
	String tblName;
	int rootPageNo;
	LinkedHashMap<String, Byte> column;
	LinkedHashMap<String, Boolean> isNullable;
	
	public table(String dir, String table_name) {
		// TODO Auto-generated constructor stub
		try {
			this.directory = dir;
			this.tblName = table_name;
			table = new RandomAccessFile(dir+"/"+table_name+".tbl", "rw");
			this.column = new LinkedHashMap<String, Byte>();
			this.isNullable = new LinkedHashMap<String, Boolean>();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void close() {
		try {
			this.table.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean initTable() {
		/** Create davisbase_tables system catalog */
		try {
			
			if(this.directory == null || this.tblName == null) {
				throw new Exception("Dir or table name NULL!");
			}
			
			table = new RandomAccessFile(this.directory+"/"+this.tblName+".tbl", "rw");
			/* add the first page */
			pager.requestNewPage(table, pager.pageTypeCode[0]);
			
			//table.close();
			return true;
		}
		
		catch (Exception e) {
			System.out.println("Unable to create the tables file");
			System.out.println(e);
			return false;
		}

	}

	public boolean addColumn(String columnName, Byte type) {
		if(column != null && column.containsKey(columnName)) {
			System.out.println("Fail to add column for the duplicate name!");
			return false;
		}
		
		column.put(columnName, type);
		return true;
	}
	
	public boolean deleteColumn(String colName) {
		if(column.containsKey(colName)) {
			column.remove(colName);
			return true;
		}
		else {
			System.out.println("Fail to del column for the no such name!");
		}
		
		return false;
	}
	
	public void setColumns(LinkedHashMap<String, Byte> col) {
		this.column = col;
	}
	
	public LinkedHashMap<String, Byte> getColumns() {
		return this.column;
	}
	
	public void addNullAttr(String columnName, Boolean b) {
		isNullable.put(columnName, b);
	}
	
	public static int getIndexOfColumn(String[] keySet, String key) {
		for (int i = 0; i < keySet.length; i++) {
			if(keySet[i].equals(key))
				return i;
		}
		
		return -1;
	}
	
	//condition[0] = columnName, condition[1] = operator, condition[2] = value
	public Map<Integer, leafCell> queryTargetTuples(String[] condition) {
		
		Set<String> columnNamesSet = this.column.keySet();
		String[] columnNamesSeq = columnNamesSet.toArray(new String[columnNamesSet.size()]);
		int conditionColIdx = -1;
		
		if(condition != null && condition.length != 0) {
			if (!this.column.containsKey(condition[0])) {
				System.out.println("ERROR: CONDITION DOES NOT MATCH COLUMN.");
				return null;
			} else {
				conditionColIdx = getIndexOfColumn(columnNamesSeq, condition[0]);
			}
		}
		Map<Integer, leafCell> allTuples = queryTuples();
		Map<Integer, leafCell> resultTuples = new LinkedHashMap<Integer, leafCell>();
		
		for (Map.Entry<Integer, leafCell> entry : allTuples.entrySet()) {
			leafCell cell = entry.getValue();
			payLoad payload = cell.getPayload();
			String[] data = payload.getData();
			byte[] dataTypeCodes = payload.getDataType();

			boolean result = false;
			if (conditionColIdx == -1) {//nowhere
				result = true;
			} else if (conditionColIdx == 0) //where "rowId" "operator" "condition"
				result = checkData((byte) 0x06, entry.getKey().toString(), condition);
			else
				result = checkData(dataTypeCodes[conditionColIdx - 1], data[conditionColIdx - 1], condition);

			if (result)
				resultTuples.put(entry.getKey(), entry.getValue());
		}

		return resultTuples;
	}
	
	private static boolean checkData(byte code, String data, String[] condition) {
		int unsignedByteCode = Byte.toUnsignedInt(code);
		if (unsignedByteCode >= 0x04 && unsignedByteCode <= 0x07) {
			Long dataLong = Long.parseLong(data);
			switch (condition[1]) {
			case "=":
				if (dataLong == Long.parseLong(condition[2]))
					return true;
				break;
			case ">":
				if (dataLong > Long.parseLong(condition[2]))
					return true;
				break;
			case "<":
				if (dataLong < Long.parseLong(condition[2]))
					return true;
				break;
			case "<=":
				if (dataLong <= Long.parseLong(condition[2]))
					return true;
				break;
			case ">=":
				if (dataLong >= Long.parseLong(condition[2]))
					return true;
				break;
			case "<>":
				if (dataLong != Long.parseLong(condition[2]))
					return true;
				break;
			default:
				System.out.println("undefined operator return false");
				return false;
			}
		} else if (unsignedByteCode == 0x08 || unsignedByteCode == 0x09) {
			Double doubleData = Double.parseDouble(data);
			switch (condition[1]) {
			case "=":
				if (doubleData == Double.parseDouble(condition[2]))
					return true;
				break;
			case ">":
				if (doubleData > Double.parseDouble(condition[2]))
					return true;
				break;
			case "<":
				if (doubleData < Double.parseDouble(condition[2]))
					return true;
				break;
			case "<=":
				if (doubleData <= Double.parseDouble(condition[2]))
					return true;
				break;
			case ">=":
				if (doubleData >= Double.parseDouble(condition[2]))
					return true;
				break;
			case "<>":
				if (doubleData != Double.parseDouble(condition[2]))
					return true;
				break;
			default:
				System.out.println("undefined operator return false");
				return false;
			}
		} else if (unsignedByteCode >= 0x0C) {
			condition[2] = condition[2].replaceAll("'", "").replaceAll("\"", "");
			switch (condition[1]) {
			case "=":
				if (data.equalsIgnoreCase(condition[2]))
					return true;
				break;
			case "<>":
				if (!data.equalsIgnoreCase(condition[2]))
					return true;
				break;
			default:
				System.out.println("undefined operator return false");
				return false;
			}
		}
		return false;
	}
	
	public Map<Integer, leafCell> queryTuples() {
		Map<Integer, leafCell> tuples = new LinkedHashMap<Integer, leafCell>();
		int fisrtLeafPageNo = pager.locatePageNumberOfRowId(table, rootPageNo, 1);
		leafPager lpReader = new leafPager(table);
		lpReader.setPageIndex(fisrtLeafPageNo - 1);
		lpReader.updatePageBaseAddr();
		lpReader.retrieveLeafCells();
		lpReader.retrieveTuples();
		Map<Integer, leafCell> tMap = lpReader.getLeafCells();
		tuples.putAll(tMap);
		
		int rightMostPageNo = 0;
		while ((rightMostPageNo = lpReader.getRightMostPageNumber()) != 0) {
			lpReader.setPageIndex(rightMostPageNo - 1);
			lpReader.updatePageBaseAddr();
			lpReader.retrieveLeafCells();
			lpReader.retrieveTuples();
			tMap = lpReader.getLeafCells();
			tuples.putAll(tMap);
		}
		
		return tuples;
	}
	
	public int getMaximumKey() {
		int key = 0;
		List<Integer> parentPagePath = new ArrayList<Integer>();
		
		if(!pager.lookUpNewLeafPagePath(table, this.rootPageNo, parentPagePath)) {
			return 0;
		}
		
		leafPager lpReader = new leafPager(table);
		lpReader.setPageIndex(parentPagePath.get(parentPagePath.size()-1) - 1);
		lpReader.updatePageBaseAddr();
		lpReader.retrieveLeafCells();
		Map<Integer, leafCell> cells = lpReader.getLeafCells();
		if(cells != null && cells.size() != 0) {
			TreeSet<Integer> keyTree = new TreeSet<Integer>(cells.keySet());
			Integer largestRowId = keyTree.last();
			return largestRowId.intValue();
		}
		
		return key;
	}
	
	//columnNames are all converted into lowercase.
	public boolean insertTuple(String[] columnNames, String[] values) {
		if(values == null || columnNames == null || columnNames.length != values.length) {
			//System.out.println("Input's integrity fails!");
			return false;
		}
		
		//get all the column names 
		String[] fullColumnNmaes = new String[this.column.size()];
		Set<String> fullKeySet = this.column.keySet();
		fullColumnNmaes = fullKeySet.toArray(new String[this.column.size()]);
		//get the define data type
		Collection<Byte> collectionDatatype = this.column.values();
		Byte[] defineByteDateType = collectionDatatype.toArray(new Byte[this.column.size()]);
		byte[] defineDataType = new byte[this.column.size()];
		for (int i = 0; i < this.column.size(); i++) {
			defineDataType[i] = defineByteDateType[i].byteValue();	
		}
		
		Collection<Boolean> isNullableDef = this.isNullable.values();
		Boolean[] defineIsNullable = isNullableDef.toArray(new Boolean[this.column.size()]);
		//fill the values with the user inserted
		//check if user insert the primary key. Assume the primary key stays at the first place.
		boolean isExistPrimaryKey = false;
		int primaryKey = -1;
		String[] fullValues = new String[this.column.size()];
		for (int i = 0; i < columnNames.length; i++) {
			if(!column.containsKey(columnNames[i])) {
				//not contains, failed.
				System.out.println("Error: ColumnName('"+ columnNames[i] +"') is an invaild column.");
				return false;
			}
			
			int index = getIndexOfColumn(fullColumnNmaes, columnNames[i]);
			fullValues[index] = values[i];
			
			if (columnNames[i].equals("rowid")) { 
				isExistPrimaryKey = true; 
				if(values[i] == null || values[i].isEmpty()) {
					System.out.println("Error: Primary key could not be empty.");
					return false;
				}
				
				try {
					primaryKey = Integer.parseInt(values[i]);
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println("Error: Primary key must be decimal.");
					return false;
				}
			}
		}
		
		if(!isExistPrimaryKey) {
			System.out.println("Error: Primary key is empty.");
			return false;
		}
		
		//check the values is set to null but its definition asks for not null.
		for (int i = 0; i < defineIsNullable.length; i++) {
			if (defineIsNullable[i].booleanValue() == false) {
				if(fullValues[i] == null || fullValues[i].isEmpty()) {
					System.out.println("Error: Column("+fullColumnNmaes[i]+") could not be null due to the schema.");
					return false;
				}
			}	
		}
		
		leafPager p = new leafPager(table);
		int[] rootPageNumber = new int[1];
		rootPageNumber[0] = rootPageNo;
		
		boolean b = p.insertLeafCell(rootPageNumber, p.createLeafCell(primaryKey, defineDataType, fullValues));
		if (rootPageNo != rootPageNumber[0]) {
			rootPageNo = rootPageNumber[0];
			sqlBase.updateTableRootPage(this.tblName, rootPageNo);
		}
		
		return b;
	}
	
	public boolean updateTuples(String[] set_value, String[] whereCondition) {
		String set_columnName = set_value[0];
		String set_Value = set_value[2];
		
		String where_columnName = null;
		String where_Value = null;
		
		if(whereCondition != null && whereCondition.length!=0) {
			where_columnName = whereCondition[0];
			where_Value = whereCondition[2];
		}
		
		Set<String> colNames = this.column.keySet();
		String[] colNamesSeq = colNames.toArray(new String[colNames.size()]);
		
		int indexOfSetColumn = getIndexOfColumn(colNamesSeq, set_columnName); // full column index
		int indexOfWhereColumn = getIndexOfColumn(colNamesSeq, where_columnName); // full column index
		
		if(indexOfSetColumn == -1 || indexOfSetColumn == 0) {
			System.out.println("ERROR: NOT SUCH COLUMN IN TABLE & PROHIBIT TO MODIFY PRIMARY KEY.");
			return false;
		}
		
		int nextLeafPageNo = pager.locatePageNumberOfRowId(table, rootPageNo, 1);
		leafPager lpReader = new leafPager(table);
		Map<Integer, leafCell> tMap;
		Byte[] tablesDataType = this.column.values().toArray(new Byte[this.column.size()]);
		
		while (nextLeafPageNo != 0) {
			lpReader.setPageIndex(nextLeafPageNo - 1);
			lpReader.updatePageBaseAddr();
			lpReader.retrieveLeafCells();
			lpReader.retrieveTuples();
			tMap = lpReader.getLeafCells();
		
			for (Map.Entry<Integer, leafCell> entry : tMap.entrySet()) {
				if (indexOfWhereColumn == -1) {
					//NO WHERE:  update TABLE set COLUMN_NAME = VALUE
					leafCell lCell = entry.getValue();
					//checkData(set_columnDataType, lCell.getPayload().getData()[indexOfSetColumn - 1], set_value);
					if(set_Value != null 
						&& !set_Value.isEmpty() 
						&& !set_Value.equals(lCell.getPayload().getData()[indexOfSetColumn - 1])) {
						byte[] bs = lpReader.modifyLeafCellContent2Bytes(lCell, indexOfSetColumn, set_Value, tablesDataType);
						lpReader.updateLeafCell(lCell, bs);
					}
				} else if (indexOfWhereColumn == 0) {//WHERE
					//rowId
					//update TABLE set COLUMN_NAME = VALUE where rowid = xxx;
					if(!entry.getKey().equals((Integer.valueOf(where_Value)))) {
						continue;
					} else {
						leafCell lCell = entry.getValue();
						if(set_Value != null 
							&& !set_Value.isEmpty() 
							&& !set_Value.equals(lCell.getPayload().getData()[indexOfSetColumn - 1])) {
							byte[] bs = lpReader.modifyLeafCellContent2Bytes(lCell, indexOfSetColumn, set_Value, tablesDataType);
							lpReader.updateLeafCell(lCell, bs);
						} else {
							System.out.println("ERROR: SET VALUE IS NULL OR THE SAME AS THE OLD ONE.");
						}
						return true;
					}
				} else {
					//update TABLE set COLUMN_NAME = VALUE where COLUMN_NAME2 = xxx;
					leafCell lCell = entry.getValue();
					boolean bool = checkData(tablesDataType[indexOfWhereColumn].byteValue(), 
							lCell.getPayload().getData()[indexOfWhereColumn - 1], whereCondition);
					if (bool) {
						if(set_Value != null 
							&& !set_Value.isEmpty() 
							&& !set_Value.equals(lCell.getPayload().getData()[indexOfSetColumn - 1])) {
							byte[] bs = lpReader.modifyLeafCellContent2Bytes(lCell, indexOfSetColumn, set_Value, tablesDataType);
							lpReader.updateLeafCell(lCell, bs);
						} else {
							System.out.println("ERROR: SET VALUE IS NULL OR THE SAME AS THE OLD ONE.");
						}
					}
				}
			}
			
			nextLeafPageNo = lpReader.getRightMostPageNumber();
		}
		
		return true;
	}
	
	public boolean deleteTuples(String[] whereCondition) {
		String where_columnName = null;
		String where_Value = null;
		
		if(whereCondition!=null&&whereCondition.length!=0) {
			where_columnName = whereCondition[0];
			where_Value = whereCondition[2];
		}
		Set<String> colNames = this.column.keySet();
		String[] colNamesSeq = colNames.toArray(new String[colNames.size()]);
		int indexOfWhereColumn = getIndexOfColumn(colNamesSeq, where_columnName); // full column index
		
		int nextLeafPageNo = pager.locatePageNumberOfRowId(table, rootPageNo, 1);
		leafPager lpReader = new leafPager(table);
		Map<Integer, leafCell> tMap;
		Byte[] tablesDataType = this.column.values().toArray(new Byte[this.column.size()]);
	
		while (nextLeafPageNo != 0) {
			lpReader.setPageIndex(nextLeafPageNo - 1);
			lpReader.updatePageBaseAddr();
			lpReader.retrieveLeafCells();
			lpReader.retrieveTuples();
			tMap = lpReader.getLeafCells();
			List<leafCell> lcList = new ArrayList<leafCell>();
			
			for (Map.Entry<Integer, leafCell> entry : tMap.entrySet()) {
				if (indexOfWhereColumn == -1) {
					//NO WHERE:  delete all tuples
					leafCell lCell = entry.getValue();
					lcList.add(lCell);
				} else if (indexOfWhereColumn == 0) {//WHERE
					//rowId
					//update TABLE set COLUMN_NAME = VALUE where rowid = xxx;
					if(!entry.getKey().equals((Integer.valueOf(where_Value)))) {
						continue;
					} else {
						leafCell lCell = entry.getValue();
						lcList.add(lCell);
					}
				} else {
					//update TABLE set COLUMN_NAME = VALUE where COLUMN_NAME2 = xxx;
					leafCell lCell = entry.getValue();
					boolean bool = checkData(tablesDataType[indexOfWhereColumn].byteValue(), 
							lCell.getPayload().getData()[indexOfWhereColumn - 1], whereCondition);
					if (bool) {
						lcList.add(lCell);
					} else {
						continue;
					}
				}
			}
			
			if (!lcList.isEmpty()) {
				lpReader.deleteLeafCell(lcList);
			}
			nextLeafPageNo = lpReader.getRightMostPageNumber();
		}
		
		return true;
	}
	
	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getTblName() {
		return tblName;
	}

	public void setTblName(String tblName) {
		this.tblName = tblName;
	}
	
	public int getRootPageNo() {
		return rootPageNo;
	}

	public void setRootPageNo(int rootPageNo) {
		this.rootPageNo = rootPageNo;
	}
	
	public LinkedHashMap<String, Boolean> getIsNullable() {
		return isNullable;
	}
	public void setIsNullable(LinkedHashMap<String, Boolean> isNullable) {
		this.isNullable = isNullable;
	}
}
