package http;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
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

    static {
        config = RequestConfig.DEFAULT;
        try (InputStream is = Request.class.getClassLoader().getResourceAsStream("httpRequestConfig.properties")) {
            PropertyResourceBundle bundle = new PropertyResourceBundle(is);
            config = RequestConfig.custom()
                    .setSocketTimeout(Integer.parseInt(bundle.getString("socketTimeout")))
                    .setConnectTimeout(Integer.parseInt(bundle.getString("connectTimeout")))
                    .build();
        } catch (NullPointerException e) {
            Logger.getGlobal().warning("找不到配置文件或配置文件内容有误，已使用默认RequestConfig配置");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            Logger.getGlobal().warning("配置文件出错，已使用默认RequestConfig配置");
        }
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

        try (CloseableHttpClient client = getHttpClient()) {
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
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                resp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    private static CloseableHttpClient getHttpClient() {
        return HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();
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
        try (CloseableHttpClient client = getHttpClient()) {
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
        try (CloseableHttpClient client = getHttpClient()) {
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
