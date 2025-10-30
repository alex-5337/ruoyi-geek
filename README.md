# 若依微服务架构项目

## 项目介绍

本项目是基于若依框架的微服务架构实现，包含以下三个主要子项目：

1. **ruoyi-geek-app** - 移动端应用项目（基于uni-app开发）
2. **ruoyi-geek-vue3** - Web前端项目（基于Vue3开发）
3. **ruoyi-spring-boot3-geek** - 后端服务项目（基于Spring Boot 3开发）

## 技术栈

- 后端：Spring Boot 3、Spring Cloud、MyBatis、MySQL
- 前端：Vue 3、Vite、Element Plus
- 移动端：uni-app、Vue.js
- 其他：JWT、OAuth2、Redis、Quartz

## 功能模块

- 用户认证与授权
- 系统管理
- 权限管理
- 支付模块
- 在线开发
- 代码生成
- 定时任务

## 部署说明

### 后端部署

1. 导入数据库脚本（位于 `ruoyi-spring-boot3-geek/sql/` 目录）
2. 配置数据库连接（修改相关配置文件）
3. 运行 `ruoyi-spring-boot3-geek/bin/run.bat` 启动服务

### 前端部署

1. 安装依赖：`npm install`
2. 开发环境运行：`npm run dev`
3. 生产环境构建：`npm run build`

## 注意事项

- 确保JDK版本为17或更高
- 确保Node.js版本为16或更高
- 数据库使用MySQL 8.0或更高版本