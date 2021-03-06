package com.taobao.javaclient;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ibm.staf.*;
import com.ibm.staf.wrapper.*;
import junit.framework.TestCase;

/**
 * @author ashu.cs
 *
 */
public class BaseTestCase extends TestCase {
	protected static Logger log=Logger.getLogger("Test");
	protected STAFHandle stafhandle=null;
	
	public String getShellOutput(STAFResult result)
	{
		Map rstMap=(Map)result.resultObj;
		List rstList=(List)rstMap.get("fileList");
		Map stdoutMap=(Map)rstList.get(0);
		String stdout=(String)stdoutMap.get("data");
		return stdout;
	}
	
	public BaseTestCase()
	{
		super();
		gethandle();
	}
	
	public BaseTestCase(String arg0)
	{
		super(arg0);
		gethandle();
	}
	
	public STAFResult executeShell(STAFHandle stafHandle,String machine ,String cmd )
	{
		STAFResult result=stafHandle.submit2(machine, "PROCESS", "START SHELL COMMAND "+STAFUtil.wrapData(cmd)+" WAIT RETURNSTDOUT STDERRTOSTDOUT");
		return result;
	}
	
	public void waitto(int sec)
	{
		log.debug("wait for "+sec+"s");
		try
		{
			Thread.sleep(sec*1000);
		}
		catch(Exception e)
		{}
	}

	private void gethandle()
	{
		try{
			stafhandle=new STAFHandle("Base Handle");
			}
			catch(STAFException e)
			{
				log.error("create handle failed!",e);
			}
	}
}
