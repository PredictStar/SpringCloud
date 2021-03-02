package cn.nzxxx.predict;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


@SpringBootApplication
@MapperScan("cn.nzxxx.predict.**.mapper")
public class PredictApplication {
	public static void main(String[] args) {
		SpringApplication.run(PredictApplication.class, args);
	}
}
