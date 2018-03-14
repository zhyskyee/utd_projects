<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Upload file</title>
</head>
<body>

<%
	out.print("Please upload your files(Book.csv + Borrower.csv)...");
%>

<form action="${pageContext.request.contextPath}/UploadHandler" enctype="multipart/form-data" method="post">
  <br/>
  <table>
  	<tr>
  		<td align="right">Book Info: </td>
  		<td align="left"><input type="file" name="BookFile" required></td>
  	</tr>
  	<tr>
  		<td align="right">Borrower Info: </td>
  		<td align="left"><input type="file" name="BorrowerFile" required></td>
  	</tr>
  	<tr>
  		<td align="right">Upload </td>
  		<td align="left"><input type="submit" value="   submit   "></td>
  	</tr>
  </table>
 </form>
</body>
</html>