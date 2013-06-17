package com.alibaba.napoli.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman Date: 2/29/12 Time: 1:59 下午
 */
public class HttpUtil {
    private static final Log log = LogFactory.getLog(HttpUtil.class);

    public static void createQueue(String consoleAddress, String queueName, String groupName) {
        if (groupName == null) {
            throw new IllegalStateException("please set groupName");
        }
        createQueue(consoleAddress, queueName, groupName, null);
    }

    public static void createQueue(String consoleAddress, String queueName, Integer machineCount) {
        if (machineCount == null) {
            throw new IllegalStateException("please set machinecount");
        }
        createQueue(consoleAddress, queueName, null, machineCount);
    }

    public static void createQueueIfNotExist(String consoleAddress, String queueName, String groupName) {
        if (groupName == null) {
            throw new IllegalStateException("please set groupName");
        }
        createQueueIfNotExist(consoleAddress, queueName, groupName, null);
    }

    public static void createQueueIfNotExist(String consoleAddress, String queueName, Integer machineCount) {
        if (machineCount == null) {
            throw new IllegalStateException("please set machinecount");
        }
        createQueueIfNotExist(consoleAddress, queueName, null, machineCount);
    }

    public static void deleteQueueFromVtopic(String consoleAddress, String topicName, String[] queues) {
        String[] address = consoleAddress.split("/");
        String url;
        if (address.length == 1) {
            url = "http://" + consoleAddress + "/napoli/autoTest/createVtopicAuto?vtopicName=";
        } else {
            url = "http://" + address[0] + "/napoli/autoTest/createVtopicAuto?schema=" + address[1] + "&vtopicName=";
        }

        url += topicName;
        for (int i = 0; i < queues.length; i++) {
            if (i == 0) {
                url += "&rqueueNames=" + queues[i];
            } else {
                url += "," + queues[i];
            }
        }
        String result = getHttpResponse(url);
        if (!result.equals("ok")) {
            throw new RuntimeException("create vtopic " + topicName + " error!");
        }
    }

    private static void createQueue(String consoleAddress, String queueName, String groupName, Integer machineCount) {
        String[] address = consoleAddress.split("/");
        String url;
        if (address.length == 1) {
            url = "http://" + consoleAddress + "/napoli/autoTest/createQueueAuto?queueName=";
        } else {
            url = "http://" + address[0] + "/napoli/autoTest/createQueueAuto?schema=" + address[1] + "&queueName=";
        }
        if (queueName == null) {
            throw new IllegalStateException("no queue name");
        }
        url += queueName.trim();

        if (groupName != null && groupName.trim().length() > 0) {
            url += "&groupName=" + groupName.trim();
        } else if (machineCount != null && machineCount >= 0) {
            url += "&machineCount=" + machineCount;
        }
        String result = getHttpResponse(url);
        if (!result.equals("ok")) {
            throw new RuntimeException("create queue " + queueName + " error!");
        }
    }

    private static void createQueueIfNotExist(String consoleAddress, String queueName, String groupName,
                                              Integer machineCount) {
        String[] address = consoleAddress.split("/");
        String url;
        if (address.length == 1) {
            url = "http://" + consoleAddress + "/napoli/autoTest/createQueueAuto?exist=true&queueName=";
        } else {
            url = "http://" + address[0] + "/napoli/autoTest/createQueueAuto?exist=true&schema=" + address[1]
                    + "&queueName=";
        }

        if (queueName == null) {
            throw new IllegalStateException("no queue name");
        }
        url += queueName.trim();

        if (groupName != null && groupName.trim().length() > 0) {
            url += "&groupName=" + groupName.trim();
        } else if (machineCount != null && machineCount >= 0) {
            url += "&machineCount=" + machineCount;
        }
        String result = getHttpResponse(url);
        if (!result.equals("ok")) {
            throw new RuntimeException("create queue " + queueName + " error!");
        }
    }

    public static void deleteQueue(String consoleAddress, String queueName) {
        if (consoleAddress == null || queueName == null) {
            throw new IllegalStateException("consoleAddress or queueName is null");
        }
        String[] address = consoleAddress.split("/");
        String url;
        if (address.length == 1) {
            url = "http://" + consoleAddress + "/napoli/autoTest/deleteManyQueue?queueName=";
        } else {
            url = "http://" + address[0] + "/napoli/autoTest/deleteManyQueue?schema=" + address[1] + "&queueName=";
        }
        url += queueName;
        String result = getHttpResponse(url);
        if (!result.equals("ok")) {
            throw new RuntimeException("delete queue " + queueName + " error! errMsg:"+result);
        }
    }

    public static void createVtopic(String consoleAddress, String topicName, String[] queues) {
        if (consoleAddress == null || topicName == null || queues == null || queues.length == 0) {
            throw new IllegalStateException("argument has null");
        }
        String[] address = consoleAddress.split("/");
        String url;
        if (address.length == 1) {
            url = "http://" + consoleAddress + "/napoli/autoTest/createVtopicAuto?vtopicName=";
        } else {
            url = "http://" + address[0] + "/napoli/autoTest/createVtopicAuto?schema=" + address[1] + "&vtopicName=";
        }
        url += topicName;
        for (int i = 0; i < queues.length; i++) {
            if (i == 0) {
                url += "&queueNames=" + queues[i];
            } else {
                url += "," + queues[i];
            }
        }
        String result = getHttpResponse(url);
        if (!result.equals("ok")) {
            throw new RuntimeException("create vtopic " + topicName + " error!");
        }
    }

    public static void deleteVtopic(String consoleAddress, String topicName) {
        if (consoleAddress == null || topicName == null) {
            throw new IllegalStateException("arguments has null");
        }
        String[] address = consoleAddress.split("/");
        String url;
        if (address.length == 1) {
            url = "http://" + consoleAddress + "/napoli/autoTest/deleteVtopicAuto?vtopicName=" + topicName;
        } else {
            url = "http://" + address[0] + "/napoli/autoTest/deleteVtopicAuto?schema=" + address[1] + "&vtopicName="
                    + topicName;
        }
        String result = getHttpResponse(url);
        if (!result.equals("ok")) {
            throw new RuntimeException("delete vtopic " + topicName + " error!");
        }
    }

    public static void createTopic(String consoleAddress, String topicName, String groupName) {
        String[] address = consoleAddress.split("/");
        if (address.length != 2) {
            throw new IllegalArgumentException("consoleAddress[" + consoleAddress + "] is error format");
        }

        String url = "http://" + address[0] + "/napoli/autoTest/createTopicAuto?schema=" + address[1] + "&topicName="
                + topicName + "&groupName=" + groupName.trim();
        String result = getHttpResponse(url);
        if (!result.equals("ok")) {
            throw new RuntimeException("create topic " + topicName + " error!");
        }
    }

    public static void createTopicIfNotExist(String consoleAddress, String topicName, String groupName) {
        String[] address = consoleAddress.split("/");
        if (address.length != 2) {
            throw new IllegalArgumentException("consoleAddress[" + consoleAddress + "] is error format");
        }

        String url = "http://" + address[0] + "/napoli/autoTest/createTopicAuto?exist=true&schema=" + address[1]
                + "&topicName=" + topicName + "&groupName=" + groupName.trim();
        String result = getHttpResponse(url);
        if (!result.equals("ok")) {
            throw new RuntimeException("create topic " + topicName + " error!");
        }
    }

    public static void deleteTopic(String consoleAddress, String topicName) {
        String[] address = consoleAddress.split("/");
        if (address.length != 2) {
            throw new IllegalArgumentException("consoleAddress[" + consoleAddress + "] is error format");
        }
        String url = "http://" + address[0] + "/napoli/autoTest/deleteTopicAuto?schema=" + address[1] + "&topicName="
                + topicName;
        String result = getHttpResponse(url);
        if (!result.equals("ok")) {
            throw new RuntimeException("delete topic " + topicName + " error!");
        }
    }

    public static String getHttpResponse(String link) {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        InputStream urlStream = null;

        //URL url = null;
        String result = "";
        try {
            URL url = new URL(link);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.connect();
            urlStream = conn.getInputStream();
            reader = new BufferedReader(new InputStreamReader(urlStream));
            String s = "";
            while ((s = reader.readLine()) != null) {
                result += s;
            }
            //System.out.println(result);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (urlStream != null) {
                    urlStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.getMessage(),e);
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return result;
    }

    public static void main(String[] args) {
        HttpUtil.createQueue("10.33.145.22:80", "queue.exampleQueue0", "one-hornetq");
    }

}
