package com.merchant.ops.service;

import com.merchant.ops.dto.UserLoginDTO;
import com.merchant.ops.entity.User;

public interface UserService {
    /*
    *微信登录
    * @param code
    * @return
     */
    User wxlogin(UserLoginDTO userLoginDTO);
}
