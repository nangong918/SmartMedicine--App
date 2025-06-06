package com.czy.api.domain.dto.http.request;


import com.czy.api.domain.dto.http.base.BaseNettyRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

/**
 * @author 13225
 * @date 2025/1/2 15:11
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LoginResetPasswordRequest extends BaseNettyRequest {
    @NotEmpty(message = "用户账号不能为空")
    private String account;
    @NotEmpty(message = "用户密码不能为空")
    private String password;
    @NotEmpty(message = "用户新密码不能为空")
    private String newPassword;
}
