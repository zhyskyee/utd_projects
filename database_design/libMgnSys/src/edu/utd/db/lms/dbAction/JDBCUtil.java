package edu.utd.db.lms.dbAction;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Properties;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.csvreader.CsvReader;

/***********************************************
* @author hxz174130@utdallas.edu
* 
* @date Feb 24, 2018 6:18:32 PM
* 
***********************************************/
public class JDBCUtil {
	private static String url = null;
	private static String driverClass = null;
	private static String user = null;
	private static String password = null;
	
	static {
		try {
			Properties prop = new Properties();
			InputStream inStream = JDBCUtil.class.getResourceAsStream("/db.properties");
			//Loading
			prop.load(inStream);
			//read
			url = prop.getProperty("url");
			driverClass = prop.getProperty("driverClass");
			user = prop.getProperty("user");
			password = prop.getProperty("password");
			//reg
			Class.forName(driverClass);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println("Error: failed to reg JDBC driver.");
		}
	}
	
	/*
	 * connect db
	 * */
	public static Connection getConnection() {
		try {
			Connection connection = DriverManager.getConnection(url, user, password);
			return connection;
		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	/*
	 * close db
	 * */
	public static void close(Connection conn, Statement stmt, ResultSet rSet) {
		if (rSet != null) {
			try {
				rSet.close();
			} catch (SQLException e) {
				// TODO: handle exception
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				// TODO: handle exception
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO: handle exception
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
	
	private static Boolean executePreStmt(PreparedStatement ps)
	{
		try {
			ps.execute();
			return true;
		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		} 
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}
	
	private static Boolean executeStmt(Statement s, String sql)
	{
		try {
			s.executeUpdate(sql);
			return true;
		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println("catch SQLEx!");
		} 
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}
	private String subString_WrapChar(String s, String element) {
		
		if (s.length() == 0)
		{
			return s;
		}
		
		int beginIndex = s.indexOf(element) == 0 ? 1 : 0;  
        int endIndex = s.lastIndexOf(element) + 1 == s.length() ? s.lastIndexOf(element) : s.length(); 
        return s.substring(beginIndex, endIndex);  
	}
	private String preProcessStr(String s) {
		s = s.trim();
		s = subString_WrapChar(s, "\"");
		s = subString_WrapChar(s, "'");
		return s;
	}
	/*
	 * input: Book.csv filePath
	 * */
	public long initBook(String csvFilePath) {
		String createBookSql = "CREATE TABLE BOOK" +
			                   "(Isbn VARCHAR(10)," +
			                   "Isbn13 CHAR(13) UNIQUE," +
			                   "Title VARCHAR(255)," + 
			                   "Cover VARCHAR(255)," +
			                   "Publisher VARCHAR(255)," +
			                   "Page INTEGER," +
			                   "AvailNum INTEGER DEFAULT 1," +
			                   "PRIMARY KEY(Isbn))";
		String createAuthorSql = "CREATE TABLE AUTHOR" +
				                "(Author_id INTEGER NOT NULL AUTO_INCREMENT," +
				                "Name VARCHAR(255) UNIQUE DEFAULT 'N/A'," + 
				                "PRIMARY KEY(Author_id)" +
				                ")";
		String createBookAuthorSql = "CREATE TABLE BOOK_AUTHOR" +
					                "(Auth_id INTEGER NOT NULL," +
					                "Isbn VARCHAR(10) NOT NULL," + 
					                "PRIMARY KEY(Auth_id,Isbn)," +
					                "CONSTRAINT fk_baAuth FOREIGN KEY(Auth_id) REFERENCES AUTHOR(Author_id) ON DELETE CASCADE ON UPDATE CASCADE," +
					                "CONSTRAINT fk_baIsbn FOREIGN KEY(Isbn) REFERENCES BOOK(Isbn) ON DELETE CASCADE ON UPDATE CASCADE)";
		
		String insertBookSql = "INSERT INTO BOOK(Isbn, Isbn13, Title, Cover, Publisher, Page) VALUES(?,?,?,?,?,?)";
		String insertAuthorSql = "INSERT INTO AUTHOR(Name) VALUES(?)";
		String insertBASql = "INSERT INTO BOOK_AUTHOR(Auth_id, Isbn) VALUES(?,?)";
		
		String isExistBookSql = "SELECT Isbn FROM BOOK WHERE Isbn=";
		String isExistAIdSql = "SELECT Author_id FROM AUTHOR WHERE Name=";
		
		try {
			Connection con = JDBCUtil.getConnection();
			if(con == null)
			{
				System.out.println("Error here!!");
				throw new SQLException();
			}
			Statement stmt = null;
			stmt = con.createStatement();
			/* if table exists, thread wont shutdown due to exception */
			executeStmt(stmt, createBookSql);
			executeStmt(stmt, createAuthorSql);
			executeStmt(stmt, createBookAuthorSql);
	
			/*read csv file*/
			PreparedStatement pBookStmt = (PreparedStatement) con.prepareStatement(insertBookSql);
			PreparedStatement pAuthorStmt = (PreparedStatement) con.prepareStatement(insertAuthorSql, Statement.RETURN_GENERATED_KEYS);
			PreparedStatement pBAStmt = (PreparedStatement) con.prepareStatement(insertBASql);
			
			/* insert nothing to make up a default author started form 1. */
			executeStmt(stmt, "INSERT INTO AUTHOR(Author_id) VALUES(1)");
			ArrayList<String[]> csvList = new ArrayList<String[]>(); //store data
			ResultSet rs = null;
			int author_id = -1;
			int readRecordCnt = 0;
			/*create csvreader*/
			/* Book.csv format:
			 * ISBN10	ISBN13	Title	Authro	Cover	Publisher	Pages */
			CsvReader reader = new CsvReader(csvFilePath, '	', Charset.forName("UTF-8"));
			/* skip the header */
			reader.readHeaders();
			/* begin reading */
			while (reader.readRecord()) {
				csvList.add(reader.getValues());
				
				String Isbn = preProcessStr(csvList.get(0)[0]); 	//ISBN10		
				String Isbn13 = preProcessStr(csvList.get(0)[1]); 	//ISBN13				
				String Title = preProcessStr(csvList.get(0)[2]); 		//Title				
				String Authors = preProcessStr(csvList.get(0)[3]); 	//Author				
				String Cover = preProcessStr(csvList.get(0)[4]); 		//Cover				
				String Publisher = preProcessStr(csvList.get(0)[5]); 	//Publisher				
				int Pages = Integer.parseInt(preProcessStr(csvList.get(0)[6])); 		//Pages
				
				rs = stmt.executeQuery(isExistBookSql + "'" + Isbn.replace("'","‘'") + "'");
				if (rs.next()) {
					/* duplicate record, abandon it */
					/* rm to save memory */
					csvList.remove(csvList.get(0));
					continue;
				}
				
				pBookStmt.setString(1, Isbn);
				pBookStmt.setString(2, Isbn13);
				pBookStmt.setString(3, Title);
				pBookStmt.setString(4, Cover);
				pBookStmt.setString(5, Publisher);
				pBookStmt.setInt(6, Pages);
				
				executePreStmt(pBookStmt);
				
				String[] author = Authors.split(",");
				
				for (int i = 0; author.length == 0 || i < author.length; i++) {
					String queryAuthorName = (author.length == 0 ? "N/A" : author[i]);
					/* if there is a author id. */
					rs = stmt.executeQuery(isExistAIdSql + "'" + queryAuthorName.replace("'", "''") + "'");
					author_id = -1;
					if (rs.next())
					{
						/* exits iff one record */
						author_id = rs.getInt(1);
					}
					else 
					{
						pAuthorStmt.setString(1, author[i]);
						executePreStmt(pAuthorStmt);
						try {
							rs = pAuthorStmt.getGeneratedKeys();
						} catch (SQLException e) {
							// TODO: handle exception
							e.printStackTrace();
						}
						
						if (rs.next()) {
							author_id = rs.getInt(1);
						}
					}
					
					if(author_id != -1)
					{
						pBAStmt.setInt(1, author_id);
						pBAStmt.setString(2, Isbn);
						executePreStmt(pBAStmt);
					}
					
					if (author.length == 0) 
					{
						/* only loop once */
						break;
					}
				}
				
				/* rm to save memory */
				csvList.remove(csvList.get(0));
				readRecordCnt++;
			}
			reader.close();
			close(con, stmt, rs);
			return readRecordCnt;
		} catch (SQLException e) {
			// TODO: handle exception
			System.out.println("Error in manipulating Database!");
			e.printStackTrace();
			
			throw new RuntimeException();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("UnHandle Exception!!!");
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	public long initBorrower(String csvFilePath) {
		String createBorowerSql = "CREATE TABLE BORROWER " +
			                   "(Card_id INTEGER," +
			                   "Ssn CHAR(11) UNIQUE," +
			                   "Bname VARCHAR(255) DEFAULT 'N/A'," + 
			                   "Email VARCHAR(255) DEFAULT 'N/A'," +
			                   "Address VARCHAR(255) DEFAULT 'N/A'," +
			                   "Phone VARCHAR(14) DEFAULT 'N/A'," +
			                   "PRIMARY KEY(Card_id))";
		String createBookLoanSql = "CREATE TABLE BOOK_LOAN " +
				                "(Loan_id INTEGER NOT NULL AUTO_INCREMENT," +
				                "Isbn VARCHAR(10) NOT NULL," + 
				                "Card_id INTEGER NOT NULL," +
				                "Date_out DATE NOT NULL," +
				                "Due_Date DATE NOT NULL," +
				                "Date_in DATE," +
				                "PRIMARY KEY(Loan_id)," +
				                "CONSTRAINT fk_BLIsbn FOREIGN KEY(Isbn) REFERENCES BOOK(Isbn) " +
				                "ON DELETE NO ACTION ON UPDATE CASCADE," +
				                "CONSTRAINT fk_BLCardId FOREIGN KEY(Card_id) REFERENCES BORROWER(Card_id) " +
				                "ON DELETE NO ACTION ON UPDATE CASCADE" +
				                ")";
		String createFineSql = "CREATE TABLE FINE " +
					           "(Loan_id INTEGER NOT NULL," +
					           "Fine_amt DECIMAL(10,2)," + 
					           "Paid BOOL DEFAULT 0, "+
					           "PRIMARY KEY(Loan_id)," +
					           "CONSTRAINT fk_fineLid FOREIGN KEY(Loan_id) REFERENCES BOOK_LOAN(Loan_id) " +
					           "ON DELETE NO ACTION ON UPDATE CASCADE)";
		
		String insertBorrowerSql = "INSERT INTO BORROWER(Card_id, Ssn, Bname, Email, Address, Phone) VALUES(?,?,?,?,?,?)";
		
		String isExistBorrowerSql = "SELECT Card_id FROM BORROWER WHERE Card_id=";
		
		try {
			Connection con = JDBCUtil.getConnection();
			if(con == null)
			{
				System.out.println("Error here!!");
				throw new SQLException();
			}
			
			Statement stmt = null;
			stmt = con.createStatement();
			/* if table exists, thread wont shutdown due to exception */
			executeStmt(stmt, createBorowerSql);
			executeStmt(stmt, createBookLoanSql);
			executeStmt(stmt, createFineSql);
	
			/*read csv file*/
			PreparedStatement pBorrowerStmt = (PreparedStatement) con.prepareStatement(insertBorrowerSql);
			
			ArrayList<String[]> csvList = new ArrayList<String[]>(); //store data
			ResultSet rs = null;
			int readRecordCnt = 0;
			/*create csvreader*/
			/* Borrower.csv format:
			 * borrower_id	ssn	first_name	last_name	email	address	city	 state	phone */
			CsvReader reader = new CsvReader(csvFilePath, ',', Charset.forName("UTF-8"));
			/* skip the header */
			reader.readHeaders();
			/* begin reading */
			while (reader.readRecord()) {
				csvList.add(reader.getValues());
				
				int card_id = Integer.parseInt(preProcessStr(csvList.get(0)[0])); 		//card_id				
				String ssn = preProcessStr(csvList.get(0)[1]); 			//ssn				
				String bname = null;
				String fname = preProcessStr(csvList.get(0)[2]);
				String lname = preProcessStr(csvList.get(0)[3]);
				if (!fname.equals("") && fname != null && !lname.equals("") && lname != null) {
					bname = String.join(" ", fname, lname); 		//firstName + LastName
				}
				
				String email = preProcessStr(csvList.get(0)[4]); 		//email				
				String addr = preProcessStr(csvList.get(0)[5]);
				String city = preProcessStr(csvList.get(0)[6]);
				String state = preProcessStr(csvList.get(0)[7]);
				String address = null; 
				if (!addr.equals("") && addr != null && !city.equals("") 
						&& city != null && !state.equals("") && state != null)
				{
					address = String.join(",", addr, city, state);	//address=address+city+state
				}
				
				String phone = preProcessStr(csvList.get(0)[8]); 		//phone
				
				rs = stmt.executeQuery(isExistBorrowerSql + card_id);
				if (rs.next()) {
					/* duplicate record, abandon it */
					/* rm to save memory */
					csvList.remove(csvList.get(0));
					continue;
				}
				
				pBorrowerStmt.setInt(1, card_id);
				pBorrowerStmt.setString(2, ssn);
				pBorrowerStmt.setString(3, bname);
				pBorrowerStmt.setString(4, email);
				pBorrowerStmt.setString(5, address);
				pBorrowerStmt.setString(6, phone);			
				executePreStmt(pBorrowerStmt);
				/* rm to save memory */
				csvList.remove(csvList.get(0));
				readRecordCnt++;
			}
			reader.close();
			close(con, stmt, rs);
			return readRecordCnt;
		} catch (SQLException e) {
			// TODO: handle exception
			System.out.println("Error in manipulating Database!");
			e.printStackTrace();
			throw new RuntimeException();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("UnHandle Exception!!!");
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
}
