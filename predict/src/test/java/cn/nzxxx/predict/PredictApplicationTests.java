package cn.nzxxx.predict;

import cn.nzxxx.predict.web.HelloController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.awt.*;

@RunWith(SpringRunner.class)//引入spring对JUnit4的支持
@SpringBootTest
public class PredictApplicationTests {
    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new HelloController()).build();
    }

    @Test
    public void getAccount() throws Exception {
        //常用
        //mockMvc.perform(post("/test/aa"));//执行HTTP的请求
        //mockMvc.perform(get("/test/aa")).andReturn().getResponse().getContentAsString();//Controller方法会执行,且此返回值是Controller方法返回值

    }
}
