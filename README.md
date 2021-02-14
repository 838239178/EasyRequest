# EasyRequest

 apache httpclient的简单封装，让java像python一样简单！

## Dependencies 依赖

### JDK 环境

`jdk 1.8.0` or `JavaSE 8`

### Lib 外部库

```xml
    <dependencies>
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpclient</artifactId>
            <version>4.5.2</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.73</version>
        </dependency>
    </dependencies>
```

### Installation 安装方法

目前请下载Release中的`.jar`文件，并在IDE里引用。后续可能发布到Maven仓库中，请持续关注。

1.  download from release then import to your project
2.  use `Maven`to import (TODO)

### Usage 运行方法

#### Request

1. 使用`get()`发送请求

   该方法有两个参数，可缺省`headers`使用

   - url：请求地址必须以 `http://`或`https://`开头，可以使用`?key1=value1&key2=value2`来携带参数
   - headers：请求头，参数类型为 `HashMap<String,String>`

2. 使用 `post()`发送请求

   该方法有两种类型，每种都要三种参数，可缺省`headers`使用

   - url：说明同上

   - headers：说明同上

   - param/text：

     对应两个重载

     param对应 `application/x-www.from-urlencoded` 请求体，参数类型为 `HashMap<String, String>`

     text对应 `text/plain`or`application/json`请求体，参数类型 `String`

#### Response

1. 使用 `getHeader(name)`获取请求头内容，如果不存在会返回空字符串

2. 使用 `content()`获取请求体二进制数组 `byte[]`

3. 使用 `json()`解析请求体并获取JSON对象`com.alibaba.fastjson.JSONObject`

4. 使用 `text()`获取请求体的纯文本内容 `String`

5. 如果请求体类型是`application/json`，推荐使用 `getXxx(name)`or`getXxxArr(name)`获取参数，支持获取String，Integer，Float，Double 以及对应的数组，这是些是安全的方法，如果出现错误将返回空数组或者一些基本值，如`0`和空字符串，它将不会返回`null`~ :smile:

   `name`：对于复杂的 JSON 对象，请使用`a.b.c`这种格式来直接获取嵌套的对象，example`getString("school.student.name")`

6. 当然也可使用泛型方法`get(class, name)`or`getArr(class, name)`来获取其他类型，但是你需要处理异常！:disappointed:

7. 新增：当请求体类型是`application/json`时，使用`getBean(class, name)`来快速转换Json，获得一个JavaBean对象，有关JavaBean的标准请移步[JavaBean的定义](https://www.liaoxuefeng.com/wiki/1252599548343744/1260474416351680)，另外请注意使用时确保存在**无参构造函数**！

8. 新增：当请求体类型是`application/json`时，使用`getParam(name)`可以获得任意对象及数组，返回为`Object`类型