package cn.weiguangfu.swagger2.plus.model.manager;

import cn.weiguangfu.swagger2.plus.enums.ApiModelTypeEnum;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 完整路径分割模板名称管理
 * @author 魏广甫
 * @version 2.7.0
 * @since 2.7.0-1-beta3
 */
@Component
public class CompletePathModelNameManager implements ModelNameManager {


    /**
     * 下划线分隔符
     */
    private final static String UNDERLINE_SEPARATOR = "_";
    /**
     * 需要转换的字符
     */
    private final static String CHARACTERS_TO_REPLACE = "/";
    /**
     * 路径起始符号
     */
    private final static String PATH_START = "(";
    /**
     * 路径结束符号
     */
    private final static String PATH_END = ")";

    @Override
    public String getModelPlusName(String path, String uniqueid, ApiModelTypeEnum apiModelTypeEnum,
                                   String originalModelName) {
        String apiModelType = Objects.nonNull(apiModelTypeEnum) ? apiModelTypeEnum.getModelType() : "";
        return originalModelName
                + PATH_START
                + path.replace(CHARACTERS_TO_REPLACE, UNDERLINE_SEPARATOR)
                + UNDERLINE_SEPARATOR
                + uniqueid
                + UNDERLINE_SEPARATOR
                + apiModelType
                + PATH_END;
    }
}