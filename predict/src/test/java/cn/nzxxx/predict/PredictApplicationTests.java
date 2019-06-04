package cn.nzxxx.predict;

import cn.nzxxx.predict.toolitem.tool.Helper;
import cn.nzxxx.predict.webrequest.HelloController;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.math.BigDecimal;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
        //System.out.println(new BigDecimal("-.8"));

        System.out.println("空:"+isNum(""));
        System.out.println(".8:"+isNum(".8"));
        System.out.println("-.8:"+isNum("-.8"));
        System.out.println("88:"+isNum("88"));
        System.out.println("-88:"+isNum("-88"));
    }
    //通过代码规范得知,上行提取出做常量属性(类的属性)会预编译(效率高些),例
    private static Pattern pattern = Pattern.compile("-?[0-9]*(\\.[0-9]*)?");
    public boolean isNum(String strNum){
        if(StringUtils.isBlank(strNum)){return false;}
        Matcher matcher = pattern.matcher(strNum);
        return  matcher.matches();
    }

}
