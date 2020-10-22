package cn.weiguangfu.swagger2.plus.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EnableSwaggerPlus {

    // 英文错误, 保持老版本
    @Value("${swagger.push.enable:false}")
    private boolean enable;

    @Value("${swagger.plus.enable:false}")
    private boolean plusEnable;

    public boolean isPlusEnable() {
        return plusEnable | enable;
    }
}
