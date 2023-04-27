package com.wealth.fly.common;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class HttpClientUtil {


    private static CloseableHttpClient client = null;
    private static RequestConfig requestConfig = null;
    private static final int MAX_CONNECTIONS = 10;
    private static final int MAX_PER_ROUTE = 10;
    private static final int CONNECT_TIMEOUT_IN_SECONDS = 30000;
    private static final int READ_TIMEOUT_IN_SECONDS = 30000;
    private static final int CONNECT_REQUEST_TIMEOUT = 30000;


    static {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(MAX_CONNECTIONS);
        connManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);

        client = HttpClients.custom().setConnectionManager(connManager).build();
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                .setStaleConnectionCheckEnabled(true).setConnectionRequestTimeout(CONNECT_REQUEST_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT_IN_SECONDS).setSocketTimeout(READ_TIMEOUT_IN_SECONDS);
        requestConfigBuilder.setProxy(new HttpHost("127.0.0.1", 8001, "http"));
        requestConfig = requestConfigBuilder.build();

        IdleConnectionMonitorThread idleConnectionMonitorThread = new IdleConnectionMonitorThread(
                connManager);
        idleConnectionMonitorThread.start();
        try {
            idleConnectionMonitorThread.join(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static String postBody(String url, String content, String apiDesc) throws IOException {
        return postBody(url, content, null, apiDesc);
    }

    public static String postBody(String url, String requestBody, Map<String, String> headers, String apiDesc) throws IOException {

        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        httpPost.setEntity(new StringEntity(requestBody, "utf-8"));

        setHeaders(httpPost, headers);

        log.debug("提交post请求体-{} {} {}", apiDesc, url, requestBody);
        CloseableHttpResponse httpResponse = client.execute(httpPost);

        HttpEntity respnoseEntity = httpResponse.getEntity();
        String responseBody = EntityUtils.toString(respnoseEntity, "utf-8");
        EntityUtils.consume(httpResponse.getEntity());

        log.debug("post请求响应-{} {} request:{} response:{}", apiDesc, url, requestBody, responseBody);
        return responseBody;
    }

    public static String get(String url, String apiDesc) throws IOException {
        return get(url, null, apiDesc);
    }

    public static String get(String url, Map<String, String> headers, String apiDesc) throws IOException {
        log.debug("get请求-{} {}", apiDesc == null ? "" : apiDesc, url);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        setHeaders(httpGet, headers);
        CloseableHttpResponse httpResponse = client.execute(httpGet);

        HttpEntity responseEntity = httpResponse.getEntity();
        String result = EntityUtils.toString(responseEntity, "utf-8");
        EntityUtils.consume(httpResponse.getEntity());
        log.debug("get请求响应-{} {} {} ", apiDesc == null ? "" : apiDesc, url, result);
        return result;
    }

    public static String get(String url) throws IOException {
        return get(url, null, null);
    }

    public static String postParams(Object paramObj, String url) throws IOException {
        Map params = null;
        try {
            params = BeanUtils.describe(paramObj);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException("解析参数错误" + JsonUtil.toJSONString(paramObj));
        }
        return postParams(params, url);
    }


    public static String postParams(Map<String, Object> params, String url) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("User-Agent", "Rich Powered/1.0");
        httpPost.setConfig(requestConfig);

        List list = new ArrayList<NameValuePair>();
        for (String key : params.keySet()) {
            list.add(new BasicNameValuePair(key, String.valueOf(params.get(key))));
        }
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, "utf-8");
        httpPost.setEntity(entity);

        CloseableHttpResponse httpResponse = client.execute(httpPost);

        HttpEntity respnoseEntity = httpResponse.getEntity();
        String result = EntityUtils.toString(respnoseEntity, "utf-8");
        EntityUtils.consume(httpResponse.getEntity());

        return result;
    }

    private static void setHeaders(AbstractHttpMessage httpMessage, Map<String, String> headersMap) {
        httpMessage.setHeader("accept", "*/*");
        httpMessage.setHeader("Content-Type", "application/json");
        httpMessage.setHeader("Connection", "Keep-Alive");
        httpMessage.setHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        if (headersMap == null || headersMap.isEmpty()) {
            return;
        }
        for (String headerName : headersMap.keySet()) {
            httpMessage.setHeader(headerName, headersMap.get(headerName));
        }
    }

    public static class IdleConnectionMonitorThread extends Thread {

        private final HttpClientConnectionManager connMgr;
        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(5000);
                        // Close expired connections
                        connMgr.closeExpiredConnections();
                        // Optionally, close connections
                        // that have been idle longer than 300  sec
                        connMgr.closeIdleConnections(300, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        public void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }

    }
}
