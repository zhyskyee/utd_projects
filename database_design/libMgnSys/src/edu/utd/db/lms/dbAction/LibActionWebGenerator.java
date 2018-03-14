package edu.utd.db.lms.dbAction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LibActionWebGenerator {
    
	public LibActionWebGenerator() {
        jdbcUtil = new JDBCUtil();
    		conn_init();
    }
    
    JDBCUtil jdbcUtil;
    Connection conn;
    Statement st;
    int tabNum;
    boolean enableTab;
    int currentPage; 
    int pageSize; 
    int totalPage; 
    int previousPage; 
    int nextPage; 
    int columnCount; 
    int totalRecord; 
    String tableTitle; 
    String currentPageUrl;
    boolean sort; //sort dynamically
    int sortType;
    String desc="▼";
    String asc="▲";
    String sortColumn;
    boolean search;
    String searchType;
    String searchCondition;
    boolean bfilter;
    int sumType;
    int paidType;
    boolean AddRow;
	private int role = 0; /* 0=>student 1=>librarian */
	private int actionType = 0; /* role = 1: 0=>CheckOut, 1=>Edit_Fine_amt */
    
    private void conn_init() {
    		conn = JDBCUtil.getConnection();
        setStatement();
    }

    private void setStatement() {
        try {
            st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public String tryCheckOut(String Isbn, String Card_id) {
    		ResultSet rs;
    		int card_id = Integer.parseInt(Card_id);
    		int availableNumer = 0;
    		try {
			rs = this.st.executeQuery("SELECT AvailNum FROM BOOK WHERE Isbn='" + Isbn + "'");
			if(rs.next()) {
				
				if ((availableNumer = rs.getInt(1)) == 0) { /* book is been checkout */
					return "The book has been checked out!";
				}
				
				rs = this.st.executeQuery("SELECT COUNT(*) FROM BORROWER WHERE Card_id=" + card_id);
				if(rs.next()) {
					if(rs.getInt(1) == 0) {
						//borrower not exist!
						return "The Card_id does not exist, please vertify it!";
					}
					
					//rs = this.st.executeQuery("SELECT FINE.Paid FROM (SELECT * FROM BOOK WHERE Isbn='"+Isbn+"') AS BOOK "
					//					   + "JOIN (SELECT * FROM BOOK_LOAN WHERE Card_id="+card_id+") AS BOOK_LOAN "
					//					   + "JOIN (SELECT * FROM FINE WHERE Paid=0) AS FINE");
					//if(rs.next()) { 
						/*
						 * The borrower checked out this book before 
						 * and didn't return it on time 
						 * and does not pay for the fine*/ 
					//	return "Can not check this book out before the payment of the student of this book is done!";
					//}
					
					rs = this.st.executeQuery("SELECT COUNT(*) "
										+ "FROM (Select BOOK_LOAN.Date_in "
										+ "FROM BORROWER JOIN BOOK_LOAN ON BORROWER.Card_id=BOOK_LOAN.Card_id "
										+ "WHERE BORROWER.Card_id="+card_id+" AND BOOK_LOAN.Date_in is null) AS BwrLoanCount");
					if(rs.next()) {
						if(rs.getInt(1) >= 3) {
							//This borrower borrow too much!
							return "The student has already reached his/her checkout limit!";
						} 
						//all check is OK
						//create a new tuple for this checkout
						Date date = new Date();
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(date);
						calendar.add(Calendar.DAY_OF_MONTH, 14);
						Date dueDate = calendar.getTime();
						java.sql.Date sqlCurDate = new java.sql.Date(date.getTime());
						java.sql.Date sqlDueDate = new java.sql.Date(dueDate.getTime());
						try {
							/* create a new book_loan */
							this.st.execute("INSERT INTO BOOK_LOAN(Isbn,Card_id,Date_out,Due_date) "
									+ "VALUES('"+Isbn+"',"+card_id+",'"+sqlCurDate+"','"+sqlDueDate+"')");
							
							/* update the available number */
							this.st.execute("UPDATE BOOK SET AvailNum="+(availableNumer-1)+" WHERE Isbn='" + Isbn + "'");
							
							return "Check out successfully!";
						} catch (Exception e) {
							// TODO: handle exception
							return e.getMessage();
						}	
					} else {
						/* Unknown error, for SQL can not get the count */
						return "Unknown error!";
					}
				} else {
					/* Unknown error, for SQL can not get the count */
					return "Unknown error!";
				}
			} else {
				//no such book
				return "The book does not exist!";
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return e.getMessage();
		}
    }
    
    public String tryCheckIn(String Card_id, String Isbn) {
    		ResultSet rs;
		int card_id = Integer.parseInt(Card_id);
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		/*clear h:m:s.ms*/
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		date = calendar.getTime();
		Date dueDate = new Date();
		java.sql.Date sqlDate = new java.sql.Date(date.getTime());
		try {
			//Loan_id | Isbn  | Card_id | Date_out   | Due_Date   | Date_in
			rs = this.st.executeQuery("SELECT * "
									+ "FROM BOOK_LOAN "
									+ "WHERE BOOK_LOAN.Isbn='" + Isbn + "' "
									+ "AND BOOK_LOAN.Card_id=" + card_id + " "
									+ "AND BOOK_LOAN.Date_in is null");
			if (rs.next()) {
				/*get the loan id*/
				int loan_id = rs.getInt(1);
				dueDate = rs.getDate(5);
				this.st.execute("UPDATE BOOK_LOAN SET Date_in='"+sqlDate+"' WHERE Loan_id="+loan_id);
				rs = this.st.executeQuery("SELECT AvailNum FROM BOOK WHERE Isbn='"+Isbn+"'");
				
				if (rs.next()) {
					int availnum = rs.getInt(1);
					this.st.execute("UPDATE BOOK SET AvailNum="+(availnum+1)+" WHERE Isbn='"+Isbn+"'");
				}
				
				/*get the due Date col*/
				if(date.after(dueDate)) {
					/* pass the due, create a new fine record */
					int diffDays = (int) ((date.getTime() - dueDate.getTime()) / (24*3600*1000));
					float fine_amt = (float) (((float)diffDays) * 0.25);
					this.st.execute("UPDATE FINE SET Fine_amt="+fine_amt+" WHERE Loan_id="+loan_id);
				}
				return "Check in successfully!";
			} else {
				//could not find the unreturned record;
				return "Unknown error!";
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}
    }
    
    public String payOneFine(String Isbn, String Card_id) {
    		String sql = "SELECT * FROM BOOK_LOAN JOIN FINE ON BOOK_LOAN.Loan_id=FINE.Loan_id"
    					+ " WHERE BOOK_LOAN.Isbn='"+Isbn+"' AND BOOK_LOAN.Card_id="+
    					Card_id+" AND FINE.Paid=0";
    		Statement st2 = null;
    		try {
    			st2 = this.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    		} catch (SQLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			return e.getMessage();
    		}
    		
    		ResultSet  rSet;
    		try {
				rSet = this.st.executeQuery(sql);
				if (rSet.next()) { //the Fine has been updated
					Date date_in  = rSet.getDate(6);
					int Loan_id = rSet.getInt(1);
					if(date_in == null || 
					   date_in.getTime() == 0 || 
					   date_in.toString().equals("") ||
					   date_in.toString().equals("null"))
					{
						st2.close();
						return Isbn+": Failed to pay! Because the book has not returned yet!";
					}
					else
					{//the book is return!!!,only set the paid flag to true would be OK!
						//Update fine_amt;
						st2.execute("UPDATE FINE SET Paid=1 WHERE Loan_id="+Loan_id);
						st2.close();
						return Isbn+": Payment OK!";
					}
				} else {
					st2.close();
					return Isbn+": Can not find this fine record in the database!";
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					st2.close();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return Isbn+": "+e.getMessage();
			}
    }
    
    public String payFines(String Card_id) {
    		String sql = "SELECT * FROM BOOK_LOAN JOIN FINE ON BOOK_LOAN.Loan_id=FINE.Loan_id"
				+ " WHERE BOOK_LOAN.Card_id="+Card_id+" AND FINE.Paid=0";
    		Statement tmpst = null;
    		try {
    			tmpst = this.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    		} catch (SQLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			return e.getMessage();
    		}
    		
    		ResultSet resultSet;
    		try {
				resultSet = tmpst.executeQuery(sql);
				String Isbn = "";
				String totalResStr = "";
				while (resultSet.next()) {
					Isbn = resultSet.getString(2); //Get the Isbn
					totalResStr += payOneFine(Isbn, Card_id);
				}
				return totalResStr;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return Card_id+": "+e.getMessage();
			}
	}
    
   
    public String AddNewBorrower(List<String> l) {
    	
		String sql = "INSERT INTO BORROWER(Card_id, Ssn, Bname, Email, Address, Phone) VALUES('"
					+l.get(0)+"','"+l.get(1)+"','"+l.get(2)+"','"+l.get(3)+"','"+l.get(4)+"','"+l.get(5)+"')";
		
		try {
			this.st.execute(sql);
	    		return "Add the borrower successfully!";
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		} catch (Exception e) {
			// TODO: handle exception
			return e.getMessage();
		}
    }
    
    
    public String updateFines() {
    		Statement st2 = null;
    		Statement st3 = null;
    		Date curDate = new Date();
    		//Update fine_amt;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(curDate);
		/*clear h:m:s.ms*/
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		curDate = calendar.getTime();
		
		String Need2UpdSql = "SELECT * FROM BOOK_LOAN WHERE Date_in is null OR Date_in > Due_date";
		try {
			st2 = this.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			st3 = this.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}
		
		ResultSet rSet;
		ResultSet rSet2;
		
		try {
			rSet = this.st.executeQuery(Need2UpdSql);
			while (rSet.next()) {
				Date dueDate = rSet.getDate(5);
				Date date_in = rSet.getDate(6);
				int loan_id = rSet.getInt(1); //loan_id
				String isLoanExist = "SELECT * FROM FINE WHERE Loan_id="+loan_id;
				
				rSet2 = st2.executeQuery(isLoanExist);		
				if(rSet2.next()) {
					boolean paid = rSet2.getBoolean(3);//paid item
					if(paid == false)
					{
						//there is already a existing fine, just to update the amount
						Date date2cal = new Date();
						float fine_amt = rSet2.getFloat(2); //get the fine amount;
						date2cal = (date_in != null ? date_in : curDate);
						/*get the due Date col*/
						if(date2cal.after(dueDate)) {
							/* pass the due, create a new fine record */
							int diffDays = (int) ((date2cal.getTime() - dueDate.getTime()) / (24*3600*1000));
							float fine_amt_now = (float) (((float)diffDays) * 0.25);
							if(fine_amt != fine_amt_now) {
								st3.execute("UPDATE FINE SET Fine_amt="+fine_amt_now+" WHERE Loan_id="+loan_id);
							}
						}
					}	
				} else { //no such loan_id
					//create a new one
					Date date2cal = new Date();
					date2cal = (date_in != null ? date_in : curDate);
					if (date2cal.after(dueDate)) {
						/* pass the due, create a new fine record */
						int diffDays = (int) ((date2cal.getTime() - dueDate.getTime()) / (24*3600*1000));
						float fine_amt_now = (float) (((float)diffDays) * 0.25);
						st3.execute("INSERT INTO FINE(Loan_id,Fine_amt) VALUES("+loan_id+","+fine_amt_now+")");
					}
				}	
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			
			return e1.getMessage();
		}
		
		try {
			st2.close();
			st3.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Update successfully!";
	}
    
    /**
     * 
     * @param sql select query: select [columns_name] from table_name ....
     * @param CurrentPage
     * @param PageSize 
     * @return  result into StringBuffer
     */
    public StringBuffer getResult_withTableFormat(String sql, int CurrentPage, int PageSize, boolean bEnableAction) {
        int rowNum = 0; //total rows
        int TotalPage = 0; //total pages
        int beginRow = 0; //the start row
        int endRow = 0; //the end row
        StringBuffer resultRows = new StringBuffer(""); //result, save as string
        this.pageSize = PageSize; //save the page size in bean
        
        try {
			rowNum = getTotalResultSetNum(sql);
			this.totalRecord = rowNum;
			
			TotalPage = ((rowNum % PageSize == 0) ? (rowNum / PageSize) : (rowNum / PageSize + 1));
			this.nextPage = ((CurrentPage >= TotalPage) ? (CurrentPage = TotalPage) : (CurrentPage + 1));
			this.previousPage = ((CurrentPage <= 1) ? (CurrentPage = 1) : (CurrentPage - 1));
			this.currentPage = CurrentPage;
			this.totalPage = TotalPage;
			
			beginRow = (CurrentPage - 1) * PageSize + 1;
			endRow = beginRow + PageSize;
			
			resultRows = getExecuteResult_withTableFormat(sql, beginRow, endRow, bEnableAction);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();  
		}
        return resultRows;
    }

    
    /**
     * 
     * @param sql 
     * @param startPos 
     * @param endPos 
     * @param bEnableAction 
     * @return turn the results into the buffer string
     */
    private StringBuffer getExecuteResult_withTableFormat
    						(String sql, int startPos, int endPos, boolean bEnableAction) {
        ResultSet rs;
        ResultSetMetaData rsmd; 
        int ColumnCount; 
        StringBuffer resultStr = new StringBuffer("");
        
        if(isSearch()) {
            resultStr.append(generateSearchForm(sql));
        }
        
        resultStr.append("<table border=1 align=center width=\"90%\">");
        try {
        	 	rs = st.executeQuery(sql);
            rsmd = rs.getMetaData();
            ColumnCount = rsmd.getColumnCount();
            this.columnCount = ColumnCount;
            resultStr.append("<tr>");
            /* 1 for the ID column, Action is the second column, except the two */
            resultStr.append("<td colspan=" + (ColumnCount + 1 + (bEnableAction?1:0)) + " align=center>");
            resultStr.append("<font size=6>" + getTableTitle() + "</font>");
            resultStr.append("</td>");
            resultStr.append("</tr>");
            resultStr.append("<tr>");
            
            for (int j = 0; j <= ColumnCount; j++) {
            		if (j == 0) {
            			/* First column save for Number column, auto-generate */
            			resultStr.append("<td><b>");
	                	resultStr.append("No.");
	            		resultStr.append("</b></td>");
	            		continue;
            		}
            		
            		String columnName = rsmd.getColumnName(j);
            		String curPageUrlNoParam = getCurrentPageURLWithTab(getCurrentPageUrl());
            		
        			//in fine page, we have to keep the parameter going as the user set
        			if(getSearchCondition() != null && getSearchCondition().equals("null")) {
                		curPageUrlNoParam = concatLinkWithParam(curPageUrlNoParam, "searchCondition", getSearchCondition());
    				}
    				if (getSearchType() != null && getSearchType().equals("null")) {
    					curPageUrlNoParam = concatLinkWithParam(curPageUrlNoParam, "searchType", getSearchType());
    				}
    				
	    			if (getTab()==4) {
	    				if (getSumType() != 0) {
	    					curPageUrlNoParam = concatLinkWithParam(curPageUrlNoParam, "sum", Integer.toString(getSumType()));
	    				}
	    				if (getPaidType() != 0) {
	    					curPageUrlNoParam = concatLinkWithParam(curPageUrlNoParam, "paid", Integer.toString(getPaidType()));
	    				}
            		}
            		
            		if (columnName.equals("Cover") || columnName.equals("AvailNum")) {
					//skip Cover column, do not have the sort ability
	                	resultStr.append("<td><b>");
	                	resultStr.append(columnName);
	            		resultStr.append("</b></td>");
                		continue;
				}
                
                	if (isSort()) {
                		String url = concatLinkWithParam(curPageUrlNoParam, "columnName",columnName);
                    if (getSortType() == 1) {
                        if (columnName.equals(getSortColumn())) {
                        		url = concatLinkWithParam(url, "sortType", "asc");
                            resultStr.append("<td align=left onclick=SortByThis('" + url + "')>");
                        } else {
                        		url = concatLinkWithParam(url, "sortType", "desc");
                            resultStr.append("<td align=left onclick=SortByThis('" + url +"')>");
                        }
                    }
                    
                    if (getSortType() == 2 || getSortType() == 0) {
                    		url = concatLinkWithParam(url, "sortType", "desc");
                    		resultStr.append("<td align=left onclick=SortByThis('" + url +"')>");
                    }                    
                }
                else {
                    resultStr.append("<td align=left>");
                }                
                resultStr.append("&nbsp;<b>");
                resultStr.append(columnName);

                if (getSortType() != 0 && columnName.equals(getSortColumn()))
                {
                    if (getSortType()==1) {
                        resultStr.append("/" + this.desc);
                    }
                    if (getSortType()==2) {
                        resultStr.append("/" + this.asc);
                    }
                }
                resultStr.append("</b></td>");
            }
           
            if (bEnableAction == true) {
                resultStr.append("<td>");
                resultStr.append("<b>Action</b>");
                resultStr.append("</td>");
            }
            resultStr.append("</tr>");
            
            int currentRecordNum = (this.currentPage - 1) * this.pageSize + 1;
            if (rs.next()) {
                rs.absolute(startPos);
                for (int i = 0; i < (endPos - startPos); i++) {
                    resultStr.append("<tr>");
                    String componentID = "";
                    for (int j = 0; j <= ColumnCount; j++) {
                        if (j == 0) {
							resultStr.append("<td align=left>&nbsp;");
							resultStr.append(currentRecordNum++);
							resultStr.append("</td>");
                        } else if (j == 1) {
                        		resultStr.append("<td align=left>&nbsp;");
							resultStr.append((componentID = rs.getString(j)));
							resultStr.append("</td>");
						} else {
							/* if it is the Cover col then use the Pic */
							if(rsmd.getColumnName(j).equals("Cover")) {
		                    		resultStr.append("<td align=center>");
		                    		resultStr.append("<img src=\"" + rs.getString(j) + "\" alt=\"NotFound\" width=\"56\" height=\"72\"/>"); //
		                    		resultStr.append("</td>");
		                    } else {
		                    	 	resultStr.append("<td align=left>&nbsp;");
		                    	 	resultStr.append(rs.getString(j));
		                    	 	resultStr.append("</td>");
							}
						}
                    }

                    if (bEnableAction == true) {
                    		resultStr.append("<td>");
						resultStr.append("<nobr>");
                    		switch (this.role) {
							case 1: //A librian's Action is provide the student, componentID = Isbn
								if (this.actionType == 0) {	//Checkout
									/* TIPS: Index(ColumnCount) = AvailNum */
									if (rs.getInt(ColumnCount) > 0) {
										String url = concatLinkWithParam(getCurrentPageURLWithTab(getCurrentPageUrl()), "IsbnCheckOut", componentID);
										if(getSearchCondition() != null && !getSearchCondition().equals("null")) {
											url = concatLinkWithParam(url, "searchCondition", getSearchCondition());
										}
										if (getSearchType() != null && !getSearchType().equals("null")) {
											url = concatLinkWithParam(url, "searchType", getSearchType());
										}
										resultStr.append("<a href=#"+" onclick=\"checkOut('"+ url + "&Card_id=');\"><font color=blue>Check Out</font></a>");
									} else {
										resultStr.append("<p><font color=\"#808080\">Check Out</font></p>");
									}
								} else if (this.actionType == 1) {					//check in
									/* TIPS: Index(3) = Card_id; Index(ColumnCount) = Date_in */
									String card_id = Integer.toString(rs.getInt(3));
									String title = rs.getString(2);
									String dateIn = rs.getString(ColumnCount);
									if (dateIn == null || dateIn.equals("") || dateIn.equalsIgnoreCase("null")) {
										String url = concatLinkWithParam(getCurrentPageURLWithTab(getCurrentPageUrl()), "IsbnCheckIn", componentID);
										url = concatLinkWithParam(url, "Card_id", card_id);
										if(getSearchCondition() != null && !getSearchCondition().equals("null")) {
											url = concatLinkWithParam(url, "searchCondition", getSearchCondition());
										}
										if (getSearchType() != null && !getSearchType().equals("null")) {
											url = concatLinkWithParam(url, "searchType", getSearchType());
										}
										resultStr.append("<a href=#"+" onclick=\"checkIn('"+url+"','"+title+"','"+card_id+"');\">"
														+ "<font color=blue>Return</font></a>");
									} else {
										resultStr.append("<p><font color=\"#808080\">Returned</font></p>");
									}
								} else if (this.actionType == 2) { //pay
									if(getTab() == 4)
									{
										String httpurl = getCurrentPageURLWithTab(getCurrentPageUrl());
										if(getSearchCondition() != null && !getSearchCondition().equals("null")) {
											httpurl = concatLinkWithParam(httpurl, "searchCondition", getSearchCondition());
										}
										if (getSearchType() != null && !getSearchType().equals("null")) {
											httpurl = concatLinkWithParam(httpurl, "searchType", getSearchType());
										}
										if (getSumType() != 0) {
											httpurl = concatLinkWithParam(httpurl, "sum", Integer.toString(getSumType()));
										}
										if (getPaidType() != 0) {
											httpurl = concatLinkWithParam(httpurl, "paid", Integer.toString(getPaidType()));
										}
										
										if (getSumType() == 0) { //each
											String Isbn = rs.getString(1); //Isbn item
											String Card_id = rs.getString(2); //Card_id item
											boolean bPaid = rs.getBoolean(ColumnCount); //paid item
											
											if(bPaid == true) {
												resultStr.append("<p><font color=\"#808080\">Paid</font></p>");
											} else {
												String url = concatLinkWithParam(httpurl, "Isbn", Isbn);
												url = concatLinkWithParam(url, "Fine_Cid", Card_id);
												resultStr.append("<a href="+url+"><font color=blue>To pay</font></a>");
											}
										} else { //sum
											String Card_id = rs.getString(2);// card_id item
											boolean bPaid = rs.getBoolean(ColumnCount); //Paid item
											if (getPaidType() != 0) {
												if(bPaid == true) {
													resultStr.append("<p><font color=\"#808080\">Paid</font></p>");
												} else {
													String url = concatLinkWithParam(httpurl, "Fine_Cid", Card_id);
													resultStr.append("<a href="+url+"><font color=blue>To pay</font></a>");
												}
											} else { //sum with all, you can not do anything.
												resultStr.append("<p><font color=\"#808080\">N/A</font></p>");
											}
										}
									}
								}
								
								break;
							case 0:
								//TODO: student review his own records.
								break;
							default:
								break;
							}
                        
                        resultStr.append("</nobr>");
                        resultStr.append("</td>");
                    }
                    resultStr.append("</tr>");
                    rs.next();
                    if (rs.isAfterLast()) {
                        break;
                    }
                }
            }
            resultStr.append("<tr>");
            resultStr.append("<td colspan=" + (ColumnCount + 1 + (bEnableAction?1:0)) + " align=center>");
            resultStr.append(setTurnPageString(getCurrentPageURLWithTab(getCurrentPageUrl())));
            resultStr.append("</td>");
            resultStr.append("</tr>");
            
            if (isAddRow()) {
	        		List<String> list = new ArrayList<String>();
	        		for (int i = 1; i <= ColumnCount; i++) {
	        			try {
							list.add(rsmd.getColumnName(i));
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
	        		resultStr.append("<tr>");
	        		resultStr.append("<td style=\"border-right-style:none;border-left-style:none;border-top-style:none;border-bottom-style:none\" colspan=" + (ColumnCount + 1 + (bEnableAction?1:0)) + " align=left>");
	        		resultStr.append(generateAddRow(list));
	        		resultStr.append("</td>");
	            resultStr.append("</tr>");
            }
            resultStr.append("</table>");
            resultStr.append(getJSFunction());                
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return resultStr;
    }
    
    
    private StringBuffer generateAddRow(List<String> l) {
    		StringBuffer resultStr = new StringBuffer("");
    		String divideS = "&nbsp;&nbsp;&nbsp;&nbsp;";
    		//resultStr.append("<br/>");
    		resultStr.append("<form method=get action=" + getCurrentPageURLWithoutParameteor(getCurrentPageUrl()) + " >"); //onsubmit=\"return validate();\"
    		if (isTabEnable())
         {	/* form with a hidden tab parame */
    			resultStr.append("<input type=hidden name=tab value="+getTab()+">");
         }
    		
    		resultStr.append(divideS);
    		
    		String text_type = "";
    		//under fine management, we dont mean to add a new row, just to add a btn to update fines
    		for (int i = 0; getTab() != 4 && i < l.size(); i++) {
    			if(l.get(i).contains("Email")) {
    				text_type = "email";
    			} else if (l.get(i). contains("Phone")) {
    				text_type = "tel";
    			} else if (l.get(i).contains("Card_id")) {
    				text_type = "number";
    			} else {
    				text_type = "text";	
			}
    			resultStr.append(l.get(i)+": <input type="+text_type+" required=\"required\" name="+ l.get(i) + " style=\"width:100px; height:20px;\" >&nbsp;&nbsp;");
		}
    		
    		if(getTab() == 4) {
    			resultStr.append("Update all fines right: <input type=submit name=update value=UpdateFines style=\"width:100px;height:25px;font-size:20px;\">");
    			resultStr.append("<input type=hidden name=sum value="+getSumType()+">");
    			resultStr.append("<input type=hidden name=paid value="+getPaidType()+">");
    		} else {
        		resultStr.append("&nbsp;&nbsp;<input type=submit name=addRow value=\"Add new row\" style=\"width:100px;height:25px;font-size:20px;\">");
    		}
    		
    		resultStr.append("</form>");
    		return resultStr;
    }
    
    /**
     * 
     * @param sql: select .. from ...
     * @return 
     */
    private StringBuffer generateSearchForm(String sql) {
        StringBuffer str = new StringBuffer("");
        String searchType = getSearchType();
        ResultSet rs;
        
        try {
            rs = st.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            int ColumnCount = rsmd.getColumnCount();
            String searchCondition = getSearchCondition();
            if (searchCondition == null)
                searchCondition = "";
            str.append("<br/>");
            str.append("<form style=\"margin-left:5%;\" method=get action=" + getCurrentPageURLWithoutParameteor(getCurrentPageUrl()) + ">");
            str.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Search content: <input type=text name=searchCondition "
            			+ "style=\"width:200px; height:20px;\" value=\"" + searchCondition + "\"> ");
           
            if (isTabEnable())
            {	/* form with a hidden tab param */
            		str.append("<input type=hidden name=tab value="+getTab()+">");
            }
            
            str.append("&nbsp;&nbsp;&nbsp;&nbsp;Type: <select name=searchType style=\"width:150px; height:20px;\">");
            
            if (getTab() != 4) { //if fine mgn, just need one option
            		if (getTab() != 2) { //book loan does use the all option, need to specify one
            			str.append("<option value=null>please pick one</option>");
				}
	            for(int i = 1; i <= ColumnCount; i++) {                
	                String cName = rsmd.getColumnName(i);
	                if (searchType != null && searchType.equals(cName)) {
	                    str.append("<option value=" + cName + " selected>" + cName + "</option>");
	                } else {
	                		if (!cName.equals("Cover") && 
	                			!cName.equals("Page") && 
	                			!cName.equals("AvailNum")) {
	                				str.append("<option value=" + cName + ">" + cName + "</option>");
						}
	                }
	            }
            } else {
            		/* fine management, only using Card_id to search */
				str.append("<option value=Card_id selected>Card_id</option>");
			}
            str.append("</select>");
            
            if(getTab() != 4) {
            		str.append("&nbsp;&nbsp;&nbsp;&nbsp;<input type=submit value=SEARCH style=\"width:100px;height:25px;font-size:20px;\">");
            }
            
            if (getTab() == 4) {
            		String[] sumStr = {"Each", "Sum"};
            		String[] paidStr = {"All", "Paid", "Unpaid"};
            		int sumTypeSel = getSumType();
            		int paidTypeSel = getPaidType();
            		
            		str.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Amount Type: <select name=sum style=\"width:100px; height:20px;\">");
            		for (int i = 0; i < sumStr.length; i++) {
					if (i == sumTypeSel) {
						str.append("<option value="+i+" selected>"+sumStr[i]+"</option>");
					} else {
						str.append("<option value="+i+">"+sumStr[i]+"</option>");
					}
				}
            		str.append("</select>");
            		str.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Paid Type: <select name=paid style=\"width:100px; height:20px;\">");
            		for (int i = 0; i < paidStr.length; i++) {
            			if (i == paidTypeSel) {
    						str.append("<option value="+i+" selected>"+paidStr[i]+"</option>");
    					} else {
    						str.append("<option value="+i+">"+paidStr[i]+"</option>");
    					}
				}
            		str.append("</select>");
            		str.append("&nbsp;&nbsp;&nbsp;&nbsp;<input type=submit value=APPLY style=\"width:100px;height:25px;font-size:20px;\">");
			}
            
            str.append("<br/><br/>");
            str.append("</form>");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return str;
    }
    
    private StringBuffer getJSFunction() {
    		StringBuffer str=new StringBuffer("");        
        str.append("<script language=JavaScript1.2 type=text/javascript>");
        str.append("function SortByThis(url){window.location=url;}");
        str.append("function goToPage(httpUrl){var page=document.getElementById(\"goToPage\").value;var url=httpUrl+page;window.location=url;}");
        str.append("function checkOut(httpUrl){var cid=prompt(\"Please enter the student's card_id:\",\"\"); var url=httpUrl+cid;window.location=url;}");
        str.append("function checkIn(url,title,card_id){var msg = \"Are you sure to return the book(\"+title+\") for the student(\"+card_id+\")?\"; if (confirm(msg) == true) {window.location.href=url;} else {return false;}}");
        str.append("</script>");
        return str;
    }
    /**
     * 
     * @param httpUrl SetWithParamter of Pages
     * @return String of the row about pages info
     */
    private String setTurnPageString(String httpUrl) {
        String httpURL = new String(httpUrl);
    		String turnPageStrng = "";
        String divideS = "&nbsp;&nbsp;&nbsp;&nbsp;";
        turnPageStrng = "TotalRecords:" + totalRecord;
        //turnPageStrng = turnPageStrng + divideS + "TotalPage:" + totalPage;
        turnPageStrng = turnPageStrng + divideS + "PageSize:" + pageSize;
        
        if(getSearchCondition() != null && !getSearchCondition().equals("null")) {
        		httpURL = concatLinkWithParam(httpURL, "searchCondition", getSearchCondition());
		}
		if (getSearchType() != null && !getSearchType().equals("null")) {
			httpURL = concatLinkWithParam(httpURL, "searchType", getSearchType());
		}
		if(getTab()==4) {
			if (getSumType() != 0) {
			httpURL = concatLinkWithParam(httpURL, "sum", Integer.toString(getSumType()));
			}
			if (getPaidType() != 0) {
				httpURL = concatLinkWithParam(httpURL, "paid", Integer.toString(getPaidType()));
			}
		}
		
		httpURL = concatLinkWithParam(httpURL, "page", "");
		
        String fS = "<a href=" + httpURL + "1>FrontPage</a>" + divideS;
        String pS = "<a href=" + httpURL + previousPage + ">PrevPage</a>" + divideS;
        String cS = "Page: " + currentPage + " / " + totalPage + divideS;
        String tS = "Goto: <input type=text size=2 id=goToPage";
        tS += " onKeyDown=\"if(window.event.keyCode==13) goToPage('"; //click on enter
        tS += httpURL + "');\" value=" + nextPage + "> page";
        tS += "<input type=button name=g value=Go ";
        tS += "onclick=\"goToPage('" + httpURL + "');\">" + divideS;
        String nS = "<a href=" + httpURL + nextPage + ">NextPage</a>" + divideS;
        String lS = "<a href=" + httpURL + totalPage + ">LastPage</a>";
        turnPageStrng = turnPageStrng + divideS + fS + pS + cS + tS + nS + lS;
        return turnPageStrng;
    }

    /**
     * get the total result
     * @return
     */
    private int getTotalResultSetNum(String sql) {
        int num = 0;
        ResultSet rs;
        try {
            rs = st.executeQuery(sql);
            if (rs.next()) {
                rs.last(); //move to the last
                num = rs.getRow(); //the total record num
                rs.first(); //go back to the first one
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return num;
    }
    
    public String concatLinkWithParam(String url, String paramName, String param) {
    		if(url.indexOf("?")>0)
    		{
    			return url + "&" + paramName + "=" + param;
    		}
    		else
    		{
    			return url + "?" + paramName + "=" + param;
    		} 
    }
    
    private String getCurrentPageURLWithTab(String url) {
    		if (isTabEnable()) {
    			return (getCurrentPageURLWithoutParameteor(url) + "?tab=" + getTab());
    		} else {
    			return getCurrentPageURLWithoutParameteor(url);
    		}
    }
    
    /**
     * 
     * @param url rm the parameters
     * @return Current url
     */
    private String getCurrentPageURLWithoutParameteor(String url) {
        if(url.indexOf("?")>0)
            return url.split("\\?")[0];
        else
            return url;
    }
   
    /**************************Methods of getter and setter**************************/
    //Set table title
    public void setTableTitle(String tableTitle) {
        this.tableTitle = tableTitle;
    }
    //Get table title
    private String getTableTitle() {
        return tableTitle;
    }
    
    //set currentPage url
    public void setCurrentPageUrl(String currentPageUrl) {
        this.currentPageUrl = currentPageUrl;
    }
    //get currentPage url
    private String getCurrentPageUrl() {
        return currentPageUrl;
    }    

    public void setSort(boolean sort) {
        this.sort = sort;
    }

    private boolean isSort() {
        return sort;
    }

    public void setSortType(int sortType) {
        this.sortType = sortType;
    }

    private int getSortType() {
        return sortType;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    private String getSortColumn() {
        return sortColumn;
    }

    public void setSearch(boolean search) {
        this.search = search;
    }

    private boolean isSearch() {
        return search;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    private String getSearchType() {
        return searchType;
    }

    public void setSearchCondition(String searchCondition) {
        this.searchCondition = searchCondition;
    }

    private String getSearchCondition() {
        return searchCondition;
    }
    public void setActionType(int actionType) {
		this.actionType = actionType;
	}

	public void setRole(int role) {
		this.role = role;
	}

	private int getTab() {
		return tabNum;
	}

	public void setTab(int no) {
		this.tabNum = no;
	}
	
	public void setEnableTab(boolean b) {
		this.enableTab = b;
	}
	
	private boolean isTabEnable() {
		return this.enableTab;
	}

	public boolean isAddRow() {
		return AddRow;
	}

	public void setAddRow(boolean AddRow) {
		this.AddRow = AddRow;
	}
	
	 public void setFilter(boolean bfilter) {
        this.bfilter = bfilter;
    }

    public int getSumType() {
		return sumType;
	}

	public void setSumType(int sumType) {
		this.sumType = sumType;
	}

	public int getPaidType() {
		return paidType;
	}

	public void setPaidType(int paidType) {
		this.paidType = paidType;
	}
    
}