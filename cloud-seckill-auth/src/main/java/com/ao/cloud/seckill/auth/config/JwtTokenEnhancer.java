package com.ao.cloud.seckill.auth.config;

import com.ao.cloud.seckill.auth.model.dataobject.User;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.Map;

/**

 * @createDate: 2021-12-12 16:19
 * @description: JWT内容增强器
 */
public class JwtTokenEnhancer implements TokenEnhancer {

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {

        Map<String, Object> map = new HashMap<>(2);
        User user = (User) authentication.getPrincipal();
        map.put("user_id", user.getId());
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(map);
        return accessToken;

    }
}
