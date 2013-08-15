package com.taobao.tddl.client.sequence.util;

import java.util.Random;

import com.taobao.tddl.client.sequence.exception.SequenceException;


public class RandomSequence {
	/**
	 * 产生包含0~n-1的n个数值的随机序列
	 * @param n
	 * @return
	 * @throws SequenceException 
	 */
	public static int [] randomIntSequence(int n) throws SequenceException
	{	
		if(n<=0)
		{
			throw new SequenceException("产生随机序列范围值小于等于0");
		}
		int num[] =new int[n];
		for(int i=0;i<n;i++)
		{
			num[i]=i;
		}
		if(n==1)
		{
			return num;
		}
		Random random=new Random();
		if(n==2 && random.nextInt(2)==1) //50%的概率换一下
		{
			int temp=num[0];
			num[0]=num[1];
			num[1]=temp;
		}
		
		for(int i=0;i<n+10;i++)
		{
			int rindex=random.nextInt(n);//产生0~n-1的随机数
			int mindex=random.nextInt(n);
			int temp=num[mindex];
			num[mindex]=num[rindex];
			num[rindex]=temp;
		}
		return num;
	}
}
