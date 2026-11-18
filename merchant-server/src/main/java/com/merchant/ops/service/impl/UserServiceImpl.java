package com.merchant.ops.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.merchant.ops.constant.MessageConstant;
import com.merchant.ops.dto.UserLoginDTO;
import com.merchant.ops.entity.User;
import com.merchant.ops.exception.LoginFailedException;
import com.merchant.ops.mapper.UserMapper;
import com.merchant.ops.properties.WeChatProperties;
import com.merchant.ops.service.UserService;
import com.merchant.ops.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.juli.logging.LogConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatProperties weChatProperties;

    /*
    *微信登录
    *
     */
    public User wxlogin(UserLoginDTO userLoginDTO) {
        String openid = getOpenid(userLoginDTO.getCode());
        //判断openid是否为空
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //判断当前登录用户是否为新用户
        User user = userMapper.getByOpenid(openid);
        if (user == null) {
            //新用户，插入数据
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }



        return user;


    }

    /*
    *获取用户的openid
    * @param code
    * @return
     */
    private String getOpenid(String code) {
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");

        //调用工具类向微信服务器发送请求
        String json = HttpClientUtil.doGet(WX_LOGIN, map);
        log.info("微信返回的json数据：{}", json);

        //解析json数据
        JSONObject jsonObject = JSONObject.parseObject(json);
        String openid = jsonObject.getString("openid");

        return openid;
    }

}
