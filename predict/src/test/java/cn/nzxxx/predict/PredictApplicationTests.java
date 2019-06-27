package cn.nzxxx.predict;

import cn.nzxxx.predict.toolitem.tool.Helper;
import cn.nzxxx.predict.webrequest.HelloController;

import com.mongodb.*;
import com.mongodb.client.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.hibernate.validator.internal.util.StringHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RunWith(SpringRunner.class)//引入spring对JUnit4的支持
@SpringBootTest
public class PredictApplicationTests {
    private Logger log = LoggerFactory.getLogger(this.getClass());	//import org.slf4j.LoggerFactory;

    private MockMvc mockMvc;
    @Value("${spring.freemarker.charset}")
    private String springbootUrl;
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new HelloController()).build();
    }


    @Test
    public void test() throws Exception {
        int[][] a=new int[7][];
        a[1]=new int[8];
        a[1][2]=4;
        System.out.println(a);
       // System.out.println(-3|0);
}
    @Autowired
    private MongoTemplate mongoTemplate;
    @Test
    public void getAccount() throws Exception {
        MongoCollection<Document> collection = mongoTemplate.getCollection("test");

        //db.test.aggregate([{$group : {_id : "$likes"}}])
        List<BasicDBObject> dbList = new ArrayList();
        BasicDBObject dbmap= new BasicDBObject();
        BasicDBObject groupmap= new BasicDBObject();
        groupmap.put("_id","$likes");
        dbmap.put("$group",groupmap);
        dbList.add(dbmap);
        AggregateIterable<Document> findIterable =   collection.aggregate(dbList);
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        while(mongoCursor.hasNext()){
            Document doc=mongoCursor.next();
            System.out.println(doc);
        }


    }
    //判断正整数是否是质数,是则返回true
    public boolean isPrimeNum(int num){
        boolean isPrimeNum=false;
        List<Integer> pnums=new ArrayList<Integer>(16);//当前质数集
        for(int i=2;i<=num;i++){//1不是质数
            boolean bol=true;//当前i是集合里没有的质数
            for (int j=0;j<pnums.size();j++){//可用迭代器简化一下,这样就无需bol参数了
                int primeNum =pnums.get(j);
                if(i%primeNum==0){//说明此非质数
                    bol=false;
                    break;
                }
            }
            if(bol){
                pnums.add(i);
            }
        }
        //System.out.println(Helper.listToStringJSON(pnums));
        int sizeL=pnums.size();
        if(sizeL!=0&&pnums.get(sizeL-1)==num){
            isPrimeNum=true;
        }
        return  isPrimeNum;
    }

}
