
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

<meta http-equiv="Content-Type" content="text/html; charset=GBK" />
<script type="text/javascript">
   function confirmForDelete(){
       return window.confirm("你确认要删除该配置信息吗??");  
   }
   
   function saveIPAppName(e){	      
	      var ee = e.target.parentNode.parentNode;
	      var app = ee.cells[2].childNodes[0].value;
	      var ip =  ee.cells[0].innerHTML;
	      var sURL = __prefix+'graph/json/save_ip_app';
	      var data ={};
	      data.app = app;
	      data.ip = ip;
	      console.debug("ip:"+ip);
	      console.debug("app:"+app);
	      APPOPS.PAGE.json_request(sURL,{"app":app,"ip":ip});	      
	   }
   
   function doCheckConfigInfo(e){
	   alert("doCheckConfigInfo");/*
	     var ee = e.target.parentNode.parentNode;
	     var app = ee.cells[2].childNodes[0].value;
	     var dataId =  ee.cells[0].innerHTML;
	      var group = ee.cells[1].innerHTML;	      
	   var form = document.all.queryForm;
       form.method.value="checkConfig";
       form.dataId = dataId;
       form.group = group;
       form.submit();    */
       
   }
  
</script>
</head>
<body>

<c:url var="checkUrl" value="/check.do" >
</c:url>


 <form name="queryForm" action="${checkUrl}">
 		<input type="hidden" name="dataId"/>
        <input type="hidden" name="group"/>
 </form>
	<c:if test="${configInfos!=null}">
		<table border='1' width="800">
			<tr>
				<th>dataId</th>
				<th>group</th>				
				<th>cache(Mem) content MD5</th>
				<th>disk content MD5</th>
				<th>status</th>
				<th>operation</th>
			</tr>
			<c:forEach items="${configInfos}" var="configInfo">
				<tr>
					<td name="tagDataID"><c:out value="${configInfo.dataId}" /></td>
					<td name="tagGroup"><c:out value="${configInfo.group}"	escapeXml="false" /></td>
					<td name="tagMemMd5"><c:out value="${configInfo.memMd5}"	escapeXml="false" /></td>		
					<td name="tagDiskMd5"><c:out value="${configInfo.diskMd5}"	escapeXml="false" /></td>	
					<c:choose>			
			   <c:when test="${configInfo.memMd5==configInfo.diskMd5}">			   
			   		<c:set var="tdStyle"  value="green"/>
			   </c:when>
			   
			   <c:otherwise>
			        <c:set var="tdStyle"  value="gray"/>
			   </c:otherwise>
			  
			</c:choose>
						
					<td style="background:<c:out value="${tdStyle}"/> "></td>
					<td ><input type="button" value="doCheck" onclick="doCheckConfigInfo(event)"/></td>
					<c:url var="getConfigInfoUrl" value="/admin.do">
						<c:param name="method" value="detailConfig" />
						<c:param name="group" value="${configInfo.group}" />
						<c:param name="dataId" value="${configInfo.dataId}" />
					</c:url>
					<c:url var="deleteConfigInfoUrl" value="/admin.do">
						<c:param name="method" value="deleteConfig" />
						<c:param name="id" value="${configInfo.id}" />
					</c:url>
					<c:url var="saveToDiskUrl" value="/notify.do">
						<c:param name="method" value="notifyConfigInfo" />
						<c:param name="group" value="${configInfo.group}" />
						<c:param name="dataId" value="${configInfo.dataId}" />
					</c:url>
					<c:url var="previewUrl" value="/config.do">
						<c:param name="group" value="${configInfo.group}" />
						<c:param name="dataId" value="${configInfo.dataId}" />
					</c:url>
				</tr>
			</c:forEach>
		</table>
	</c:if>
</body>
</html>
