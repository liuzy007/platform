package mizar;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public interface ${interfaceName} {

		<#if methods ??>
		<#list methods as method>
		@WebMethod
		public <#if method.returnType ??> ${method.returnType} <#else> void </#if> ${method.name}(
		<#if method.parameters ??>
		<#list method.parameters as parameter>
			${parameter.type} ${parameter.name} <#if parameter_has_next>,</#if>
		</#list>
		</#if>
		);
		</#list>
		</#if>

	}
