package com.leyou;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
//@MapperScan("com.leyou.user.mapper")      为什么这样写不可以？？？
public class LeyouUserApplication {
    public static void main(String[] args) {
        SpringApplication.run( LeyouUserApplication.class );
    }
}
