package com.wealth.fly.common;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpEntity;
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
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientUtil {


  private static CloseableHttpClient client = null;
  private static RequestConfig requestConfig = null;
  private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtil.class);
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
//        requestConfigBuilder.setProxy(new HttpHost("127.0.0.1",1080,"http"));
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

  public static void main(String[] args) throws Exception {
    String result = get("https://www.baidu.com");

    System.out.println(">>>>>" + result);
  }

  public static String sendPostUrl(String url, String content) throws IOException {

    HttpPost httpPost = new HttpPost(url);
    httpPost.setConfig(requestConfig);
    httpPost.setEntity(new StringEntity(content, "utf-8"));
    httpPost.setHeader("accept", "*/*");
    httpPost.setHeader("Content-Type", "application/json");
    httpPost.setHeader("Connection", "Keep-Alive");
    httpPost.setHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");

    CloseableHttpResponse httpResponse = client.execute(httpPost);

    HttpEntity respnoseEntity = httpResponse.getEntity();
    String result = EntityUtils.toString(respnoseEntity, "utf-8");
    EntityUtils.consume(httpResponse.getEntity());

    return result;
  }

  public static String get(String url) throws IOException {
    LOGGER.info("receive get request " + url);
    HttpGet httpGet = new HttpGet(url);
    CloseableHttpResponse httpResponse = client.execute(httpGet);

    HttpEntity respnoseEntity = httpResponse.getEntity();
    String result = EntityUtils.toString(respnoseEntity, "utf-8");
    EntityUtils.consume(httpResponse.getEntity());
    LOGGER.info("get request response " + result);
    return result;
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
