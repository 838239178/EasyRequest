package http;

import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

public class RequestTest {

    @org.junit.Test
    public void get() {
        Response resp = Request.get("http://m.fjcyl.com/validateCode?0.312311width=58&height=19&num=4", new HashMap<>());
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

    @org.junit.Test
    public void post() {
    }
}