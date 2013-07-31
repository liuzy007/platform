package com.alibaba.napoli.client.mock;

import com.alibaba.napoli.domain.client.ClientDestination;
import com.alibaba.napoli.domain.client.ClientQueue;
import com.alibaba.napoli.domain.client.ClientVirtualTopic;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

public class EmbededConfigService {
    private BeanFactory                     beanFactory;
    private Map<String, ClientQueue>        queues;
    private Map<String, ClientVirtualTopic> topics;

    @SuppressWarnings("unchecked")
    public EmbededConfigService(String config) {
        beanFactory = new XmlBeanFactory(new ClassPathResource(config));
        queues = (Map) beanFactory.getBean("queues");
        topics = (Map) beanFactory.getBean("topics");
        if (queues == null) {
            queues = new HashMap<String, ClientQueue>();
        }
        if (topics == null) {
            topics = new HashMap<String, ClientVirtualTopic>();
        }
    }

    public Map<String, ClientDestination> fetchDestination(List<String> nameList) {
        Map<String, ClientDestination> ret = new HashMap<String, ClientDestination>();
        for (String key : nameList) {
            if (queues.containsKey(key)) {
                ret.put(key, queues.get(key));
            } else if (topics.containsKey(key)) {
                ret.put(key, topics.get(key));
            }
        }
        return ret;
    }

    public ClientDestination fetchDestination(String destName) {
        if (queues.containsKey(destName)) {
            return queues.get(destName);
        } else if (topics.containsKey(destName)) {
            return topics.get(destName);
        }
        throw new RuntimeException("no destinationContext " + destName);
    }

    public Map<String, ClientQueue> fetchQueue4Router() {
        Map<String, ClientQueue> ret = new HashMap<String, ClientQueue>();
        for (String key : queues.keySet()) {
            ClientQueue queue = queues.get(key);
            if (queue != null && queue.getRouterTargetUrl() != null) {
                ret.put(key, queues.get(key));
            }
        }
        return ret;
    }

    public float getVersion() {
        return 1.3f;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public String getNormandyAddress() {
        return null;
    }
}
