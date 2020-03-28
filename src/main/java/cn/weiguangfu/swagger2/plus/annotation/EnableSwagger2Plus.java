package cn.weiguangfu.swagger2.plus.annotation;

import com.weiguangfu.swagger2.plus.extension.ApiListingScannerCustom;
import org.springframework.context.annotation.Import;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.lang.annotation.*;

/**
 * 开启springfox-swagger2-plus, 同时会开启swagger2.
 * @author 魏广甫
 * @version 2.7.0-1-beta1
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableSwagger2
@Import({ApiListingScannerCustom.class})
public @interface EnableSwagger2Plus {

}