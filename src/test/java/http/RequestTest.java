package http;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


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

    }
}