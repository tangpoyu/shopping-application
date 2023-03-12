//package com.example.productservice.web.config;
//
//import com.netflix.appinfo.AmazonInfo;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.commons.util.InetUtils;
//import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//
//@Configuration
//public class EurekaClientConfig {
//
//    @Autowired
//    InetUtils inetUtils;
//
//    @Bean
//    @Profile("!default")
//    public EurekaInstanceConfigBean eurekaInstanceConfig(InetUtils inetUtils) {
//        EurekaInstanceConfigBean bean = new EurekaInstanceConfigBean(inetUtils);
//        AmazonInfo info = AmazonInfo.Builder.newBuilder().autoBuild("eureka");
//        bean.setDataCenterInfo(info);
//        return bean;
//    }
//}
