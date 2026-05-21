package com.zzu.kaoyan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;

//@ComponentScan(basePackages = "com.zzu.kaoyan",
//		excludeFilters = @ComponentScan.Filter(
//				type = FilterType.REGEX,
//				pattern = "com.zzu.kaoyan.module.activity.*"
//		)
//)
@SpringBootApplication
@MapperScan("com.zzu.kaoyan.**.mapper")
@EnableAsync
public class KaoyanApplication {

	public static void main(String[] args) {
		SpringApplication.run(KaoyanApplication.class, args);
	}

}
