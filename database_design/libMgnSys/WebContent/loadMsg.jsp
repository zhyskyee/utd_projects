<%@page import="org.eclipse.jdt.internal.compiler.batch.Main"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Result</title>
</head>
<body>
	<h1 font color="#997cb9">${message}</h1>
	<br/>
<%
	 if (request.getAttribute("message").toString().equals("Success!")) {
%>
	<script>
	alert("Upload Successfully!")
	window.location.href="librarian.jsp"
	</script>
<% 
	} else {
%>
	<script>
	alert("Upload Failed!")
	window.location.href="librarian.jsp"
	</script>
	<% 
		}
	%>
</body>
</html>