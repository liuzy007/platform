 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GBK" />
<title>添加分组信息</title>
	<script src="../../../js/prototype_for_validation.js" type="text/javascript"></script>
    <script src="../../../js/effects.js" type="text/javascript"></script>
	<script src="../../../js/validation_cn.js" type="text/javascript"></script>
	<style type="text/css">
		body td {
			color: #333;
			font-family: Arial, Helvetica, sans-serif;
			font-size: 10pt;
		}
		.validation-advice {
			margin: 0px 0;
			padding: 0px;
			margin-left: 10px;
			color : #FF3300;
			font-weight: bold;
			display: inline;
		}
		</style>
</head>
<body>
<c:import url="/jsp/common/message.jsp"/>
<center><h1><strong>新增分组信息</strong></h1></center>
<p align='center'>
     <c:url var="addGroupUrl" value="/admin.do" >
        <c:param name="method" value="addGroup" />
     </c:url>
     <form action="${addGroupUrl}" method="post" id="group-form">
        <table align='center'>
        <tr>
            <td>IP地址:</td>
            <td>
               <input type='text' name='address' width="128" class="required validate-ip max-length-128"/>
            </td>
        </tr>
         <tr>
            <td>DataId:</td>
            <td>
               <input type='text' name='dataId' width="128" class="required max-length-128"/>
            </td>
        </tr>
        <tr>
            <td>组名:</td>
            <td> <input type='text' name='group' width="128" class="required max-length-128"/></td>
        </tr>
        <tr> 
            <td colspan="2"><input type="submit" value="提交"/>
        </tr>
     </form>
  </p>
  
  </body>
  <script type="text/javascript">
    	new Validation('group-form',{immediate:true}); 
  </script>
  </html>