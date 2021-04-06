package cn.nzxxx.predict.webrequest.controller;

import cn.nzxxx.predict.config.pdftable.FormPdf;
import cn.nzxxx.predict.toolitem.tool.Helper;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.layout.font.FontProvider;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HelloController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public int a=2;
    /**
     * http://localhost:8181/test/aa
     */
    @RequestMapping(value="/test/aa")
    @ResponseBody
    public String testBB(String str, HttpServletRequest request) throws Exception{
        FormPdf fpdf=new FormPdf();
        int abc = 0;
        a++;
        String s="<p style=''>xx黑xx</p><style>p{color:green}</style>";
        File pdfDest = new File("d:/ruijian/a.pdf");
        ConverterProperties converterProperties = new ConverterProperties();
        FontProvider fontProvider = new DefaultFontProvider();
        InputStream fis = getClass().getClassLoader().getResourceAsStream("META-INF/resources/ttf/heiti.ttf");
        byte[] in_b = Helper.inputStreamToByte(fis);
        FontProgram fontProgram1 = FontProgramFactory.createFont(in_b);//("C:/Users/18722/Desktop/heiti.ttf");
        fontProvider.addFont(fontProgram1);
        converterProperties.setFontProvider(fontProvider);
        HtmlConverter.convertToPdf(s,new FileOutputStream(pdfDest), converterProperties);
        return "110";
    }
    @RequestMapping("/hello")
    public String hello(ModelMap map) {
        // 加入一个属性，用来在模板中读取
        map.addAttribute("name", "li");
        map.addAttribute("bookTitle", "近代简史");
        logger.error(System.getProperty("user.dir")+"近代简史");
        return "/hello";
    }
}
