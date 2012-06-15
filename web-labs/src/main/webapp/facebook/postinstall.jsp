<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="com.socialcomputing.labs.facebook.FacebookRestProvider"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">	
<head>
<title>Just Map It! Facebook</title>
<meta http-equiv="refresh" content="0; url=<%=FacebookRestProvider.APP_URL%>?code=<%=request.getParameter("code") %>" />
</head><body></body>
</html>