<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="edu.utd.db.lms.dbAction.LibActionWebGenerator"%>
<%@ page import="java.net.URLEncoder"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Librarian</title>
  </head>
  <body>
  <br/>
  <form style="margin-left:3%;">
  	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  	<a href="?tab=1" ><b>Book search</b></a>&nbsp;&nbsp;
  	<a href="?tab=2" ><b>Book loan</b></a>&nbsp;&nbsp;
  	<a href="?tab=3" ><b>Borrower management</b></a>&nbsp;&nbsp;
  	<a href="?tab=4" ><b>Fine management</b></a>&nbsp;&nbsp;
  	<a href="upLoad.jsp"><b>Import files</b></a>
  </form>
  <br/>
  <hr style="filter: alpha(opacity=100,finishopacity=0,style=3)" width="95%" color="#997cb9" size=5>

  <%
    LibActionWebGenerator libPage = new LibActionWebGenerator();
    String tableTitle = "";
	String sql = "";
	String pageUrl = request.getRequestURI(); //get the url without params
    
	libPage.setRole(1); 			//librarian: ActionType=>0 for checkout; 1 for check in; 2 for fines payment;
    libPage.setEnableTab(true);
    	String tab = request.getParameter("tab");
    int tabNo = 0;
	
    if(tab == null || tab.equals("") || tab.equalsIgnoreCase("null")) {
		//the first one tab
   		tabNo = 1;
	} else {
		tabNo = Integer.parseInt(tab);
	}
	
	libPage.setTab(tabNo);
	boolean bActionOpen  = false;
	boolean bAdd = false;
	boolean bFilter = false;
	boolean bSearch = false;
	switch(tabNo) {
	case 2://Book loan&Check in
		{
			tableTitle = "Book Loan";
			sql = "SELECT BOOK.Isbn,BOOK.Title,BORROWER.Card_id,BORROWER.Ssn,BORROWER.Bname,"+
				  "BOOK_LOAN.Date_out,BOOK_LOAN.Due_date,BOOK_LOAN.Date_in "+
				  "FROM # JOIN $ ON BORROWER.Card_id=BOOK_LOAN.Card_id JOIN @ "+
				  "ON BOOK_LOAN.Isbn=BOOK.Isbn";
			
			/****************************Start of Search Cfg*************************/
		    bSearch = true;
		   
			String searchType = null;
		    searchType = request.getParameter("searchType");
		    String searchCondition = null;
		    searchCondition = request.getParameter("searchCondition");
		    if(searchType != null && !searchType.equals("null") && !searchType.equals("")) {
		    		libPage.setSearchType(searchType);
		        libPage.setSearchCondition(searchCondition);
		        String urlSearchCondition = URLEncoder.encode(searchCondition);
		        String urlSearchType = URLEncoder.encode(searchType);
		        pageUrl = libPage.concatLinkWithParam(pageUrl, "searchCondition", urlSearchCondition);
		        pageUrl = libPage.concatLinkWithParam(pageUrl, "searchType", urlSearchType);
			    	if(searchCondition != null && !searchCondition.equals("null") && !searchCondition.equals("")) {
			    		if(searchType.equals("Ssn") ||
			    		   searchType.equals("Bname"))
			    		{//borrower's info
			    			sql = sql.replace("$", "BOOK_LOAN");
			    			sql = sql.replace("@", "BOOK");
			    			sql = sql.replace("#", "(SELECT * FROM BORROWER WHERE "+searchType+" LIKE '%"+searchCondition+"%') AS BORROWER");
			    		} else if(searchType.equals("Isbn") || 
					    		  searchType.equals("Title")) {//book's info
			    			sql = sql.replace("#", "BORROWER");
			    			sql = sql.replace("$", "BOOK_LOAN");
			    			sql = sql.replace("@", "(SELECT * FROM BOOK WHERE "+searchType+" LIKE '%"+searchCondition+"%') AS BOOK");
			    		} else {//book_loan's info
			    			sql = sql.replace("#", "BORROWER");
			    			sql = sql.replace("@", "BOOK");
			    			sql = sql.replace("$", "(SELECT * FROM BOOK_LOAN WHERE "+searchType+" LIKE '%"+searchCondition+"%') AS BOOK_LOAN");
			    		}
			    } else { //search content is empty, then display all
			    		sql = sql.replace("#", "BORROWER");
				    	sql = sql.replace("$", "BOOK_LOAN");
		    			sql = sql.replace("@", "BOOK");
			    }
		    } else { //both search is empty, then display all 
			    	sql = sql.replace("#", "BORROWER");
			    	sql = sql.replace("$", "BOOK_LOAN");
	    			sql = sql.replace("@", "BOOK");
		    }
			/****************************End of Search Cfg*************************/
			
			/****************************Start of Action Cfg*************************/
			bActionOpen = true;
			libPage.setActionType(1);
			/****************************End of Action Cfg*************************/

			/**************************Start of Check in Cfg***************************/
		    String Isbn_Ci = null;
		    Isbn_Ci = request.getParameter("IsbnCheckIn");
		    if(Isbn_Ci != null && !Isbn_Ci.equals("null"))
		    {
		    		String Card_id = request.getParameter("Card_id");
	    			if(Card_id != null && !Card_id.equals("null"))
		    		{
		    			StringBuffer infoStr = new StringBuffer("<script>alert(\"");
		    			infoStr.append(libPage.tryCheckIn(Card_id, Isbn_Ci)); 			
		    			infoStr.append("\")</script>");
		    			out.println(infoStr);
		    		}
		    }
		    /**************************End of Check in Cfg***************************/
		}
		break;
	case 3://Borrower management
		{
			tableTitle = "Borrower Information";
			sql ="SELECT * FROM BORROWER";
			/****************************Start of Search Cfg*************************/
		    bSearch = true;
		    
			String searchType = null;
		    searchType = request.getParameter("searchType");
		    String searchCondition = null;
		    searchCondition = request.getParameter("searchCondition");
			if(searchCondition != null && !searchCondition.equals("null") && !searchCondition.equals("")) {
		    		libPage.setSearchType(searchType);
		        libPage.setSearchCondition(searchCondition);
		        String urlSearchCondition = URLEncoder.encode(searchCondition);
		        String urlSearchType = URLEncoder.encode(searchType);
		       
		        pageUrl = libPage.concatLinkWithParam(pageUrl, "searchCondition", urlSearchCondition);
		        pageUrl = libPage.concatLinkWithParam(pageUrl, "searchType", urlSearchType);
	   
		        if(searchType != null && !searchType.equals("null") && !searchType.equals("")) {
			    		sql += " WHERE BORROWER."+searchType+" LIKE '%"+searchCondition+"%'";
			    } else {
			    		sql += " WHERE BORROWER.Card_id LIKE '%"+ searchCondition +"%' "+
			    			  "OR BORROWER.Ssn LIKE '%" + searchCondition + "%' "+
			    			  "OR BORROWER.Bname LIKE '%" + searchCondition + "%' " + 
			    			  "OR BORROWER.Address LIKE '%" + searchCondition + "%' "+
			    			  "OR BORROWER.Phone LIKE '%"+searchCondition+"%'";
			    }
		    }
			/****************************End of Search Cfg*************************/
			bAdd = true;
			/****************************Start of Add new tuple cfg*************************/
			List<String> inputList = new ArrayList<String>();
			/*the Column number is the same as the select column number*/
			String bAddRow = request.getParameter("addRow");
			if(bAddRow != null && !bAddRow.equalsIgnoreCase("null") && !bAddRow.equals(""))
			{
				String Card_id = request.getParameter("Card_id");
				inputList.add(Card_id);
				String Ssn = request.getParameter("Ssn");
				inputList.add(Ssn);
				String Bname = request.getParameter("Bname");
				inputList.add(Bname);
				String Email = request.getParameter("Email");
				inputList.add(Email);
				String Address = request.getParameter("Address");
				inputList.add(Address);
				String Phone = request.getParameter("Phone");
				inputList.add(Phone);
				
				StringBuffer infoStr = new StringBuffer("<script>alert(\"");
    	        		infoStr.append(libPage.AddNewBorrower(inputList));
    				infoStr.append("\")</script>");
    				out.println(infoStr);	
			}
			/****************************End of Add new tuple cfg*************************/
		}
		break;
	case 4://Fine management
		{
			tableTitle = "Fine Management";
			sql ="SELECT BOOK_LOAN.Isbn,BOOK_LOAN.Card_id,BOOK_LOAN.Date_out,BOOK_LOAN.Due_date,BOOK_LOAN.Date_in,"+
					"FINE.Fine_amt,FINE.Paid FROM $ JOIN # ON FINE.Loan_id=BOOK_LOAN.Loan_id";
			/****************************Start of Search Cfg*************************/
		    bSearch = true;
			
		    String searchType = null;
		    searchType = request.getParameter("searchType");
		    String searchCondition = null;
		    searchCondition = request.getParameter("searchCondition");
		    int card_id = -1;
		    if(searchCondition != null && !searchCondition.equals("null") && !searchCondition.equals("")) {
		    		libPage.setSearchType(searchType);
		        libPage.setSearchCondition(searchCondition);
		        
		        String urlSearchCondition = URLEncoder.encode(searchCondition);
		        String urlSearchType = URLEncoder.encode(searchType);
		       
		        pageUrl = libPage.concatLinkWithParam(pageUrl, "searchCondition", urlSearchCondition);
		        pageUrl = libPage.concatLinkWithParam(pageUrl, "searchType", urlSearchType);     
		        //Fine mgn only contains one searchType(Card_id)
			    //sql = "SELECT * FROM ("+sql+") AS FINE_INFO WHERE FINE_INFO."+searchType+"="+searchCondition;
		        //card_id is entered.
		        card_id = Integer.parseInt(searchCondition);
		        sql = sql.replace("#", "(SELECT * FROM BOOK_LOAN WHERE Card_id="+card_id+") AS BOOK_LOAN");
		    } 

			/****************************End of Search Cfg*************************/
			
			/****************************Display option*************************/
			bFilter = true;
			
			String sumEach = request.getParameter("sum"); //0 EACH;1 SUM
			int se = 0;
			if(sumEach != null && !sumEach.equals("null") && !sumEach.equals(""))
			{
				se = Integer.parseInt(sumEach);
			}
			
			String paidUnpaid = request.getParameter("paid"); //0 PAID&UNPAID; 1 PAID; 2 UNPAID
			int pu = 0;
			if(paidUnpaid != null && !paidUnpaid.equals("null") && !paidUnpaid.equals(""))
			{
				pu = Integer.parseInt(paidUnpaid);
			}
			
			libPage.setSumType(se);
			libPage.setPaidType(pu);
			
			switch(se) {
			case 1://sum
				if(pu != 0) {//paid or unpaid
					sql = sql.replace("$", "(SELECT * FROM FINE WHERE Paid="+(pu==1?1:0)+") AS FINE");
				}
				sql = sql.replace("FINE.Fine_amt,", "SUM(FINE.Fine_amt) AS Sum_amount,");
				sql = sql.replace("BOOK_LOAN.Date_out,BOOK_LOAN.Due_date,BOOK_LOAN.Date_in,","");
				sql = sql.replace("BOOK_LOAN.Isbn,", "group_concat(BOOK_LOAN.Isbn) AS BooksIsbn,");
				sql = sql + " GROUP BY BOOK_LOAN.Card_id";
				break;
			default://each
				if(pu != 0) {//paid or unpaid
					sql = sql.replace("$", "(SELECT * FROM FINE WHERE Paid="+(pu==1?1:0)+") AS FINE");
				}
				break;
			}
			
			sql = sql.replace("$", "FINE");
			sql = sql.replace("#", "BOOK_LOAN");
			/****************************Display option*************************/

			/****************************Start of Action Cfg*************************/
			bActionOpen = true;
			libPage.setActionType(2);//pay for the books
			/****************************End of Action Cfg*************************/
			
			/**************************Start of pay Cfg***************************/
		    String Fine_Cid = null;
		    Fine_Cid = request.getParameter("Fine_Cid");
			String resStr = "";
		    if(Fine_Cid != null && !Fine_Cid.equals("null"))
		    {
		    		String Isbn = request.getParameter("Isbn");
		    		if(Isbn != null && !Isbn.equals("null")) {
		    			//make up a payment for each tuple
		    			//parameters should contains Isbn & Fine_Cid 
		    			//(implicitly specified the only fines which is unpaid)
		    			resStr = libPage.payOneFine(Isbn, Fine_Cid);
		    		} else {
		    			//parameters only contains Fine_Cid(implicitily imply all unpaid fines)
		    			resStr = libPage.payFines(Fine_Cid);
		    		}
		    		
	    			StringBuffer infoStr = new StringBuffer("<script>alert(\"");
	    			infoStr.append(resStr);
	    			infoStr.append("\")</script>");
	    			out.println(infoStr);
		    }
		    /**************************End of Paid Cfg***************************/
		    bAdd = true;
			/****************************Start of UpdateFines cfg*************************/
			String update = request.getParameter("update");
			if(update != null && !update.equals("null") && !update.equals("")) {
				//user click on update
				StringBuffer infoStr = new StringBuffer("<script>alert(\"");
	    			infoStr.append(libPage.updateFines());
	    			infoStr.append("\")</script>");
	    			out.println(infoStr);
			}
			
		}
		break;
	default: //1 = Book search&Check out & Unknow cases
		{
			tableTitle = "Book Information";
			sql ="SELECT BOOK.Isbn,BOOK.Isbn13,BOOK.Title,group_concat(AUTHOR.Name) AS AuthorName,"+
					"BOOK.Cover,BOOK.Publisher,BOOK.Page,BOOK.AvailNum FROM "+
					"BOOK LEFT JOIN BOOK_AUTHOR ON BOOK.Isbn=BOOK_AUTHOR.Isbn "+
					"LEFT JOIN AUTHOR ON BOOK_AUTHOR.Auth_id=AUTHOR.Author_id GROUP BY BOOK.Isbn";
			/****************************Start of Search Cfg*************************/
		    bSearch = true;
		    
			String searchType = null;
		    searchType = request.getParameter("searchType");
		    String searchCondition = null;
		    searchCondition = request.getParameter("searchCondition");
		    if(searchCondition != null && !searchCondition.equals("null") && !searchCondition.equals("")) {
		    		libPage.setSearchType(searchType);
		        libPage.setSearchCondition(searchCondition);
		        String urlSearchCondition = URLEncoder.encode(searchCondition);
		        String urlSearchType = URLEncoder.encode(searchType);
		       
		        pageUrl = libPage.concatLinkWithParam(pageUrl, "searchCondition", urlSearchCondition);
		        pageUrl = libPage.concatLinkWithParam(pageUrl, "searchType", urlSearchType);
		        
		        if(searchType != null && !searchType.equals("null") && !searchType.equals("")) {
			    		sql = "SELECT * FROM ("+sql+") AS BOOK_INFO WHERE BOOK_INFO."+searchType+" LIKE '%"+searchCondition+"%'";
			    } else {
			    		sql = "SELECT * FROM ("+sql+") AS BOOK_INFO WHERE BOOK_INFO.Isbn LIKE '%"+ searchCondition +"%' "+
			    			  "OR BOOK_INFO.Isbn13 LIKE '%" + searchCondition + "%' "+
			    			  "OR BOOK_INFO.Title LIKE '%" + searchCondition + "%' " + 
			    			  "OR BOOK_INFO.Publisher LIKE '%" + searchCondition + "%' "+
			    			  "OR BOOK_INFO.AuthorName LIKE '%"+searchCondition+"%'";
			    }
		    }
			/****************************End of Search Cfg*************************/
			
			/****************************Start of Action Cfg*************************/
			bActionOpen = true;
			libPage.setActionType(0);
			/****************************End of Action Cfg*************************/
		    
		    /**************************Start of Check out Cfg***************************/
		    String Isbn_Co = null;
		    Isbn_Co = request.getParameter("IsbnCheckOut");
		    if(Isbn_Co != null && !Isbn_Co.equals("null"))
		    {
		    		String Card_id = request.getParameter("Card_id");
		    		if(Card_id != null && !Card_id.equals("null"))
		    		{
		    			StringBuffer infoStr = new StringBuffer("<script>alert(\"");
		    			infoStr.append(libPage.tryCheckOut(Isbn_Co, Card_id));
		    			infoStr.append("\")</script>");
		    			out.println(infoStr);
		    		}
		    }
		    /**************************End of Check out Cfg***************************/
		}
		break;
	}
	libPage.setSearch(bSearch);
	libPage.setFilter(bFilter);
	libPage.setAddRow(bAdd);
  	libPage.setTableTitle(tableTitle);
    int pageSize = 20; //max display page size
    
    //get the current page index
    int currentPage;
    try {
         currentPage = Integer.parseInt(request.getParameter("page"));
    } catch(NumberFormatException e) {
        currentPage = 1;
        try {
            currentPage = (Integer)(request.getAttribute("page"));
        } catch (Exception e1)
        {
            currentPage = 1;            
        }
    }
    
    /*****************Add sort function****************/
    libPage.setSort(true);
    String sortType = null;
    String sortColumn = null;
    sortColumn = request.getParameter("columnName");//the colunm used to sort
    if(sortColumn != null)
    {        
        sortType = request.getParameter("sortType");//get the sort type, desc or asc
        sql = "SELECT * FROM (" + sql + ") AS SORT_TBL" + " ORDER BY SORT_TBL." + sortColumn + " " + sortType;
    }
    if(sortColumn != null)
    {
    		libPage.setSortColumn(sortColumn);
        if(sortType.equals("desc"))
        {
        		libPage.setSortType(1);
        }
        if(sortType.equals("asc"))
        {
        		libPage.setSortType(2);
        }
        
        pageUrl = libPage.concatLinkWithParam(pageUrl, "columnName", sortColumn);
        pageUrl = libPage.concatLinkWithParam(pageUrl, "sortType", sortType);
    }
    /*****************Add sort function***************/
    
    libPage.setCurrentPageUrl(pageUrl);
    StringBuffer s = libPage.getResult_withTableFormat(sql, currentPage, pageSize, bActionOpen);
    out.println(s);
  %>

  <table>
  <tr>
  </tr>
  </table>
  </body>
</html>