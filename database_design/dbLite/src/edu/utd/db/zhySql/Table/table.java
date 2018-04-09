package edu.utd.db.zhySql.Table;

import java.lang.*;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.utd.db.zhySql.Page.cell;
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
	
	LinkedHashMap<String, Byte> column;
	LinkedHashMap<String, Boolean> isNullable;
	
	public table(String dir, String table_name) {
		// TODO Auto-generated constructor stub
		try {
			this.directory = dir;
			this.tblName = table_name;
			table = new RandomAccessFile(dir+"/"+table_name+".tbl", "rw");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void initTable() {
		/** Create davisbase_tables system catalog */
		try {
			
			if(this.directory == null || this.tblName == null) {
				throw new Exception("Dir or table name NULL!");
			}
			
			table = new RandomAccessFile(this.directory+"/"+this.tblName+".tbl", "rw");
			/* add the first page */
			pager.addPage(table, pager.pageTypeCode[0]);
			
			table.close();
		}
		
		catch (Exception e) {
			System.out.println("Unable to create the tables file");
			System.out.println(e);
		}

	}

	public boolean addColumn(Map.Entry<String, Byte> entry) {
		if(column != null && column.containsKey(entry.getKey())) {
			System.out.println("Fail to add column for the duplicate name!");
			return false;
		}
		
		column.put(entry.getKey(), entry.getValue());
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
	
	public static short calcTuplePayLoadSize(String[] values, byte[] defineDataType) {
		short size = 1; //1 byte for column number
		short n = (short) defineDataType.length; //n byte
		size += n;
		short maxByteNumber;
		for (int i = 0; i < defineDataType.length; i++) {
			maxByteNumber = pager.getSerialCodeTypeByteLength(defineDataType[i]);
			if (defineDataType[i] < 0x0c) {
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
	
	private static int getIndexOfColumn(String[] keySet, String key) {
		for (int i = 0; i < keySet.length; i++) {
			if(keySet[i] == key)
				return i;
		}
		
		return -1;
	}
	
	//columnNames are all converted into lowercase.
	public boolean insertTuple(String[] columnNames, String[] values) {
		if(values == null || columnNames == null || columnNames.length != values.length) {
			System.out.println("Input's integrity fails!");
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
		String[] fullValues = new String[this.column.size()];
		for (int i = 0; i < columnNames.length; i++) {
			if(!column.containsKey(columnNames[i])) {
				//not contains, failed.
				System.out.println("Error: ColumnName('"+ columnNames[i] +"') is an invaild column.");
				return false;
			}
			int index = getIndexOfColumn(fullColumnNmaes, columnNames[i]);
			fullValues[index] = values[i];
			
			if (columnNames[i].equals("row_id")) { 
				isExistPrimaryKey = true; 
				if(values[i] == null || values[i].isEmpty()) {
					System.out.println("Error: Primary key could not be empty.");
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
		
		
		return false;
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
}
