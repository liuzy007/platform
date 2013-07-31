package com.alibaba.napoli.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.napoli.domain.client.ClientDestination;
import com.alibaba.napoli.domain.client.ClientQueue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman Date: 2/27/12 Time: 2:41 下午
 */
public class ConfigServiceHttpImpl {
    private static final Log log = LogFactory.getLog(ConfigServiceHttpImpl.class);
    private final String     consoleUrl;

    public ConfigServiceHttpImpl(String address) {
        consoleUrl = "http://" + address + "/";
    }

    public float getVersion() {
        return -1f;
    }

    public ClientDestination fetchDestination(String destName) {
        String result = HttpUtil.getHttpResponse(consoleUrl + destName.toUpperCase());
        return JsonUtil.fromJson(result);
    }

    public Map<String, ClientQueue> fetchQueue4Router() {
        String result = HttpUtil.getHttpResponse(consoleUrl + "routers");
        return JSON.parseObject(result, new TypeReference<HashMap<String, ClientQueue>>() {
        });
    }

    @SuppressWarnings("unchecked")
    public Map<String, ClientDestination> fetchDestination(List<String> nameList) {
        if (nameList == null || nameList.size() == 0) {
            return null;
        }
        Map<String, ClientDestination> result = new HashMap<String, ClientDestination>();
        for (String destinationName : nameList) {
            ClientDestination destination = fetchDestination(destinationName);
            result.put(destinationName, destination);
        }
        return result;
    }

    public String getNormandyAddress() {
        return HttpUtil.getHttpResponse(consoleUrl + "normandyAddress");
    }

    public static void main(String[] args) {
        ConfigServiceHttpImpl configService = new ConfigServiceHttpImpl("10.33.145.22/hz-cn");
        //System.out.println(configService.getVersion());
        Map<String, ClientQueue> routers = configService.fetchQueue4Router();
        //System.out.println(routers);
        long before = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            configService.fetchDestination("heymantest");
            //System.out.println(configService.fetchDestination("heymantest"));
        }
        System.out.println(System.currentTimeMillis() - before);
    }
}
