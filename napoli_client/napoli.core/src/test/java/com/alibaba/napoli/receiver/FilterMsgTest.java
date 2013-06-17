package com.alibaba.napoli.receiver;

import static org.junit.Assert.assertTrue;

import com.alibaba.napoli.common.util.NapoliMessageUtil;
import com.alibaba.napoli.spi.TransportConsumer;
import java.util.ArrayList;
import java.util.List;

import javax.jms.Message;

import com.alibaba.napoli.client.async.NapoliWorker;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.napoli.mqImpl.ConsumerContext;
import com.alibaba.napoli.receiver.filter.Context;
import com.alibaba.napoli.receiver.filter.Filter;
import com.alibaba.napoli.receiver.filter.FilterFinder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FilterMsgTest {

	//ConsumerWorkListener listener;
	ConsumerContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

		TransportConsumer consumer = mock(TransportConsumer.class);
		context = mock(ConsumerContext.class);
		NapoliWorker worker = mock(NapoliWorker.class);
		//listener = new MockConsumerWorkListener(consumer, context, worker);
	}

	/**
	 * filterList为空不会导致错误
	 */
	@Test
	public void testFilterMsg1() {
		try {
			when(context.getFilterList()).thenReturn(null);

			//listener.filterMsg("message");
			when(context.getFilterList()).thenReturn(new ArrayList<Filter>());
			//listener.filterMsg("message");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	/**
	 * 测试消息过滤
	 */
	@Test
	public void testFilterMsg2() {
		try {
			List<Filter> list = new ArrayList<Filter>();
			list.add(new MockFilter(0));
			list.add(new MockFilter(1));
			list.add(new MockFilter(2));
			when(context.getFilterList()).thenReturn(list);
			String msg = "msg--";
			String ret = (String) NapoliMessageUtil.filterMsg(list,msg);
			System.out.println(ret);
			assertTrue(ret.equals(msg + "012"));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	/**
	 * 测试Filter异常
	 */
	@Test
	public void testFilterMsg3() {
		try {
			List<Filter> list = new ArrayList<Filter>();
			MockFilter exFilter = new MockFilter(0);
			exFilter.ex = true;
			list.add(exFilter);
			when(context.getFilterList()).thenReturn(list);
			String msg = "msg--";
            NapoliMessageUtil.filterMsg(list,msg);
			assertTrue(false);
		} catch (RuntimeException e) {
			// ignore it,wanted exception
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}	

	@After
	public void tearDown() throws Exception {
		
	}

	class MockFilter implements Filter {

		int index;
		boolean ex = false;

		public MockFilter(int index) {
			this.index = index;
		}

		public void destroy() {
			// TODO Auto-generated method stub

		}

		public void filter(Context context, FilterFinder next) {
			if (!ex) {
				context.setOutputObject(context.getOutputObject().toString()
						+ index);
				Filter filter = next.nextFilter(this);
				if (filter != null) {
					filter.filter(context, next);
				}
			} else {
				throw new RuntimeException("Mock filter exception.");
			}
		}

		public void init() {
			// TODO Auto-generated method stub

		}
	}
}
