package com.aifleetops.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class StatusUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "状态不能为空")
    private String status;
}
