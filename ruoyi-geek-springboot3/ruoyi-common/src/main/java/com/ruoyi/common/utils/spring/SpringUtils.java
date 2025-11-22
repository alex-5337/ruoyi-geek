package com.ruoyi.common.utils.spring;

import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.StringUtils;

/**
 * spring工具类 方便在非spring管理环境中获取bean
 * 
 * @author ruoyi
 */
@Component
public final class SpringUtils implements BeanFactoryPostProcessor, ApplicationContextAware 
{
    /** Spring应用上下文环境 */
    private static volatile ConfigurableListableBeanFactory beanFactory;

    private static volatile ApplicationContext applicationContext;

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException 
    {
        Assert.notNull(beanFactory, "ConfigurableListableBeanFactory must not be null");
        SpringUtils.beanFactory = beanFactory;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException 
    {
        Assert.notNull(applicationContext, "ApplicationContext must not be null");
        SpringUtils.applicationContext = applicationContext;
    }

    /**
     * 获取对象
     *
     * @param name  bean名称
     * @return 一个以所给名字注册的bean的实例
     * @throws org.springframework.beans.BeansException
     */
    public static <T> T getBean(@NonNull String name) throws BeansException
    {
        Assert.notNull(beanFactory, "BeanFactory is not initialized yet");
        // 获取bean实例
        Object bean = beanFactory.getBean(name);
        // 这种方法在运行时仍有类型安全风险，但通过适当的文档说明用法
        @SuppressWarnings("unchecked")
        T result = (T) bean;
        return result;
    }
    
    /**
     * 获取指定名称和类型的Bean，提供更安全的类型检查
     * 
     * @param name bean名称
     * @param requiredType 期望的Bean类型
     * @return 指定类型的Bean实例
     * @throws BeansException 如果获取失败或类型不匹配
     */
    public static <T> T getBean(@NonNull String name, @NonNull Class<T> requiredType) throws BeansException
    {
        Assert.notNull(beanFactory, "BeanFactory is not initialized yet");
        // 使用Spring提供的类型安全的getBean方法，自动进行类型检查
        return beanFactory.getBean(name, requiredType);
    }

    /**
     * 获取类型为requiredType的对象
     *
     * @param clz  bean类型
     * @return 指定类型的bean实例
     * @throws org.springframework.beans.BeansException
     */
    public static <T> T getBean(@NonNull Class<T> clz) throws BeansException
    {
        Assert.notNull(beanFactory, "BeanFactory is not initialized yet");
        // 直接使用泛型方法，避免额外的类型转换
        return beanFactory.getBean(clz);
    }

    /**
     * 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true
     *
     * @param name
     * @return boolean
     */
    public static boolean containsBean(@NonNull String name)
    {
        Assert.notNull(beanFactory, "BeanFactory is not initialized yet");
        return beanFactory.containsBean(name);
    }

    /**
     * 判断以给定名字注册的bean定义是一个singleton还是一个prototype。 如果与给定名字相应的bean定义没有被找到，将会抛出一个异常（NoSuchBeanDefinitionException）
     *
     * @param name
     * @return boolean
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
     *
     */
    public static boolean isSingleton(@NonNull String name) throws NoSuchBeanDefinitionException
    {
        Assert.notNull(beanFactory, "BeanFactory is not initialized yet");
        return beanFactory.isSingleton(name);
    }

    /**
     * @param name
     * @return Class 注册对象的类型
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
     *
     */
    public static Class<?> getType(@NonNull String name) throws NoSuchBeanDefinitionException
    {
        Assert.notNull(beanFactory, "BeanFactory is not initialized yet");
        return beanFactory.getType(name);
    }

    /**
     * 如果给定的bean名字在bean定义中有别名，则返回这些别名
     *
     * @param name
     * @return
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
     *
     */
    public static String[] getAliases(@NonNull String name) throws NoSuchBeanDefinitionException
    {
        Assert.notNull(beanFactory, "BeanFactory is not initialized yet");
        return beanFactory.getAliases(name);
    }

    /**
     * 获取aop代理对象
     * 
     * @param invoker
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAopProxy(@NonNull T invoker)
    {
        try {
            return (T) AopContext.currentProxy();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Cannot find current proxy. Set 'exposeProxy' property on Advised to 'true' to make it available.", e);
        }
    }

    /**
     * 获取当前的环境配置，无配置返回null
     *
     * @return 当前的环境配置
     */
    public static String[] getActiveProfiles()
    {
        Assert.notNull(applicationContext, "ApplicationContext is not initialized yet");
        return applicationContext.getEnvironment().getActiveProfiles();
    }

    /**
     * 获取当前的环境配置，当有多个环境配置时，只获取第一个
     *
     * @return 当前的环境配置
     */
    public static String getActiveProfile()
    {
        final String[] activeProfiles = getActiveProfiles();
        return StringUtils.isNotEmpty(activeProfiles) ? activeProfiles[0] : null;
    }

    /**
     * 获取配置文件中的值
     *
     * @param key 配置文件的key
     * @return 当前的配置文件的值
     *
     */
    public static String getRequiredProperty(@NonNull String key)
    {
        Assert.notNull(applicationContext, "ApplicationContext is not initialized yet");
        return applicationContext.getEnvironment().getRequiredProperty(key);
    }
}
