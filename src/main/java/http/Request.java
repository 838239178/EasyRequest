package http;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
* 非常简洁的Request类，使用方法类似于Python的request模块<br>
* 返回{@link Response}对象
*
* @author ShiJh
*/
public class Request {

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

        try (CloseableHttpClient client = HttpClients.createDefault()) {
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

    private static void packageHeader(HttpRequestBase request, Map<String, String> headers) {
        headers.forEach(request::setHeader);
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
     * @param json 请求体参数，使用json格式的字符串
     * @return {@link Response}
     */
    public static Response post(String url, String json) {
        return post(url, new HashMap<>(), json);
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
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);

            //设置请求体
            List<NameValuePair> params = new ArrayList<>();
            param.forEach((key, value) -> params.add(new BasicNameValuePair(key, value)));
            HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
            httpPost.setEntity(entity);

            //设置请求头，如果有
            packageHeader(httpPost, header);
            httpPost.setHeader(entity.getContentType());

            resp = client.execute(httpPost);
            res = new Response(resp.getEntity(), resp.getAllHeaders(), resp.getStatusLine().getStatusCode());
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

    /**
     * 发送post请求，请求头和参数使用字典
     *
     * @param url    请求地址
     * @param header 请求头，使用字典
     * @param json   请求体参数，使用json格式的字符串
     * @return {@link Response}
     */
    public static Response post(String url, Map<String, String> header, String json) {
        Response res = null;
        CloseableHttpResponse resp = null;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);

            //设置请求体
            StringEntity entity = new StringEntity(json, "UTF-8");
            httpPost.setEntity(entity);

            //设置请求头，如果有
            packageHeader(httpPost, header);
            httpPost.setHeader(entity.getContentType());

            resp = client.execute(httpPost);
            res = new Response(resp.getEntity(), resp.getAllHeaders(), resp.getStatusLine().getStatusCode());
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
}
