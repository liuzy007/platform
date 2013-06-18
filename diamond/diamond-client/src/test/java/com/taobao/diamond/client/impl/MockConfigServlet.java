package com.taobao.diamond.client.impl;

import static com.taobao.diamond.common.Constants.LINE_SEPARATOR;
import static com.taobao.diamond.common.Constants.WORD_SEPARATOR;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.md5.MD5;


/**
 * Mock config servlet
 * 
 * @author boyan
 * @date 2010-6-29
 */
class MockConfigServlet extends HttpServlet {
    volatile String configInfo = "hello world";
    volatile String md5 = MD5.getInstance().getMD5String(configInfo);
    volatile long lastModified = System.currentTimeMillis();
    volatile ConfigKey configKey;
    volatile String probeModify = null;


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=GBK");
        resp.setHeader(Constants.CONTENT_MD5, md5);
        String lastModifiedFromClient = req.getHeader(Constants.IF_MODIFIED_SINCE);
        if (lastModifiedFromClient != null && lastModifiedFromClient.equals(String.valueOf(lastModified))) {
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        if (this.configInfo == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        resp.setHeader(Constants.LAST_MODIFIED, String.valueOf(lastModified));

        PrintWriter writer = resp.getWriter();
        writer.write(configInfo);
        writer.flush();
        // super.doGet(req, resp);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        probeModify = req.getParameter(Constants.PROBE_MODIFY_REQUEST);
        final List<ConfigKey> configKeyList = getProbeConfigKeyList(probeModify);

        StringBuilder resultBuilder = new StringBuilder();
        StringBuilder newResultBuilder = new StringBuilder();

        for (ConfigKey key : configKeyList) {
            resultBuilder.append(key.getDataId()).append(":").append(key.getGroup()).append(";");
            newResultBuilder.append(key.getDataId()).append(WORD_SEPARATOR).append(key.getGroup())
                .append(LINE_SEPARATOR);

        }
        resp.addHeader(Constants.PROBE_MODIFY_RESPONSE, resultBuilder.toString());
        resp.addHeader(Constants.PROBE_MODIFY_RESPONSE_NEW, newResultBuilder.toString());
        // super.doPost(req, resp);
    }


    private static List<ConfigKey> getProbeConfigKeyList(String configKeysString) {
        List<ConfigKey> configKeyList = new LinkedList<ConfigKey>();
        if (null == configKeysString || "".equals(configKeysString)) {
            return configKeyList;
        }
        String[] configKeyStrings = configKeysString.split(LINE_SEPARATOR);
        for (String configKeyString : configKeyStrings) {
            String[] configKey = configKeyString.split(WORD_SEPARATOR);
            if (configKey.length > 3) {
                continue;
            }
            ConfigKey key = new ConfigKey();
            if ("".equals(configKey[0])) {
                continue;
            }
            key.setDataId(configKey[0]);
            if (configKey.length >= 2 && !"".equals(configKey[1])) {
                key.setGroup(configKey[1]);
            }
            if (configKey.length == 3 && !"".equals(configKey[2])) {
                key.setMd5(configKey[2]);
            }
            configKeyList.add(key);
        }

        return configKeyList;
    }


    public List<ConfigKey> getConfigKeyList(String configKeysString) {
        List<ConfigKey> configKeyList = new LinkedList<ConfigKey>();
        if (this.configKey != null)
            configKeyList.add(this.configKey);

        return configKeyList;
    }

    public static class ConfigKey {
        private String dataId;
        private String group;
        private String md5;


        public ConfigKey() {
        }


        public ConfigKey(String dataId, String group, String md5) {
            super();
            this.dataId = dataId;
            this.group = group;
            this.md5 = md5;
        }


        public String getDataId() {
            return dataId;
        }


        public void setDataId(String dataId) {
            this.dataId = dataId;
        }


        public String getGroup() {
            return group;
        }


        public void setGroup(String group) {
            this.group = group;
        }


        public String getMd5() {
            return md5;
        }


        public void setMd5(String md5) {
            this.md5 = md5;
        }


        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("DataID: ").append(dataId).append("\r\n");
            sb.append("Group: ").append((null == group ? "" : group)).append("\r\n");
            sb.append("MD5: ").append((null == md5 ? "" : md5)).append("\r\n");
            return sb.toString();
        }
    }

}
