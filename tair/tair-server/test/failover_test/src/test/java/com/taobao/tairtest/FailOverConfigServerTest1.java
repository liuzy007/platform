/**
 * 
 */
package com.taobao.tairtest;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FailOverConfigServerTest1 extends FailOverBaseCase {

	@Test
	public void testFailover_01_restart_master_cs() {
		log.error("start config test Failover case 01");
		int waitcnt = 0;
		// start cluster
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");

		log.error("Start Cluster Successful!");

		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");

		// write 100k data to cluster
		execute_data_verify_tool();

		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}

		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("Write data over!");

		// close master cs
		if (!control_cs(csList.get(0), stop, 0))
			fail("close master cs failed!");

		log.error("master cs has been closed!");

		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");
		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		log.error("Read data over!");
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertEquals("get data verify failed!", datacnt, getVerifySuccessful());
		log.error("Successfully Verified data!");

		// restart master cs
		if (!control_cs(csList.get(0), start, 0))
			fail("restart master cs failed!");
		log.error("Restart master cs successful!");

		waitto(down_time);

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		log.error("Read data over!");
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertEquals("get data verify failed!", datacnt, getVerifySuccessful());
		log.error("Successfully verified data!");
		// end test
		log.error("end config test Failover case 01");
	}

	@Test
	public void testFailover_02_restart_slave_cs() {
		log.error("start config test Failover case 02");
		int waitcnt = 0;
		// start cluster
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");
		log.error("Start Cluster Successful!");

		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");

		// write 100k data to cluster
		execute_data_verify_tool();

		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("Write data over!");

		// close slave cs
		if (!control_cs(csList.get(1), stop, 0))
			fail("close slave cs failed!");

		log.error("slave cs has been closed!");

		waitto(2);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");
		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;
		log.error("Read data over!");

		// verify get result
		log.error(getVerifySuccessful());
		assertEquals("verify data failed!", datacnt, getVerifySuccessful());
		log.error("Successfully Verified data!");

		// restart slave cs
		if (!control_cs(csList.get(1), start, 0))
			fail("restart slave cs failed!");
		log.error("Restart slave cs successful!");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		log.error("Read data over!");
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertEquals("verify data failed!", datacnt, getVerifySuccessful());
		log.error("Successfully verified data!");
		// end test
		log.error("end config test Failover case 02");
	}

	@Test
	public void testFailover_03_restart_all_cs() {
		log.error("start config test Failover case 03");
		int waitcnt = 0;
		// start cluster
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");
		log.error("Start Cluster Successful!");

		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");

		// write 100k data to cluster
		execute_data_verify_tool();

		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("Write data over!");

		// close all cs
		if (!batch_control_cs(csList, stop, 0))
			fail("close all cs failed!");

		log.error("all cs has been closed! wait for 2 seconds to restart ...");

		waitto(2);

		// restart all cs
		if (!batch_control_cs(csList, start, 0))
			fail("restart all cs failed!");
		log.error("Restart all cs successful!");

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		log.error("Read data over!");
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertEquals("verify data failed", datacnt, getVerifySuccessful());
		log.error("Successfully verified data!");
		// end test
		log.error("end config test Failover case 03");
	}

	@Test
	public void testFailover_04_add_ds_and_recover_master_cs_before_rebuild() {
		log.error("start config test Failover case 04");
		int waitcnt = 0;

		// modify group configuration
		if (!comment_line(csList.get(0), tair_bin + groupconf,
				dsList.get(0), "#"))
			fail("change group.conf failed!");
		if (!comment_line(csList.get(1), tair_bin + groupconf,
				dsList.get(0), "#"))
			fail("change group.conf failed!");
		log.error("group.conf has been changed!");

		// start cluster
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");

		log.error("Start Cluster Successful!");

		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");

		// write 100k data to cluster
		execute_data_verify_tool();

		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}

		log.error("Write data over!");

		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;

		// verify get result
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("finish put data!");

		// close master cs
		if (!control_cs(csList.get(0), stop, 0))
			fail("close master cs failed!");
		log.error("master cs has been closed!");

		waitto(2);

		// uncomment cs group.conf
		if (!uncomment_line(csList.get(0),
				tair_bin + groupconf, dsList.get(0), "#"))
			fail("change group.conf failed!");
		if (!uncomment_line(csList.get(1),
				tair_bin + groupconf, dsList.get(0), "#"))
			fail("change group.conf failed!");
		// touch group.conf
		if (touch_flag != 0)
			touch_file(csList.get(0), tair_bin + groupconf);
		log.error("change group.conf and touch it");

		waitto(5);

		// if(check_keyword(csList.get(1), start_migrate,
		// tair_bin+"logs/config.log")!=1)fail("Already migration!");
		// wait for migrate start
		waitto(down_time);
		while (check_keyword(csList.get(1), start_migrate, tair_bin
				+ "logs/config.log") != 1) {
			log.debug("check if migration start on cs "
					+ csList.get(1) + " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}

		if (waitcnt > 150)
			fail("down time arrived,but no migration start!");
		waitcnt = 0;
		log.error("down time arrived,migration started!");

		// stop ds
		if (!control_ds(dsList.get(0), stop, 0))
			fail("stop ds failed!");
		log.error("stop ds successful!");

		// restart master cs
		if (!control_cs(csList.get(0), start, 0))
			fail("restart master cs failed!");
		log.error("Restart master cs successful!");

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;
		log.error("Read data over!");

		// verify get result
		log.error(getVerifySuccessful());
		// assertEquals("verify data failed!", datacnt, getVerifySuccessful());
		assertTrue("verify data failed!",
				getVerifySuccessful() / put_count_float > 0.79);
		log.error("Successfully Verified data!");

		// end test
		log.error("end config test Failover case 04");
	}

	@Test
	public void testFailover_05_add_ds_and_recover_master_cs_when_rebuild() {
		log.error("start config test Failover case 05");
		int waitcnt = 0;

		// modify group configuration
		if (!comment_line(csList.get(0), tair_bin + groupconf,
				dsList.get(dsList.size() - 1), "#"))
			fail("change group.conf failed!");
		if (!comment_line(csList.get(1), tair_bin + groupconf,
				dsList.get(dsList.size() - 1), "#"))
			fail("change group.conf failed!");
		log.error("group.conf has been changed!");

		// start cluster
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");

		log.error("Start Cluster Successful!");

		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");

		// write 100k data to cluster
		execute_data_verify_tool();

		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;

		// verify get result
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("Write data over!");

		// close master cs
		if (!control_cs(csList.get(0), stop, 0))
			fail("close master cs failed!");
		log.error("master cs has been closed!");

		waitto(2);

		// uncomment cs group.conf
		if (!uncomment_line(csList.get(0),
				tair_bin + groupconf,
				dsList.get(dsList.size() - 1), "#"))
			fail("change group.conf failed!");
		if (!uncomment_line(csList.get(1),
				tair_bin + groupconf,
				dsList.get(dsList.size() - 1), "#"))
			fail("change group.conf failed!");
		// touch group.conf
		if (touch_flag != 0)
			touch_file(csList.get(1), tair_bin + groupconf);
		log.error("change group.conf and touch it");

		waitto(down_time);

		// check migration stat
		while (check_keyword(csList.get(1), start_migrate, tair_bin
				+ "logs/config.log") != 1) {
			log.debug("check if migration start on cs "
					+ csList.get(1) + " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("down time arrived,but no migration started!");
		waitcnt = 0;
		log.error("down time arrived,migration start!");

		// restart master cs
		if (!control_cs(csList.get(0), start, 0))
			fail("restart master cs failed!");
		log.error("Restart master cs successful!");

		// wait for system restart to work
		waitto(down_time);

		// check migration stat
		while (check_keyword(csList.get(0), finish_migrate, tair_bin
				+ "logs/config.log") != 1) {
			log.debug("check if migration finish on cs "
					+ csList.get(1) + " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("down time arrived,but no migration finished!");
		waitcnt = 0;
		log.error("down time arrived,migration finished!");

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		log.error("Read data over!");
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertEquals("verify data failed!", datacnt, getVerifySuccessful());
		log.error("Successfully Verified data!");

		// end test
		log.error(getVerifySuccessful());
		log.error("end config test Failover case 05");
	}

	@Test
	public void testFailover_06_add_ds_and_recover_master_cs_afterrebuild() {
		log.error("start config test Failover case 06");
		int waitcnt = 0;

		// modify group configuration
		if (!comment_line(csList.get(0), tair_bin + groupconf,
				dsList.get(dsList.size() - 1), "#"))
			fail("change group.conf failed!");
		if (!comment_line(csList.get(1), tair_bin + groupconf,
				dsList.get(dsList.size() - 1), "#"))
			fail("change group.conf failed!");
		log.error("group.conf has been changed!");

		// start cluster
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");

		log.error("Start Cluster Successful!");

		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");

		// write 100k data to cluster
		execute_data_verify_tool();

		// check verify
		while (check_process(local, toolname) != 2) {
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
				log.error("sleep exception", e);
			}
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;
		// verify get result
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("Write data over!");

		// close master cs
		if (!control_cs(csList.get(0), stop, 0))
			fail("close master cs failed!");
		log.error("master cs has been closed!");

		waitto(2);

		// uncomment cs group.conf
		if (!uncomment_line(csList.get(0),
				tair_bin + groupconf,
				dsList.get(dsList.size() - 1), "#"))
			fail("change group.conf failed!");
		if (!uncomment_line(csList.get(1),
				tair_bin + groupconf,
				dsList.get(dsList.size() - 1), "#"))
			fail("change group.conf failed!");
		// touch group.conf
		if (touch_flag != 0)
			touch_file(csList.get(0), tair_bin + groupconf);
		log.error("change group.conf and touch it");

		waitto(down_time);

		// check migration stat
		while (check_keyword(csList.get(1), finish_migrate, tair_bin
				+ "logs/config.log") != 1) {
			log.debug("check if migration finished on cs "
					+ csList.get(1) + " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("down time arrived,but no migration started or finished!");
		waitcnt = 0;
		log.error("down time arrived,migration finished!");

		// restart master cs
		if (!control_cs(csList.get(0), start, 0))
			fail("restart master cs failed!");
		log.error("Restart master cs successful!");

		// wait for system restart to work
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		log.error("Read data over!");
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertEquals("verify data failed!", datacnt, getVerifySuccessful());
		log.error("Successfully Verified data!");

		// end test
		log.error("end config test Failover case 06");
	}

	@Test
	public void testFailover_07_recover_master_cs_when_ds_down_and_rebuild_then_ds_join_again_generate_second_rebuild() {
		log.error("start config test Failover case 07");
		int waitcnt = 0;
		// start cluster
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");

		log.error("Start Cluster Successful!");

		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");

		// write 100k data to cluster
		execute_data_verify_tool();

		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;
		// verify get result
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("Write data over!");

		// close master cs
		if (!control_cs(csList.get(0), stop, 0))
			fail("close master cs failed!");
		log.error("master cs has been closed!");

		waitto(2);

		// record the version times
		int versionCount = check_keyword(csList.get(1),
				finish_rebuild, tair_bin + "logs/config.log");

		// close ds
		if (!control_ds(dsList.get(0), stop, 0))
			fail("close ds failed!");
		log.error("ds has been closed!");

		waitto(down_time);

		// check rebuild finish
		while (check_keyword(csList.get(1), finish_rebuild, tair_bin
				+ "logs/config.log") == versionCount) {
			log.debug("check if rebuld table finished on cs "
					+ csList.get(1) + " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("down time arrived,but no rebuild finished!");
		waitcnt = 0;
		log.error("down time arrived,rebuild finished!");

		// restart ds
		if (!control_ds(dsList.get(0), start, 0))
			fail("restart ds failed!");
		log.error("restart ds successful!");
		// touch conf file
		if (touch_flag != 0)
			touch_file(csList.get(1), tair_bin + groupconf);
		log.error("touch group.conf");

		waitto(down_time);

		// check second migration stat
		while (check_keyword(csList.get(1), start_migrate, tair_bin
				+ "logs/config.log") != 1) {
			log.debug("check if migration start on cs "
					+ csList.get(1) + " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("down time arrived,but no migration started!");
		waitcnt = 0;
		log.error("down time arrived,migration started!");

		// restart master cs
		if (!control_cs(csList.get(0), start, 0))
			fail("restart master cs failed!");
		log.error("Restart master cs successful!");

		// wait for system restart to work
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;
		log.error("Read data over!");

		// verify get result
		log.error(getVerifySuccessful());
		assertTrue("verify data failed!",
				getVerifySuccessful() / put_count_float > 0.79);
		log.error("Successfully Verified data!");

		// end test
		log.error("end config test Failover case 07");
	}

	@Test
	public void testFailover_08_recover_master_cs_after_ds_down_and_rebuild_then_ds_join_again_generate_second_rebuild() {
		log.error("start config test Failover case 08");
		int waitcnt = 0;
		// start cluster
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");

		log.error("Start Cluster Successful!");

		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");

		// write 100k data to cluster
		execute_data_verify_tool();

		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;
		// verify get result
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("Write data over!");

		// close master cs
		if (!control_cs(csList.get(0), stop, 0))
			fail("close master cs failed!");
		log.error("master cs has been closed!");

		waitto(2);

		// record the version times
		int versionCount = check_keyword(csList.get(1),
				finish_rebuild, tair_bin + "logs/config.log");

		// close ds
		if (!control_ds(dsList.get(0), stop, 0))
			fail("close ds failed!");
		log.error("ds has been closed!");

		waitto(down_time);

		// check build stat
		while (check_keyword(csList.get(1), finish_rebuild, tair_bin
				+ "logs/config.log") == versionCount) {
			log.debug("check if rebuild table finished on cs "
					+ csList.get(1) + " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("down time arrived,but no rebuild started or finished!");
		waitcnt = 0;
		log.error("down time arrived,rebuild table finished!");

		// restart ds
		if (!control_ds(dsList.get(0), start, 0))
			fail("restart ds failed!");
		log.error("restart ds successful!");
		// touch conf file
		if (touch_flag != 0)
			touch_file(csList.get(1), tair_bin + groupconf);
		log.error("touch group.conf");

		// wait down time for touch
		waitto(down_time);

		// check second migration stat
		while (check_keyword(csList.get(1), finish_migrate, tair_bin
				+ "logs/config.log") != 1) {
			log.debug("check if migration stop on cs " + csList.get(1)
					+ " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("down time arrived,but no migration started or stoped!");
		waitcnt = 0;
		log.error("down time arrived,migration stoped!");

		// restart master cs
		if (!control_cs(csList.get(0), start, 0))
			fail("restart master cs failed!");
		log.error("Restart master cs successful!");

		// wait for system restart to work
		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		log.error("Read data over!");
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertTrue("verify data failed!",
				getVerifySuccessful() / put_count_float > 0.8);
		log.error("Successfully Verified data!");

		// end test
		log.error("end config test Failover case 08");
	}

	@Test
	public void testFailover_09_recover_master_cs_after_add_ds_and_rebuild_then_ds_close_again_generate_second_rebuild_rebuid() {
		log.error("start config test Failover case 09");
		int waitcnt = 0;

		// modify group configuration
		if (!comment_line(csList.get(0), tair_bin + groupconf,
				dsList.get(0), "#"))
			fail("change group.conf failed!");
		if (!comment_line(csList.get(1), tair_bin + groupconf,
				dsList.get(0), "#"))
			fail("change group.conf failed!");
		log.error("group.conf has been changed!");

		// start cluster
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");

		log.error("Start Cluster Successful!");

		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");

		// write 100k data to cluster
		execute_data_verify_tool();

		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;
		// verify get result
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("Write data over!");

		// close master cs
		if (!control_cs(csList.get(0), stop, 0))
			fail("close master cs failed!");
		log.error("master cs has been closed!");

		waitto(2);
		// uncomment cs group.conf
		if (!uncomment_line(csList.get(0),
				tair_bin + groupconf, dsList.get(0), "#"))
			fail("change group.conf failed!");
		if (!uncomment_line(csList.get(1),
				tair_bin + groupconf, dsList.get(0), "#"))
			fail("change group.conf failed!");
		// touch group.conf
		if (touch_flag != 0)
			touch_file(csList.get(0), tair_bin + groupconf);
		log.error("change group.conf and touch it");

		// wait down time for touch
		waitto(down_time);

		// check second migration stat
		while (check_keyword(csList.get(1), start_migrate, tair_bin
				+ "logs/config.log") != 1) {
			log.debug("check if migration start on cs "
					+ csList.get(1) + " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("down time arrived,but no migration started!");
		waitcnt = 0;
		log.error("down time arrived,migration started!");

		// record the version times
		int versionCount = check_keyword(csList.get(1),
				finish_rebuild, tair_bin + "logs/config.log");

		// close ds
		if (!control_ds(dsList.get(0), stop, 0))
			fail("close ds failed!");
		log.error("ds has been closed!");

		waitto(down_time);

		// check rebuild stat
		while (check_keyword(csList.get(1), finish_rebuild, tair_bin
				+ "logs/config.log") != (versionCount + 1)) {
			log.debug("check if rebuild table finished on cs "
					+ csList.get(1) + " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("down time arrived,but no rebuild started or finished!");
		waitcnt = 0;
		log.error("down time arrived,rebuild table finished!");

		// restart master cs
		if (!control_cs(csList.get(0), start, 0))
			fail("restart master cs failed!");
		log.error("Restart master cs successful!");

		// wait for system restart to work
		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		log.error("Read data over!");
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertTrue("verify data failed!",
				getVerifySuccessful() / put_count_float > 0.79);
		log.error("Successfully Verified data!");

		// end test

		log.error("end config test Failover case 09");
	}

	@Test
	public void testFailover_10_recover_master_cs_after_add_ds_and_rebuild_then_ds_close_again_generate_second_rebuild_after_rebuild() {
		log.error("start config test Failover case 10");
		int waitcnt = 0;

		// modify group configuration
		if (!comment_line(csList.get(0), tair_bin + groupconf,
				dsList.get(0), "#"))
			fail("change group.conf failed!");
		if (!comment_line(csList.get(1), tair_bin + groupconf,
				dsList.get(0), "#"))
			fail("change group.conf failed!");
		log.error("group.conf has been changed!");

		// start cluster
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");

		log.error("Start Cluster Successful!");

		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");

		// write 100k data to cluster
		execute_data_verify_tool();

		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;
		// verify get result
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("Write data over!");

		// close master cs
		if (!control_cs(csList.get(0), stop, 0))
			fail("close master cs failed!");
		log.error("master cs has been closed!");

		waitto(2);
		// uncomment cs group.conf
		if (!uncomment_line(csList.get(0),
				tair_bin + groupconf, dsList.get(0), "#"))
			fail("change group.conf failed!");
		if (!uncomment_line(csList.get(1),
				tair_bin + groupconf, dsList.get(0), "#"))
			fail("change group.conf failed!");
		// touch group.conf
		if (touch_flag != 0)
			touch_file(csList.get(0), tair_bin + groupconf);
		log.error("change group.conf and touch it");

		// wait down time for touch
		waitto(down_time);

		// check first migration stat
		while (check_keyword(csList.get(1), start_migrate, tair_bin
				+ "logs/config.log") != 1) {
			log.debug("check if migration start on cs "
					+ csList.get(1) + " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("down time arrived,but no migration started!");
		waitcnt = 0;
		log.error("down time arrived,migration started!");
		// record the version times
		int versionCount = check_keyword(csList.get(1),
				finish_rebuild, tair_bin + "logs/config.log");

		// close ds
		if (!control_ds(dsList.get(0), stop, 0))
			fail("close ds failed!");
		log.error("ds has been closed!");

		waitto(down_time);

		// check rebuild stat
		while (check_keyword(csList.get(1), finish_rebuild, tair_bin
				+ "logs/config.log") != (versionCount + 1)) {
			log.debug("check if rebuild table finished on cs "
					+ csList.get(1) + " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("down time arrived,but no rebuild started or finished!");
		waitcnt = 0;
		log.error("down time arrived,rebuild table finished!");

		waitto(down_time);

		// restart master cs
		if (!control_cs(csList.get(0), start, 0))
			fail("restart master cs failed!");
		log.error("Restart master cs successful!");

		// wait for system restart to work
		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		log.error("Read data over!");
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertTrue("verify data failed!",
				getVerifySuccessful() / put_count_float > 0.79);
		log.error("Successfully Verified data!");

		// end test
		log.error("end config test Failover case 10");
	}

	@Test
	public void testFailover_11_recover_slave_cs_and_add_ds_before_rebuild() {
		log.error("start config test Failover case 11");
		int waitcnt = 0;

		// modify group configuration
		if (!comment_line(csList.get(0), tair_bin + groupconf,
				dsList.get(0), "#"))
			fail("change group.conf failed!");
		if (!comment_line(csList.get(1), tair_bin + groupconf,
				dsList.get(0), "#"))
			fail("change group.conf failed!");
		log.error("group.conf has been changed!");

		// start cluster
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");

		log.error("Start Cluster Successful!");

		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");

		// write 100k data to cluster
		execute_data_verify_tool();

		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;
		// verify get result
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("Write data over!");

		// close slave cs
		if (!control_cs(csList.get(1), stop, 0))
			fail("close slave cs failed!");
		log.error("slave cs has been closed!");

		waitto(2);

		// uncomment cs group.conf
		if (!uncomment_line(csList.get(0),
				tair_bin + groupconf, dsList.get(0), "#"))
			fail("change group.conf failed!");
		if (!uncomment_line(csList.get(1),
				tair_bin + groupconf, dsList.get(0), "#"))
			fail("change group.conf failed!");
		// touch group.conf
		if (touch_flag != 0)
			touch_file(csList.get(0), tair_bin + groupconf);
		log.error("change group.conf and touch it");

		log.error("wait 5 seconds to restart before rebuild ...");
		waitto(5);

		// if(check_keyword(csList.get(0), start_migrate,
		// tair_bin+"logs/config.log")!=1)fail("Already migration!");
		// check first migration stat
		while (check_keyword(csList.get(0), start_migrate, tair_bin
				+ "logs/config.log") != 1) {
			log.debug("check if migration start on cs "
					+ csList.get(0) + " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("down time arrived,but no migration started!");
		waitcnt = 0;
		log.error("down time arrived,migration started!");

		// restart ds
		if (!control_ds(dsList.get(0), stop, 0))
			fail("restart ds failed!");
		log.error("Restop ds successful!");

		// restart slave cs
		if (!control_cs(csList.get(1), start, 0))
			fail("restart slave cs failed!");
		log.error("Restart slave cs successful!");

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		log.error("Read data over!");
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertTrue("verify data failed!",
				getVerifySuccessful() / put_count_float > 0.79);
		log.error("Successfully Verified data!");

		// end test
		log.error("end config test Failover case 11");
	}

	@Test
	public void testFailover_12_recover_slave_cs_when_add_ds_and_rebuild() {
		log.error("start config test Failover case 12");
		int waitcnt = 0;

		// modify group configuration
		if (!comment_line(csList.get(0), tair_bin + groupconf,
				dsList.get(0), "#"))
			fail("change group.conf failed!");
		if (!comment_line(csList.get(1), tair_bin + groupconf,
				dsList.get(0), "#"))
			fail("change group.conf failed!");
		log.error("group.conf has been changed!");

		// start cluster
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");

		log.error("Start Cluster Successful!");

		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");

		// write 100k data to cluster
		execute_data_verify_tool();

		// check verify
		while (check_process(local, toolname) != 2) {
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
				log.error("sleep exception", e);
			}
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;
		// verify get result
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("Write data over!");

		// close slave cs
		if (!control_cs(csList.get(1), stop, 0))
			fail("close slave cs failed!");
		log.error("slave cs has been closed!");

		waitto(2);

		// uncomment cs group.conf
		if (!uncomment_line(csList.get(0),
				tair_bin + groupconf, dsList.get(0), "#"))
			fail("change group.conf failed!");
		if (!uncomment_line(csList.get(1),
				tair_bin + groupconf, dsList.get(0), "#"))
			fail("change group.conf failed!");
		// touch group.conf
		if (touch_flag != 0)
			touch_file(csList.get(0), tair_bin + groupconf);
		log.error("change group.conf and touch it");

		waitto(down_time);

		// check migration stat
		while (check_keyword(csList.get(0), start_migrate, tair_bin
				+ "logs/config.log") != 1) {
			log.debug("check if migration start on cs "
					+ csList.get(0) + " log");
			waitto(2);
			if (++waitcnt > 210)
				break;
		}
		if (waitcnt > 210)
			fail("down time arrived,but no migration started!");
		waitcnt = 0;
		log.error("down time arrived,migration start!");

		// restop slave cs
		if (!control_cs(csList.get(1), stop, 0))
			fail("restop slave cs failed!");
		log.error("Restop slave cs successful!");

		// wait for system restart to work
		log.error("wait system initialize ...");
		waitto(down_time);

		// check migration finished
		while (check_keyword(csList.get(0), finish_migrate, tair_bin
				+ "logs/config.log") != 1) {
			log.debug("check if migration finish on cs "
					+ csList.get(0) + " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("down time arrived,but no migration finished!");
		waitcnt = 0;
		log.error("down time arrived,migration finished!");

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;
		log.error("Read data over!");

		// verify get result
		log.error(getVerifySuccessful());
		assertEquals("verify data failed!", datacnt, getVerifySuccessful());
		log.error("Successfully Verified data!");

		// end test
		log.error(getVerifySuccessful());
		log.error("end config test Failover case 12");
	}

	@Test
	public void testFailover_13_recover_slave_cs_after_add_ds_and_rebuild() {
		log.error("start config test Failover case 13");
		int waitcnt = 0;

		// modify group configuration
		if (!comment_line(csList.get(0), tair_bin + groupconf,
				dsList.get(0), "#"))
			fail("change group.conf failed!");
		if (!comment_line(csList.get(1), tair_bin + groupconf,
				dsList.get(0), "#"))
			fail("change group.conf failed!");
		log.error("group.conf has been changed!");

		// start cluster
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");

		log.error("Start Cluster Successful!");

		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");

		// write 100k data to cluster
		execute_data_verify_tool();

		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;
		// verify get result
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("Write data over!");

		// close slave cs
		if (!control_cs(csList.get(1), stop, 0))
			fail("close slave cs failed!");
		log.error("slave cs has been closed!");

		waitto(2);

		// uncomment cs group.conf
		if (!uncomment_line(csList.get(0),
				tair_bin + groupconf, dsList.get(0), "#"))
			fail("change group.conf failed!");
		if (!uncomment_line(csList.get(1),
				tair_bin + groupconf, dsList.get(0), "#"))
			fail("change group.conf failed!");
		// touch group.conf
		if (touch_flag != 0)
			touch_file(csList.get(0), tair_bin + groupconf);
		log.error("change group.conf and touch it");

		waitto(down_time);

		// check migration stat
		while (check_keyword(csList.get(0), finish_migrate, tair_bin
				+ "logs/config.log") != 1) {
			log.debug("check if migration finished on cs "
					+ csList.get(0) + " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("down time arrived,but no migration started or finished!");
		waitcnt = 0;
		log.error("down time arrived,migration finished!");

		// restart slave cs
		if (!control_cs(csList.get(1), start, 0))
			fail("restart slave cs failed!");
		log.error("Restart slave cs successful!");

		// wait for system restart to work
		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		log.error("Read data over!");
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertEquals("verify data failed!", datacnt, getVerifySuccessful());
		log.error("Successfully Verified data!");

		// end test
		log.error("end config test Failover case 13");
	}

	@Test
	public void testFailover_14_recover_slave_cs_when_ds_down_and_rebuild_then_ds_join_again_generate_second_rebuild() {
		log.error("start config test Failover case 14");
		int waitcnt = 0;
		// start cluster
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");

		log.error("Start Cluster Successful!");

		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");

		// write 100k data to cluster
		execute_data_verify_tool();

		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;
		// verify get result
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("Write data over!");

		// close slave cs
		if (!control_cs(csList.get(1), stop, 0))
			fail("close slave cs failed!");
		log.error("slave cs has been closed!");

		waitto(2);
		// record the version times
		int versionCount = check_keyword(csList.get(0),
				finish_rebuild, tair_bin + "logs/config.log");

		// close ds
		if (!control_ds(dsList.get(0), stop, 0))
			fail("close ds failed!");
		log.error("ds has been closed!");

		waitto(down_time);

		// check rebuild stat
		while (check_keyword(csList.get(0), finish_rebuild, tair_bin
				+ "logs/config.log") != (versionCount + 1)) {
			log.debug("check if rebuild finished on cs "
					+ csList.get(0) + " log");
			waitto(2);
			if (++waitcnt > 210)
				break;
		}
		log.error("down time arrived,rebuild finished!");
		if (waitcnt > 210)
			fail("down time arrived,but no rebuild started or finished!");
		waitcnt = 0;

		// restart ds
		if (!control_ds(dsList.get(0), start, 0))
			fail("restart ds failed!");
		log.error("restart ds successful!");
		// touch conf file
		if (touch_flag != 0)
			touch_file(csList.get(0), tair_bin + groupconf);
		log.error("touch group.conf");
		waitto(down_time);

		// check firsh migration stat
		while (check_keyword(csList.get(0), start_migrate, tair_bin
				+ "logs/config.log") != 1) {
			log.debug("check if migration start on cs "
					+ csList.get(0) + " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("down time arrived,but no migration started!");
		waitcnt = 0;
		log.error("down time arrived,migration started!");

		// restart slave cs
		if (!control_cs(csList.get(1), start, 0))
			fail("restart slave cs failed!");
		log.error("Restart slave cs successful!");

		// wait for system restart to work
		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;
		log.error("Read data over!");
		// verify get result
		log.error(getVerifySuccessful());
		assertTrue("verify data failed!",
				getVerifySuccessful() / put_count_float > 0.79);
		log.error("Successfully Verified data!");

		// end test
		log.error("end config test Failover case 14");
	}

	@Test
	public void testFailover_15_recover_slave_cs_after_ds_down_and_rebuild_then_ds_join_again_generate_second_rebuild() {
		log.error("start config test Failover case 15");
		int waitcnt = 0;
		// start cluster
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");

		log.error("Start Cluster Successful!");

		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");

		// write 100k data to cluster
		execute_data_verify_tool();

		// check verify
		while (check_process(local, toolname) != 2) {
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
				log.error("sleep exception", e);
			}
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;
		// verify get result
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("Write data over!");

		// close slave cs
		if (!control_cs(csList.get(1), stop, 0))
			fail("close slave cs failed!");
		log.error("slave cs has been closed!");

		waitto(2);
		// record the version times
		int versionCount = check_keyword(csList.get(0),
				finish_rebuild, tair_bin + "logs/config.log");

		// close ds
		if (!control_ds(dsList.get(0), stop, 0))
			fail("close ds failed!");
		log.error("ds has been closed!");

		// change master cs cause down time by 2
		waitto(down_time * 2);

		// check migration stat
		while (check_keyword(csList.get(0), finish_rebuild, tair_bin
				+ "logs/config.log") != (versionCount + 1)) {
			log.debug("check if rebuild finished on cs "
					+ csList.get(0) + " log");
			waitto(2);
			if (++waitcnt > 210)
				break;
		}
		if (waitcnt > 210)
			fail("down time arrived,but no rebuild started or finished!");
		waitcnt = 0;
		log.error("down time arrived,rebuild finished!");

		// restart ds
		if (!control_ds(dsList.get(0), start, 0))
			fail("restart ds failed!");
		log.error("restart ds successful!");
		// touch conf file
		if (touch_flag != 0)
			touch_file(csList.get(0), tair_bin + groupconf);
		log.error("touch group.conf");

		// wait down time for touch
		waitto(down_time);

		// check first migration stat
		while (check_keyword(csList.get(0), finish_migrate, tair_bin
				+ "logs/config.log") != 1) {
			log.debug("check if migration stop on cs " + csList.get(0)
					+ " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("down time arrived,but no migration started or stoped!");
		waitcnt = 0;
		log.error("down time arrived,migration stoped!");

		// restart slave cs
		if (!control_cs(csList.get(1), start, 0))
			fail("restart slave cs failed!");
		log.error("Restart slave cs successful!");

		// wait for system restart to work
		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		log.error("Read data over!");
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertTrue("verify data failed!",
				getVerifySuccessful() / put_count_float > 0.8);
		log.error("Successfully Verified data!");

		// end test
		log.error("end config test Failover case 15");
	}

	@Test
	public void testFailover_16_recover_slave_cs_after_add_ds_and_rebuild_then_ds_close_again_generate_second_rebuild_after_rebuild() {
		log.error("start config test Failover case 16");
		int waitcnt = 0;

		// modify group configuration
		if (!comment_line(csList.get(0), tair_bin + groupconf,
				dsList.get(0), "#"))
			fail("change group.conf failed!");
		if (!comment_line(csList.get(1), tair_bin + groupconf,
				dsList.get(0), "#"))
			fail("change group.conf failed!");
		log.error("group.conf has been changed!");

		// start cluster
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");

		log.error("Start Cluster Successful!");

		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");

		// write 100k data to cluster
		execute_data_verify_tool();

		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;
		// verify get result
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("Write data over!");

		// close slave cs
		if (!control_cs(csList.get(1), stop, 0))
			fail("close slave cs failed!");
		log.error("slave cs has been closed!");

		waitto(2);
		// uncomment cs group.conf
		if (!uncomment_line(csList.get(0),
				tair_bin + groupconf, dsList.get(0), "#"))
			fail("change group.conf failed!");
		if (!uncomment_line(csList.get(1),
				tair_bin + groupconf, dsList.get(0), "#"))
			fail("change group.conf failed!");
		// touch group.conf
		if (touch_flag != 0)
			touch_file(csList.get(0), tair_bin + groupconf);
		log.error("change group.conf and touch it");

		// wait down time for touch
		waitto(down_time);

		// check firse migration stat
		while (check_keyword(csList.get(0), start_migrate, tair_bin
				+ "logs/config.log") != 1) {
			log.debug("check if migration start on cs "
					+ csList.get(1) + " log");
			waitto(2);
			if (++waitcnt > 210)
				break;
		}
		if (waitcnt > 210)
			fail("down time arrived,but no migration started!");
		waitcnt = 0;
		log.error("down time arrived,migration started!");
		// record the version times
		int versionCount = check_keyword(csList.get(0),
				finish_rebuild, tair_bin + "logs/config.log");

		// close ds
		if (!control_ds(dsList.get(0), stop, 0))
			fail("close ds failed!");
		log.error("ds has been closed!");

		waitto(down_time);

		// check migration stat
		while (check_keyword(csList.get(0), finish_rebuild, tair_bin
				+ "logs/config.log") != (versionCount + 1)) {
			log.debug("check if rebuild table finished on cs "
					+ csList.get(1) + " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("down time arrived,but no rebuild started or finished!");
		waitcnt = 0;
		log.error("down time arrived,rebuild table finished!");

		// restart slave cs
		if (!control_cs(csList.get(1), start, 0))
			fail("restart slave cs failed!");
		log.error("Restart slave cs successful!");

		// wait for system restart to work
		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		log.error("Read data over!");
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertTrue("verify data failed!",
				getVerifySuccessful() / put_count_float > 0.79);
		log.error("Successfully Verified data!");

		// end test
		log.error("end config test Failover case 16");
	}

	@Test
	public void testFailover_17_add_ds_shutdown_master_cs_ds_and_slave_cs_one_by_one_then_recover_one_by_one() {
		log.error("start config test Failover case 17");
		int waitcnt = 0;

		// modify group configuration
		if (!comment_line(csList.get(0), tair_bin + groupconf,
				dsList.get(0), "#"))
			fail("change group.conf failed!");
		if (!comment_line(csList.get(1), tair_bin + groupconf,
				dsList.get(0), "#"))
			fail("change group.conf failed!");
		if (!comment_line(csList.get(0), tair_bin + groupconf,
				dsList.get(1), "#"))
			fail("change group.conf failed!");
		if (!comment_line(csList.get(1), tair_bin + groupconf,
				dsList.get(1), "#"))
			fail("change group.conf failed!");
		log.error("group.conf has been changed!");

		// start cluster
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");
		log.error("start cluster successfull!");

		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");

		// write 100k data to cluster
		execute_data_verify_tool();

		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;
		// verify get result
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("Write data over!");

		// stop master cs
		if (!control_cs(csList.get(0), stop, 0))
			fail("stop master cs failed!");
		log.error("stop master cs successful!");

		// uncomment cs group.conf
		if (!uncomment_line(csList.get(0),
				tair_bin + groupconf, dsList.get(0), "#"))
			fail("change group.conf failed!");
		if (!uncomment_line(csList.get(1),
				tair_bin + groupconf, dsList.get(0), "#"))
			fail("change group.conf failed!");
		// touch group.conf
		if (touch_flag != 0)
			touch_file(csList.get(1), tair_bin + groupconf);
		log.error("change group.conf and touch it");

		// wait migration start
		waitto(down_time);

		// check migration stat
		while (check_keyword(csList.get(1), start_migrate, tair_bin
				+ "logs/config.log") != 1) {
			log.debug("check if migration started on cs "
					+ csList.get(1) + " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("down time arrived,but no migration started!");
		waitcnt = 0;
		log.error("down time arrived,migration started!");

		// restart master cs
		if (!control_cs(csList.get(0), start, 0))
			fail("restart master cs failed!");
		log.error("restart master cs successful!");

		// stop slave cs
		if (!control_cs(csList.get(1), stop, 0))
			fail("close slave cs failed!");
		log.error("stop slave cs successful!");

		// wait down time for master cs work
		waitto(down_time);

		// //check migration stat
		// while(check_keyword(csList.get(1), finish_migrate,
		// tair_bin+"logs/config.log")!=1)
		// {
		// log.debug("check if migration finished on cs "+csList.get(1)+" log");
		// waitto(2);
		// if(++waitcnt>210)break;
		// }
		// if(waitcnt>210)fail("down time arrived,but no migration started or finished!");
		// waitcnt=0;
		// log.error("down time arrived,migration finished!");

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		log.error("Read data over!");
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertEquals("verify data failed!", datacnt, getVerifySuccessful());
		log.error("Successfully Verified data!");

		// restart slave cs
		if (!control_cs(csList.get(1), start, 0))
			fail("restart slave cs failed!");
		log.error("restart slave cs successful!");

		// uncomment cs group.conf
		if (!uncomment_line(csList.get(0),
				tair_bin + groupconf, dsList.get(1), "#"))
			fail("change group.conf failed!");
		if (!uncomment_line(csList.get(1),
				tair_bin + groupconf, dsList.get(1), "#"))
			fail("change group.conf failed!");
		// touch group.conf
		if (touch_flag != 0)
			touch_file(csList.get(0), tair_bin + groupconf);
		log.error("change group.conf and touch it");

		// wait down time
		waitto(down_time);

		// check migration stat
		while (check_keyword(csList.get(0), finish_migrate, tair_bin
				+ "logs/config.log") != 1) {
			log.debug("check if migration started on cs "
					+ csList.get(0) + " log");
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("down time arrived,but no migration started or finished!");
		waitcnt = 0;
		log.error("down time arrived,migration started!");

		// stop master cs
		if (!control_cs(csList.get(0), stop, 0))
			fail("stop master cs failed!");
		log.error("stop master cs successful!");
		// wait to down time
		waitto(down_time);

		// //check migration stat
		// while(check_keyword(csList.get(1), finish_migrate,
		// tair_bin+"logs/config.log")!=1)
		// {
		// log.debug("check if migration finished on cs "+csList.get(1)+" log");
		// waitto(2);
		// if(++waitcnt>210)break;
		// }
		// if(waitcnt>210)fail("down time arrived,but no migration started or finished!");
		// waitcnt=0;
		// log.error("down time arrived,migration finished!");

		// restart master cs
		if (!control_cs(csList.get(0), start, 0))
			fail("restart master cs failed!");
		log.error("restart master cs successful!");
		// wait to down time
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		log.error("Read data over!");
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertEquals("verify data failed!", datacnt, getVerifySuccessful());
		log.error("Successfully Verified data!");

		log.error("end config test Failover case 17");
	}

	@Test
	public void testFailover_18_close_ds_close_master_cs_restart_it_after_add_a_ds_and_migration() {
		log.error("start config test Failover case 18!");

		int waitcnt = 0;

		// start part cluster
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");
		log.error("start cluster successful!");

		// wait down time for cluster to work
		waitto(down_time);

		// write verify data to cluster
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed!");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");
		execute_data_verify_tool();

		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;
		// verify get result
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("put data over!");
		// record the version times
		int versionCount = check_keyword(csList.get(0),
				finish_rebuild, tair_bin + "logs/config.log");

		// stop ds
		if (!control_ds(dsList.get(dsList.size() - 1),
				stop, 0))
			fail("stop ds failed!");
		log.error("ds has been closeed!");
		// wait down time for migration
		waitto(down_time);

		// check rebuild stat of start
		while (check_keyword(csList.get(0), finish_rebuild, tair_bin
				+ "logs/config.log") != (versionCount + 1)) {
			waitto(2);
			if (++waitcnt > 210)
				break;
		}
		if (waitcnt > 210)
			fail("check rebuild start time out!");
		waitcnt = 0;
		log.error("check rebuild finished!");

		// close master cs
		if (!control_cs(csList.get(0), stop, 0))
			fail("stop master cs failed!");
		log.error("close master cs successful!");

		// wait down time for cs change
		waitto(down_time);

		// restart master cs
		if (!control_cs(csList.get(0), start, 0))
			fail("restart master cs failed!");
		log.error("restart master cs successful!");

		// wait down time for cs change
		waitto(down_time);

		// close slave cs
		if (!control_cs(csList.get(1), stop, 0))
			fail("stop slave cs failed!");
		log.error("close slave cs successful!");

		// wait down time for cs change
		waitto(down_time);

		// restart slave cs
		if (!control_cs(csList.get(1), start, 0))
			fail("restart slave cs failed!");
		log.error("restart slave cs successful!");

		// wait down time for cs change
		waitto(down_time);

		// verify data
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify tool config file failed!");
		execute_data_verify_tool();

		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("get data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertTrue("verify data failed!",
				getVerifySuccessful() / put_count_float > 0.79);
		log.error("Successfully Verified data!");

		log.error("end config test Failover case 18");
	}

	@Test
	public void testFailover_19_close_master_cs_restart_it_when_close_ds_and_begin_migration() {
		log.error("start config test Failover case 19!");

		int waitcnt = 0;

		// start part cluster for next migration step
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");
		log.error("start cluster successful!");

		// wait down time for cluster to work
		waitto(down_time);

		// write verify data to cluster
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed!");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");
		execute_data_verify_tool();

		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;
		// verify get result\
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("put data over!");

		// close master cs
		if (!control_cs(csList.get(0), stop, 0))
			fail("stop master cs failed!");
		log.error("close master cs successful!");
		// record the version times
		int versionCount = check_keyword(csList.get(1),
				finish_rebuild, tair_bin + "logs/config.log");

		// stop ds
		if (!control_ds(dsList.get(dsList.size() - 1),
				stop, 0))
			fail("start ds failed!");
		log.error("close ds successful!");
		// wait down time for migration
		waitto(down_time);

		// check rebuild stat of start
		while (check_keyword(csList.get(1), finish_rebuild, tair_bin
				+ "logs/config.log") == versionCount) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("check rebuild finished time out!");
		waitcnt = 0;
		log.error("check rebuild finished!");

		// restart master cs
		if (!control_cs(csList.get(0), start, 0))
			fail("restart master cs failed!");
		log.error("restart master cs successful!");

		// wait down time for cs change
		waitto(down_time);

		// verify data
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify tool config file failed!");
		execute_data_verify_tool();

		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("get data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertTrue("verify data failed!",
				getVerifySuccessful() / put_count_float > 0.79);
		log.error("Successfully Verified data!");

		log.error("end config test Failover case 19");
	}

	@Test
	public void testFailover_20_close_slave_cs_restart_it_after_close_ds_and_migration() {
		log.error("start config test Failover case 20!");

		int waitcnt = 0;

		// start part cluster for next migration step
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");
		log.error("start cluster successful!");

		// wait down time for cluster to work
		waitto(down_time);

		// write verify data to cluster
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed!");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");
		execute_data_verify_tool();

		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;
		// verify get result
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("put data over!");

		// close slave cs
		if (!control_cs(csList.get(1), stop, 0))
			fail("stop slave cs failed!");
		log.error("close slave cs successful!");

		// record the version times
		int versionCount = check_keyword(csList.get(0),
				finish_rebuild, tair_bin + "logs/config.log");

		// stop ds
		if (!control_ds(dsList.get(dsList.size() - 1),
				stop, 0))
			fail("start ds failed!");
		log.error("stop ds successfuly");

		// wait down time for rebuild
		waitto(down_time);

		// check migration stat of start
		while (check_keyword(csList.get(0), finish_rebuild, tair_bin
				+ "logs/config.log") != (versionCount + 1)) {
			waitto(2);
			if (++waitcnt > 210)
				break;
		}
		if (waitcnt > 210)
			fail("check rebuild finished time out!");
		waitcnt = 0;
		log.error("check rebuild finished!");

		// restart slave cs
		if (!control_cs(csList.get(1), start, 0))
			fail("restart slave cs failed!");
		log.error("restart slave cs successful!");

		// wait down time for cs change
		waitto(down_time);

		// verify data
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify tool config file failed!");
		execute_data_verify_tool();

		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("get data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertTrue("verify data failed!",
				getVerifySuccessful() / put_count_float > 0.79);
		log.error("Successfully Verified data!");

		log.error("end config test Failover case 20");
	}

	@Test
	public void testFailover_21_shutdown_master_cs_and_slave_cs_one_by_one_then_recover_one_by_one() {
		log.error("start config test Failover case 21");
		int waitcnt = 0;
		// start cluster
		if (!control_cluster(csList, dsList, start, 0))
			fail("start cluster failed!");
		log.error("start cluster successfull!");

		log.error("wait system initialize ...");
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, put))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				datasize, put_count))
			fail("modify configure file failed");
		if (!modify_config_file(local, test_bin + toolconf,
				filename, kv_name))
			fail("modify configure file failed");

		// write 100k data to cluster
		execute_data_verify_tool();

		// check verify
		while (check_process(local, toolname) != 2) {
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
				log.error("sleep exception", e);
			}
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("put data time out!");
		waitcnt = 0;
		// verify get result
		int datacnt = getVerifySuccessful();
		log.error(getVerifySuccessful());
		assertTrue("put successful rate small than 90%!",
				datacnt / put_count_float > 0.9);
		log.error("Write data over!");

		// stop master cs
		if (!control_cs(csList.get(0), stop, 0))
			fail("stop master cs failed!");
		log.error("stop master cs successful!");

		// wait 2x(down time) to restart,make sure original slave cs become new
		// master cs
		waitto(down_time * 2);

		// restart master cs
		if (!control_cs(csList.get(0), start, 0))
			fail("restart master cs failed!");
		log.error("restart master cs successful!");
		// wait down time for master cs work
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		log.error("Read data over!");
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertEquals("verify data failed!", datacnt, getVerifySuccessful());
		log.error("Successfully Verified data!");

		// stop slave cs
		if (!control_cs(csList.get(1), stop, 0))
			fail("stop slave cs failed!");
		log.error("stop slave cs successful!");

		// wait 2x(down time) to restart,make sure original slave cs become new
		// master cs
		waitto(down_time * 2);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		log.error("Read data over!");
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertEquals("verify data failed!", datacnt, getVerifySuccessful());
		log.error("Successfully Verified data!");

		// restart slave cs
		if (!control_cs(csList.get(1), start, 0))
			fail("restart slave cs failed!");
		log.error("restart slave cs successful!");
		// wait down time for master cs work
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;
		log.error("Read data over!");

		// verify get result
		log.error(getVerifySuccessful());
		assertEquals("verify data failed!", datacnt, getVerifySuccessful());
		log.error("Successfully Verified data!");

		// stop master cs again
		if (!control_cs(csList.get(0), stop, 0))
			fail("stop master cs failed!");
		log.error("stop master cs successful!");

		// wait 2x(down time) to restart,make sure original slave cs become new
		// master cs
		waitto(down_time * 2);

		// stop slave cs again
		if (!control_cs(csList.get(1), stop, 0))
			fail("stop slave cs failed!");
		log.error("stop slave cs successful!");

		// keep all cs down for down_time seconds
		waitto(down_time);

		// restart slave cs again
		if (!control_cs(csList.get(1), start, 0))
			fail("restart slave cs failed!");
		log.error("restart slave cs successful!");

		// restart master cs again
		if (!control_cs(csList.get(0), start, 0))
			fail("restart master cs failed!");
		log.error("restart master cs successful!");
		// wait down time for master cs work
		waitto(down_time);

		// change test tool's configuration
		if (!modify_config_file(local, test_bin + toolconf,
				actiontype, get))
			fail("modify configure file failed");

		// read data from cluster
		execute_data_verify_tool();
		// check verify
		while (check_process(local, toolname) != 2) {
			waitto(2);
			if (++waitcnt > 150)
				break;
		}
		log.error("Read data over!");
		if (waitcnt > 150)
			fail("Read data time out!");
		waitcnt = 0;

		// verify get result
		log.error(getVerifySuccessful());
		assertEquals("verify data failed!", datacnt, getVerifySuccessful());
		log.error("Successfully Verified data!");

		log.error("end config test Failover case 21");
	}

	@Before
	public void setUp() {
        log.info("clean tool and cluster while setUp!");
		clean_tool(local);
		reset_cluster(csList, dsList);
		// execute_shift_tool(local, "conf5");// for kdb
		batch_uncomment(csList, tair_bin + groupconf, dsList, "#");
		if (!batch_modify(csList, tair_bin + groupconf, copycount,
				"1"))
			fail("modify configure file failed");
		if (!batch_modify(dsList, tair_bin + groupconf, copycount,
				"1"))
			fail("modify configure file failed");
	}

	@After
	public void tearDown() {
		log.info("clean tool and cluster while tearDown!");
        clean_tool(local);
        reset_cluster(csList, dsList);
        batch_uncomment(csList, tair_bin + groupconf, dsList, "#");
	}
}