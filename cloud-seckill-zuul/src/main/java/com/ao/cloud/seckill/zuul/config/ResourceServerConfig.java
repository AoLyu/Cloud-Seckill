package com.ao.cloud.seckill.zuul.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/**

 * @description: 资源服务器配置
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Autowired
    AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    AccessDeniedHandler accessDeniedHandler;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/auth/**")
                .permitAll()
                .antMatchers("/**")
                .authenticated()
                ;//配置需要保护的资源路径
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
//        resources.tokenExtractor(customTokenExtractor);
        resources.authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler);
    }
}
