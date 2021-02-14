package http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 通过以下方法获取你想要的值<br>
 * 请求头 getHeader()<br>
 * 请求体 json() / text() / content()<br>
 * 推荐使用getXxx()来获取json参数<br>
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

    /**
     * 返回状态码 <br>
     * Code: 200 means OK
     *
     * @return int
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * 返回对应请求头的内容
     *
     * @param key 请求头参数名
     * @return {@link String}
     */
    public String getHeader(String key) {
        return headers.getOrDefault(key, "");
    }

    /**
     * 将content解析成JSONObject
     *
     * @return {@link JSONObject}
     * @throws ClassCastException if content is not a json object
     */
    public JSONObject json() throws ClassCastException {
        return JSON.parseObject(text());
    }

    /**
     * 把 Json 对象包装成 JavaBean <br>
     * JavaBean 必须有无参构造函数
     *
     * @param clazz JavaBean的类
     * @param name  键名，使用<b>"a.b.c"</b>来获得嵌套的对象
     * @param <T>   任意JavaBean类
     * @return JavaBean
     */
    public <T> T getBean(Class<T> clazz, String name) {
        JSONObject json = json();
        String[] keys = name.split("\\.");
        for (String key : keys) {
            json = json.getJSONObject(key);
        }
        return JSON.toJavaObject(json, clazz);
    }

    /**
     * get content as string
     *
     * @return {@link String}
     */
    public String text() {
        try {
            return new String(content, getCharset());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * get content as bytes <br>
     * can be solved as image or others
     *
     * @return byte[]
     */
    public byte[] content() {
        return content;
    }

    //region get methods

    /**
     * 获得任意类型的对象及数组，以Object形式返回
     *
     * @param name 键名，使用<b>"a.b.c"</b>来获得嵌套的对象
     * @return 返回为 {@link Object}
     * @throws NoSuchElementException 如果不存在该键
     */
    public Object getParam(String name) {
        // 已知 get、getArr 在Object该状态下只可能抛出NullPointerException
        try {
            return get(Object.class, name);
        } catch (NullPointerException e) {
            try {
                return getArr(Object.class, name);
            } catch (NullPointerException ne) {
                throw new NoSuchElementException("parameter not found");
            }
        }
    }

    /**
     * 获得任意类型的对象
     *
     * @param clazz 对象的类
     * @param name  键名，使用<b>"a.b.c"</b>来获得嵌套的对象
     * @param <T>   任意类
     * @return 返回类型为 <b>T</b> 的对象
     * @throws JSONException        如果响应体不是 <b>application/json</b> 类型
     * @throws ClassCastException   如果最终得到的对象类型无法转换为 <b>T</b>
     * @throws NullPointerException 如果该键不存在
     */
    public <T> T get(Class<T> clazz, String name) throws JSONException, ClassCastException, NullPointerException {
        JSONObject json = json();
        String[] keys = name.split("\\.");
        for (String key : keys) {
            if (json.get(key) instanceof JSONObject) {
                json = ((JSONObject) json.get(key));
            } else if (json.get(key).getClass().equals(clazz)) {
                return json.getObject(key, clazz);
            } else {
                throw new ClassCastException(json.get(key).getClass().getSimpleName() + " couldn't cast to " + clazz.getSimpleName());
            }
        }
        throw new ClassCastException("JSONObject couldn't cast to " + clazz.getSimpleName());
    }

    /**
     * 获得对应值的对象
     *
     * @param name 键名，使用<b>"a.b.c"</b>来获得嵌套的对象
     * @return {@link String}
     */
    public String getString(String name) {
        try {
            return get(String.class, name);
        } catch (JSONException | ClassCastException | NullPointerException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获得对应值的对象
     *
     * @param name 键名，使用<b>"a.b.c"</b>来获得嵌套的对象
     * @return {@link Integer}
     */
    public Integer getInt(String name) {
        try {
            return get(Integer.class, name);
        } catch (JSONException | ClassCastException | NullPointerException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获得对应值的对象
     *
     * @param name 键名，使用<b>"a.b.c"</b>来获得嵌套的对象
     * @return {@link Float}
     */
    public Float getFloat(String name) {
        try {
            return get(Float.class, name);
        } catch (JSONException | ClassCastException | NullPointerException e) {
            e.printStackTrace();
        }
        return 0f;
    }

    /**
     * 获得对应值的对象
     *
     * @param name 键名，使用<b>"a.b.c"</b>来获得嵌套的对象
     * @return {@link Double}
     */
    public Double getDouble(String name) {
        try {
            return get(Double.class, name);
        } catch (JSONException | ClassCastException | NullPointerException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * 获得对应值的数组
     *
     * @param name 键名，使用<b>"a.b.c"</b>来获得嵌套的对象
     * @return array of {@link String}
     */
    public String[] getStrArr(String name) {
        try {
            return getArr(String.class, name).toArray(new String[0]);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    /**
     * 获得对应值的数组
     *
     * @param name 键名，使用<b>"a.b.c"</b>来获得嵌套的对象
     * @return array of {@link Integer}
     */
    public Integer[] getIntArr(String name) {
        try {
            return getArr(Integer.class, name).toArray(new Integer[0]);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return new Integer[0];
    }

    /**
     * 获得对应值的数组
     *
     * @param name 键名，使用<b>"a.b.c"</b>来获得嵌套的对象
     * @return array of {@link Float}
     */
    public Float[] getFloatArr(String name) {
        try {
            return getArr(Float.class, name).toArray(new Float[0]);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return new Float[0];
    }

    /**
     * 获得对应值的数组
     *
     * @param name 键名，使用<b>"a.b.c"</b>来获得嵌套的对象
     * @return array of {@link Double}
     */
    public Double[] getDoubleArr(String name) {
        try {
            return getArr(Double.class, name).toArray(new Double[0]);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return new Double[0];
    }

    /**
     * 获得任意类型的对象的集合{@link List}
     *
     * @param clazz 对象的类
     * @param name  键名，使用<b>"a.b.c"</b>来获得嵌套的对象
     * @param <T>   任意类
     * @return 返回类型为 <b>T</b> 的集合
     * @throws NullPointerException 如果该键不存在
     */
    public <T> List<T> getArr(Class<T> clazz, String name) throws NullPointerException {
        try {
            JSONArray jArr = get(JSONArray.class, name);
            return jArr.toJavaList(clazz);
        } catch (JSONException | ClassCastException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    //endregion


}
