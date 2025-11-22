package com.ruoyi.common.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import com.ruoyi.common.utils.spring.SpringUtils;

/**
 * 获取i18n资源文件
 * 
 * @author ruoyi
 */
public class MessageUtils
{
    /**
     * 根据消息键和参数 获取消息 委托给spring messageSource
     *
     * @param code 消息键
     * @param args 参数
     * @return 获取国际化翻译值
     */
    public static String message(String code, Object... args)
    {
        if (code == null)
        {
            return StringUtils.EMPTY;
        }
        
        MessageSource messageSource = SpringUtils.getBean(MessageSource.class);
        if (messageSource == null)
        {
            return StringUtils.EMPTY;
        }
        
        String result = messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
        return result != null ? result : StringUtils.EMPTY;
    }
}