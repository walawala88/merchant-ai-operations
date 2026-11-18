package com.merchant.ops.controller.user;

import com.merchant.ops.constant.JwtClaimsConstant;
import com.merchant.ops.dto.UserLoginDTO;
import com.merchant.ops.entity.User;
import com.merchant.ops.properties.JwtProperties;
import com.merchant.ops.result.Result;
import com.merchant.ops.service.UserService;
import com.merchant.ops.utils.JwtUtil;
import com.merchant.ops.vo.UserLoginVO;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
@Api(tags = "C端用户接口")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;
    /*
    *微信登录
    * @param code
     */
    @RequestMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("微信登录，code：{}", userLoginDTO.getCode());
        //微信登录
        User user = userService.wxlogin(userLoginDTO);
        //为用户生成JWT令牌
        Map<String , Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }
}
