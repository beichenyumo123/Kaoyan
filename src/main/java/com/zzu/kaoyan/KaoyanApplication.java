package com.zzu.kaoyan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;


@SpringBootApplication
@MapperScan({"com.zzu.kaoyan.mapper", "com.zzu.kaoyan.module.*.mapper"})
public class KaoyanApplication {

	public static void main(String[] args) {
		SpringApplication.run(KaoyanApplication.class, args);
	}

}
