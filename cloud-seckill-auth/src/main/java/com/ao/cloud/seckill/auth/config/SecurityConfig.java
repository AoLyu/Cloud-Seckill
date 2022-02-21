package com.ao.cloud.seckill.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @createDate: 2021-10-16 17:55
 * @description: Spring Security 配置类
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 将密码传给授权服务器
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/oauth/**",
                        "/logout/**",
                        "/webjars/**",
                        "/resources/**",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/v2/api-docs")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .permitAll();
    }

//    /**
//     * 配置用户签名服务 主要是user-details 机制，身份验证管理生成器
//     * @param auth 签名管理器构造器，用于构建用户具体权限控制
//     * @throws Exception
//     */
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception;
//    /**
//     * 用来构建 Filter 链
//     * @param web
//     * @throws Exception
//     */
//    public void configure(WebSecurity web) throws Exception;
//    /**
//     * 用来配置拦截保护的请求, HTTP请求安全处理
//     * @param http
//     * @throws Exception
//     */
//    protected void configure(HttpSecurity http) throws Exception;



}
