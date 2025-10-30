package com.ruoyi.framework.config.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.lang.NonNull;

import org.apache.commons.lang3.RegExUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.utils.spring.SpringUtils;

/**
 * 匿名访问URL属性配置类
 * <p>
 * 该类负责收集所有使用{@link Anonymous}注解标记的控制器方法和类的URL路径，
 * 并将其添加到允许匿名访问的URL列表中。实现了Spring的InitializingBean和ApplicationContextAware接口，
 * 在Spring容器初始化完成后自动执行URL收集逻辑。
 * 
 * @author ruoyi
 */
@Configuration
public class PermitAllUrlProperties implements InitializingBean, ApplicationContextAware {
    /** 用于匹配URL路径中占位符的正则表达式 */
    private static final Pattern PATTERN = Pattern.compile("\\{(.*?)\\}");

    /** Spring应用上下文，用于获取RequestMappingHandlerMapping等Bean */
    private ApplicationContext applicationContext;

    /** 存储收集到的允许匿名访问的URL列表 */
    private List<String> urls = new ArrayList<>();

    /** 用于替换URL路径中占位符的通配符字符 */
    public String ASTERISK = "*";

    /**
     * 内部辅助类，用于封装RequestMappingInfo和对应的Anonymous注解
     */
    static class Pair {
        /** RequestMappingInfo对象，包含URL映射信息 */
        public RequestMappingInfo first;
        /** Anonymous注解对象，标记匿名访问权限 */
        public Anonymous second;

        /**
         * 构造函数
         * @param first RequestMappingInfo对象
         * @param second Anonymous注解对象
         */
        public Pair(RequestMappingInfo first, Anonymous second) {
            this.first = first;
            this.second = second;
        }
    }

    /**
     * 在Spring容器初始化完成后执行的方法
     * <p>
     * 该方法负责扫描所有控制器方法和类，收集使用Anonymous注解标记的URL路径，
     * 并将路径中的变量占位符（如{id}）替换为通配符*，然后添加到urls列表中。
     */
    @Override
    public void afterPropertiesSet() {
        // 获取RequestMappingHandlerMapping，用于访问所有的请求映射信息
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        // 获取所有请求映射信息和对应的处理方法
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        // 获取Spring MVC的路径匹配策略配置
        String matching = SpringUtils.getRequiredProperty("spring.mvc.pathmatch.matching-strategy").toLowerCase();
        
        // 流式处理所有请求映射信息
        map.keySet()
                .stream()
                // 第一步：将每个RequestMappingInfo转换为两个Pair对象（分别对应方法级别和类级别的注解）
                .flatMap(info -> {
                    HandlerMethod handlerMethod = map.get(info);
                    // 获取方法级别上的Anonymous注解
                    Anonymous method = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Anonymous.class);
                    // 获取类级别上的Anonymous注解
                    Anonymous controller = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), Anonymous.class);
                    // 创建并返回包含两个Pair的流
                    return Arrays.stream(new Pair[] { new Pair(info, method), new Pair(info, controller) });
                })
                // 第二步：过滤掉不包含Anonymous注解的Pair对象
                .filter(pair -> pair.second != null)
                // 第三步：根据不同的路径匹配策略，安全地提取URL模式
                .flatMap(pair -> {
                    // 根据配置的路径匹配策略选择不同的处理方式
                    switch (matching) {
                        case "ant_path_matcher": {
                            // 处理Ant风格的路径匹配
                            var patternsCondition = pair.first.getPatternsCondition();
                            if (patternsCondition != null) {
                                var patterns = patternsCondition.getPatterns();
                                if (patterns != null) {
                                    return patterns.stream();
                                }
                            }
                            break;
                        }
                        case "path_pattern_parser": {
                            // 处理PathPattern风格的路径匹配
                            var pathPatternsCondition = pair.first.getPathPatternsCondition();
                            if (pathPatternsCondition != null) {
                                var patternValues = pathPatternsCondition.getPatternValues();
                                if (patternValues != null) {
                                    return patternValues.stream();
                                }
                            }
                            break;
                        }
                        default: {
                            // 默认使用Ant风格的路径匹配
                            var patternsCondition = pair.first.getPatternsCondition();
                            if (patternsCondition != null) {
                                var patterns = patternsCondition.getPatterns();
                                if (patterns != null) {
                                    return patterns.stream();
                                }
                            }
                            break;
                        }
                    }
                    // 如果任何条件为null，返回空流而不是null，避免NPE
                    return java.util.stream.Stream.empty();
                })
                // 第四步：将URL路径中的变量占位符（如{id}）替换为通配符*
                .map(url -> RegExUtils.replaceAll(url, PATTERN, ASTERISK))
                // 第五步：将处理后的URL添加到urls列表中
                .forEach(urls::add);
    }

    /**
     * 设置Spring应用上下文
     * <p>
     * 实现ApplicationContextAware接口的方法，Spring容器会自动调用该方法注入ApplicationContext
     * 
     * @param context Spring应用上下文
     * @throws BeansException 当设置应用上下文失败时抛出
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        this.applicationContext = context;
    }

    /**
     * 获取允许匿名访问的URL列表
     * 
     * @return 允许匿名访问的URL列表
     */
    public List<String> getUrls() {
        return urls;
    }

    /**
     * 设置允许匿名访问的URL列表
     * 
     * @param urls 允许匿名访问的URL列表
     */
    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}
