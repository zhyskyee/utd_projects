package edu.utd.db.zhySql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.util.TreeSet;

import edu.utd.db.zhySql.Page.leafCell;
import edu.utd.db.zhySql.Page.pager;
import edu.utd.db.zhySql.Table.table;
/***********************************************
* @author hxz174130@utdallas.edu
* 
* @date Apr 13, 2018 6:49:26 PM
* 
***********************************************/
public class sqlBase {
	/* This can be changed to whatever you like */
	static String prompt = "sql> ";
	static String version = "v1.0";
	static String copyright = "©2018 Wayne Zhang";
	static boolean isExit = false;

	static String catalogDir = new String("data/catalog");
	static String catalogSetupFile = new String("setup.ini");
	
	static String catalogTablesName = new String("base_tables");
	static String catalogTablesFile = new String("base_tables.tbl");
	static int catalogTablesRootPage = 1;
	static String[] catalogTableColumnsName = {"rowid","table_name","record_count","avg_length","root_page"};
	static String[] catalogTableColumnDataType = {"INT", "TEXT","INT", "SMALLINT", "SMALLINT"};

	static String catalogColumnsName = new String("base_columns");
	static String catalogColumnsFile = new String("base_columns.tbl");
	static int catalogColumnsRootPage = 1;
	static String[] catalogColumnsColumnsName = {"rowid", "table_name", "column_name","data_type","ordinal_position","is_nullable"};
	static String[] catalogColumnsColumnDataType = {"INT","TEXT","TEXT","TEXT","TINYINT","TEXT"};
	static String userDataDir = new String("data/user_data");
	
	/* 
	 *  The Scanner class is used to collect user commands from the prompt
	 *  There are many ways to do this. This is just one.
	 *
	 *  Each time the semicolon (;) delimiter is entered, the userCommand 
	 *  String is re-populated.
	 */
	static Scanner scanner = new Scanner(System.in).useDelimiter(";");
	
	private static void initializeDataStore() {
		/** Create data directory at the current OS location to hold */
		try {
			File catalogDirFile = new File(catalogDir);
			File userDataDirFile = new File(userDataDir);
			
			File tablesFile = new File(catalogDir+"/"+catalogTablesFile);
			File columnsFile = new File(catalogDir+"/"+catalogColumnsFile);
			File setUpInitFile = new File(catalogDir+"/"+catalogSetupFile);
			catalogDirFile.mkdirs();
			userDataDirFile.mkdirs();
			
			if (!tablesFile.exists() || !columnsFile.exists()) {
				
				if(tablesFile.exists()) tablesFile.delete();
				tablesFile.createNewFile();
				
				if(columnsFile.exists()) columnsFile.delete();
				columnsFile.createNewFile();
				
				if(setUpInitFile.exists()) setUpInitFile.delete();
				setUpInitFile.createNewFile();
				
				table tables = new table(catalogDir, catalogTablesName);
				tables.setRootPageNo(catalogTablesRootPage);
				tables.initTable();
				
				table Columns = new table(catalogDir, catalogColumnsName);
				Columns.setRootPageNo(catalogTablesRootPage);
				Columns.initTable();
				
				//update the setup.ini
				OutputStream outputStream = new FileOutputStream(setUpInitFile);
				PrintStream pStream = new PrintStream(outputStream);
				pStream.println(catalogTablesName+":"+catalogTablesRootPage);
				pStream.println(catalogColumnsName+":"+catalogColumnsRootPage);
				pStream.close();
				//--------start--------//
				initTablestableAttr(tables);
				String[] insertTablesValues = new String[catalogTableColumnsName.length];
				
				insertTablesValues[0] = "1"; //rowID
				insertTablesValues[1] = catalogTablesName; //table_name
				insertTablesValues[2] = "2"; //record_count
				insertTablesValues[3] = "0"; //avg_length
				insertTablesValues[4] = Integer.valueOf(catalogTablesRootPage).toString(); //root_page
				tables.insertTuple(catalogTableColumnsName, insertTablesValues);
				
				insertTablesValues[0] = "2"; //rowID
				insertTablesValues[1] = catalogColumnsName; //table_name
				insertTablesValues[2] = "0"; //record_count
				insertTablesValues[3] = "0"; //avg_length
				insertTablesValues[4] = Integer.valueOf(catalogColumnsRootPage).toString();; //root_page
				tables.insertTuple(catalogTableColumnsName, insertTablesValues);
				
				//-------------insert column info-------------//
				initColumnstableAttr(Columns);
				int columnstable_rowIdBase = getTableRowId(catalogColumnsName) + 1;
				for (int i = 0; i < catalogTableColumnsName.length; i++) {
					String[] insertColumnsValues = new String[catalogColumnsColumnsName.length];
					insertColumnsValues[0] = Integer.valueOf(columnstable_rowIdBase).toString(); //rowId
					insertColumnsValues[1] = catalogTablesName; //table_name
					insertColumnsValues[2] = catalogTableColumnsName[i]; //column_name
					insertColumnsValues[3] = catalogTableColumnDataType[i];
					insertColumnsValues[4] = Integer.valueOf(i+1).toString();
					insertColumnsValues[5] = "NO";
					Columns.insertTuple(catalogColumnsColumnsName, insertColumnsValues);
					increaseTableRecordCount(catalogColumnsName, columnstable_rowIdBase++);
				}
				
				for (int i = 0; i < catalogColumnsColumnsName.length; i++) {
					String[] insertColumnsValues = new String[catalogColumnsColumnsName.length];
					insertColumnsValues[0] = Integer.valueOf(columnstable_rowIdBase).toString(); //rowId
					insertColumnsValues[1] = catalogColumnsName; //table_name
					insertColumnsValues[2] = catalogColumnsColumnsName[i]; //column_name
					insertColumnsValues[3] = catalogColumnsColumnDataType[i];
					insertColumnsValues[4] = Integer.valueOf(i+1).toString();
					insertColumnsValues[5] = "NO";
					Columns.insertTuple(catalogColumnsColumnsName, insertColumnsValues);
					increaseTableRecordCount(catalogColumnsName, columnstable_rowIdBase++);
				}
				
				if (tables.getRootPageNo() != catalogTablesRootPage 
					|| Columns.getRootPageNo() != catalogColumnsRootPage) {
					outputStream = new FileOutputStream(setUpInitFile);
					pStream = new PrintStream(outputStream);
					catalogTablesRootPage = tables.getRootPageNo();
					catalogColumnsRootPage = Columns.getRootPageNo();
					pStream.println(catalogTablesName+":"+catalogTablesRootPage);
					pStream.println(catalogColumnsName+":"+catalogColumnsRootPage);
					pStream.close();
				}
				tables.close();
				Columns.close();
			} else {
				//base file is already good
				FileReader fReader = new FileReader(setUpInitFile);
				BufferedReader bufferedReader = new BufferedReader(fReader);
				String tmp = null;
		
				while ((tmp = bufferedReader.readLine()) != null) {
					if (tmp.contains(catalogTablesName)) {
						catalogTablesRootPage = Integer.parseInt(tmp.split(":")[1]);
					} else {
						catalogColumnsRootPage = Integer.parseInt(tmp.split(":")[1]);
					}
				}
				
				bufferedReader.close();
			}
		} catch (SecurityException se) {
			System.out.println("Unable to create data container directory");
			System.out.println(se);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void initTablestableAttr(table tables) {
		//--------start--------//
		byte[] b = new byte[0xff-0x0c];
		String bString = new String(b);
		for (int i = 0; i < catalogTableColumnsName.length; i++) {
			tables.addColumn(catalogTableColumnsName[i], Byte.valueOf(pager.getSerialTypeCodeContent(bString, catalogTableColumnDataType[i])));
			tables.addNullAttr(catalogTableColumnsName[i], new Boolean(false));
		}
	}
	private static void initColumnstableAttr(table Columns) {
		byte[] b = new byte[0xff-0x0c];
		String bString = new String(b);
		for (int i = 0; i < catalogColumnsColumnsName.length; i++) {
			Columns.addColumn(catalogColumnsColumnsName[i], Byte.valueOf(pager.getSerialTypeCodeContent(bString, catalogColumnsColumnDataType[i])));
			Columns.addNullAttr(catalogColumnsColumnsName[i], new Boolean(false));
		}
	}
	
	private static boolean getTableColumnAttr(String tableName, LinkedHashMap<String, Byte> columns, LinkedHashMap<String, Boolean> isNullable) {
		try {
			table table = new table(catalogDir, catalogColumnsName);
			table.setRootPageNo(catalogColumnsRootPage);
			initColumnstableAttr(table);
			
			String[] condition = new String[3];
			condition[0] = catalogColumnsColumnsName[1];
			condition[1] = "=";
			condition[2] = tableName;
			
			Map<Integer, leafCell> tuples = table.queryTargetTuples(condition);
			for (Map.Entry<Integer, leafCell> entry : tuples.entrySet()) {
				leafCell lCell = entry.getValue();
				String columnName = lCell.getPayload().getData()[1]; //columnName
				Byte dataType = pager.getSerialTypeCode(lCell.getPayload().getData()[2].toUpperCase()); //dataType
				columns.put(columnName, dataType);
				Boolean isNullable1 = Boolean.valueOf(lCell.getPayload().getData()[4].equals("NO") ? false : true); //isNUllable
				isNullable.put(columnName, isNullable1);
			}
			table.close();
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
	}
	
	public static int getTableRootPageNumber(String tableName) {
		try {
			table table = new table(catalogDir, catalogTablesName);
			table.setRootPageNo(catalogTablesRootPage);
			initTablestableAttr(table);
			
			String[] condition = new String[3];
			condition[0] = catalogTableColumnsName[1];
			condition[1] = "=";
			condition[2] = tableName;
			
			Map<Integer, leafCell> tuple = table.queryTargetTuples(condition);
			int rootpage = 0;
			if (tuple!= null && tuple.size() >=1) {
				//get the first match tuple
				leafCell lCell = tuple.get(tuple.keySet().toArray(new Integer[tuple.size()])[0]);
				String[] data = lCell.getPayload().getData();
				rootpage = Integer.parseInt(data[data.length - 1]);
			}
			table.close();
			return rootpage;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return 0;
		}
	}
	
	public static boolean updateTableRootPage(String tableName, int rootPageNo) {
		try {
			table table = new table(catalogDir, catalogTablesName);
			table.setRootPageNo(catalogTablesRootPage);
			initTablestableAttr(table);
			
			String[] set_value = new String[3];
			set_value[0] = catalogTableColumnsName[4]; //"rootPage"
			set_value[1] = "=";
			set_value[2] = Integer.valueOf(rootPageNo).toString();
			String[] whereCondition = new String[3];
			whereCondition[0] = catalogTableColumnsName[1];
			whereCondition[1] = "=";
			whereCondition[2] = tableName;
			table.updateTuples(set_value, whereCondition);
			table.close();
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
	}
	
	public static int getTableRowId(String tableName) {	
		table t = new table(catalogDir, catalogTablesName);
		t.setRootPageNo(catalogTablesRootPage);
		initTablestableAttr(t);

		String[] condition = new String[3];
		condition[0] = catalogTableColumnsName[1];
		condition[1] = "=";
		condition[2] = tableName;
		
		Map<Integer, leafCell> tuples = t.queryTargetTuples(condition);
		leafCell lCell = tuples.get(tuples.keySet().toArray(new Integer[tuples.size()])[0]);
		String record_count = lCell.getPayload().getData()[1];
		int key = Integer.parseInt(record_count);
		t.close();
		return key;
	}
	
	public static void increaseTableRecordCount(String tableName, int currentRecordCount) {	
		table t = new table(catalogDir, catalogTablesName);
		t.setRootPageNo(catalogTablesRootPage);
		initTablestableAttr(t);

		String[] condition = new String[3];
		condition[0] = catalogTableColumnsName[1];
		condition[1] = "=";
		condition[2] = tableName;
		String[] set_value = new String[3];
		set_value[0] = catalogTableColumnsName[2];
		set_value[1] = "=";
		set_value[2] = Integer.valueOf(currentRecordCount).toString();
		
		t.updateTuples(set_value, condition);
		t.close();
		return;
	}
	/** ***********************************************************************
	 *  Main method
	 */
    public static void main(String[] args) {
    		/* Initialize the date store or status */
    		initializeDataStore();
		
    		/* Display the welcome screen */
		splashScreen();

		/* Variable to collect user input from the prompt */
		String userCommand = ""; 

		while(!isExit) {
			System.out.print(prompt);
			/* toLowerCase() renders command case insensitive */
			userCommand = scanner.next().replace("\n", " ").replace("\r", "").trim().toLowerCase();
			// userCommand = userCommand.replace("\n", "").replace("\r", "");
			parseUserCommand(userCommand);
		}
		
		System.out.println("Exiting...");
	}

	/** ***********************************************************************
	 *  Static method definitions
	 */

	/**
	 *  Display the splash screen
	 */
	public static void splashScreen() {
		System.out.println(line("-",80));
        System.out.println("Welcome to WayneSqlite");
		System.out.println("WayneSql Version " + getVersion());
		System.out.println(getCopyright());
		System.out.println("\nType \"help;\" to display supported commands.");
		System.out.println(line("-",80));
	}
	
	/**
	 * @param s The String to be repeated
	 * @param num The number of time to repeat String s.
	 * @return String A String object, which is the String s appended to itself num times.
	 */
	public static String line(String s,int num) {
		String a = "";
		for(int i=0;i<num;i++) {
			a += s;
		}
		return a;
	}
	
	public static void printCmd(String s) {
		System.out.println("\n\t" + s + "\n");
	}
	public static void printDef(String s) {
		System.out.println("\t\t" + s);
	}
	
	/**
	 *  Help: Display supported commands
	 */
	public static void help() {
		System.out.println(line("*",80));
		System.out.println("SUPPORTED COMMANDS\n");
		System.out.println("All commands below are case insensitive\n");
		printCmd("SHOW TABLES;");
		printDef("Display the names of all tables.");
		printCmd("SELECT * FROM <table_name>;");
		printDef("Display all records in the table <table_name>.");
		printCmd("SELECT <column_list> FROM <table_name> [WHERE <condition>];");
		printDef("Display table records whose optional <condition>");
		printDef("is <column_name> = <value>.");
		printCmd("CREATE TABLE <table_name> (<column_attribute_list>)");
		printDef("Create a new table, <column_attribute_list> = ");
		printDef("(rowid int primary key, column_name1 DATA_TYPE [NOT NULL], ....)");
		printDef("[rowid int primary key] must be implemented.");
		printCmd("DELETE FROM <table_name> [WHERE <condition>]");
		printDef("Delete a single/multiple record from a table given the specific condition.");
		printCmd("DROP TABLE <table_name>;");
		printDef("Remove table data (i.e. all records) and its schema.");
		printCmd("INSERT INTO <table_name> (<column_list>) VALUES(<value_list>)");
		printDef("Insert a new row into the table except the 'rowid'.");
		printCmd("UPDATE TABLE <table_name> SET <column_name> = <value> [WHERE <condition>];");
		printDef("Modify records data whose optional <condition> is");
		printCmd("VERSION;");
		printDef("Display the program version.");
		printCmd("HELP;");
		printDef("Display this help information.");
		printCmd("EXIT;");
		printDef("Exit the program.");
		System.out.println(line("*",80));
	}

	/** return the DavisBase version */
	public static String getVersion() {
		return version;
	}
	
	public static String getCopyright() {
		return copyright;
	}
	
	public static void displayVersion() {
		System.out.println("DavisBaseLite Version " + getVersion());
		System.out.println(getCopyright());
	}
		
	public static void parseUserCommand (String userCommand) {
		
		/* commandTokens is an array of Strings that contains one token per array element 
		 * The first token can be used to determine the type of command 
		 * The other tokens can be used to pass relevant parameters to each command-specific
		 * method inside each case statement */
		ArrayList<String> commandTokens = new ArrayList<String>(Arrays.asList(userCommand.split(" ")));
		
		/*
		*  This switch handles a very small list of hardcoded commands of known syntax.
		*  You will want to rewrite this method to interpret more complex commands. 
		*/
		switch (commandTokens.get(0)) {
			case "show":
				parseQuery("select * from base_tables");
				break;
			case "select":
				parseQuery(userCommand);
				break;
			case "drop":
				dropTable(userCommand);
				break;
			case "create":
				parseCreateTable(userCommand);
				break;
			case "update":
				parseUpdateString(userCommand);
				break;
			case "help":
				help();
				break;
			case "version":
				displayVersion();
				break;
			case "insert":
				parseInsertString(userCommand);
				break;
			case "delete":
				parseDeleteString(userCommand);	
				break;
			case "hexdump":
				String tableName = userDataDir+"/"+userCommand.split(" ")[1].trim()+".tbl";
				try {
					RandomAccessFile file = new RandomAccessFile(tableName,"r");
					HexDump.displayBinaryHex(file, pager.pageSize);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case "exit":
			case "quit":
				isExit = true;
				break;
			default:
				System.out.println("I didn't understand the command: \"" + userCommand + "\"");
				break;
		}
	}
	
	/**
	 *  Stub method for dropping tables
	 *  @param dropTableString is a String of the user input
	 */
	public static void dropTable(String dropTableString) {
		System.out.println("STUB: This is the dropTable method.");
		System.out.println("\tParsing the string:\"" + dropTableString + "\"");
		String[] droptemp = dropTableString.split(" ");
		
		String tableName = droptemp[2].trim();
		String dir;
		if (tableName.equalsIgnoreCase(catalogTablesName) || tableName.equalsIgnoreCase(catalogColumnsName)) {
			System.out.println("FATAL: PROHIBIT TO DROP THIS TABLE.");
			return;
		} else {
			dir = userDataDir;
		}
		
		if(!isTablePresent(dir, tableName)) {
			System.out.println("Table "+tableName+" is not present.");
			System.out.println();
			return;
		} else {
			String delTableAttr = "delete from base_tables where table_name="+tableName;
			String delColmnsAttr = "delete from base_columns where table_name="+tableName;
			parseDeleteString(delTableAttr);
			parseDeleteString(delColmnsAttr);
			File file=new File(userDataDir+"/"+tableName+".tbl");
			file.delete();
		}
	}
	
	/**
	 *  Stub method for executing queries
	 *  @param queryString is a String of the user input
	 */
	public static void parseQuery(String queryString) {
		System.out.println("STUB: This is the parseQuery method");
		System.out.println("\tParsing the string:\"" + queryString + "\"");

		String tableName;
		String[] columnNames;
		String[] condition = new String[0];
		String temp[] = queryString.split("where");
		
		tableName = temp[0].split("from")[1].trim();
		columnNames = temp[0].split("from")[0].replaceAll("select"," ").split(",");
		
		String dir;
		int rootPageNo;
		if (tableName.equalsIgnoreCase(catalogTablesName) || tableName.equalsIgnoreCase(catalogColumnsName)) {
			dir = catalogDir;
			if(tableName.equalsIgnoreCase(catalogTablesName)) {
				rootPageNo = catalogTablesRootPage;
			} else {
				rootPageNo = catalogColumnsRootPage;
			}
		} else {
			dir = userDataDir;
			rootPageNo = getTableRootPageNumber(tableName);
		}
		
		if(!isTablePresent(dir, tableName)) {
			System.out.println("Table not present");
			return;
		}
		
		for(int i = 0; i < columnNames.length; i++) {
			columnNames[i] = columnNames[i].trim();
		}
		
		if(temp.length > 1) {
			condition = parseCondition(temp[1]);
		}
		
		table table = new table(dir, tableName);
		table.setRootPageNo(rootPageNo);
		LinkedHashMap<String, Byte> columns = new LinkedHashMap<String, Byte>();
		LinkedHashMap<String, Boolean> isNullable = new LinkedHashMap<String, Boolean>();
		if(!getTableColumnAttr(tableName, columns, isNullable)) {
			System.out.println("FATAL: UNABLE TO GET TABLE'S ATTR.");
			return;
		}
		table.setColumns(columns);
		table.setIsNullable(isNullable);
		Map<Integer, leafCell> tuples = table.queryTargetTuples(condition);
		String[] filterColumnNames;
		String[] fullColumnNames = table.getColumns().keySet().toArray(new String[table.getColumns().size()]);
		
		if (columnNames.length == 1 && columnNames[0].equals("*")) {
			filterColumnNames = fullColumnNames;
		} else {
			filterColumnNames = columnNames;
		}
		
		sqlBase.printTable(fullColumnNames, filterColumnNames, tuples);
		table.close();
	}
	
	
	/**
	 *  Stub method for updating records
	 *  @param updateString is a String of the user input
	 */
	public static void parseUpdateString(String updateString)  {
		System.out.println("STUB: This is the dropTable method");
		System.out.println("Parsing the string:\"" + updateString + "\"");
		String[] updates = updateString.toLowerCase().split("set");
		String tablename = updates[0].trim().split(" ")[2].trim();
		String set_value;
		String where = null;
		String dir;
		int rootpageNo;
		
		if (tablename.equalsIgnoreCase(catalogTablesName) || tablename.equalsIgnoreCase(catalogColumnsName)) {
			dir = catalogDir;
			if(tablename.equalsIgnoreCase(catalogTablesName)) {
				rootpageNo = catalogTablesRootPage;
			} else {
				rootpageNo = catalogColumnsRootPage;
			}
		} else {
			dir = userDataDir;
			rootpageNo = getTableRootPageNumber(tablename);
		}
		
		if(!isTablePresent(dir, tablename)) {
			System.out.println("Table not present");
			return;
		}
		
		table t = new table(dir, tablename);
		t.setRootPageNo(rootpageNo);
		LinkedHashMap<String, Byte> dataType = new LinkedHashMap<String, Byte>();
		LinkedHashMap<String, Boolean> isNUllAble = new LinkedHashMap<String, Boolean>();
		if(!getTableColumnAttr(tablename, dataType, isNUllAble)) {
			System.out.println("FATAL: UNABLE TO GET TABLE'S ATTR.");
			return;
		}
		t.setColumns(dataType);
		t.setIsNullable(isNUllAble);
		
		if(updates[1].contains("where")) {
			String []findupdate = updates[1].split("where");
			set_value = findupdate[0].trim();
			where = findupdate[1].trim();
			t.updateTuples(parseCondition(set_value), parseCondition((where)));
		} else { 
			set_value=updates[1].trim();
			String[] no_where = new String[0];
			t.updateTuples(parseCondition(set_value), no_where);
		}
		t.close();
	}

	/**
	 *  Stub method for creating new tables
	 *  @param queryString is a String of the user input
	 */
	public static void parseCreateTable(String createTableString) {
		Pattern pattern = Pattern.compile("\\(.*?\\)");
		Matcher matcher = pattern.matcher(createTableString);
		String columnAttrStr = null;
		while (matcher.find()) {
			columnAttrStr = matcher.group().replaceAll("\\(\\)", "");
		}
		
		if (columnAttrStr == null || !columnAttrStr.contains("rowid int primary key")) {
			System.out.println("ERROR: NOT ATTRIBUTE OR NO PRI KEY.");
			return;
		}
		
		System.out.println("STUB: Calling your method to create a table");
		System.out.println("Parsing the string:\"" + createTableString + "\"");
		
		ArrayList<String> createTableTokens = new ArrayList<String>(Arrays.asList(createTableString.split(" ")));
		String tableName = createTableTokens.get(2);
		/* YOUR CODE GOES HERE */
		
		/*  Code to create a .tbl file to contain table data */
		try {
			/*  Create RandomAccessFile tableFile in read-write mode.
			 *  Note that this doesn't create the table file in the correct directory structure
			 */
			
			if (isTablePresent(userDataDir, tableName)) {
				System.out.println("ERROR: TABLE ALREADY EXIST.");
				return;
			}
			
			String[] columnAttr = columnAttrStr.replaceAll("\\(", "").replaceAll("\\)", "").split(",");
			String[] columnName = new String[columnAttr.length];
			String[] datatype = new String[columnAttr.length];
			String[] isNullable = new String[columnAttr.length];
			
			for (int i = 0; i < isNullable.length; i++) {
				String[] tmp = columnAttr[i].trim().split(" ");
				isNullable[i] = "YES";
				for (int j = 0; j < tmp.length; j++) {
					if(j == 0) {
						columnName[i] = tmp[j].trim();
					} else if (j == 1) {
						datatype[i] = tmp[j].trim().toUpperCase();
					} else {
						if(columnAttr[i].contains("not null") || columnAttr[i].contains("primary key")) {
							isNullable[i] = "NO";
						} else {
							isNullable[i] = "YES";
						}
					}
				}
			}
			
			table table = new table(userDataDir, tableName);
			table.initTable();
			table.setRootPageNo(1); //Set the firstPageNumber
			//reg root page.
			table Tables = new table(catalogDir, catalogTablesName);
			Tables.setRootPageNo(catalogTablesRootPage);
			initTablestableAttr(Tables);
			
			int tablestable_rowIdBase = getTableRowId(catalogTablesName) + 1;
			String[] insertTablesValues = new String[catalogTableColumnsName.length];
			insertTablesValues[0] = Integer.valueOf(tablestable_rowIdBase).toString(); //rowID
			insertTablesValues[1] = tableName; //table_name
			insertTablesValues[2] = "0"; //record_count
			insertTablesValues[3] = "0"; //avg_length
			insertTablesValues[4] = Integer.valueOf(table.getRootPageNo()).toString(); //root_page
			if(Tables.insertTuple(catalogTableColumnsName, insertTablesValues)) {
				increaseTableRecordCount(catalogTablesName, tablestable_rowIdBase);
			}
			
			table Columns = new table(catalogDir, catalogColumnsName);
			Columns.setRootPageNo(catalogColumnsRootPage);
			initColumnstableAttr(Columns);
			
			int columnstable_rowIdBase = getTableRowId(catalogColumnsName) + 1;
			for (int i = 0; i < columnAttr.length; i++) {
				String[] insertColumnsValues = new String[catalogColumnsColumnsName.length];
				insertColumnsValues[0] = Integer.valueOf(columnstable_rowIdBase).toString(); //rowId
				insertColumnsValues[1] = tableName; //table_name
				insertColumnsValues[2] = columnName[i]; //column_name
				insertColumnsValues[3] = datatype[i];
				insertColumnsValues[4] = Integer.valueOf(i+1).toString();
				insertColumnsValues[5] = isNullable[i];
				if(Columns.insertTuple(catalogColumnsColumnsName, insertColumnsValues)) {
					increaseTableRecordCount(catalogColumnsName, columnstable_rowIdBase++);
				}
			}
			
			if (Tables.getRootPageNo() != catalogTablesRootPage 
				|| Columns.getRootPageNo() != catalogColumnsRootPage) {
				File setUpInitFile = new File(catalogDir+"/"+catalogSetupFile);
				OutputStream outputStream = new FileOutputStream(setUpInitFile);
				PrintStream pStream = new PrintStream(outputStream);
				catalogTablesRootPage = Tables.getRootPageNo();
				catalogColumnsRootPage = Columns.getRootPageNo();
				pStream.println(catalogTablesName+":"+catalogTablesRootPage);
				pStream.println(catalogColumnsName+":"+catalogColumnsRootPage);
				pStream.close();
			}
			
			Tables.close();
			table.close();
			Columns.close();
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}
	/*  Code to insert a row in the davisbase_tables table 
	 *  i.e. database catalog meta-data 
	 */
	public static void parseInsertString(String insertString) {
		String[] insert = insertString.split(" ");
		String tableName = insert[2].trim();
		
		String values = insertString.split("values")[1].replaceAll("\\(", "").replaceAll("\\)", "").trim();
		String columnName = insertString.split("values")[0].split(tableName)[1].replaceAll("\\(", "").replaceAll("\\)", "").trim();
		
		String[] insertValues = values.split(",");
		String[] insertColumns = columnName.split(",");
		
		String dir;
		int rootPageNo;
		if (tableName.equalsIgnoreCase(catalogTablesName) || tableName.equalsIgnoreCase(catalogColumnsName)) {
			dir = catalogDir;
			if(tableName.equalsIgnoreCase(catalogTablesName)) {
				rootPageNo = catalogTablesRootPage;
			} else {
				rootPageNo = catalogColumnsRootPage;
			}
		} else {
			dir = userDataDir;
			rootPageNo = getTableRootPageNumber(tableName);
		}
		
		if (insertColumns == null) {
			System.out.println("STUB: NO COLUMNS.");
			return;
		}
		
		if (insertColumns.length != 0) {
			if(insertColumns.length != insertValues.length) {
				System.out.println("STUB: COLUMNS & VALUES NOT MATCH.");
				return;
			}
		}
		
		for(int i = 0; i < insertValues.length; i++) {
			insertValues[i] = insertValues[i].trim();
			insertColumns[i] = insertColumns[i].trim();
		}
		
		if(!isTablePresent(dir, tableName)) {
			System.out.println("Table "+tableName+" does not exist.");
			System.out.println();
			return;
		} else {
			table t = new table(dir, tableName);
			t.setRootPageNo(rootPageNo);
			LinkedHashMap<String, Byte> dataType = new LinkedHashMap<String, Byte>();
			LinkedHashMap<String, Boolean> isNUllAble = new LinkedHashMap<String, Boolean>();
			if(!getTableColumnAttr(tableName, dataType, isNUllAble)) {
				System.out.println("FATAL: UNABLE TO GET TABLE'S ATTR.");
				return;
			}
			t.setColumns(dataType);
			t.setIsNullable(isNUllAble);
			String[] inputString = new String[insertColumns.length + 1];
			String[] inputValue = new String[insertValues.length + 1];
			for (int i = 0; i < insertColumns.length; i++) {
				inputString[i+1] = insertColumns[i];
				inputValue[i+1] = insertValues[i];
			}
			int rowID = getTableRowId(tableName) + 1;
			inputString[0] = "rowid";
			inputValue[0] = Integer.valueOf(rowID).toString();
			
			boolean b = t.insertTuple(inputString, inputValue);
			
			if (b) {
				increaseTableRecordCount(tableName, rowID);
			}
			t.close();
		}		
	}
	/*  Code to insert rows in the davisbase_columns table  
	 *  for each column in the new table 
	 *  i.e. database catalog meta-data 
	 */
	
	private static void parseDeleteString(String userCommand) {
		String[] delete = userCommand.split("where");
		
		String[] table = delete[0].trim().split("from");
		
		String[] table1 = table[1].trim().split(" ");
		String tableName = table1[0].trim();
		
		String[] whereCondition = null;
		if (delete.length == 2) { 
			whereCondition = parseCondition(delete[1]);
		} else if (delete.length == 1) {
			whereCondition = new String[0];
		} else {
			System.out.println("ERROR: UNKNOWN COMMAND.");
			return;
		}
		
		String dir;
		int rootPageNo;
		if (tableName.equalsIgnoreCase(catalogTablesName) || tableName.equalsIgnoreCase(catalogColumnsName)) {
			dir = catalogDir;
			if(tableName.equalsIgnoreCase(catalogTablesName)) {
				rootPageNo = catalogTablesRootPage;
			} else {
				rootPageNo = catalogColumnsRootPage;
			}
		} else {
			dir = userDataDir;
			rootPageNo = getTableRootPageNumber(tableName);
		}
		
		if(!isTablePresent(dir, tableName)) {
			System.out.println("Table not present");
			return;
		} else {
			table t = new table(dir, tableName);
			t.setRootPageNo(rootPageNo);
			LinkedHashMap<String, Byte> dataType = new LinkedHashMap<String, Byte>();
			LinkedHashMap<String, Boolean> isNUllAble = new LinkedHashMap<String, Boolean>();
			if(!getTableColumnAttr(tableName, dataType, isNUllAble)) {
				System.out.println("FATAL: UNABLE TO GET TABLE'S ATTR.");
				return;
			}
			t.setColumns(dataType);
			t.setIsNullable(isNUllAble);
			t.deleteTuples(whereCondition);
			t.close();
		}
	}
	
	//Inner functions
	public static void printTable(String[] fullColNames, String[] userSelectColNames, Map<Integer, leafCell> tuples) {
		String colString = "";
		String recString = "";
		
		int[] colNameSpaceTake = new int[userSelectColNames.length];
		int[] userSelectColIndexSeq = new int[userSelectColNames.length];
		
		if (tuples.isEmpty()) {
			System.out.println("WARNING: EMPTY RESULTS.");
			return;
		}
		
		//print title
		for (int i = 0; i < userSelectColNames.length; ++i) {
			int index = 0;
			index = table.getIndexOfColumn(fullColNames, userSelectColNames[i]);
			if (index != -1) {
				userSelectColIndexSeq[i] = index;
			} else {
				System.out.println("Failed to Print for unknown columns.");
				return; 
			}
		}
		
		for (int i = 0; i < userSelectColIndexSeq.length; i++) {
			String colName = fullColNames[userSelectColIndexSeq[i]];
			int idx = userSelectColIndexSeq[i];
			if (colName.equals("rowid") || idx == 0) {
				TreeSet<Integer> rowIdSet = new TreeSet<Integer>(tuples.keySet());
				Integer maxRowid = rowIdSet.last(); //Maximum must be at last; 
				int rowidStrLen = maxRowid.toString().length();
				colNameSpaceTake[i] = (rowidStrLen > 5 ? rowidStrLen : 5) + 2;
			} else {
				int colStrLen = colName.length();
				idx--; //without rowid
				for (Map.Entry<Integer, leafCell> entry : tuples.entrySet()) {
					int actualDataLen = entry.getValue().getPayload().getData()[idx].length();
					colStrLen = actualDataLen > colStrLen ? actualDataLen : colStrLen;
				}
				colNameSpaceTake[i] = colStrLen + 2;
			}
		}
		
		colString ="|"; //start
		for (int i = 0; i < userSelectColNames.length; ++i) {
			String colName = userSelectColNames[i];
			int colStrLen = colNameSpaceTake[i];
			int leftGapNo = (colStrLen - colName.length()) / 2;
			int rightGapNo = colStrLen - leftGapNo - colName.length();
			
			for (int j = 0; j < leftGapNo; j++) {
				colString +=" ";
			}
			
			colString += colName;
			
			for (int j = 0; j < rightGapNo; j++) {
				colString +=" ";
			}
			colString+="|";
		}
		
		System.out.println(colString);
		
		String spliter = "";
		for (int i = 0; i < colString.length(); i++) {
			spliter+="-";
		}
		System.out.println(spliter);
		
		//print data
		for (Map.Entry<Integer, leafCell> entry : tuples.entrySet()) {
			leafCell cell = entry.getValue();
			int index = -1;
			String data[] = cell.getPayload().getData();
			
			recString = "|";
			//including rowID
			for (int k = 0; k < userSelectColIndexSeq.length; ++k) {
				index = userSelectColIndexSeq[k];
				String dataPtr = null;
				if(index == 0) {//indicating the rowid
					dataPtr = Integer.valueOf(cell.getRowId()).toString();
				} else {
					dataPtr = data[index - 1];
				}
				
				int leftGapNo = (colNameSpaceTake[k] - dataPtr.length()) / 2;
				int rightGapNo = colNameSpaceTake[k] - leftGapNo - dataPtr.length();
				for (int j = 0; j < leftGapNo; j++) {
					recString +=" ";
				}
				recString += dataPtr;
				
				for (int j = 0; j < rightGapNo; j++) {
					recString +=" ";
				}
				
				recString+="|";
			}
			System.out.println(recString);
		}
	}

	public static String[] parseCondition(String whereCondition) {
		String condition[] = new String[3];
		String values[] = new String[2];
		
		if (whereCondition.contains("=")) {
			values = whereCondition.split("=");
			condition[0] = values[0].trim();
			condition[1] = "=";
			condition[2] = values[1].trim();
		}

		if (whereCondition.contains(">")) {
			values = whereCondition.split(">");
			condition[0] = values[0].trim();
			condition[1] = ">";
			condition[2] = values[1].trim();
		}

		if (whereCondition.contains("<")) {
			values = whereCondition.split("<");
			condition[0] = values[0].trim();
			condition[1] = "<";
			condition[2] = values[1].trim();
		}

		if (whereCondition.contains(">=")) {
			values = whereCondition.split(">=");
			condition[0] = values[0].trim();
			condition[1] = ">=";
			condition[2] = values[1].trim();
		}

		if (whereCondition.contains("<=")) {
			values = whereCondition.split("<=");
			condition[0] = values[0].trim();
			condition[1] = "<=";
			condition[2] = values[1].trim();
		}

		if (whereCondition.contains("<>")) {
			values = whereCondition.split("<>");
			condition[0] = values[0].trim();
			condition[1] = "<>";
			condition[2] = values[1].trim();
		}

		return condition;
	}
	public static boolean isTablePresent(String dir, String tableName) {
		String filename = new String(tableName+".tbl");
		
		File Dir = new File(dir);
		String[] tablenames = Dir.list();
		for (String table : tablenames) {
			if(filename.equals(table))
				return true;
		}
		return false;
	}
}
