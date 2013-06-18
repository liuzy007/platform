 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GBK" />
<title>Diamond配置信息管理</title>
<script type="text/javascript">
   function confirmForDelete(){
       return window.confirm("你确认要删除该分组信息吗??");  
   }
   
   function moveGroup(address,oldGroup,link){
       var newGroup=window.prompt("请输入目标分组名：");
       if(newGroup==null||newGroup.length==0)
         return false;
       link.href=link.href+"&newGroup="+newGroup;
       return window.confirm("你确认要将"+address+"从"+oldGroup+"移动到"+newGroup+"吗??");  
   }
  
</script>
</head>
<body>
<c:import url="/jsp/common/message.jsp"/>
<center><h1><strong>分组信息管理</strong></h1></center>
   <p align='center'>
     <c:if test="${groupMap!=null}">
      <table border='1' width="800">
          <tr>
              <td>IP地址</td>
              <td>dataId</td>
              <td>组名</td>
              <td>操作</td>
          </tr>
          <c:forEach items="${groupMap}" var="entry">
           <c:forEach items="${entry.value}" var="groupInfo">
            <tr>
               <td>
                  <c:out value="${entry.key}"/>
               </td>
              <td name="tagDataID">
                  <c:out value="${groupInfo.key}" />
               </td>
              <td name="tagGroup">
                  <c:out value="${groupInfo.value.group}" />
               </td>
              <c:url var="moveGroupUrl" value="/admin.do" >
                  <c:param name="method" value="moveGroup" />
                  <c:param name="id" value="${groupInfo.value.id}" />
               </c:url>
               <c:url var="deleteGroupUrl" value="/admin.do" >
                  <c:param name="method" value="deleteGroup" />
                   <c:param name="id" value="${groupInfo.value.id}" />
               </c:url>
              <td>
                 <a href="${moveGroupUrl}" onclick="return moveGroup('${entry.key}','${groupInfo.value.group}',this);">移动到分组</a>&nbsp;&nbsp;&nbsp;
                 <a href="${deleteGroupUrl}" onclick="return confirmForDelete();">删除</a>&nbsp;&nbsp;&nbsp;
              </td>
            </tr>
            </c:forEach>
          </c:forEach>
       </table>
    </c:if>
  </p>
  <p align='center'>
    <a href="<c:url value='/jsp/admin/group/new.jsp' />">添加分组信息</a> &nbsp;&nbsp;&nbsp;&nbsp;<a href="<c:url value='/admin.do?method=reloadGroup' />">重新加载分组信息</a>
  </p>
</body>
</html>