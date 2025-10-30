package com.ruoyi.online.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.framework.web.service.PermissionService;
import com.ruoyi.online.domain.OnlineMb;
import com.ruoyi.online.service.IOnlineMbService;
import com.ruoyi.online.utils.SqlMapper;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 在线接口控制器
 * 用于处理在线配置的API接口请求，支持动态SQL执行
 * @author 系统自动生成
 */
@RestController // 标识为REST风格控制器
@Anonymous // 允许匿名访问，不需要登录验证
@RequestMapping("/online") // 基础请求路径
public class OnLineController extends BaseController {
    /**
     * 在线模块服务接口
     * 用于查询在线配置的接口信息
     */
    @Autowired
    private IOnlineMbService onlineMbService;
    
    /**
     * 权限服务
     * 使用@Resource注解并指定name为"ss"进行注入，用于权限验证
     */
    @Resource(name = "ss")
    private PermissionService permissionService;

    /**
     * MyBatis会话工厂
     * 用于创建SqlSession执行SQL操作
     */
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 处理请求参数
     * 将URL查询参数和请求体参数进行整合，提取params格式的参数并统一处理
     * 
     * @param params URL查询参数
     * @param data 请求体JSON参数
     * @return 处理后的参数映射，包含params子对象
     */
    public Map<String, Object> getParams(HashMap<String, Object> params, HashMap<String, Object> data) {
        // 最终返回的参数对象
        Map<String, Object> object = new HashMap<>();
        // 专门用于存储params格式的参数子对象
        HashMap<String, Object> object_params = new HashMap<String, Object>();
        
        // 正则表达式：用于匹配params[key]格式的参数名
        String keyregex = "params\\[(.*?)\\]";
        Pattern pattern = Pattern.compile(keyregex);
        
        // 处理URL查询参数
        if (params != null) {
            params.keySet().forEach(key -> {
                Matcher matcher = pattern.matcher(key);
                // 如果参数名符合params[key]格式，则提取key作为参数名
                if (matcher.find()) {
                    object_params.put(matcher.group(1), params.get(key));
                } else {
                    // 否则作为普通参数直接放入object
                    object.put(key, params.get(key));
                }
            });
        }
        
        // 处理请求体参数
        if (data != null) {
                // 如果请求体中包含params字段
                if (data.containsKey("params")) {
                    // 问题: data.get("params")返回的是Object类型，需要转换为HashMap<String, Object>
                    // 方案: 使用@SuppressWarnings("unchecked")抑制未检查转换警告
                    @SuppressWarnings("unchecked")
                    HashMap<String, Object> paramsMap = (HashMap<String, Object>) data.get("params");
                    // 将params中的参数合并到object_params
                    object_params.putAll(paramsMap);
                    // 移除已处理的params，避免重复
                    data.remove("params");
                }
                // 将剩余的其他参数添加到object中
                object.putAll(data);
            }
        
        // 将处理好的params子对象添加到最终返回对象中
        object.put("params", object_params);
        return object;
    }

    /**
     * 检查权限
     * 根据权限类型验证当前用户是否拥有相应权限
     * 
     * @param permission 权限类型字符串
     * @return 有权限返回true，无权限返回false
     */
    public Boolean checkPermission(String permission) {
        // 如果权限为空，则默认允许访问
        if(permission == null) return true;
        
        // 使用switch表达式根据权限类型调用不同的权限验证方法
        return switch (permission) {
            case "hasPermi" -> permissionService.hasPermi(permission); // 检查是否有指定权限
            case "lacksPermi" -> !permissionService.lacksPermi(permission); // 检查是否缺少指定权限
            case "hasAnyPermi" -> permissionService.hasAnyPermi(permission); // 检查是否有任意一个权限
            case "hasRole" -> permissionService.hasRole(permission); // 检查是否有指定角色
            case "lacksRole" -> !permissionService.lacksRole(permission); // 检查是否缺少指定角色
            case "hasAnyRoles" -> permissionService.hasAnyRoles(permission); // 检查是否有任意一个角色
            default -> true; // 默认允许访问
        };
    }

    /**
     * 处理Mapper执行
     * 根据指定的SQL内容和执行器类型执行相应的SQL操作
     * 
     * @param sqlContext SQL语句内容
     * @param actuatot 执行器类型（selectList、insert、selectOne、update、delete）
     * @param params SQL参数
     * @return 执行结果
     */
    public Object processingMapper(String sqlContext, String actuatot, Map<String, Object> params) {
        // 包装SQL语句为MyBatis脚本格式，支持动态SQL
        String sql = "<script>\n" + sqlContext + "\n</script>";
        
        // 获取MyBatis会话
        SqlSession sqlSession = sqlSessionFactory.openSession();
        // 创建SQL映射器工具类实例
        SqlMapper sqlMapper = new SqlMapper(sqlSession);
        
        Object res = null;
        
        // 根据执行器类型执行不同的SQL操作
        res = switch (actuatot) {
            case "selectList" -> getDataTable(sqlMapper.selectList(sql, params)); // 查询列表，使用getDataTable格式化结果
            case "insert" -> toAjax(sqlMapper.insert(sql, params)); // 插入操作，返回受影响行数的Ajax响应
            case "selectOne" -> success(sqlMapper.selectOne(sql, params)); // 查询单条，使用success包装结果
            case "update" -> toAjax(sqlMapper.update(sql, params)); // 更新操作，返回受影响行数的Ajax响应
            case "delete" -> toAjax(sqlMapper.delete(sql, params)); // 删除操作，返回受影响行数的Ajax响应
            default -> AjaxResult.error(500, "系统错误，执行器错误"); // 未知执行器类型，返回错误
        };
        
        // 关闭SQL会话，释放资源
        sqlSession.close();
        return res;
    }

    /**
     * 处理所有在线API请求
     * 使用通配符映射所有/online/api/**路径的请求
     * 
     * @param params URL查询参数，可选
     * @param data 请求体参数，可选
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @return API执行结果
     */
    @RequestMapping("/api/**") // 匹配所有/api/开头的子路径
    public Object api(@RequestParam(required = false) HashMap<String, Object> params,
            @RequestBody(required = false) HashMap<String, Object> data, HttpServletRequest request,
            HttpServletResponse response) {
        // 创建在线模块查询对象
        OnlineMb selectOnlineMb = new OnlineMb();
        // 设置路径：从URI中移除/online/api前缀，获取实际的API路径
        selectOnlineMb.setPath(request.getRequestURI().replace("/online/api", ""));
        // 设置HTTP方法（GET、POST、PUT等）
        selectOnlineMb.setMethod(request.getMethod());

        // 处理合并请求参数
        Map<String, Object> object = getParams(params, data);

        // 根据路径和方法查询对应的在线接口配置
        List<OnlineMb> selectOnlineMbList = onlineMbService.selectOnlineMbList(selectOnlineMb);
        
        // 接口配置不存在
        if (selectOnlineMbList.size() == 0) {
            return AjaxResult.error("没有资源" + selectOnlineMb.getPath());
        } 
        // 接口配置重复（一个路径和方法对应多个配置）
        else if (selectOnlineMbList.size() > 1) {
            return AjaxResult.error(500, "系统错误，在线接口重复");
        } 
        // 接口配置存在且唯一
        else {
            OnlineMb onlineMb = selectOnlineMbList.get(0);
            
            // 检查权限
            if (!checkPermission(onlineMb.getPermissionValue()))
                return AjaxResult.error(403, "没有权限，请联系管理员授权");

            // 如果配置了部门ID为1，则自动注入当前用户的部门ID
            if (onlineMb.getDeptId() != null && onlineMb.getDeptId().equals("1")) {
                object.put("deptId", SecurityUtils.getDeptId());
            }
            // 如果配置了用户ID为1，则自动注入当前用户的ID
            if (onlineMb.getUserId() != null && onlineMb.getUserId().equals("1")) {
                object.put("userId", SecurityUtils.getUserId());
            }

            // 执行SQL并返回结果
            return processingMapper(onlineMb.getSql(), onlineMb.getActuator(), object);
        }
    }

}
