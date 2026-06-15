package com.zzu.kaoyan.module.auth.service;

import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.module.auth.entity.RegisterDTO;

public interface AuthService {
    User verifyAccountAndPassword(String account,String password);

    User register(RegisterDTO registerDTO);
}
