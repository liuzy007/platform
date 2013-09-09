package com.pktech.oal.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HttpUtil {
    private static Log log = LogFactory.getLog(HttpUtil.class);

    /**
     * 执行一个HTTP GET请求，返回请求响应的HTML
     * 
     * @param url
     *            请求的URL地址
     * @param queryString
     *            请求的查询参数,可以为null
     * @param charset
     *            字符集
     * @param pretty
     *            是否美化
     * @return 返回请求响应的HTML
     */
    public static String doGet(String url, String queryString, String charset) {
        StringBuffer response = new StringBuffer();
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(url);
        try {
            if (StringUtils.isNotBlank(queryString)) {
                // 对get请求参数做了http请求默认编码，好像没有任何问题，汉字编码后，就成为%式样的字符串
                method.setQueryString(URIUtil.encodeQuery(queryString));
            }
            client.executeMethod(method);
            if (method.getStatusCode() == HttpStatus.SC_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), charset));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            }
        } catch (URIException e) {
            log.error("执行HTTP Get请求时，编码查询字符串“" + queryString + "”发生异常！", e);
        } catch (IOException e) {
            log.error("执行HTTP Get请求" + url + "时，发生异常！", e);
        } finally {
            method.releaseConnection();
        }
        return response.toString();
    }

    /**
     * 执行一个HTTP POST请求，返回请求响应的HTML
     * 
     * @param url
     *            请求的URL地址
     * @param params
     *            请求的查询参数,可以为null
     * @param charset
     *            字符集
     * @param pretty
     *            是否美化
     * @return 返回请求响应的HTML
     */
    public static String doPost(String url, Map<String, String> params, String charset) {
        StringBuffer response = new StringBuffer();
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(url);
        
//        method.setRequestHeader("accept", "*/*");
//        method.setRequestHeader("connection", "Keep-Alive");
////        postMethod.setRequestHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
//        method.setRequestHeader("Accept-Language", "zh-cn,zh;q=0.5");
//        // postMethod.setRequestHeader("Accept-Encoding", "gzip,deflate");
//        method.setRequestHeader("Content-Type", "text/html;charset=utf-8");

        // 设置Http Post数据
        if (params != null) {

            NameValuePair[] data = new NameValuePair[params.size()];
            // 将表单的值放入postMethod中
            int i = 0;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                data[i] = new NameValuePair(entry.getKey(), entry.getValue());
                i = i + 1;
            }
            method.setRequestBody(data);
        }
        try {
            client.executeMethod(method);
            if (method.getStatusCode() == HttpStatus.SC_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), charset));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            }
        } catch (IOException e) {
            log.error("执行HTTP Post请求" + url + "时，发生异常！", e);
        } finally {
            method.releaseConnection();
        }
        return response.toString();
    }

    public static void main(String[] args) {
        Map<String, String> m = new HashMap<String, String>();
        m.put("aa", "dfadsf");
        System.out.println(HttpUtil.doPost("http://127.0.0.1:8080/mizar/Gateway.do", m, "utf-8"));
    }
}
