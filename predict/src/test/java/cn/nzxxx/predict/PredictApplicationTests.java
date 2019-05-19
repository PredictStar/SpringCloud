package cn.nzxxx.predict;

import cn.nzxxx.predict.toolitem.tool.Helper;
import cn.nzxxx.predict.webrequest.HelloController;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.hibernate.validator.internal.util.StringHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import org.xmlunit.util.Convert;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Date;


@RunWith(SpringRunner.class)//引入spring对JUnit4的支持
@SpringBootTest
public class PredictApplicationTests {
    private MockMvc mockMvc;

    @Value("${spring.freemarker.charset}")
    private String springbootUrl;
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new HelloController()).build();
    }
    @Test
    public void getAccount() throws Exception {
        Integer i=null;
        //System.out.println(i>0);

        /*String s="aa";
        byte[] digest=s.getBytes();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < digest.length; i++) {
            sb.append(Integer.toHexString(((int) digest[i]) & 0xFF));
        }
        String resultString = sb.toString();
        System.out.println(resultString);
        System.out.println(new String(digest));*/


    }

    public void aa(Object...params){
    }

}
