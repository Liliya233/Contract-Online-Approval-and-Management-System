package com.liliya.contract.config;

import com.liliya.contract.service.impl.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;

import javax.annotation.Resource;
import javax.sql.DataSource;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Resource
    private UserDetailsServiceImpl userDetailsService;

    @Resource
    private DataSource dataSource;

    @Bean
    public JdbcTokenRepositoryImpl tokenRepository(){
        JdbcTokenRepositoryImpl jr = new JdbcTokenRepositoryImpl();
        jr.setDataSource(dataSource);
        //jr.setCreateTableOnStartup(true);
        return jr;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 允许 iframe 同源加载
                .headers()
                .frameOptions()
                .sameOrigin()

                // 登录页面放行
                .and()
                .authorizeRequests()
                .antMatchers("/login/**").permitAll()

                // 前端资源放行
                .and()
                .authorizeRequests()
                .antMatchers("/echarts/**", "/js/**","/layui/**").permitAll()
                .anyRequest()
                .authenticated()

                // 跨域资源共享
                .and()
                .cors()

                // 登录配置
                .and()
                .formLogin()
                .loginPage("/login")
                .successHandler(new MyAuthenticationSuccessHandler())
                .failureHandler(new MyAuthenticationFailureHandler())

                // 记住我
                .and()
                .rememberMe()
                .tokenValiditySeconds(60*60*24)
                .userDetailsService(userDetailsService)
                .tokenRepository(tokenRepository());
    }
}

