package cn.weiguangfu.swagger2.plus.plus;

import springfox.documentation.schema.Model;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.service.ResourceGroup;
import springfox.documentation.spi.service.contexts.RequestMappingContext;

import java.util.List;
import java.util.Map;

public interface Swagger2Plus {

    /**
     * 获取新的API描述列表
     * @param resourceGroup 请求Controller类上下文
     * @param each 请求接口上下文
     * @param models 参数定义列表
     * @param apiDescriptionList 需要增强的API描述列表
     * @return 增强的API描述列表
     */
    List<ApiDescription> getNewApiDescriptionList(ResourceGroup resourceGroup,
            RequestMappingContext each, Map<String, Model> models, List<ApiDescription> apiDescriptionList);
}