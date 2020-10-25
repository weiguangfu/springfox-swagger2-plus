### 2.7.0-1-beta2
* 项目加入Travis-CI持续集成.
* 项目源码路径由com.weiguangfu.swagger2.plus变更为cn.weiguangfu.swagger2.plus, 与groupId保持一致.
* 完善文档, 增加swagger2对应swagger-ui版本的依赖, 此版本后, 引用项目不需要在单独引入swagger-ui依赖.

### 2.7.0-1-beta4, 2.8.0-1-beta2, 2.9.2-1-beta2, 2.9.1-1-beta2
* 修改参数中有层级管理(继承)时报错问题.

### 2.7.0-1 (临时发布字段必填的版本, 待稳定后将发布其他对应版本)
* 增加注解 @ApiRequestFieldRequired 可以标注文档字段是否为必填字段.
* 配置文件错误修复: 启用Swagger增强配置由原 swagger.push.enable 变更为 swagger.plus.enable, 为不影响原版本使用, 原配置依然可以使用, 优先使用 swagger.plus.enable .

