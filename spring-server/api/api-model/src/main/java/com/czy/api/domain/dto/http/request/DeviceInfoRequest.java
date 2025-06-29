package com.czy.api.domain.dto.http.request;


import com.czy.api.domain.dto.http.base.BaseHttpRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 13225
 * @date 2025/1/8 21:33
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DeviceInfoRequest extends BaseHttpRequest {
    private String uuid;
    private String deviceName;
    private String imei;
}
