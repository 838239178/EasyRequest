package cn.shijh.http;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;


/**
 * 非常简洁的Request类，使用方法类似于Python的request模块<br>
 * 返回{@link Response}对象
 *
 * @author ShiJh
 */
public class Request {
    private static RequestConfig config;
    private static PoolingHttpClientConnectionManager manager;
    private static CloseableHttpClient client;

    static {
        config = RequestConfig.DEFAULT;
        try (InputStream is = Request.class.getClassLoader().getResourceAsStream("httpRequestConfig.properties")) {
            PropertyResourceBundle bundle = new PropertyResourceBundle(is);
            config = RequestConfig.custom()
                    .setSocketTimeout(Integer.parseInt(bundle.getString("socketTimeout")))
                    .setConnectTimeout(Integer.parseInt(bundle.getString("connectTimeout")))
                    .build();

            try {
                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslcsf = new SSLConnectionSocketFactory(builder.build());

                // 配置同时支持 HTTP 和 HTTPS
                Registry<ConnectionSocketFactory> socketFactoryRegistry =
                        RegistryBuilder.<ConnectionSocketFactory>
                                create()
                                .register("cn/shijh/http", PlainConnectionSocketFactory.getSocketFactory()).register("https", sslcsf).build();

                // 初始化连接管理器
                manager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
                manager.setMaxTotal(Integer.parseInt(bundle.getString("connectionMaxTotal")));// 同时最多连接数

                // 设置最大路由
                manager.setDefaultMaxPerRoute(Integer.parseInt(bundle.getString("connectionMaxPerRoute")));
            } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                e.printStackTrace();
            }
        } catch (NullPointerException e) {
            Logger.getGlobal().warning("找不到配置文件或配置文件内容有误，已使用默认配置");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            Logger.getGlobal().warning("配置文件出错，已使用默认配置");
        }
        client = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .setConnectionManager(manager)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(3, false))
                .build();
    }

    /**
     * 发送一个get请求
     * 请求参数链接于地址
     *
     * @param url 请求地址
     * @return {@link Response}
     */
    public static Response get(String url) {
        return get(url, new HashMap<>());
    }

    /**
     * 发送一个get请求
     * 请求参数链接于地址
     * 请求头使用字典
     *
     * @param url    请求地址
     * @param header 请求头，使用字典
     * @return {@link Response}
     */
    public static Response get(String url, Map<String, String> header) {
        Response res = null;
        CloseableHttpResponse resp = null;

        try {
            //解析链接
            String[] urlParts = url.split("\\?");

            URIBuilder uri = new URIBuilder(urlParts[0]);

            if (urlParts.length >= 2) {
                String[] params = urlParts[1].split("&");
                for (String param : params) {
                    String[] pair = param.split("=");
                    if (pair.length > 2) {
                        String key = pair[0];
                        String value = pair[1];
                        uri.setParameter(key, value);
                    } else {
                        uri.setParameter("", pair[0]);
                    }
                }
            }

            //创建Get请求
            HttpGet httpGet = new HttpGet(uri.build());

            //设置请求头
            packageHeader(httpGet, header);

            resp = client.execute(httpGet);

            //发送请求
            res = new Response(resp.getEntity(), resp.getAllHeaders(), resp.getStatusLine().getStatusCode());
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resp != null) {
                    resp.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    private static void packageHeader(HttpRequestBase request, Map<String, String> headers) {
        headers.forEach(request::setHeader);
        if (request instanceof HttpEntityEnclosingRequestBase) {
            HttpEntityEnclosingRequestBase heerb = (HttpEntityEnclosingRequestBase) request;
            heerb.setHeader("Content-Type", String.valueOf(heerb.getEntity().getContentType()));
        }
    }

    /**
     * 发送post请求，参数使用字典
     *
     * @param url   请求地址
     * @param param 请求体参数，使用字典
     * @return {@link Response}
     */
    public static Response post(String url, Map<String, String> param) {
        return post(url, new HashMap<>(), param);
    }

    /**
     * 发送post请求，参数使用字典
     *
     * @param url  请求地址
     * @param text 请求体参数，使用字符串
     * @return {@link Response}
     */
    public static Response post(String url, String text) {
        return post(url, new HashMap<>(), text);
    }

    /**
     * 发送post请求，请求头和参数使用字典
     *
     * @param url    请求地址
     * @param header 请求头，使用字典
     * @param param  请求体参数，使用字典
     * @return {@link Response}
     */
    public static Response post(String url, Map<String, String> header, Map<String, String> param) {
        Response res = null;
        CloseableHttpResponse resp = null;
        try {
            HttpPost httpPost = new HttpPost(url);

            //设置请求体
            List<NameValuePair> params = new ArrayList<>();
            param.forEach((key, value) -> params.add(new BasicNameValuePair(key, value)));
            HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
            httpPost.setEntity(entity);

            //设置请求头，如果有
            packageHeader(httpPost, header);

            resp = client.execute(httpPost);
            res = new Response(resp.getEntity(), resp.getAllHeaders(), resp.getStatusLine().getStatusCode());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resp != null) {
                    resp.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    /**
     * 发送post请求，请求头和参数使用字典
     *
     * @param url    请求地址
     * @param header 请求头，使用字典
     * @param text   请求体，使用字符串
     * @return {@link Response}
     */
    public static Response post(String url, Map<String, String> header, String text) {
        Response res = null;
        CloseableHttpResponse resp = null;
        try {
            HttpPost httpPost = new HttpPost(url);

            //设置请求体
            StringEntity entity = new StringEntity(text, "UTF-8");
            httpPost.setEntity(entity);

            //设置请求头，如果有
            packageHeader(httpPost, header);

            resp = client.execute(httpPost);
            res = new Response(resp.getEntity(), resp.getAllHeaders(), resp.getStatusLine().getStatusCode());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resp != null) {
                    resp.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }
}
