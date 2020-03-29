package cn.weiguangfu.swagger2.plus.model.manager;

import cn.weiguangfu.swagger2.plus.enums.ApiModelTypeEnum;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 模板下划线名称分割管理
 * @author 魏广甫
 * @version 2.7.0
 * @since 2.7.0-1-beta3
 */
@Component
public class UnderlineModelNameManager implements ModelNameManager {

    /**
     * 下划线分隔符
     */
    private final static String UNDERLINE_SEPARATOR = "_";
    /**
     * 需要转换的字符
     */
    private final static String CHARACTERS_TO_REPLACE = "/";

    @Override
    public String getModelPlusName(String path, ApiModelTypeEnum apiModelTypeEnum, String originalModelName) {
        String apiModelType = Objects.nonNull(apiModelTypeEnum) ? apiModelTypeEnum.getModelType() : "";
        return path.replace(CHARACTERS_TO_REPLACE, UNDERLINE_SEPARATOR)
                + UNDERLINE_SEPARATOR
                + apiModelType
                + UNDERLINE_SEPARATOR
                + originalModelName;
    }
}