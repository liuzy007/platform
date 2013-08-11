package com.taobao.tddl.tddl_sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import com.taobao.tddl.interact.bean.TargetDB;
import com.taobao.tddl.rule.ruleengine.util.RuleUtils;

/**
 * 鐢ㄤ簬姣旇緝targetDatasource
 * @author shenxun
 *
 */
public class TargetComparator {
	public static void compare(TargetDB source,TargetDB target){
		Assert.assertEquals(source.getDbIndex(),target.getDbIndex());
		Set<String> set = source.getTableNames();
		Assert.assertEquals(set.size(), target.getTableNames().size());
		for(String str:set){
			Assert.assertTrue(str+"|"+target.getTableNames().toString(),target.getTableNames().contains(str));
		}
	}
	public static void compare(List<TargetDB> source,List<TargetDB> target){
		Assert.assertEquals(source.size(), target.size());
		for(TargetDB src:source){
			String sourceDBIndex = src.getDbIndex();
			boolean eq = false;
			for(TargetDB tar:target){
				
				boolean alreadyEqual = false;
				if(tar.getDbIndex().equals(sourceDBIndex)){
					eq = true;
					if(!alreadyEqual){
						alreadyEqual = true;
						compare(src, tar);
					}else{
						Assert.fail("equivalent to mach times");
					}
				}
				
			}
			if(!eq){
				Assert.fail("not eq,"+sourceDBIndex+":"+target);
			}
		}
	}
	public static class Bean{
		public int index;
		public int[] tableSuffix;
	}
	public static List<TargetDB> buildTargetDB(String dbFactor
			,String dbPadding,String tableFactor,String tablePadding,
			int width
			,Bean[] dbMartrix){
		List<TargetDB> target = new ArrayList<TargetDB>();
		for(Bean bean : dbMartrix){
			//a db
			TargetDB targetDB = new TargetDB();
			targetDB.setDbIndex(dbFactor+RuleUtils.placeHolder(2, bean.index));
			for(int i :bean.tableSuffix){
				String suffix = RuleUtils.placeHolder(width, i);
				targetDB.addOneTable(tableFactor+tablePadding+suffix);
			}
		
			target.add(targetDB);
		}
		return target;
	}
}
