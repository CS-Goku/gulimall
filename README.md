# 简介
[[分布式基础概念]]

# 环境
## 服务器
[[云服务器搭建Linux]]
[[虚拟机搭建Docker]]
[[MySQL环境]]
[[Redis环境搭建]]

## 开发环境

idea
- IDEA安装插件lombok，mybatisX。IDEA设置里配置好maven3.4.1[[Maven环境搭建]]

VSCode插件
- Auto Close Tag
- Auto Rename Tag
- Chinese
- ESlint
- HTML CSS Support
- HTML Snippets
- JavaScript ES6
- Live Server
- open in brower
- Vetur

版本控制[[Git环境搭建]]

Github新建仓库，仓库名gulimall，选择语言java，在.gitignore选中maven（就会忽略掉maven一些个人无需上传的配置文件），许可证选Apache-2.0，开发模型选生成/开发模型，开发时在dev分支，发布时在master分支，创建。

[[SpringBoot快速构建]]

## 数据库

1.下载sql文件

2.虚拟机中的docker基本操作

```bash
sudo docker ps
sudo docker ps -a
# 这两个命令的差别就是后者会显示  【已创建但没有启动的容器】

# 我们接下来设置我们要用的容器每次都是自动启动
sudo docker update redis --restart=always
sudo docker update mysql --restart=always
# 如果不配置上面的内容的话，我们也可以选择手动启动
sudo docker start mysql
sudo docker start redis
# 如果要进入已启动的容器
sudo docker exec -it mysql /bin/bash
# /bin/bash就是进入一般的命令行，如果改成redis就是进入了redis
```

3.创建数据库，并执行对应的sql语句

gulimall-oms  订单
gulimall-pms  商品
gulimall-sms  优惠券
gulimall-ums  用户
gulimall-wms 仓储


# 快速开发
[[renren后台管理]]


# 分布式组件
SpringCloud Alibaba
> netflix把feign闭源了，spring cloud开了个**open feign**
>
> 阿里18年开发的微服务一站式解决方案。https://github.com/alibaba/spring-cloud-alibaba/blob/master/README-zh.md

在common的pom.xml中加入 对spring cloud alibaba组件进行统一管理

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-dependencies</artifactId>
            <version>2.2.0.RELEASE</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

[[Nacos注册和配置中心搭建]]1.1.3
[[OpenFeign使用]]
[[Gateway使用]]

# 前端
[[ES6规范]]
[[VUE]]
[[Element-ui]]
# 商品服务
[[商品服务]]
# 仓储服务-API
[[仓储服务]]

# 分布式基础篇总结

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210506080500803.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3JlYWxfcm9va2ll,size_16,color_FFFFFF,t_70)

crud程序员已经很落后了，都有逆向工程可以生成前后端基本的代码，重点是后面的高级篇**高并发、高性能、高可用**

