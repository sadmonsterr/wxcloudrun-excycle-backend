# wxcloudrun-excycle-backend
[![GitHub license](https://img.shields.io/github/license/WeixinCloud/wxcloudrun-express)](https://github.com/WeixinCloud/wxcloudrun-express)
![GitHub package.json dependency version (prod)](https://img.shields.io/badge/maven-3.6.0-green)
![GitHub package.json dependency version (prod)](https://img.shields.io/badge/jdk-8-green)
![GitHub package.json dependency version (prod)](https://img.shields.io/badge/spring%20boot-2.5.5-green)

微信云托管 Java Spring Boot 框架模版，实现 excycle 业务系统，包括用户管理、订单管理、物品管理等完整功能，使用云托管 MySQL 数据库。

![](https://qcloudimg.tencent-cloud.cn/raw/be22992d297d1b9a1a5365e606276781.png)


## 快速开始
前往 [微信云托管快速开始页面](https://developers.weixin.qq.com/miniprogram/dev/wxcloudrun/src/basic/guide.html)，选择相应语言的模板，根据引导完成部署。

## 本地调试
下载代码在本地调试，请参考[微信云托管本地调试指南](https://developers.weixin.qq.com/miniprogram/dev/wxcloudrun/src/guide/debug/)。

## 实时开发
代码变动时，不需要重新构建和启动容器，即可查看变动后的效果。请参考[微信云托管实时开发指南](https://developers.weixin.qq.com/miniprogram/dev/wxcloudrun/src/guide/debug/dev.html)

## Dockerfile最佳实践
请参考[如何提高项目构建效率](https://developers.weixin.qq.com/miniprogram/dev/wxcloudrun/src/scene/build/speed.html)

## 目录结构说明
~~~
.
├── Dockerfile                      Dockerfile 文件
├── LICENSE                         LICENSE 文件
├── README.md                       README 文件
├── container.config.json           模板部署「服务设置」初始化配置（二开请忽略）
├── mvnw                            mvnw 文件，处理mevan版本兼容问题
├── mvnw.cmd                        mvnw.cmd 文件，处理mevan版本兼容问题
├── pom.xml                         pom.xml文件
├── settings.xml                    maven 配置文件
├── springboot-cloudbaserun.iml     项目配置文件
└── src                             源码目录
    └── main                        源码主目录
        ├── java                    业务逻辑目录
        └── resources               资源文件目录
~~~


## 服务 API 文档

### 用户管理 API

#### `GET /api/v1/users`
获取用户列表

#### `GET /api/v1/users/{id}`
获取用户详情

#### `POST /api/v1/users`
创建用户

#### `POST /api/v1/users/{id}`
更新用户

#### `DELETE /api/v1/users/{id}`
删除用户

### 订单管理 API

#### `POST /api/v1/orders/list`
获取订单列表

#### `GET /api/v1/orders/{id}`
获取订单详情（包含订单物品）

#### `POST /api/v1/orders`
创建订单

#### `POST /api/v1/orders/{id}`
更新订单

#### `DELETE /api/v1/orders/{id}`
删除订单

#### `GET /api/v1/orders/count`
获取订单总数

### 物品管理 API

#### `GET /api/v1/items/list`
获取物品列表

#### `GET /api/v1/items/{id}`
获取物品详情

#### `POST /api/v1/items`
创建物品

#### `POST /api/v1/items/{id}`
更新物品

#### `DELETE /api/v1/items/{id}`
删除物品

#### `POST /api/v1/items/{id}/price`
更新物品价格

#### `POST /api/v1/items/user/{userId}/prices`
批量更新用户物品价格

### 认证 API

#### `POST /api/v1/auth/login`
用户登录

#### `POST /api/v1/auth/logout`
用户登出

#### `GET /api/v1/auth/info`
获取当前用户信息

### 文件上传 API

#### `POST /api/v1/upload/upload`
上传文件

#### `GET /api/v1/upload/{fileName}`
获取文件下载链接

## 使用注意
如果不是通过微信云托管控制台部署模板代码，而是自行复制/下载模板代码后，手动新建一个服务并部署，需要在「服务设置」中补全以下环境变量，才可正常使用，否则会引发无法连接数据库，进而导致部署失败。

### 数据库配置
- MYSQL_ADDRESS - MySQL数据库地址
- MYSQL_PASSWORD - MySQL数据库密码
- MYSQL_USERNAME - MySQL数据库用户名
以上三个变量的值请按实际情况填写。如果使用云托管内MySQL，可以在控制台MySQL页面获取相关信息。

### 微信配置
- WECHAT_APPID - 微信小程序AppID
- WECHAT_SECRET - 微信小程序Secret

### 文件上传配置
- COS_SECRET_ID - 腾讯云COS SecretId
- COS_SECRET_KEY - 腾讯云COS SecretKey
- COS_BUCKET_NAME - COS桶名称
- COS_REGION - COS地域

### JWT配置
- JWT_SECRET - JWT密钥

### 系统配置
- SERVER_PORT - 服务端口（默认8080）
- SPRING_PROFILES_ACTIVE - Spring环境配置（建议dev/test/prod）


## License

[MIT](./LICENSE)
