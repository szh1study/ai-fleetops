package com.aifleetops.dto;

import com.aifleetops.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class DeviceStatusUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "设备类型不能为空")
    private DeviceType deviceType;

    @NotBlank(message = "设备状态不能为空")
    private String status;
}
