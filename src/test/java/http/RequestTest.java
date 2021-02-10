package http;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;


public class RequestTest {

    @Test
    public void get() {
        Response resp = Request.get("http://m.fjcyl.com/validateCode?0.312311width=58&height=19&num=4");
        try {
            BufferedImage im = ImageIO.read(new ByteArrayInputStream(resp.content()));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(im, "jpeg", baos);
            byte[] data = baos.toByteArray();
            System.out.println(Base64.encodeBase64String(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void post() {
        String url = "http://data.zz.baidu.com/urls?site=https://blog.pressed.top&token=by6mvvujkkDYRcdo";
        String content = "https://blog.pressed.top/LinkGame";
        HashMap<String, String> header = new HashMap<>();
        header.put("User-Agent", "curl/7.12.1");
        header.put("Host", "data.zz.baidu.com");
        Response resp = Request.post(url, header, content);
        System.out.println(resp.text());
    }
}