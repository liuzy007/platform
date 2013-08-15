package com.alibaba.napoli.metamorphosis.utils;

import java.util.ArrayList;
import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.apache.commons.lang.StringUtils;

import com.alibaba.napoli.metamorphosis.utils.MetaZookeeper.ZKGroupTopicDirs;
import com.alibaba.napoli.metamorphosis.utils.MetaZookeeper.ZKTopicBorkerRightDirs;
import com.alibaba.napoli.metamorphosis.utils.MetaZookeeper.ZKTopicRightDirs;

public class MetaZookeeperHelper {
	
	private final MetaZookeeper zookeeper;
	
	public MetaZookeeperHelper(MetaZookeeper zookeeper){
		this.zookeeper = zookeeper;
	}
	
	public List<String> getAllCousumerGroups(String topic,int brokerId) {
		List<String> returnGroups = new ArrayList<String>();
		String consumerDir = zookeeper.consumersPath;
		List<String> allGroups =  ZkUtils.getChildren(zookeeper.getZkClient(), consumerDir);
		if(allGroups != null){
			for(String group:allGroups){
				final ZKGroupTopicDirs topicDirs = zookeeper.new ZKGroupTopicDirs(topic,group);
				final String znode = topicDirs.consumerOffsetDir ;
				if(checkOffsetNode(znode, brokerId).size()>0){
					returnGroups.add(group);
				}
			}
		}
		return returnGroups;
	}
	
	/**
	 * get all offset path which container specific topic and borkerid
	 * @param topic specific topic it exist in comsumers
	 * @param borkerId specific borkerid it exist in comsumers 
	 * @return
	 */
	public List<String> getAllPath(String topic,int borkerId){
		List<String> paths = new ArrayList<String>();
		String consumerDir = zookeeper.consumersPath;
		List<String> allGroups =  ZkUtils.getChildren(zookeeper.getZkClient(), consumerDir);
		if(allGroups != null){
			for(String group:allGroups){
				final ZKGroupTopicDirs topicDirs = zookeeper.new ZKGroupTopicDirs(topic,group);
				final String offsetNode = topicDirs.consumerOffsetDir ;
				paths.addAll(checkOffsetNode(offsetNode,borkerId));
			}
		}
		return paths;
	}
	
	private List<String> checkOffsetNode(String offsetNode,int borkerId){
		List<String> paths = new ArrayList<String>();
		if(StringUtils.isBlank(offsetNode)){
			return paths;
		}
		if(ZkUtils.pathExists(zookeeper.getZkClient(), offsetNode)){
			List<String> partitions = ZkUtils.getChildren(zookeeper.getZkClient(), offsetNode);
			if(partitions != null){
				for(String partition:partitions){
					if(StringUtils.startsWith(partition, borkerId+"-")){
						paths.add(offsetNode+ "/" +partition);
					}
				}
				
			}
		}
		return paths;
	}
	
	public void createTopicBrokerAllRight(String topic,int brokerId) throws Exception{
		ZKTopicBorkerRightDirs dirs = zookeeper.new ZKTopicBorkerRightDirs(topic,brokerId);
		ZkUtils.createEphemeralPath(zookeeper.getZkClient(), dirs.infoTopicBrokerReadRightDir, "");
		ZkUtils.createEphemeralPath(zookeeper.getZkClient(), dirs.infoTopicBrokerWriteRightDir, "");
	}
	
	public void createTopicBrokerReadRight(String topic,int brokerId) throws Exception{
		ZKTopicBorkerRightDirs dirs = zookeeper.new ZKTopicBorkerRightDirs(topic,brokerId);
		ZkUtils.createEphemeralPath(zookeeper.getZkClient(), dirs.infoTopicBrokerReadRightDir, "");
	}
	
	public void createTopicBrokerWriteRight(String topic,int brokerId) throws Exception{
		ZKTopicBorkerRightDirs dirs = zookeeper.new ZKTopicBorkerRightDirs(topic,brokerId);
		ZkUtils.createEphemeralPath(zookeeper.getZkClient(), dirs.infoTopicBrokerWriteRightDir, "");
	}
	
	public void deleteTopicBrokerAllRight(String topic,int brokerId) throws Exception{
		ZKTopicBorkerRightDirs dirs = zookeeper.new ZKTopicBorkerRightDirs(topic,brokerId);
		ZkUtils.deletePath(zookeeper.getZkClient(), dirs.infoTopicBrokerReadRightDir);
		ZkUtils.deletePath(zookeeper.getZkClient(), dirs.infoTopicBrokerWriteRightDir);
	}
	
	public void deleteTopicBrokerReadRight(String topic,int brokerId) throws Exception{
		ZKTopicBorkerRightDirs dirs = zookeeper.new ZKTopicBorkerRightDirs(topic,brokerId);
		ZkUtils.deletePath(zookeeper.getZkClient(), dirs.infoTopicBrokerReadRightDir);
	}
	
	public void deleteTopicBrokerWriteRight(String topic,int brokerId) throws Exception{
		ZKTopicBorkerRightDirs dirs = zookeeper.new ZKTopicBorkerRightDirs(topic,brokerId);
		ZkUtils.deletePath(zookeeper.getZkClient(), dirs.infoTopicBrokerWriteRightDir);
	}
	
	public List<String> getAllReadRightBrokers(String topic){
		ZKTopicRightDirs dirs = zookeeper.new ZKTopicRightDirs(topic);
		return ZkUtils.getChildren(zookeeper.getZkClient(), dirs.infoTopicReadRightDir);
	}
	
	public List<String> getAllWriteRightBrokers(String topic){
		ZKTopicRightDirs dirs = zookeeper.new ZKTopicRightDirs(topic);
		return ZkUtils.getChildren(zookeeper.getZkClient(), dirs.infoTopicWriteRightDir);
	}
	
	public void addUrgentMessage(String messageId,String content) throws Exception{
		if(StringUtils.isBlank(messageId) || StringUtils.isBlank(content))
			return;
		ZkUtils.createPersistentPath(zookeeper.getZkClient(), zookeeper.infoUrgentMessagesPath+"/"+messageId, content);
	}
	
	public void deleteUrgentMessage(String messageId) throws Exception{
		ZkUtils.deletePath(zookeeper.getZkClient(), zookeeper.infoUrgentMessagesPath+"/"+messageId);
	}
	
	public void subscribeUrgentMessages(IZkChildListener listener) throws Exception{
		ZkUtils.makeSurePersistentPathExists(zookeeper.getZkClient(), zookeeper.infoUrgentMessagesPath);
		zookeeper.getZkClient().subscribeChildChanges( zookeeper.infoUrgentMessagesPath,listener);
	}
	
	public String getUrgentMessageContent(String messageId){
		return ZkUtils.readData(zookeeper.getZkClient(), zookeeper.infoUrgentMessagesPath+"/"+messageId);
	}

}
