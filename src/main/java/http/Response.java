package http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * 通过以下方法获取你想要的值<br>
 * 请求头 getHeader()<br>
 * 请求体 json() / text() / content()<br>
 * 状态码 getStatusCode()<br>
 */
public class Response {

    private final byte[] content;
    private final HashMap<String, String> headers;
    private final int statusCode;

    public Response(HttpEntity entity, Header[] headers, int statusCode) throws IOException {
        this.content = EntityUtils.toByteArray(entity);
        this.headers = new HashMap<>();
        this.statusCode = statusCode;
        setHeaders(headers);
    }

    public void setHeaders(Header[] headers) {
        for (Header header : headers) {
            this.headers.put(header.getName(), header.getValue());
        }
    }

    public String getCharset() {
        String ContentType = getHeader("Content-Type");
        String[] param = ContentType.split(";");
        for (String p : param) {
            if (p.toLowerCase().contains("charset")) {
                return p.split("=")[1];
            }
        }
        return "utf-8";
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getHeader(String key) {
        return headers.getOrDefault(key, "");
    }


    public Map<String, Object> json() {
        JSONObject json = JSON.parseObject(text());
        return json.getInnerMap();
    }

    public String text() {
        try {
            return new String(content, getCharset());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public byte[] content() {
        return content;
    }

}
