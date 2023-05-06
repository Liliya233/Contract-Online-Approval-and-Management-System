package com.liliya.contract.config;

import org.jodconverter.DocumentConverter;
import org.jodconverter.LocalConverter;
import org.jodconverter.office.OfficeManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class JodConverterConfig {
    @Resource
    private OfficeManager officeManager;
    @Bean
    public DocumentConverter documentConverter() {
        return LocalConverter.builder()
                .officeManager(officeManager)
                .build();
    }
}
