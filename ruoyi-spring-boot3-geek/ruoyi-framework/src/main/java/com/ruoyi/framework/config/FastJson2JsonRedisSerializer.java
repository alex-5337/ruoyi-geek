package com.ruoyi.framework.config;

import java.nio.charset.Charset;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.springframework.lang.Nullable;
import com.alibaba.fastjson2.filter.Filter;

/**
 * Redis使用FastJson序列化器实现类
 * 用于处理Redis中对象的序列化和反序列化，特别是Spring Security认证相关对象
 */
public class FastJson2JsonRedisSerializer<T> implements RedisSerializer<T>
{
    // 默认字符集设置为UTF-8
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    // 自动类型过滤器配置 - 只允许指定的类进行自动类型转换，提高安全性
    static final Filter autoTypeFilter = JSONReader.autoTypeFilter(
        "org.springframework.security.core.authority.SimpleGrantedAuthority", // Spring Security权限类，必须包含
        "com.ruoyi.common.core.domain.model.LoginUser", // 登录用户信息类，包含认证和权限数据
        "com.ruoyi.common.core.domain.entity.SysUser" // 系统用户实体类，存储用户基本信息
    );

    // 泛型类型对象的Class引用
    private Class<T> clazz;

    /**
     * 构造函数
     * @param clazz 要序列化/反序列化的目标类型Class
     */
    public FastJson2JsonRedisSerializer(Class<T> clazz)
    {
        super();
        this.clazz = clazz;
    }

    /**
     * 序列化方法 - 将Java对象转换为字节数组
     * @param t 要序列化的对象，可为null
     * @return 序列化后的字节数组，对象为null时返回空数组
     * @throws SerializationException 序列化异常
     */
    @Override
    public byte[] serialize(@Nullable T t) throws SerializationException
    {
        // 判断对象是否为null，为null返回空字节数组
        // 使用WriteClassName特性，在序列化时写入类名信息，便于反序列化时识别类型
        return t == null ? new byte[0] : JSON.toJSONString(t, JSONWriter.Feature.WriteClassName).getBytes(DEFAULT_CHARSET);
    }

    /**
     * 反序列化方法 - 将字节数组转换为Java对象
     * @param bytes 要反序列化的字节数组，可为null
     * @return 反序列化后的Java对象，字节数组为空时返回null
     * @throws SerializationException 反序列化异常
     */
    @Override
    public T deserialize(@Nullable byte[] bytes) throws SerializationException
    {
        // 判断字节数组是否为null或空，直接返回null
        if (bytes == null || bytes.length <= 0)
            return null;
            
        // 将字节数组转换为字符串
        // 使用autoTypeFilter进行安全反序列化，只允许在过滤器中配置的类类型被创建
        return JSON.parseObject(new String(bytes, DEFAULT_CHARSET), clazz, autoTypeFilter);
    }
}
