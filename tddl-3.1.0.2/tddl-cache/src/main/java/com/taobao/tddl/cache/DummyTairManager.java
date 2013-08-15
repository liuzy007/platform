package com.taobao.tddl.cache;

import java.io.Serializable;
import java.util.List;

import com.taobao.common.tair.DataEntry;
import com.taobao.common.tair.Result;
import com.taobao.common.tair.ResultCode;

public class DummyTairManager implements com.taobao.common.tair.TairManager {
	private boolean returnSourceValue = false;
	private int val = 50;
	public int getVal() {
		return val;
	}

	public void setVal(int val) {
		this.val = val;
	}

	public Result<Integer> decr(int arg0, Object arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultCode delete(int arg0, Object arg1) {
		return new ResultCode(0);
	}

	public Result<DataEntry> get(int arg0, Object arg1) {
		if(returnSourceValue){
			DataEntry de = new DataEntry(((Integer)arg1)+ val);
			Result<DataEntry> result = new Result<DataEntry>(ResultCode.SUCCESS, de);
			return result;
		}
		// TODO Auto-generated method stub
		return null;
	}

	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	public Result<Integer> incr(int arg0, Object arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultCode invalid(int arg0, Object arg1) {
		return new ResultCode(0);
	}

	public ResultCode mdelete(int arg0, List<Object> arg1) {
		return new ResultCode(0);
	}

	public Result<List<DataEntry>> mget(int arg0, List<Object> arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultCode minvalid(int arg0, List<? extends Object> arg1) {
		return new ResultCode(0);
	}

	public ResultCode put(int arg0, Object arg1, Serializable arg2) {
		return new ResultCode(0);
	}

	public ResultCode put(int arg0, Object arg1, Serializable arg2, int arg3) {
		return new ResultCode(0);
	}

	public ResultCode put(int arg0, Object arg1, Serializable arg2, int arg3, int arg4) {
		return new ResultCode(0);
	}

	public boolean isReturnSourceValue() {
		return returnSourceValue;
	}

	public void setReturnSourceValue(boolean returnSourceValue) {
		this.returnSourceValue = returnSourceValue;
	}

}
