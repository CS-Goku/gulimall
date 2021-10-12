1. # 简介

   ## 快速开发此项目

   项目中有Mybatis-Puls和SpringCloud内容，可以利用本项目来进行实践，也可以用此来初识Cloud，最好对微服务有一定了解。

   sql：https://github.com/FermHan/gulimall

   ## 分布式基础概念

   ##### 1.微服务（架构风格）

   单体应用开发成一个个小模块小服务，每个服务独立运行在进程里，相互之间用轻量级通信http api，保证集中式管理

   ##### 2.集群（物理形态）

   几台服务器集中在一起实现同一个业务，**分布式（工作方式）**是将不同业务分布在不同地方。比如京东是一个分布式系统，用户业务和订单业务各自分布在各个服务器集群上

   ##### 3.远程调用

   分布式系统中，各个服务处于不同的主机，服务之间要互相调用，比如购物车服务要调用商品服务，SpringCloud中使用HTTP+JSON的方式完成远程调用

   ##### 4.负载均衡

   A服务需要调用B服务，B服务是集群化实现的，A就可以调用任何一个B服务器来完成功能

   ​	**目的：**为了使各个服务器不要太忙也不要太闲，**后宫佳丽三千雨露均沾**

   ​	**常见的负载均衡算法：**

   - 轮询：按顺序列表循环
   - 最小连接：优先选择连接最少的服务器
   - 散列：根据请求源IP的散列（hash）来选择，确保特定用户能访问到同一个服务器

   ##### 5.注册中心（服务器的注册和发现）

   A服务调用B服务，不知道B的哪些服务器是可用的，哪些下线，所以B服务的服务器上线就要通过服务注册到注册中心，A在调用前先去注册中心发现一下

   **就相当于一个可用服务器清单**

   ##### 6.配置中心

   每个服务都有大量的配置，又可能配置在多台服务器上，需要变更配置时通过配置中心，让每个服务获取自己的配置

   **就是设置各个服务的配置**

   ##### 7.服务的熔断降级

   A调用B调用C，这样的链条调用，如果C宕机，请求超时，就可能导致整个链路雪崩

   ​	1）、熔断：设置服务超时时间，你经常调用失败达到了阈值，就给你断路，给你默认数据

   ​	2）、降级：系统高峰期，资源紧张，可以让非核心业务降级运行。也就是让一些服务不出来，或者简单处理（抛异常、null、调用Mock数据、调用Fallback处理逻辑）

   ##### 8.API网关

   前端通过HTTP请求的方式发给后端完成功能，在这中间搞一个网关，所有请求都先到达网关，提供了客户端负载均衡，服务熔断，灰度发布，统一认证（合法非法），限流流空，日志统计等等。他是安检

   API **Gateway**作为微服务架构的重要组件，它抽象了微服务中都需要的**公共功能**，同时提供了客户端负载均衡，服务熔断降级，灰度发布，统一认证，限流流控，日志统计等丰富的功能，解决了很多API管理难题

   ##### 9.微服务架构图

   ![谷粒商城-微服务架构團](/Users/r1ff/Desktop/学习笔记/img/谷粒商城-微服务架构團.jpg)

   **前后端分离开发分为内网部署和外网部署**

   - 外网是面向公众访问的，部署前端项目，手机APP，电脑网页等

   - 内网部署的是后端集群

     1）、前端在页面上操作**发送请求**到后端，经过**Nginx集群**，Nginx把请求转交给**API网关（SpringCloud Gateway）**（网关根据请求**动态地址路由**到指定服务），比如商品服务，如果请求很多，那么可以**负载均衡**地调用商品服务器中的一台，当商品服务器出现问题也可以在网关层对服务进行**熔断降级（使用阿里的sentinel组件）**，网关还能实现认证授权、限流（只放行部分到服务器）等功能

     2）、到达服务器后进行处理**（Spring Boot）**，服务之间相互调用**（使用Feign组件）**，有些请求需要登陆才能进行（基于**OAuth2.0认证中心**，安全和权限使用**Spring Security**控制）

     3）、服务可能保存了一些数据或者需要使用缓存，使用**Redis集群（分片+哨兵集群）**。持久化使用**MySQL（读写分离+分库分表）**

     4）、服务和服务之间会是用消息队列**（RabbitMQ）**，来完成异步解耦，分布式事务一致性。有些服务需要全文检索**（ElaticSearch）**

     5）、服务存取数据，使用**阿里云对象存储服务OSS**

     6）、项目上线为了快速定位问题，使用**ELK**对日志进行处理，使用**LogStash**手机业务里的各种日志，把日志存储到**ES**中，用**Kibana可视化页面**从ES中检索出相关信息

     7）、分布式系统中，每个服务可能部署在很多台机器上，服务之间相互调用，得知道彼此在哪，所有服务将注册到**注册中心**，服务从注册中心发现其他服务所在位置（**阿里Nacos**作为注册中心）

     8）、每个服务配置众多，为了实现改一处配置就同步更改，需要**配置中心**（也使用**阿里Nacos**），服务从配置中心动态取配置

     9）、**服务追踪**，追踪服务调用链哪出现问题，使用SpringCloud提供的**Sleuth、Zipkin、Metrics**把每个服务的信息交给开源的**Prometheus**进行聚合分析，再由**Grafana**进行可视化展示，提供Prometheus的**AlterManager**实时得到服务的告警信息，以短信/邮件的方式告知开发人员

     10）、**持续集成和持续部署**，项目发布后，微服务众多，每一个都打包部署到服务器太麻烦，有了持续集成后开发人员可以将修改后的代码提交到**GitHub**，运维人员可以通过**自动化工具Jenkins Pipeline**将GItHub中获取的代码打包成**Docker镜像**，最终是由**k8s集成Docker服务**，将服务以**Docker容器**的方式运行。

   ##### 10.微服务划分图

   ![谷粒商城-微服务划分图](/Users/r1ff/Desktop/学习笔记/img/谷粒商城-微服务划分图.png)

   前端部分：

   - admin-vue（工作人员使用的后台管理系统）
   - shop-vue（面向公众访问的web网站）
   - app（面向公众）
   - 小程序（面向公众）

   后端部分：

   - 商品服务：商品的增删改查、上下架、详情
   - 支付服务：
   - 优惠服务：
   - 用户服务：用户的个人中心、收货地址
   - 仓储服务：商品的库存
   - 秒杀服务：
   - 订单服务：订单增删改查
   - 检索服务：商品的检索ES
   - 中央认证服务：登陆、注册、单点登录、社交登录
   - 购物车服务：
   - 后台管理系统：添加优惠信息等

   # 环境

   ## Linux环境搭建（具体操作省略）

   #### **阿里云服务器部署Linux虚拟机（方便）**

   - 安装docker
   - docker配置阿里云镜像加速
   - 通过docker 安装配置好了mysql和redis

   

   #### windows：

   visualBox进行安装需要cpu开启虚拟化，在开机启动的时候设置主板，CPU configuration，然后点击Intel Vitualization Technology。重启电脑

   普通安装linux虚拟机太麻烦，可以利用vagrant可以帮助我们快速地创建一个虚拟机。主要装了vitualbox，vagrant可以帮助我们快速创建出一个虚拟机。他有一个镜像仓库。

   去https://www.vagrantup.com/ 下载vagrant安装，安装后重启系统。cmd中输入vagrant有版本代表成功了。

   输入`vagrant init centos/7`，即可初始化一个centos7系统。（注意这个命令在哪个目录下执行的，他的Vagrantfile就生成在哪里）

   `vagrant up`启动虚拟机环境。

   启动后出现default folder:/cygdrive/c/User/... =>/vagrant。然后ctrl+c退出

   前面的页面中有ssh账号信息。`vagrant  ssh` 就会连上虚拟机。可以使用exit退出

   >  下次使用也可以直接vagrant up直接启动，但要确保当前目录在C:/用户/ 文件夹下，他下面有一个`Vagrantfile`，不过我们也可以配置环境变量。
   >
   >  启动后再次`vagrant  ssh `连上即可

   不过他使用的网络方式是网络地址转换NAT（端口转发），如果其他主机要访问虚拟机，必须由windows端口如3333断发给虚拟机端口如3306。这样每在linux里安一个软件都要进行端口映射，不方便，（也可以在virualBox里挨个设置）。我们想要给虚拟机一个固定的ip地址，windows和虚拟机可以互相ping通。

   - 方式1是在虚拟机中配置静态ip。

   - 也可以更改Vagrantfile更改虚拟机ip，修改其中的`config.vm.network "private_network",ip:"192.168.56.10"`，这个ip需要在windows的ipconfig中查到vitualbox的虚拟网卡ip，然后更改下最后一个数字就行（不能是1，1是我们的主机）。配置完后vagrant reload重启虚拟机。在虚拟机中`ip addr`就可以查看到地址了。互相ping也能ping通。

   - 关掉防火墙，VirualBox中第一个网卡设置NAT，第二个网卡设置仅主机

   - 如果ping不了baidu

     - cd /etc/sysconfig/network-scripts

     - ls  一般有ifcfg-eth0  1

     - ip  addr 看哪个网格是192.168.56网段，然后vim他

     - vim ifcfg-eth1  加入

       ```sh
       GATEWAY=192.168.56.1
       DNS1=114.114.114.114
       DNS2=8.8.8.8
       ```

     - service network restart

   默认只允许ssh登录方式，为了后来操作方便，文件上传等，我们可以配置允许账号密码登录

   ```sh
   vim /etc/ssh/sshd_config
   修改
   PasswordAuthentication yes
   重启
   service sshd restart
   账号root
   密码vagrant
   ```

   配置源

   ```sh
   # 备份原yum源
   
   mv /etc/yum.repos.d/CentOS-Base.repo /etc/yum.repos.d/CentOS-Base.repo.backup
   # 使用新yum源
   curl -o /etc/yum.repos.d/CentOS-Base.repo http://mirrors.163.com/.help/CentOS7-Base-163.repo
   # 生成缓存
   yum makecache
   ```

   

   ##### 虚拟机安装docker

   https://docs.docker.com/engine/install/centos/

   ```bash
   #卸载系统之前的docker 
   sudo yum remove docker \
                     docker-client \
                     docker-client-latest \
                     docker-common \
                     docker-latest \
                     docker-latest-logrotate \
                     docker-logrotate \
                     docker-engine
                     
                     
   sudo yum install -y yum-utils
   
   # 配置镜像
   sudo yum-config-manager \
       --add-repo \
       https://download.docker.com/linux/centos/docker-ce.repo
       
   sudo yum install docker-ce docker-ce-cli containerd.io
   
   sudo systemctl start docker
   # 设置开机自启动
   sudo systemctl enable docker
   
   docker -v
   sudo docker images
   
   # 配置镜像加速
   
   ```

   https://cr.console.aliyun.com/cn-qingdao/instances/mirrors

   根据页面命令执行完命令

   ```bash
   sudo mkdir -p /etc/docker
   sudo tee /etc/docker/daemon.json <<-'EOF'
   {
     "registry-mirrors": ["https://chqac97z.mirror.aliyuncs.com"]
   }
   EOF
   
   sudo systemctl daemon-reload
   sudo systemctl restart docker
   ```

   ##### 安装mysql

   用docker安装上mysql，去docker仓库里搜索mysql

   ```bash
   sudo docker pull mysql:5.7
   
   # --name指定容器名字 -v目录挂载 -p指定端口映射  -e设置mysql参数 -d后台运行
   sudo docker run -p 3306:3306 --name mysql \
   -v /mydata/mysql/log:/var/log/mysql \
   -v /mydata/mysql/data:/var/lib/mysql \
   -v /mydata/mysql/conf:/etc/mysql \
   -e MYSQL_ROOT_PASSWORD=root \
   -d mysql:5.7
   ```

   ```bash
   su root 密码为vagrant，这样就可以不写sudo了
   ```

   ```bash
   [root@localhost vagrant]# docker ps
   CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                               NAMES
   6a685a33103f        mysql:5.7           "docker-entrypoint.s…"   32 seconds ago      Up 30 seconds       0.0.0.0:3306->3306/tcp, 33060/tcp   mysql
   ```

   ```bash
   docker exec -it mysql bin/bash
   exit;
   
   因为有目录映射，所以我们可以直接在镜像外执行
   vi /mydata/mysql/conf/my.conf 
   
   [client]
   default-character-set=utf8
   [mysql]
   default-character-set=utf8
   [mysqld]
   init_connect='SET collation_connection = utf8_unicode_ci'
   init_connect='SET NAMES utf8'
   character-set-server=utf8
   collation-server=utf8_unicode_ci
   skip-character-set-client-handshake
   skip-name-resolve
   
   保存(注意评论区该配置不对，不是collection而是collation)
   
   docker restart mysql
   ```

   > 如何通过其他工具链接ssh
   >
   > 修改/etc/ssh/sshd_config
   >
   > 修改 PasswordAuthentication yes
   >
   > systemctl restart sshd.service  或 service sshd restart
   >
   > 连接192.168.56.10:22端口成功，用户名root，密码vagrant
   >
   > 也可以通过vagrant ssh-config查看ip和端口，此时是127.0.0.1:2222

   ##### Redis

   如果直接挂载的话docker会以为挂载的是一个目录，所以我们先创建一个文件然后再挂载，在虚拟机中。

   ```bash
   # 在虚拟机中
   mkdir -p /mydata/redis/conf
   touch /mydata/redis/conf/redis.conf
   
   docker pull redis
   
   docker run -p 6379:6379 --name redis \
   -v /mydata/redis/data:/data \
   -v /mydata/redis/conf/redis.conf:/etc/redis/redis.conf \
   -d redis redis-server /etc/redis/redis.conf
   
   # 直接进去redis客户端。
   docker exec -it redis redis-cli
   ```

   默认是不持久化的。在配置文件中输入appendonly yes，就可以aof持久化了。修改完docker restart redis，docker -it redis redis-cli

   ```bash
   vim /mydata/redis/conf/redis.conf
   # 插入下面内容
   appendonly yes
   保存
   
   docker restart redis
   ```

   设置redis容器在docker启动的时候启动

   ```shell
   docker update redis --restart=always
   ```

   ##### 安装ngidocker安装nginx为P124的内容

   ```bash
   docker pull nginx:1.10
   # 随便启动一个nginx实例，只是为了复制出配置，放到docker里作为镜像的统一配置
   docker run -p 80:80 --name nginx -d nginx:1.10
   
   cd /mydata/nginx
   docker container cp nginx:/etc/nginx .
   然后在外部 /mydata/nginx/nginx 有了一堆文件
   mv /mydata/nginx/nginx /mydata/nginx/conf
   # 停掉nginx
   docker stop nginx
   docker rm nginx
   
   # 创建新的nginx
   docker run -p 80:80 --name nginx \
   -v /mydata/nginx/html:/usr/share/nginx/html \
   -v /mydata/nginx/logs:/var/log/nginx \
   -v /mydata/nginx/conf:/etc/nginx \
   -d nginx:1.10
   
   # 注意一下这个路径映射到了/usr/share/nginx/html，我们在nginx配置文件中是写/usr/share/nginx/html，不是写/mydata/nginx/html
   
   docker update nginx --restart=always
   ```

   测试

   ```bash
   cd /mydata/nginx/html/
   vim index.html
   随便写写
   测试 http://192.168.56.10:80
   ```

   

   ## 开发环境

   ##### Maven

   - maven：在settings中配置阿里云镜像，配置jdk1.8。

   - IDEA安装插件lombok，mybatisX。IDEA设置里配置好maven

   ##### VSCode

   ​	插件

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

   ## Git版本控制

   ##### Git for Mac安装配置

   https://www.jianshu.com/p/c058fbd7bb90 

   ##### 码云

   1.在码云新建仓库，仓库名gulimall，选择语言java，在.gitignore选中maven（就会忽略掉maven一些个人无需上传的配置文件），许可证选Apache-2.0，开发模型选生成/开发模型，开发时在dev分支，发布时在master分支，创建。

   2.在IDEA中New--Project from version control--git--复制刚才项目的地址，如`https://github.com/1046762075/mall`

   3.IDEA中New Module--Spring Initializer--com.atguigu.gulimall ， Artifact填 gulimall-product。Next---选择`web`（web开发），springcloud routing里选中`openFeign`（rpc调用）。

   ##### SpringBoot快速构建以下服务

   商品服务product

   存储服务ware

   订单服务order

   优惠券服务coupon

   用户服务member

   共同点：

   导入web和openFeign

   group：com.atguigu.gulimall

   Artifact：gulimall-XXX

   每一个服务，包名com.atguigu.gulimall.XXX{product/order/ware/coupon/member}

   模块名：gulimall-XXX

   **service窗口可以快速启动各个微服务**

   从某个项目粘贴个pom.xml粘贴到项目目录，修改他

   ```xml
   <?xml version="1.0" encoding="UTF-8"?><project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">	<modelVersion>4.0.0</modelVersion>	<groupId>com.atguigu.gulimall</groupId>	<artifactId>gulimall</artifactId>	<version>0.0.1-SNAPSHOT</version>	<name>gulimall</name>	<description>聚合服务</description>	<packaging>pom</packaging>	<modules>		<module>gulimall-coupon</module>		<module>gulimall-member</module>		<module>gulimall-order</module>		<module>gulimall-product</module>		<module>gulimall-ware</module>	</modules></project>
   ```

   在maven窗口刷新，并点击+号，找到刚才的pom.xml添加进来，发现多了个root。这样比如运行root的clean命令，其他项目也一起clean了。

   修改总项目的`.gitignore`，把小项目里的垃圾文件在提交的时候忽略掉，比如HELP.md。。。

   ```
   **/mvnw**/mvnw.cmd**/.mvn**/target/.idea**/.gitignore
   ```

   在version control/local Changes，点击刷新看Unversioned Files，可以看到变化。

   全选最后剩下21个文件，选择右键、Add to VCS。

   在IDEA中安装插件：gitee，重启IDEA。

   在fault changelist右键点击commit，去掉右面的勾选Perform code analysis、CHECK TODO，然后点击COMMIT，有个下拉列表，点击commit and push才会提交到云端。此时就可以在浏览器中看到了。

   - commit只是保存更新到本地
   - push才是提交到gitee

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

   ## 人人项目npm

   在码云搜索人人开源，使用renren-fast（后端）、renren-fast-vue（前端）项目。

   https://gitee.com/renrenio

   ```
   git clone https://gitee.com/renrenio/renren-fast.git
   
   git clone https://gitee.com/renrenio/renren-fast-vue.git
   ```

   （删掉.git文件）

   **后端：**

   下载到了桌面，我们把renren-fast移动到我们的项目文件夹

   在IDEA项目里的pom.xml添加一个renrnen-fast

   ```xml
   <modules>
       <module>gulimall-coupon</module>
       <module>gulimall-member</module>
       <module>gulimall-order</module>
       <module>gulimall-product</module>
       <module>gulimall-ware</module>
   
       <module>renren-fast</module>
   </modules>
   ```

   然后打开`renren-fast/db/mysql.sql`，复制全部，在Navicat Premium中创建库`guli-admin`，粘贴刚才的内容执行。

   然后修改项目里renren-fast的application-dev.yml中的数库库的url，通常把localhost修改为虚拟机ip`112.124.27.121`,后面的数据库名为gulimall_admin（ssl报错加上useSSL=false）。

   ```yaml
   url: jdbc:mysql://112.124.27.121/gulimall_admin?useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
   username: root
   password: root
   ```

   8080端口被占用，application.yml中port改成808

   浏览器输入http://localhost:8081/renren-fast/ 得到{"msg":"invalid token","code":401}就代表无误

   **前端**

   用VSCode打开renren-fast-vue

   安装node：http://nodejs.cn/download/ 

   `NPM`是随同`NodeJS`一起安装的包管理工具。JavaScript-NPM类似于java-Maven。

   命令行输入`node -v` 检查配置好了，配置npm的镜像仓库地址，再执

   ```bash
   node -v
   npm config set registry http://registry.npm.taobao.org/
   ```

   然后去VScode的项目终端中输入 `npm install`，是要去拉取依赖（package.json类似于pom.xml的dependency），但是会报错，然后进行如下操作：

   启动项目：`npm run dev`

   停止运行：control+c

   **P16 npm install报错问题**

   视频评论区没几个说对的，个人的各种分析写到了这里：https://blog.csdn.net/hancoder/article/details/113821646

   ## 人人项目-逆向工程

   ### 逆向工程搭建renren-generator

   ```bash
   git clone https://gitee.com/renrenio/renren-generator.git
   ```

   下载到桌面后，同样把里面的.git文件删除，然后移动到我们IDEA项目目录中，同样配置好pom.xml

   ```xml
   <modules>    <module>gulimall-coupon</module>    <module>gulimall-member</module>    <module>gulimall-order</module>    <module>gulimall-product</module>    <module>gulimall-ware</module>    <module>renren-fast</module>    <module>renren-generator</module></modules>
   ```

   在maven中刷新一下，让项目名变粗体，稍等下面进度条完成。

   ### 生成product代码

   修改application.yml

   ```yaml
   url: jdbc:mysql://112.124.27.121:3306/gulimall_pms?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghaiusername: rootpassword: root
   ```

   然后修改generator.properties（这里乱码的百度IDEA设置properties编码）

   ````properties
   # 主目录mainPath=com.atguigu#包名package=com.atguigu.gulimall#模块名moduleName=product#作者author=lufei#emailemail=790002348@qq.com#表前缀(类名不会包含表前缀) # 我们的pms数据库中的表的前缀都pms# 如果写了表前缀，每一张表对于的javaBean就不会添加前缀了tablePrefix=pms_
   ````

   运行RenrenApplication。如果启动不成功，修改application中是port为801。访问http://localhost:801/

   在网页上下方点击每页显示50个（pms库中的表），以让全部都显示，然后点击全部，点击生成代码。下载了压缩包

   解压压缩包，把main放到gulimall-product的同级目录下。

   ### 创建common

   然后在项目上右击（在项目上右击很重要）new modules--- maven---然后在name上输入gulimall-common。

   在pom.xml中也自动添加了`<module>gulimall-common</module>`

   在common项目的pom.xml中添加

   ```xml
   <!-- mybatisPLUS--><dependency>    <groupId>com.baomidou</groupId>    <artifactId>mybatis-plus-boot-starter</artifactId>    <version>3.3.2</version></dependency><!--简化实体类，用@Data代替getset方法--><dependency>    <groupId>org.projectlombok</groupId>    <artifactId>lombok</artifactId>    <version>1.18.8</version></dependency><!-- httpcomponent包。发送http请求 --><dependency>    <groupId>org.apache.httpcomponents</groupId>    <artifactId>httpcore</artifactId>    <version>4.4.13</version></dependency><dependency>    <groupId>commons-lang</groupId>    <artifactId>commons-lang</artifactId>    <version>2.6</version></dependency>
   ```

   我们把每个微服务里公共的类和依赖放到common里。

   > tips: shift+F6修改项目名
   >
   > 此外，说下maven依赖的问题。
   >
   > - `<dependency>`代表本项目依赖，子项目也依赖
   > - 如果有个`<optional>`标签，代表本项目依赖，但是子项目不依赖

   然后在product项目中的pom.xml中加入下面内容，作为common的子项目

   ```xml
   <dependency>    <groupId>com.atguigu.gulimall</groupId>    <artifactId>gulimall-common</artifactId>    <version>0.0.1-SNAPSHOT</version></dependency>
   ```

   ### 解决逆向工程的代码报错

   复制

   - renren-fast----utils包下的Query和`PageUtils`、`R`、`Constant`复制到common项目的`java/com.atguigu.common.utils`下。另外关于R的类，它继承了hashmap，你会发现map里的table数组是transient的，也就是不序列化的，但还好在它实现了Clonable接口重写了clone方法，该方法中会new新的数组作为序列号内容，所以hashmap可以用作序列化。但是序列号还是浅拷贝。在远程调用响应中，按理说应该自己序列化深拷贝后远程才能拿到，所以我得想法是应该自己序列化之后穿字节码(所以说json字符串)过去，但是视频里直接设置object就传输过去了，我比较奇怪为什么这种情况传输的不是浅拷贝？难道是mvc有这个自动序列化机制？之前读mvc源码没太注意，如果有人懂这个问题麻烦告知一下

   - 把@RequiresPermissions这些注解掉，因为是shiro的

   - 复制renren-fast中的xss包粘贴到common的com.atguigu.common目录下。

   - > 还复制了exception文件夹，对应的位置关系自己观察一下就行


   总之什么报错就去fast里面找

   - 注释掉product项目下类中的`//import org.apache.shiro.authz.annotation.RequiresPermissions;`，他是shiro的东西

   一个个注释太麻烦，统一把generator声明模版改了，重新生成代码，替换掉product的controller

   - 注释renren-generator\src\main\resources\template/Controller中所有的@RequiresPermissions。`## import org.apache.shiro.authz.annotation.RequiresPermissions;`

   ### 测试一下

   测试与整合商品服务里的mybatisplus，增删改查能不能用

   MyBatis-Plis快速入门：https://mp.baomidou.com/guide/quick-start.html# 

   在common的pom.xml中导入

   ```xml
   <!-- 数据库驱动 https://mvnrepository.com/artifact/mysql/mysql-connector-java --><dependency>    <groupId>mysql</groupId>    <artifactId>mysql-connector-java</artifactId>    <version>8.0.17</version></dependency><!--tomcat里一般都带--><dependency>    <groupId>javax.servlet</groupId>    <artifactId>servlet-api</artifactId>    <version>2.5</version>    <scope>provided</scope></dependency>
   ```

   删掉common里xss/xssfiler和XssHttpServletRequestWrapper

   在product项目的resources目录下新建application.yml

   ```yaml
   spring:  datasource:    username: root    password: root    url: jdbc:mysql://112.124.27.121:3306/gulimall_pms?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai    driver-class-name: com.mysql.jdbc.Driver# MapperScan# sql映射文件位置mybatis-plus:  mapper-locations: classpath:/mapper/**/*.xml  global-config:    db-config:      id-type: auto      logic-delete-value: 1      logic-not-delete-value: 0server:  port: 10000
   ```

   > classpath 和 classpath* 区别：
   > classpath：只会到你的class路径中查找找文件;
   > classpath*：不仅包含class路径，还包括jar文件中(class路径)进行查找
   >
   > `classpath*`的使用：当项目中有多个classpath路径，并同时加载多个classpath路径下（此种情况多数不会遇到）的文件，`*`就发挥了作用，如果不加`*`，则表示仅仅加载第一个classpath路径。

   然后在主启动类上加上注解@MapperScan()

   ```java
   @MapperScan("com.atguigu.gulimall.product.dao")@SpringBootApplicationpublic class gulimallProductApplication {    public static void main(String[] args) {        SpringApplication.run(gulimallProductApplication.class, args);    }}
   ```

   然后去测试，先通过下面方法给数据库添加内容

   ```java
   @SpringBootTestclass gulimallProductApplicationTests {    @Autowired    BrandService brandService;    @Test    void contextLoads() {        BrandEntity brandEntity = new BrandEntity();        brandEntity.setDescript("哈哈1哈");        brandEntity.setName("华为");        brandService.save(brandEntity);        System.out.println("保存成功");    }}
   ```

   在数据库中就能看到新增数据了

   ```java
   @SpringBootTestclass gulimallProductApplicationTests {    @Autowired    BrandService brandService;    @Test    void contextLoads() {        BrandEntity brandEntity = new BrandEntity();        brandEntity.setBrandId(1L);        brandEntity.setDescript("修改");        brandService.updateById(brandEntity);    }}
   ```

   ### 生成coupon代码

   1.优惠券服务。重新打开generator逆向工程，修改generator.properties，修改yml数据库信息

   2.启动生成RenrenApplication.java，运行后去浏览器80端口查看，同样让他一页全显示后选择全部后生成。生成后解压复制到coupon项目对应目录下。

   3.让coupon也依赖于common，修改pom.xml

   4.coupon添加application.yml

   ```yaml
   spring:  datasource:    username: root    password: root    url: jdbc:mysql://112.124.27.121:3306/gulimall-sms?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai    driver-class-name: com.mysql.cj.jdbc.Drivermybatis-plus:  mapper-locations: classpath:/mapper/**/*.xml  global-config:    db-config:      id-type: auto      logic-delete-value: 1      logic-not-delete-value: 0server:	port:7000
   ```

   5.运行测试gulimallCouponApplication.java

   http://localhost:7000/coupon/coupon/list

   ```
   {"msg":"success","code":0,"page":{"totalCount":0,"pageSize":10,"totalPage":0,"currPage":1,"list":[]}}
   ```

   ### 生成member、order、ware代码

   同上4步操作

   端口号分配情况：

   - coupon：7000

   - member：8000

   - order：9000

   - product：10000

   - ware：11000

   测试：

   member：

   http://localhost:8000/member/growthchangehistory/list

   ```
   {"msg":"success","code":0,"page":{"totalCount":0,"pageSize":10,"totalPage":0,"currPage":1,"list":[]}}
   ```

   order：

   http://localhost:9000/order/order/list

   ```
   {"msg":"success","code":0,"page":{"totalCount":0,"pageSize":10,"totalPage":0,"currPage":1,"list":[]}}
   ```

   ware：

   http://localhost:11000/ware/wareinfo/list

   ```
   {"msg":"success","code":0,"page":{"totalCount":0,"pageSize":10,"totalPage":0,"currPage":1,"list":[]}}
   ```

   # 分布式组件

   ## SpringCloud Alibaba简介

   SpringCloud微服务笔记—尚硅谷阳哥：https://blog.csdn.net/hancoder/article/details/109063671

   阿里18年开发的微服务一站式解决方案。https://github.com/alibaba/spring-cloud-alibaba/blob/master/README-zh.md

   - 注册中心：Nacos
   - 配置中心：Nacos
   - 网关：Gateway

   netflix把feign闭源了，spring cloud开了个open feign

   在common的pom.xml中加入

   ```xml
   <dependencyManagement>    <dependencies>        <dependency>            <groupId>com.alibaba.cloud</groupId>            <artifactId>spring-cloud-alibaba-dependencies</artifactId>            <version>2.2.0.RELEASE</version>            <type>pom</type>            <scope>import</scope>        </dependency>    </dependencies></dependencyManagement>
   ```

   上面是依赖管理，相当于以后在dependencies里引spring cloud alibaba就不用写版本号， 全用dependencyManagement进行管理。注意他和普通依赖的区别，他只是备注一下，并没有加入依赖

   ## Nacos作为注册中心

   一个更易于构建云原生应用的动态服务发现、配置管理和服务管理平台。

   nacos作为我们的注册中心和配置中心。

   注册中心文档：https://github.com/alibaba/spring-cloud-alibaba/tree/master/spring-cloud-alibaba-examples/nacos-example/nacos-discovery-example

   其他文档在该项目上层即可找到，下面读一读官网给的介绍就会用了。

   安装启动nacos：下载--解压--双击bin/startup.cmd。http://127.0.0.1:8848/nacos/ 账号密码nacos

   > 自己搭建nacos源码（推荐）：https://blog.csdn.net/xiaotian5180/article/details/105478543
   >
   > 为了能git管理nacos及其内置的数据库，我们采用这种方式，方便你运行时也保留原有内置数据库内容

   > Linux/Unix/Mac 操作系统，执行命令 `sh startup.sh -m standalone`

   使用nacos：

   - 在某个项目里properties里写` spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848`（yaml同理，指定nacos的地址）。再指定applicatin.name告诉注册到nacos中以什么命名

   - 依赖：common里写好依赖，哪个服务要注册再依赖common（不写版本是因为里面有了版本管理）

     ```xml
     <dependency>    <groupId>com.alibaba.cloud</groupId>    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId></dependency>
     ```

   - 使用 `@EnableDiscoveryClient` 注解开启服务注册与发现功能

     ```java
     @SpringBootApplication@EnableDiscoveryClientpublic class ProviderApplication {    public static void main(String[] args) {        SpringApplication.run(Application.class, args);    }    @RestController    class EchoController {        @GetMapping(value = "/echo/{string}")        public String echo(@PathVariable String string) {            return string;        }    }}
     ```

   - 最后application.yml内容，配置了服务中心名和当前模块名字

   ```yaml
   spring:  cloud:    nacos:      discovery:        server-addr: 127.0.0.1:8848  application:    name: gulimall-coupon
   ```

   然后依次给member、order、product、ware配置上面的yaml，改下name就行。再给每个项目配置类上加上注解@EnableDiscoveryClient

   nacos测试：

   **测试member和coupon的远程调用**

   想要获取当前会员领取到的所有优惠券。先去注册中心找优惠券服务，注册中心调一台优惠券服务器给会员，会员服务器发送请求给这台优惠券服务器，然后对方响应。

   - 服务请求方发送了2次请求，先问nacos要地址，然后再请求

   ## Openfeign（远程调用）

   ### **声明式远程调用：**

   （以会员服务远程调用优惠券服务为例）

   1.member服务加入openfeign依赖

   ```xml
   <dependency>    <groupId>org.springframework.cloud</groupId>    <artifactId>spring-cloud-starter-openfeign</artifactId></dependency>
   ```

   2.在coupon服务中写一个供调用的方法

   ```java
   @RequestMapping("coupon/coupon")public class CouponController {    @Autowired    private CouponService couponService;    @RequestMapping("/member/list")    public R membercoupons(){//全系统的所有返回都返回R        // 简化了，不去数据库查了，构造了一个优惠券给他返回        CouponEntity couponEntity = new CouponEntity();        couponEntity.setCouponName("满100-10");//优惠券的名字        return R.ok().put("coupons",Arrays.asList(couponEntity));    }
   ```

   3.在member的com.atguigu.gulimall.member.feign包下写一个接口

   ```java
   @FeignClient("gulimall-coupon") //3.1告诉spring cloud这个接口是，要调用coupon服务(nacos中找到)，具体是调用coupon服务的/coupon/coupon/member/list对应的方法public interface CouponFeignService {    @RequestMapping("/coupon/coupon/member/list")// 3.2远程服务的url    public R membercoupons();//3.3复制coupon函数部分，得到一个R对象}
   ```

   > @FeignClient+@RequestMapping构成远程调用的坐标
   >
   > //注意这里不是控制层，所以这个请求映射请求的不是我们服务器上的东西，而是nacos注册中心的

   4.在member的启动类上加注解`@EnableDiscoveryClient`告诉member是一个远程调用客户端

   ```java
   @EnableFeignClients(basePackages="com.atguigu.gulimall.member.feign")//扫描接口方法注解@EnableDiscoveryClient@SpringBootApplicationpublic class gulimallMemberApplication {	public static void main(String[] args) {		SpringApplication.run(gulimallMemberApplication.class, args);	}}
   ```

   5.测试，在member的控制层写一个测试请求

   ```java
   @RestController@RequestMapping("member/member")public class MemberController {    @Autowired    private MemberService memberService;    @Autowired    CouponFeignService couponFeignService;    @RequestMapping("/coupons")    public R test(){        MemberEntity memberEntity = new MemberEntity();        memberEntity.setNickname("会员昵称张三");        R membercoupons = couponFeignService.membercoupons();//假设张三去数据库查了后返回了张三的优惠券信息        //打印会员和优惠券信息        //membercoupons是一个R对象，R继承了HashMap，membercoupons.get("coupons")代表Key为"coupons"的value        //两个return的对象R不是同一个        return R.ok().put("member",memberEntity).put("coupons",membercoupons.get("coupons"));//    }
   ```

   重新启动服务

   http://localhost:8000/member/member/coupons

   ```json
   {    "msg":"success",    "code":0,    "coupons":[        {"id":null,"couponType":null,"couponImg":null,"couponName":"满100-10","num":null,"amount":null,"perLimit":null,"minPoint":null,"startTime":null,"endTime":null,"useType":null,"note":null,"publishCount":null,"useCount":null,"receiveCount":null,"enableStartTime":null,"enableEndTime":null,"code":null,"memberLevel":null,"publish":null}    ],    "member":{"id":null,"levelId":null,"username":null,"password":null,"nickname":"会员昵称张三","mobile":null,"email":null,"header":null,"gender":null,"birth":null,"city":null,"job":null,"sign":null,"sourceType":null,"integration":null,"growth":null,"status":null,"createTime":null}}
   ```

   **重点：**

   coupon里的R.ok()是什么，就是设置了个msg

   ```java
   public class R extends HashMap<String, Object> {//R继承了HashMap    // ok是个静态方法，new了一个R对象，并且  	public static R ok() {			return new R();		}  	public R() {			put("code", 0);			put("msg", "success");		}  	public R put(String key, Object value) {			super.put(key, value);//就是hashmap的put			return this;		}}
   ```

   ## Nacos作为配置中心

   配置中心的意思是不在application.properties等文件中配置了，而是放到nacos配置中心公用，这样无需每台机器都改。

   官方教程：https://github.com/alibaba/spring-cloud-alibaba/blob/master/spring-cloud-alibaba-examples/nacos-example/nacos-config-example/readme-zh.md

   ### 简单配置

   common中添加依赖 nacos配置中心

   ```xml
   <dependency>     <groupId>com.alibaba.cloud</groupId>     <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId> </dependency>
   ```

   在coupons项目中创建/src/main/resources/**bootstrap.properties** ，这个文件是springboot里规定的，他的优先级别比application.properties高

   ```properties
   # 改名字，对应nacos里的配置文件名spring.application.name=gulimall-couponspring.cloud.nacos.config.server-addr=127.0.0.1:8848
   ```

   浏览器去nacos里的配置列表，点击＋号，data ID：`gulimall-coupon.properties`，配置

   ```properties
   # gulimall-coupon.propertiescoupon.user.name="配置中心"      coupon.user.age=12
   ```

   然后点击发布。

   实际生产中不能重启应用。在coupon的控制层上加`@RefreshScope`

   ```java
   @RefreshScope@RestController@RequestMapping("coupon/coupon")public class CouponController {    @Autowired    private CouponService couponService;    @Value("${coupon.user.name}")//从application.properties中获取//不要写user.name，他是环境里的变量    private String name;    @Value("${coupon.user.age}")    private Integer age;    @RequestMapping("/test")    public R test(){        return R.ok().put("name",name).put("age",age);    }
   ```

   http://localhost:7000/coupon/coupon/test

   ```json
   {"msg":"success","code":0,"name":"配置中心","age":12}
   ```

   nacos的配置内容优先于项目本地的配置内容。

   ### **配置中心进阶**

   在nacos浏览器中还可以配置：

   - 命名空间：用作配置隔离。（一般每个微服务一个命名空间）

     - 默认public。默认新增的配置都在public空间下

     - 开发、测试、开发可以用命名空间分割。properties每个空间有一份。

     - 在bootstrap.properties里配置（测试完去掉，学习不需要）

       ```properties
       # 可以选择对应的命名空间 # 写上对应环境的命名空间IDspring.cloud.nacos.config.namespace=b176a68a-6800-4648-833b-be10be8bab00
       ```

     - 也可以为每个微服务配置一个命名空间，微服务互相隔离

   - 配置集：一组相关或不相关配置项的集合。

   - 配置集ID：类似于配置文件名，即Data ID

   - 配置分组：默认所有的配置集都属于`DEFAULT_GROUP`。双十一，618的优惠策略改分组即可

     ```properties
     # 更改配置分组spring.cloud.nacos.config.group=DEFAULT_GROUP
     ```

   ### **最终方案：**

   **每个微服务创建自己的命名空间，然后使用配置分组区分环境（dev/test/prod）**

   ### **加载多配置集**

   把原来application.yml里的内容都分文件抽离出去。我们在nacos里创建好后，在coupon里指定要导入的配置即可。

   bootstrap.properties

   ```properties
   spring.application.name=gulimall-couponspring.cloud.nacos.config.server-addr=127.0.0.1:8848# 可以选择对应的命名空间 # 写上对应环境的命名空间IDspring.cloud.nacos.config.namespace=b176a68a-6800-4648-833b-be10be8bab00# 更改配置分组spring.cloud.nacos.config.group=dev#新版本不建议用下面的了#spring.cloud.nacos.config.ext-config[0].data-id=datasource.yml#spring.cloud.nacos.config.ext-config[0].group=dev#spring.cloud.nacos.config.ext-config[0].refresh=true#spring.cloud.nacos.config.ext-config[1].data-id=mybatis.yml#spring.cloud.nacos.config.ext-config[1].group=dev#spring.cloud.nacos.config.ext-config[1].refresh=true#spring.cloud.nacos.config.ext-config[2].data-id=other.yml#spring.cloud.nacos.config.ext-config[2].group=dev#spring.cloud.nacos.config.ext-config[2].refresh=truespring.cloud.nacos.config.extension-configs[0].data-id=datasource.ymlspring.cloud.nacos.config.extension-configs[0].group=devspring.cloud.nacos.config.extension-configs[0].refresh=truespring.cloud.nacos.config.extension-configs[1].data-id=mybatis.ymlspring.cloud.nacos.config.extension-configs[1].group=devspring.cloud.nacos.config.extension-configs[1].refresh=truespring.cloud.nacos.config.extension-configs[2].data-id=other.ymlspring.cloud.nacos.config.extension-configs[2].group=devspring.cloud.nacos.config.extension-configs[2].refresh=true
   ```

   ## Gateway 网关

   动态上下线：前端发请求需要知道商品服务的地址，该服务有123服务器，假如1号掉线，请求就得手动改，所以需要网关动态管理，他能从注册中心中实时地感知某个服务上线的还是线下**（请求先通过网关路由到服务提供者）**

   拦截：请求也要加上询问权限，看用户有没有权限访问

   网关是请求流量的入口，常用功能包括**路由转发，权限校验，限流控制**等。SpringCloud `Gateway`取代了`Zuul`网关。

   https://spring.io/projects/spring-cloud-gateway

   ### 三大核心概念：

   - **Route（路由）**:The basic building block of the gateway. It is defined by an ID, a destination URI, a collection of predicates, and a collection of filters. A route is matched if the aggregate predicate is true. 发一个请求给网关，网关要将请求路由到指定的服务。路由有id，目的地uri，断言的集合，匹配了断言就能到达指定位置，
   - **Predicate（断言）**: This is a [Java 8 Function Predicate](https://docs.oracle.com/javase/8/docs/api/java/util/function/Predicate.html). The input type is a [Spring Framework `ServerWebExchange`](https://docs.spring.io/spring/docs/5.0.x/javadoc-api/org/springframework/web/server/ServerWebExchange.html). This lets you match on anything from the HTTP request, such as headers or parameters.就是java里的断言函数，可以匹配请求里的任何信息，包括请求头等，用来判定是不是true，才能路由到指定服务
   - **Filter（过滤）**: These are instances of [Spring Framework `GatewayFilter`](https://docs.spring.io/spring/docs/5.0.x/javadoc-api/org/springframework/web/server/GatewayFilter.html) that have been constructed with a specific factory. Here, you can modify requests and responses before or after sending the downstream request.过滤器请求和响应都可以被修改。

   ### 流程：

   请求发送到网关，**断言**判定请求信息是否符合某个路由规则，才能**路由**到指定服务，路由途中经过**过滤器**

   

   客户端发请求给服务端。中间有网关。先交给映射器，如果能处理就交给handler处理，然后交给一系列Filer，然后给指定的服务，再返回回来给客户端。

   ![](/Users/r1ff/Desktop/学习笔记/img/spring_cloud_gateway_diagram.png)

   断言：

   ```yml
   spring:
     cloud:
       gateway:
         routes:
         - id: after_route
           uri: https://example.org
           predicates:
           - Cookie=mycookie,mycookievalue
   ```

   `-`代表数组，可以设置Cookie等内容。只有断言成功了，才路由到指定的地址。


   ```yml
   spring:
     cloud:
       gateway:
         routes:
         - id: after_route
           uri: https://example.org
           predicates:
           - name: Cookie
             args:
               name: mycookie
               regexp: mycookievalue
   ```

   创建，使用initilizer，Group：com.atguigu.gulimall，Artifact： gulimall-gateway，package：com.atguigu.gulimall.gateway。 搜索gateway选中。


   pom.xml里加上common依赖

   # 前端基础

   ## 技术栈简介

   ![在这里插入图片描述](https://img-blog.csdnimg.cn/202104190757038.png)

   ## ES6

   `ECMAScript6.0`（以下简称ES6，ECMAScript是一种由Ecma国际通过ECMA-262标准化的脚本），是JavaScript语言的下一代标准，2015年6月正式发布，从ES6开始的版本号采用年号，如

   - ES2015，就是ES6。
   - ES2016，就是ES7。
   - ES2017，就是ES8。

   > **ECMAScript是规范，JS的规范的具体实现**。

   打开VSCode---打开文件夹---新建es6文件夹---新建文件1、let.html---`shift+!+Enter`生成模板。填入下面内容后，右键open with live server

   ### 1、let语法

   |                                     | var  | let    |
   | ----------------------------------- | ---- | ------ |
   | 越域                                | 会   | 不会   |
   | 多次声明同一个变量                  | 可以 | 不可以 |
   | 声明之前就拿来访问（变量提升）      | 可以 | 不可以 |
   | const声明后不允许改变，且必须初始化 |      |        |

   ```html
   <!DOCTYPE html><html lang="en"><head>    <meta charset="UTF-8">    <meta http-equiv="X-UA-Compatible" content="IE=edge">    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <title>Document</title></head><body>    <script>        //var会越域，let不会        {            var a = 1;            let b = 2;        }        console.log(a);  // 1        // console.log(b);  // ReferenceError: b is not defined        // var可以声明多次 let只能声明一次        var m = 1        var m = 2        let n = 3        // let n = 4        console.log(m)  // 2        console.log(n)  // Identifier 'n' has already been declared        // var可以变量提升 let不可以变量提升        console.log(x);  // undefined        var x = 10;        // console.log(y);   //ReferenceError: y is not defined        let y = 20;        // 1. const声明之后不允许改变        // 2. 一但声明必须初始化        const c = 1;        // c = 3; //Uncaught TypeError: Assignment to constant variable.    </script></body></html>
   ```

   ### 2、解构表达式.html

   - 数组解构
   - 对象解构
   - 字符串API
   - 字符串模板 ``可以定义多行字符串
   - 字符串插入变量和JavaScript表达式。用${}

   ```html
   <!DOCTYPE html><html lang="en"><head>    <meta charset="UTF-8">    <meta http-equiv="X-UA-Compatible" content="IE=edge">    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <title>Document</title></head><body>    <script>        //1.数组解构        let arr = [1, 2, 3];        //以前写法        // // let a = arr[0];        // // let b = arr[1];        // // let c = arr[2];        let [a, b, c] = arr;        console.log(a, b, c)        //2.对象解构        const person = {            name: "jack",            age: 21,            language: ['java', 'js', 'css']        }        //以前写法        //         const name = person.name;        //         const age = person.age;        //         const language = person.language;        // 把name变量名变为abc，声明了abc、age、language三个变量        const { name: abc, age, language } = person;        console.log(abc, age, language)        //3.字符串扩展        let str = "hello.vue";        console.log(str.startsWith("hello"));//返回布尔值，是否在头部        console.log(str.endsWith(".vue"));//是否在尾部        console.log(str.includes("e"));//是否包含        console.log(str.includes("hello"));//true        //4.字符串模板 ``可以定义多行字符串        let ss = `<div>                    <span>hello world<span>                </div>`;        console.log(ss);        function fun() {            return "这是一个函数"        }        //5.字符串插入变量和JavaScript表达式。用${}        let info = `我是${abc}，今年${age + 10}了, 我想说： ${fun()}`;        console.log(info);    </script></body></html>
   ```

   ### 3、函数优化.html

   - 默认参数优化
   - 不定参数
   - 箭头函数

   ```html
   <!DOCTYPE html><html lang="en"><head>    <meta charset="UTF-8">    <meta http-equiv="X-UA-Compatible" content="IE=edge">    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <title>Document</title></head><body>    <!DOCTYPE html>    <html lang="en">    <head>        <meta charset="UTF-8">        <meta name="viewport" content="width=device-width, initial-scale=1.0">        <meta http-equiv="X-UA-Compatible" content="ie=edge">        <title>Document</title>    </head>    <body>        <script>            //在ES6以前，我们无法给一个函数参数设置默认值，只能采用变通写法：            function add(a, b) {                // 判断b是否为空，为空就给默认值1                b = b || 1;                return a + b;            }            // 传一个参数            console.log(add(10));            //1.默认参数优化：直接给参数写上默认值，没传就会自动使用默认值            function add2(a, b = 1) {                return a + b;            }            console.log(add2(20));            //2.不定参数            function fun(...values) {                console.log(values.length)            }            fun(1, 2)      //2            fun(1, 2, 3, 4)  //4            //3.箭头函数。lambda            //以前声明一个方法            // var print = function (obj) {            //     console.log(obj);            // }            var print = obj => console.log(obj);            //使用方法            print("hello");            //两数之和            // var sum = function (a, b) {            //     return a + b;            // }            var sum2 = (a, b) => a + b;            console.log(sum2(11, 12));//23            //多行            var sum3 = (a, b) => {                c = a + b;                return a + c;            }            console.log(sum3(10, 20))//40            //4.箭头函数+解构            //创建对象            const person = {                name: "jack",                age: 21,                language: ['java', 'js', 'css']            }            //原来            function hello(person) {                console.log("hello," + person.name)            }            //现在            var hello2 = ({ name }) => console.log("hello," + name);//{name}代表解构            hello2(person);        </script>    </body>    </html></body></html>
   ```

   ### 4、对象优化.html

   - keys、values、entries方法
   - assign方法合并
   - 声明对象简写
   - 对象的函数属性简写
   - 对象拓展运算符


   ```html
   <!DOCTYPE html><html lang="en"><head>    <meta charset="UTF-8">    <meta http-equiv="X-UA-Compatible" content="IE=edge">    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <title>Document</title></head><body>    <script>        //1.keys、values、entries方法        //创建对象        const person = {            name: "jack",            age: 21,            language: ['java', 'js', 'css']        }        console.log(Object.keys(person));//["name", "age", "language"]        console.log(Object.values(person));//["jack", 21, Array(3)]        console.log(Object.entries(person));//[Array(2), Array(2), Array(2)]        //2.assign方法合并        //创建对象        const target = { a: 1 };        const source1 = { b: 2 };        const source2 = { c: 3 };        //合并到target        Object.assign(target, source1, source2);        console.log(target);//{a:1,b:2,c:3}        //3.声明对象简写        const age = 23        const name = "张三"        const person1 = { age: age, name: name }        // 等价于        const person2 = { age, name }//要创建的对象属性名和要使用的变量名一样        console.log(person2);        //4.对象的函数属性简写        let person3 = {            name: "jack",            // 以前：            eat: function (food) {                console.log(this.name + "在吃" + food);            },            //箭头函数this不能使用，要使用的话需要使用：对象.属性            eat2: food => console.log(person3.name + "在吃" + food),            eat3(food) {                console.log(this.name + "在吃" + food);            }        }        person3.eat("香蕉");        person3.eat2("苹果")        person3.eat3("橘子");        //5.对象拓展运算符        //拷贝对象（深拷贝）        let p1 = { name: "Amy", age: 15 }        let someone = { ...p1 }        console.log(someone)  //{name: "Amy", age: 15}        //合并对象        let age1 = { age: 15 }        let name1 = { name: "Amy" }        let p2 = { name: "zhangsan" }        p2 = { ...age1, ...name1 }        console.log(p2)    </script></body></html>
   ```

   

   ### 5、map和reduce.html

   `map()`：接收一个函数，将原数组中的所有元素用这个函数处理后放入新数组返回。
   `reduce()` 为数组中的每一个元素依次执行回调函数，不包括数组中被删除或从未被赋值的元素，

   ```html
   <!DOCTYPE html><html lang="en"><head>    <meta charset="UTF-8">    <meta http-equiv="X-UA-Compatible" content="IE=edge">    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <title>Document</title></head><body>    <script>        let arr = ['1', '20', '-5', '3'];        //1.map()        //接收一个函数，将原数组中的所有元素用这个函数处理后放入新数组返回。        arr = arr.map(item => item * 2);        console.log(arr);        //2.reduce()        //为数组中的每一个元素依次执行回调函数，不包括数组中被删除或从未被赋值的元素，        //[2, 40, -10, 6]        //arr.reduce(callback,[initialValue])        /**        1、previousValue （是提供的初始值（initialValue））        2、currentValue （数组中当前被处理的元素）        3、index （当前元素在数组中的索引）        4、array （调用 reduce 的数组）*/        let result = arr.reduce((a, b) => {            console.log("上一次处理后：" + a);            console.log("当前正在处理：" + b);            return a + b;        }, 100);        console.log(result)    </script></body></html>
   ```

   ### 6、promise.html

   优化异步操作。封装ajax

   ```html
   <!DOCTYPE html><html lang="en"><head>    <meta charset="UTF-8">    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <meta http-equiv="X-UA-Compatible" content="ie=edge">    <title>Document</title>    <script src="https://cdn.bootcss.com/jquery/3.4.1/jquery.min.js"></script></head><body>    <script>        //1、查出当前用户信息        //2、按照当前用户的id查出他的课程        //3、按照当前课程id查出分数        // $.ajax({        //     url: "mock/user.json",        //     success(data) {        //         console.log("查询用户：", data);        //         $.ajax({        //             url: `mock/user_corse_${data.id}.json`,        //             success(data) {        //                 console.log("查询到课程：", data);        //                 $.ajax({        //                     url: `mock/corse_score_${data.id}.json`,        //                     success(data) {        //                         console.log("查询到分数：", data);        //                     },        //                     error(error) {        //                         console.log("出现异常了：" + error);        //                     }        //                 });        //             },        //             error(error) {        //                 console.log("出现异常了：" + error);        //             }        //         });        //     },        //     error(error) {        //         console.log("出现异常了：" + error);        //     }        // });        //1、Promise可以封装异步操作        // let p = new Promise((resolve, reject) => { //传入成功解析，失败拒绝        //     //1、异步操作        //     $.ajax({        //         url: "mock/user.json",        //         success: function (data) {        //             console.log("查询用户成功:", data)        //             resolve(data);        //         },        //         error: function (err) {        //             reject(err);        //         }        //     });        // });        // p.then((obj) => { //成功以后做什么        //     return new Promise((resolve, reject) => {        //         $.ajax({        //             url: `mock/user_corse_${obj.id}.json`,        //             success: function (data) {        //                 console.log("查询用户课程成功:", data)        //                 resolve(data);        //             },        //             error: function (err) {        //                 reject(err)        //             }        //         });        //     })        // }).then((data) => { //成功以后干什么        //     console.log("上一步的结果", data)        //     $.ajax({        //         url: `mock/corse_score_${data.id}.json`,        //         success: function (data) {        //             console.log("查询课程得分成功:", data)        //         },        //         error: function (err) {        //         }        //     });        // })        function get(url, data) { //自己定义一个方法整合一下            return new Promise((resolve, reject) => {                $.ajax({                    url: url,                    data: data,                    success: function (data) {                        resolve(data);                    },                    error: function (err) {                        reject(err)                    }                })            });        }        get("mock/user.json")            .then((data) => {                console.log("用户查询成功~~~:", data)                return get(`mock/user_corse_${data.id}.json`);            })            .then((data) => {                console.log("课程查询成功~~~:", data)                return get(`mock/corse_score_${data.id}.json`);            })            .then((data) => {                console.log("课程成绩查询成功~~~:", data)            })            .catch((err) => { //失败的话catch                console.log("出现异常", err)            });    </script></body></html>
   ```

   corse_score_10.json 得分

   ```json
   {    "id": 100,    "score": 90}
   ```

   user.json 用户

   ```json
   {    "id": 1,    "name": "zhangsan",    "password": "123456"}
   ```

   user_corse_1.json 课程

   ```json
   {    "id": 10,    "name": "chinese"}
   ```

   ### 7、模块化import/export

   模块化就是把代码进行拆分，方便重复利用。类似于java中的导包，而JS换了个概念，是导模块。

   模块功能主要有两个命令构成 export 和import

   - export用于规定模块的对外接口
   - import用于导入其他模块提供的功能

   user.js

   ```js
   var name = "jack"var age = 21function add(a,b){    return a + b;}// 导出变量和函数export {name,age,add}
   ```

   hello.js

   ```js
   // export const util = {//     sum(a, b) {//         return a + b;//     }// }// 导出后可以重命名export default {    sum(a, b) {        return a + b;    }}// export {util}//`export`不仅可以导出对象，一切JS变量都可以导出。比如：基本类型变量、函数、数组、对象。
   ```

   main.js

   ```js
   import abc from "./hello.js"import {name,add} from "./user.js"abc.sum(1,2);console.log(name);add(1,3);
   ```

   ## Vue

   ### MVVM思想

   - M：model 包括数据和一些基本操作
   - V：view 视图，页面渲染结果
   - VM：View-model，模型与视图间的双向操作（无需开发人员干涉）

   视图和数据通过VM绑定起来，model里有变化会自动地通过Directives填写到视view中，视图表单中添加了内容也会自动地通过DOM Listeners保存到模型中。

   vue官方教程：https://cn.vuejs.org/v2/guide/

   **特点：把开发人员从琐的D0M操作中解放出来，那关注点放在如何操作Model上。**

   ### 安装：

   - 或者在VScode终端使用npm
     - 先`npm init -y`初始化项目，生成了一个`package.json`文件，说明他是一个npm管理的项目
       - 类似于maven的pom.xml
     - `npm install vue`，安装后在项目`node_modules`里有vue
       - 类似maven install拉取远程到本地

   ### 简单使用：

   index.html

   ```html
   <!DOCTYPE html><html lang="en"><head>    <meta charset="UTF-8">    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <meta http-equiv="X-UA-Compatible" content="ie=edge">    <title>Document</title></head><body>    <div id="app">        <input type="text" v-model="num">        v-model实现双向绑定。此处代表输入框和vue里的data绑定                <button v-on:click="num++">点赞</button>        v-on:click绑定事件，实现自增。                <button v-on:click="cancel">取消</button>        回调自定义的方法。 此时字符串里代表的函数                <h1> {{name}} ,非常帅，有{{num}}个人为他点赞{{hello()}}</h1>        先从vue中拿到值填充到dom，input再改变num值，vue实例更新，然后此处也更新    </div>    <!-- 导入依赖 -->    <script src="./node_modules/vue/dist/vue.js"></script>    <script>        //1、vue声明式渲染        let vm = new Vue({ //生成vue对象            el: "#app",//绑定元素 div id="app" // 可以指定恰标签，但是不可以指定body标签            data: {  //封装数据                name: "张三",  // 也可以使用{} //表单中可以取出                num: 1            },            methods:{  //封装方法                cancel(){                    this.num -- ;                },                hello(){                    return "1"                }            }        });        // 还可以在html控制台vm.name        //2、双向绑定,模型变化，视图变化。反之亦然。        //3、事件处理        //v-xx：指令        //1、创建vue实例，关联页面的模板，将自己的数据（data）渲染到关联的模板，响应式的        //2、指令来简化对dom的一些操作。        //3、声明方法来做更复杂的操作。methods里面可以封装方法。    </script></body></html>
   ```

   在VSCode中安装vue 2 snippets语法提示插件，在谷歌浏览器中安装vue-devtool工具

   ### 指令

   #### 1、v-text、v-html.html

   > 插值闪烁：
   >
   > 使用{{}}方式在网速较慢时会出现问题。在数据未加载完成时，页面会显示出原始的`{{}}`，加载完毕后才显示正确数据，我们称为插值闪烁。
   > 我们将网速调慢一些，然后刷新页面，试试看刚才的案例

   ```html
   <!DOCTYPE html><html lang="en"><head>    <meta charset="UTF-8">    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <meta http-equiv="X-UA-Compatible" content="ie=edge">    <title>Document</title></head><body>       <div id="app">        这个叫插值表达式，可以计算，可以取值，可以调用函数        <br>        {{msg}}  {{1+1}}  {{hello()}}         <br>        如果网速慢的话会先显示括号，然后才替换成数据。        v-html 和v-text能解决这个问题        <br/>                用v-html取内容        <span v-html="msg"></span>                <br/>        原样显示        <span v-text="msg"></span>      </div>       <script src="../node_modules/vue/dist/vue.js"></script>    <script>        new Vue({            el:"#app",            data:{                msg:"<h1>Hello</h1>"            },            methods:{                hello(){                    return "World"                }            }        })    </script>    </body></html>
   ```

   #### 2、v-bind.html单向绑定

   插值表达式只能用在标签体里，如果我们这么用`<a href="{{}}">`是不起作用的，所以需要 `<a v-bind:href="link">跳转</a>`这种用法

   解决：用`v-bind:`，简写为`:`

   ```html
   <!DOCTYPE html><html lang="en"><head>    <meta charset="UTF-8">    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <meta http-equiv="X-UA-Compatible" content="ie=edge"></head><body>    <!-- 给html标签的属性绑定 -->    <div id="app">         <a v-bind:href="link">跳转</a>        <!-- class,style  {class名：vue值}-->        <span v-bind:class="{active:isActive,'text-danger':hasError}"          :style="{color: color1,fontSize: size}">你好</span>    </div>    <script src="../node_modules/vue/dist/vue.js"></script>    <script>        let vm = new Vue({            el:"#app",            data:{                link: "http://www.baidu.com",                isActive:true,                hasError:true,                color1:'red',                size:'36px'            }        })    </script></body></html>
   ```

   #### 3、v-model.html双向绑定

   ```html
   <!DOCTYPE html><html lang="en"><head>    <meta charset="UTF-8">    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <meta http-equiv="X-UA-Compatible" content="ie=edge">    <title>Document</title></head><body>    <!-- 表单项，自定义组件 -->    <div id="app">        精通的语言：如果是多选框，那么会把每个value值赋值给vue数据            <input type="checkbox" v-model="language" value="Java"> java<br/>            <input type="checkbox" v-model="language" value="PHP"> PHP<br/>            <input type="checkbox" v-model="language" value="Python"> Python<br/>        选中了 {{language.join(",")}}    </div>        <script src="../node_modules/vue/dist/vue.js"></script>    <script>        let vm = new Vue({            el:"#app",            data:{                language: []            }        })    </script></body></html>
   ```

   #### 4、v-on.html绑定事件

   事件监听可以使用 v-on 指令

   `v-on:事件类型="方法"`  ，可以简写成`@事件类型="方法"`

   

   >   事件冒泡：大小div都有单机事件，点了内部div相当于外部div也点击到了。
   >
   >   如果不想点击内部div冒泡到外部div，可以使用.prevent阻止事件冒泡
   >
   >   用法是`v-on:事件类型.事件修饰符="方法"`
   >
   >   还可以绑定按键修饰符
   >
   >   v-on:keyup.up="num+=2" @keyup.down="num-=2" @click.ctrl="num=10"

   按键修饰符

   ```html
   <!DOCTYPE html><html lang="en"><head>    <meta charset="UTF-8">    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <meta http-equiv="X-UA-Compatible" content="ie=edge">    <title>Document</title></head><body>    <div id="app">                        <!--事件中直接写js片段-->        <button v-on:click="num++">点赞</button>        <!--事件指定一个回调函数，必须是Vue实例中定义的函数-->        <button @click="cancel">取消</button>        <!--  -->        <h1>有{{num}}个赞</h1>        <!-- 事件修饰符 -->        <div style="border: 1px solid red;padding: 20px;" v-on:click.once="hello">            大div            <div style="border: 1px solid blue;padding: 20px;" @click.stop="hello">                小div <br />                <a href="http://www.baidu.com" @click.prevent.stop="hello">去百度</a>            </div>        </div>        <!-- 按键修饰符： -->        <input type="text" v-model="num" v-on:keyup.up="num+=2" @keyup.down="num-=2" @click.ctrl="num=10"><br />        提示：    </div>    <script src="../node_modules/vue/dist/vue.js"></script>    <script>        new Vue({            el:"#app",            data:{                num: 1            },            methods:{                cancel(){                    this.num--;                },                hello(){                    alert("点击了")                }            }        })    </script></body></html>
   ```

   #### 5、v-for.html

   可以遍历   数组[]  字典{}  。对于字典`<li v-for="(value, key, index) in object">`

   ```html
   <!DOCTYPE html><html lang="en"><head>    <meta charset="UTF-8">    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <meta http-equiv="X-UA-Compatible" content="ie=edge">    <title>Document</title></head><body>    <div id="app">        <ul>            <!-- 4、遍历的时候都加上:key来区分不同数据，提高vue渲染效率 -->            <li v-for="(user,index) in users" :key="user.name" v-if="user.gender == '女'">                <!-- 1、显示user信息：v-for="item in items" -->               当前索引：{{index}} ==> {{user.name}}  ==>  {{user.gender}} ==>{{user.age}} <br>                <!-- 2、获取数组下标：v-for="(item,index) in items" -->                <!-- 3、遍历对象：                        v-for="value in object"                        v-for="(value,key) in object"                        v-for="(value,key,index) in object"                 -->                对象信息：                <span v-for="(v,k,i) in user">{{k}}=={{v}}=={{i}}；</span>                <!-- 4、遍历的时候都加上:key来区分不同数据，提高vue渲染效率 -->            </li>                    </ul>        <ul>            <li v-for="(num,index) in nums" :key="index">                数字数组遍历：{{index}}=={{num}}            </li>        </ul>    </div>    <script src="../node_modules/vue/dist/vue.js"></script>    <script>                 let app = new Vue({            el: "#app",            data: {                users: [                { name: '柳岩', gender: '女', age: 21 },                { name: '张三', gender: '男', age: 18 },                { name: '范冰冰', gender: '女', age: 24 },                { name: '刘亦菲', gender: '女', age: 18 },                { name: '古力娜扎', gender: '女', age: 25 }                ],                nums: [1,2,3,4,4]            },        })    </script></body></html>
   ```

   #### 6、v-if和v-show.html

   在vue实例的data指定一个bool变量，然后v-show赋值即可。

   show里的字符串也可以比较

   if是根据表达式的真假，切换元素的显示和隐藏（操作dom元素）

   区别：show的标签F12一直都在，if的标签会移除

   if操作dom树对性能消耗大

   ```html
   <!DOCTYPE html><html lang="en"><head>    <meta charset="UTF-8">    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <meta http-equiv="X-UA-Compatible" content="ie=edge">    <title>Document</title></head><body>    <!--         v-if，顾名思义，条件判断。当得到结果为true时，所在的元素才会被渲染。        v-show，当得到结果为true时，所在的元素才会被显示。     -->    <div id="app">        <button v-on:click="show = !show">点我呀</button>        <!-- 1、使用v-if显示 -->        <h1 v-if="show">if=看到我....</h1>        <!-- 2、使用v-show显示 -->        <h1 v-show="show">show=看到我</h1>    </div>    <script src="../node_modules/vue/dist/vue.js"></script>            <script>        let app = new Vue({            el: "#app",            data: {                show: true            }        })    </script></body></html>
   ```

   #### 7、v-else和v-else-if.html

   ```html
   <!DOCTYPE html><html lang="en"><head>    <meta charset="UTF-8">    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <meta http-equiv="X-UA-Compatible" content="ie=edge">    <title>Document</title></head><body>    <div id="app">        <button v-on:click="random=Math.random()">点我呀</button>        <span>{{random}}</span>        <h1 v-if="random>=0.75">            看到我啦? &gt;= 0.75        </h1>        <h1 v-else-if="random>=0.5">            看到我啦? &gt;= 0.5        </h1>        <h1 v-else-if="random>=0.2">            看到我啦? &gt;= 0.2        </h1>        <h1 v-else>            看到我啦? &lt; 0.2        </h1>    </div>    <script src="../node_modules/vue/dist/vue.js"></script>            <script>                 let app = new Vue({            el: "#app",            data: { random: 1 }        })         </script></body></html>
   ```

   ### 计算属性和监听器和过滤器

   #### 计算属性和监听器.html

   ```html
   <!DOCTYPE html><html lang="en"><head>    <meta charset="UTF-8">    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <meta http-equiv="X-UA-Compatible" content="ie=edge">    <title>Document</title></head><body>    <div id="app">        <!-- 某些结果是基于之前数据实时计算出来的，我们可以利用计算属性。来完成 -->        <ul>            <li>西游记； 价格：{{xyjPrice}}，数量：<input type="number" v-model="xyjNum"> </li>            <li>水浒传； 价格：{{shzPrice}}，数量：<input type="number" v-model="shzNum"> </li>            <li>总价：{{totalPrice}}</li>            {{msg}}        </ul>    </div>    <script src="../node_modules/vue/dist/vue.js"></script>    <script>        //watch可以让我们监控一个值的变化。从而做出相应的反应。        new Vue({            el: "#app",            data: {                xyjPrice: 99.98,                shzPrice: 98.00,                xyjNum: 1,                shzNum: 1,                msg: ""            },            computed: {                totalPrice(){                    return this.xyjPrice*this.xyjNum + this.shzPrice*this.shzNum                }            },            watch: {                xyjNum: function(newVal,oldVal){                    if(newVal>=3){                        this.msg = "库存超出限制";                        this.xyjNum = 3                    }else{                        this.msg = "";                    }                }            },        })    </script></body></html>
   ```

   #### 过滤器.html

   定义filter组件后，管道符后面跟具体过滤器`{{user.gender | gFilter}}`

   ```html
   <!DOCTYPE html><html lang="en"><head>    <meta charset="UTF-8">    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <meta http-equiv="X-UA-Compatible" content="ie=edge">    <title>Document</title></head><body>    <!-- 过滤器常用来处理文本格式化的操作。过滤器可以用在两个地方：双花括号插值和 v-bind 表达式 -->    <div id="app">        <ul>            <li v-for="user in userList">                {{user.id}} ==> {{user.name}} ==> {{user.gender == 1?"男":"女"}} ==>                {{user.gender | genderFilter}} ==> {{user.gender | gFilter}}            </li>        </ul>    </div>    <script src="../node_modules/vue/dist/vue.js"></script>    <script>        // 全局过滤器        Vue.filter("gFilter", function (val) {            if (val == 1) {                return "男~~~";            } else {                return "女~~~";            }        })        let vm = new Vue({            el: "#app",            data: {                userList: [                    { id: 1, name: 'jacky', gender: 1 },                    { id: 2, name: 'peter', gender: 0 }                ]            },            filters: { // 局部过滤器，只可以在当前vue实例中使用                genderFilter(val) {                    if (val == 1) {                        return "男";                    } else {                        return "女";                    }                }            }        })    </script></body></html>
   ```

   ### 组件化

   ```html
   <!DOCTYPE html><html lang="en"><head>    <meta charset="UTF-8">    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <meta http-equiv="X-UA-Compatible" content="ie=edge"></head><body>    <div id="app">        <button v-on:click="count++">我被点击了 {{count}} 次</button>        每个对象都是独立统计的        <counter></counter>        <counter></counter>        <counter></counter>        <counter></counter>        <counter></counter>        <button-counter></button-counter>    </div>    <script src="../node_modules/vue/dist/vue.js"></script>    <script>        //1、全局声明注册一个组件 // counter标签，代表button        // 把页面中<counter>标签替换为指定的template，而template中的数据用data填充        Vue.component("counter", {            template: `<button v-on:click="count++">我被点击了 {{count}} 次</button>`,            data() {// 如果 Vue 没有这条规则，点击一个按钮就可能会像如下代码一样影响到其它所有实例：                return {                    count: 1 // 数据                }            }        });        //2、局部声明一个组件        const buttonCounter = {            template: `<button v-on:click="count++">我被点击了 {{count}} 次~~~</button>`,            data() {                return {                    count: 1                }            }        };        new Vue({            el: "#app",            data: {                count: 1            },            components: { // 要用的组件                'button-counter': buttonCounter            }        })    </script></body></html>
   ```

   ### 生命周期钩子函数

   ```html
   <!DOCTYPE html><html lang="en"><head>    <meta charset="UTF-8">    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <meta http-equiv="X-UA-Compatible" content="ie=edge">    <title>Document</title></head><body>    <div id="app">        <span id="num">{{num}}</span>        <button @click="num++">赞！</button>        <h2>{{name}}，有{{num}}个人点赞</h2>    </div>    <script src="../node_modules/vue/dist/vue.js"></script>        <script>        let app = new Vue({            el: "#app",            data: {                name: "张三",                num: 100            },            methods: {                show() {                    return this.name;                },                add() {                    this.num++;                }            },            beforeCreate() {                console.log("=========beforeCreate=============");                console.log("数据模型未加载：" + this.name, this.num);                console.log("方法未加载：" + this.show());                console.log("html模板未加载：" + document.getElementById("num"));            },            created: function () {                console.log("=========created=============");                console.log("数据模型已加载：" + this.name, this.num);                console.log("方法已加载：" + this.show());                console.log("html模板已加载：" + document.getElementById("num"));                console.log("html模板未渲染：" + document.getElementById("num").innerText);            },            beforeMount() {                console.log("=========beforeMount=============");                console.log("html模板未渲染：" + document.getElementById("num").innerText);            },            mounted() {                console.log("=========mounted=============");                console.log("html模板已渲染：" + document.getElementById("num").innerText);            },            beforeUpdate() {                console.log("=========beforeUpdate=============");                console.log("数据模型已更新：" + this.num);                console.log("html模板未更新：" + document.getElementById("num").innerText);            },            updated() {                console.log("=========updated=============");                console.log("数据模型已更新：" + this.num);                console.log("html模板已更新：" + document.getElementById("num").innerText);            }        });    </script></body></html>
   ```

   ### Vue脚手架模块化开发

   ```bash
   （终端运行）# 安装webpacksudo npm install webpack -g# 安装vue脚手架sudo npm install -g @vue/cli-init  
   ```

   ```sh
   # 在项目文件夹里执行# 初始化vue项目vue init webpack vue-demo# 运行，访问8080端口npm run dev 
   ```

   #### vue项目目录结构：

   | 目录/文件      | 说明                                                         |
   | :------------- | :----------------------------------------------------------- |
   | build          | 项目构建(webpack)相关代码                                    |
   | `config`       | 配置目录，包括端口号等。我们初学可以使用默认的。             |
   | `node_modules` | npm 加载的项目依赖模块                                       |
   | **src**        | 这里是我们要开发的目录，基本上要做的事情都在这个目录里。里面包含了几个目录及文件：<br />- `assets`: 放置一些图片，如logo等。<br />- `components`: 目录里面放了一个组件文件，可以不用。<br />- `App.vue`: 项目入口文件，我们也可以直接将组件写这里，而不使用 components 目录。<br />- `main.js`: 项目的核心文件。 |
   | static         | 静态资源目录，如图片、字体等。                               |
   | test           | 初始测试目录，可删除                                         |
   | .xxxx文件      | 这些是一些配置文件，包括语法配置，git配置等。                |
   | `index.html`   | 首页入口文件，你可以添加一些 meta 信息或统计代码啥的。       |
   | `package.json` | 项目配置文件。                                               |
   | README.md      | 项目的说明文档，markdown 格式                                |

   #### ==关系结构==

   ##### 1) index.html 首页入口文件

   index.html只有一个`<div id="app">`

   ```html
   <body>  <!-- 一个app元素 -->  <div id="app"></div></body>
   ```

   ##### 2) main.js 核心文件

   指定使用哪些东西用于改动DOM（文档对象模型）元素中的id

   ```js
   import Vue from 'vue'import App from './App'import router from './router'Vue.config.productionTip = false// 通过new Vue实例来挂载index的app元素new Vue({  //指定元素  el: '#app',  //指定路由  router:router,  //指定组件  components: { App:App },  //指定渲染模版  template: '<App/>'})
   ```

   ##### 3) router/index.js 路由跳转文件

   ```js
   import Vue from 'vue'import Router from 'vue-router'import HelloWorld from '@/components/HelloWorld'import Hello from '@/components/Hello'Vue.use(Router)export default new Router({  routes: [    {      path: '/',      name: 'HelloWorld',      component: HelloWorld    },    {      path:'/hello',      name:'Hello',      component:Hello    }  ]})
   ```

   ##### 4) App.vue  主要组件

   组件的三大结构

   - template
   - script
   - style

   ```vue
   <template>  <div id="app">    <img src="./assets/logo.png">    <!-- 改变路由视图的方法 -->    <router-link to="/hello">去Hello</router-link>    <router-link to="/">去首页</router-link>    <!-- 路由视图，根据url改变 -->    <router-view />  </div></template><script>export default {  name: 'App'}</script><style>#app {  font-family: "Avenir", Helvetica, Arial, sans-serif;  -webkit-font-smoothing: antialiased;  -moz-osx-font-smoothing: grayscale;  text-align: center;  color: #2c3e50;  margin-top: 60px;}</style>
   ```

   ##### 5) components/HelloWorld.vue  路由视图过来的组件

   ```vue
   <template>  <div class="hello">    <h1>{{ msg }}</h1>    <h2>Essential Links</h2>    一些超链接。。。。。。  </div></template><script>export default {  name: 'HelloWorld',  data () {    return {      msg: 'Welcome to Your Vue.js App'    }  }}</script><!-- Add "scoped" attribute to limit CSS to this component only --><style scoped>h1, h2 {  font-weight: normal;}ul {  list-style-type: none;  padding: 0;}li {  display: inline-block;  margin: 0 10px;}a {  color: #42b983;}</style>
   ```

   ## Element-ui

   官网： https://element.eleme.cn/#/zh-CN/component/installation 

   安装

   ```sh
   # 直接npm安装，在项目中执行npm i element-ui -S# 或者引入样式
   ```

   使用： 

   在 main.js 中写入以下内容： 

   ```js
   import ElementUI  from 'element-ui'import 'element-ui/lib/theme-chalk/index.css';// 让vue使用ElementUI组件Vue.use(ElementUI);
   ```

   然后.vue文件中写标签 

   

   ### **插槽**（跳过）

   由于插槽是一块模板，所以，对于任何一个组件，从模板种类的角度来分，其实都可以分为**非插槽模板**和**插槽模板**两大类。

   - 非插槽模板指的是**html模板**，指的是‘div、span、ul、table’这些，非插槽模板的显示与隐藏以及怎样显示由插件自身控制；
   - 插槽模板是slot，它是一个空壳子，因为它显示与隐藏以及最后用什么样的**html模板**显示由父组件（子标签）控制。**但是插槽显示的位置确由子组件自身决定，slot写在组件template的哪块，父组件传过来的模板将来就显示在哪块**。

   下面插槽内容参考：https://segmentfault.com/a/1190000012996217

   之前我们怎么写：定义组件之后使用

   ```html
   <div id="app">    <child content='<p>哈哈哈</p>'></child></div><script>    Vue.component('child', {        props: [            'content'        ],        template: '<div ><p>hello</p><div v-html="this.content"></div></div>'    })    var vm = new Vue({        el: '#app'    })</script>
   ```

   上面代码叫做**通过属性传递**，缺点:会多出一个div，template 不好用，所以通过content传值，直接使用p标签是有问题的，必须包一个div。传递少还行，当传递很多的时候，代码会很难去阅读

   ```html
   <div id="app">    <child>        <!-- 1.用子组件时插入一点内容 -->        <h1>hahaha</h1>    </child></div><script>    Vue.component('child', {        // 2.用<slot></slot>可以显示  <p>hahaha</p>        // 所以使用插槽会更方便的向子组件插入dom元素        template: `<div>                <p>hello</p>                <slot></slot>                    </div>`     })         var vm = new Vue({        el: '#app'    })</script>
   ```

   slot有新的特性
   1.定义默认值，定义在slot标签上
   2.具名插槽

   ```html
   <div id="app">    <child>        <div class="footer" slot="footer">footer</div>    </child></div><script>    Vue.component('child', {        template: `<div>                    <slot name="header"></slot>                    <div class="content">content</div>                    <slot name="footer"></slot>                   </div>`    })    var vm = new Vue({        el: '#app'    })</script>
   ```

   

   #### 1）匿名插槽

   首先是单个插槽、**单个插槽**是vue的官方叫法，但是其实也可以叫它**默认插槽**，或者与具名插槽相对，我们可以叫它匿名插槽。因为它不用设置name属性。

   - 单个插槽可以放置在组件的任意位置，但是就像它的名字一样，一个组件中只能有一个该类插槽。
   - 相对应的，具名插槽就可以有很多个，只要名字（name属性）不同就可以了。

   下面通过一个例子来展示。

   <img src="https://segmentfault.com/img/remote/1460000012996222?w=782&amp;h=342" alt="img" style="zoom:50%;" />

   父组件：`</template>`标签表明是个模板

   ```vue
   <template>    <div class="father">        <h3>这里是父组件</h3>        <child>            <div class="tmpl">              <span>菜单1</span>              <span>菜单2</span>              <span>菜单3</span>              <span>菜单4</span>              <span>菜单5</span>              <span>菜单6</span>            </div>        </child>    </div></template><script>  import Child from './Child.vue'        export default {        data: function () {            return {                msg: ''            }        },        components:{          'child': Child        }    }</script><style scoped>  .father{    width:100%;    background-color: #ccc;    height: 650px;  }  .tmpl{    display: flex;    justify-content: space-around;    flex-direction: row;    width: 30%;    margin: 0 auto;  }  .tmpl span{    border:1px solid red;    height:50px;    line-height: 50px;    padding: 10px;  }</style>
   ```

   子组件：`<slot>`找一个默认的`</template>`

   ```vue
   <template>    <div class="child">        <h3>这里是子组件</h3>        <slot></slot>    </div></template><script>export default {    data: function(){        return {            msg: ''        }    },}</script><style scoped>  .child{    background-color: #00bbff;    width: 100%;    -webkit-box-sizing: border-box;    -moz-box-sizing: border-box;    box-sizing: border-box;  }</style>
   ```

   什么意思呢？子组件里原来是有点内容的，不能全部用模板替换，这部分内容填充到父组件的`<slot>`里

   


   ```
   注：所有demo都加了样式，以方便观察。其中，父组件以灰色背景填充，子组件都以浅蓝色填充。
   ```

   #### 2）具名插槽

   匿名插槽没有name属性，所以是匿名插槽，那么，插槽加了name属性，就变成了具名插槽。具名插槽可以在一个组件中出现N次。出现在不同的位置。下面的例子，就是一个有两个**具名插槽**和**单个插槽**的组件，这三个插槽被父组件用同一套css样式显示了出来，不同的是内容上略有区别。

   父组件：

   ```VUE
   <template>  <div class="father">    <h3>这里是父组件</h3>    <child>      <div class="tmpl" slot="up">        <span>菜单1</span>        <span>菜单2</span>        <span>菜单3</span>        <span>菜单4</span>        <span>菜单5</span>        <span>菜单6</span>      </div>      <div class="tmpl" slot="down">        <span>菜单-1</span>        <span>菜单-2</span>        <span>菜单-3</span>        <span>菜单-4</span>        <span>菜单-5</span>        <span>菜单-6</span>      </div>      <div class="tmpl">        <span>菜单->1</span>        <span>菜单->2</span>        <span>菜单->3</span>        <span>菜单->4</span>        <span>菜单->5</span>        <span>菜单->6</span>      </div>    </child>  </div></template><script>  import Child from './Child.vue'  export default {    data: function () {      return {        msg: ''      }    },    components:{      'child': Child    }  }</script><style scoped>  .father{    width:100%;    background-color: #ccc;    height: 650px;  }  .tmpl{    display: flex;    justify-content: space-around;    flex-direction: row;    width: 30%;    margin: 0 auto;  }  .tmpl span{    border:1px solid red;    height:50px;    line-height: 50px;    padding: 10px;  }</style>
   ```

   子组件：

   ```VUE
   <template>  <div class="child">    <slot name="up"></slot>    <h3>这里是子组件</h3>    <slot name="down"></slot>    <slot></slot>  </div></template><script>  export default {    data: function(){      return {        msg: ''      }    },    computed: {    },    methods:{    },    components: {    }  }</script><style scoped>  .child{    background-color: #00bbff;    width: 100%;    padding: 10px;    -webkit-box-sizing: border-box;    -moz-box-sizing: border-box;    box-sizing: border-box;  }</style>
   ```

   显示结果如图：
   ![img](https://segmentfault.com/img/remote/1460000012996223?w=742&h=456)

   可以看到，父组件通过html模板上的slot属性关联具名插槽。没有slot属性的html模板默认关联匿名插槽。

   #### 3）作用域插槽 | 带数据的插槽

   最后，就是我们的作用域插槽。这个稍微难理解一点。官方叫它作用域插槽，实际上，对比前面两种插槽，我们可以叫它带数据的插槽。什么意思呢，就是前面两种，都是在组件的template里面写

   ```HTML
   匿名插槽<slot></slot>具名插槽<slot name="up"></slot>
   ```

   但是**作用域插槽要求，在slot上面绑定数据**。也就是你得写成大概下面这个样子。

   ```vue
   <slot name="up" :data="data"></slot> export default {    data: function(){      return {        data: ['zhangsan','lisi','wanwu','zhaoliu','tianqi','xiaoba']      }    },}
   ```

   我们前面说了，插槽最后显示不显示是看父组件有没有在child下面写模板，像下面那样。

   ```HTML
   <child>   html模板</child>
   ```

   写了，插槽就总得在浏览器上显示点东西，东西就是html该有的模样，没写，插槽就是空壳子，啥都没有。
   OK，我们说有html模板的情况，就是父组件会往子组件插模板的情况，那到底插一套什么样的样式呢，这由父组件的html+css共同决定，但是这套样式里面的内容呢？

   正因为作用域插槽绑定了一套数据，父组件可以拿来用。于是，情况就变成了这样：样式父组件说了算，但内容可以显示子组件插槽绑定的。

   我们再来对比，作用域插槽和单个插槽和具名插槽的区别，因为单个插槽和具名插槽不绑定数据，所以父组件是提供的模板要既包括样式由包括内容的，上面的例子中，你看到的文字，“菜单1”，“菜单2”都是父组件自己提供的内容；而作用域插槽，父组件只需要提供一套样式（在确实用作用域插槽绑定的数据的前提下）。

   下面的例子，你就能看到，父组件提供了三种样式(分别是flex、ul、直接显示)，都没有提供数据，数据使用的都是子组件插槽自己绑定的那个人名数组。

   父组件：

   ```HTML
   <template>  <div class="child">    <h3>这里是子组件</h3>    <slot  :data="data"></slot>  </div></template><script>  export default {      data: function(){        return {          data: ['zhangsan','lisi','wanwu','zhaoliu','tianqi','xiaoba']        }      },    computed: {    },    methods:{    },    components: {    }  }</script><style scoped>  .child{    background-color: #00bbff;    width: 100%;    padding: 10px;    -webkit-box-sizing: border-box;    -moz-box-sizing: border-box;    box-sizing: border-box;  }</style>
   ```

   子组件：

   ```HTML
   <template>  <div class="child">    <h3>这里是子组件</h3>    <slot  :data="data"></slot>  </div></template><script>  export default {      data: function(){        return {          data: ['zhangsan','lisi','wanwu','zhaoliu','tianqi','xiaoba']        }      },    computed: {    },    methods:{    },    components: {    }  }</script><style scoped>  .child{    background-color: #00bbff;    width: 100%;    padding: 10px;    -webkit-box-sizing: border-box;    -moz-box-sizing: border-box;    box-sizing: border-box;  }</style>
   ```

   结果如图所示：

   ![img](/Users/r1ff/Desktop/学习笔记/img/1460000012996224?w=703&h=651.png)

   

   以上三个demo就放在https://github.com/cunzaizhuyi/vue-slot-demo了，有需要的可以去取。使用非常方便，是基于vue-cli搭建工程。

   # 商品服务-API

   ## 三级分类

   ### 配置网关路由与路径重写

   前端新增目录
   ![image-20200425164019287](/Users/r1ff/Desktop/学习笔记/img/image-20200425164019287.png)
   新增菜单
   ![image-20200425164509143](/Users/r1ff/Desktop/学习笔记/img/image-20200425164509143.png)

   在左侧点击【商品系统-分类维护】，希望在此展示3级分类。可以看到

   - url是`http://localhost:8001/#/product-category`
   - 填写的菜单路由是product/category
   - 对应的视图是src/view/modules/product/category.vue

   再如sys-role具体的视图在`renren-fast-vue/views/modules/sys/role.vue`

   所以要自定义我们的product/category视图的话，就是创建`mudules/product/category.vue`

   输入vue快捷生成模板，然后去https://element.eleme.cn/#/zh-CN/component/tree

   看如何使用多级目录

   - el-tree中的data是要展示的树形数据
   - props属性设置
   - @node-click单击函数

   ```js
   加入 tree组件<el-tree :data="data" :props="defaultProps" @node-click="handleNodeClick"></el-tree><script>  export default {    data() {      return {        data: [],        defaultProps: {          children: 'children',          label: 'label'        }      };    },  methods: {    //获取三级分类菜单数据    getMenus() {      this.$http({        url: this.$http.adornUrl("/product/category/list/tree"),        method: "get"      }).then(({ data }) => {        console.log("成功获取到菜单数据...", data.data);        this.menus = data.data;      });  },</script>
   ```


   发现请求路径错误
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/20210420074807647.png)
   修改基准地址
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/2021042007543789.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3JlYWxfcm9va2ll,size_16,color_FFFFFF,t_70)

   转发至网关 88 端口

   发现验证码错误，将后端管理模块renren-fast也加入注册中心

   url:lb（网关负载均衡路由到renren-fast）

   predicates：（网关断言判定是否为true）

   filters：（过滤器重写地址）

   网关路由到nocos找到服务端口

   ![在这里插入图片描述](https://img-blog.csdnimg.cn/20210420075643179.png)
   ![在这里插入图片描述](/Users/r1ff/Desktop/学习笔记/img/20210420075856771.png)

   **注：后面要调用的服务，都要配置和注册到nacos，定义路由**

   例：商品服务配置和注册到nacos

   配置商品服务的网关
   定义路由：粒度大的往后，粒度细的提前（把product-route放到fast前面）

   ![在这里插入图片描述](https://img-blog.csdnimg.cn/20210420082152418.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3JlYWxfcm9va2ll,size_16,color_FFFFFF,t_70)

   

   ### 网关统一配置跨域

   ![在这里插入图片描述](https://img-blog.csdnimg.cn/20210420080006953.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3JlYWxfcm9va2ll,size_16,color_FFFFFF,t_70)

   跨域流程：

   这个跨域请求的实现是通过预检请求实现的，先发送一个OPSTIONS探路，收到响应允许跨域后再发送真实请求

   > 什么意思呢？跨域是要请求的、新的端口那个服务器限制的，不是浏览器限制的。

   ```
   跨域请求流程：非简单请求(PUT、DELETE)等，需要先发送预检请求       -----1、预检请求、OPTIONS ------>       <----2、服务器响应允许跨域 ------浏览器 |                               |  服务器       -----3、正式发送真实请求 -------->       <----4、响应数据   --------------
   ```

   **跨域的解决方案**

   - 方法1：设置nginx包含admin和gateway。都先请求nginx，这样端口就统一了
   - 方法2：让服务器告诉预检请求能跨域

   **解决方案1：**

   <img src="https://fermhan.oss-cn-qingdao.aliyuncs.com/guli/image-20200425193523849.png" alt="image-20200425193523849" style="zoom:50%;" />

   **解决方案2:** 

   在响应头中添加：参考：https://blog.csdn.net/qq_38128179/article/details/84956552

   - Access-Control-Allow-Origin  ： 支持哪些来源的请求跨域
   - Access-Control-Allow-Method ： 支持那些方法跨域
   - Access-Control-Allow-Credentials ：跨域请求默认不包含cookie，设置为true可以包含cookie
   - Access-Control-Expose-Headers  ： 跨域请求暴露的字段
     - CORS请求时，XMLHttpRequest对象的getResponseHeader()方法只能拿到6个基本字段：
       Cache-Control、Content-Language、Content-Type、Expires、Last-Modified、Pragma
       如果想拿到其他字段，就必须在Access-Control-Expose-Headers里面指定。
   - Access-Control-Max-Age ：表明该响应的有效时间为多少秒。在有效时间内，浏览器无须为同一请求再次发起预检请求。请注意，浏览器自身维护了一个最大有效时间，如果该首部字段的值超过了最大有效时间，将失效

   

   **解决方法**：在网关中定义“`GulimallCorsConfiguration`”类，该类用来做过滤，允许所有的请求跨域。

   ```java
   package com.atguigu.gulimall.gateway.config;@Configuration // gatewaypublic class GulimallCorsConfiguration {    @Bean // 添加过滤器    public CorsWebFilter corsWebFilter(){        // 基于url跨域，选择reactive包下的        UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource();        // 跨域配置信息        CorsConfiguration corsConfiguration = new CorsConfiguration();        // 允许跨域的头        corsConfiguration.addAllowedHeader("*");        // 允许跨域的请求方式        corsConfiguration.addAllowedMethod("*");        // 允许跨域的请求来源        corsConfiguration.addAllowedOrigin("*");        // 是否允许携带cookie跨域        corsConfiguration.setAllowCredentials(true);               // 任意url都要进行跨域配置        source.registerCorsConfiguration("/**",corsConfiguration);        return new CorsWebFilter(source);    }}
   ```

   

   再次访问：http://localhost:8001/#/login

   ![image-20200425195437299](https://fermhan.oss-cn-qingdao.aliyuncs.com/guli/image-20200425195437299.png)

   http://localhost:8001/renren

   已拦截跨源请求：同源策略禁止读取位于 http://localhost:88/api/sys/login 的远程资源。

   （原因：不允许有多个 'Access-Control-Allow-Origin' CORS 头）

   renren-fast/captcha.jpg?uuid=69c79f02-d15b-478a-8465-a07fd09001e6

   出现了多个请求，并且也存在多个跨源请求。

   为了解决这个问题，需要修改renren-fast项目，注释掉“io.renren.config.CorsConfig”类。然后再次进行访问。

   ### 前端

   category.vue

   ```vue
   <template>  <div>    <!-- 引入Tree树形控件 -->    <el-tree      :data="menus"      :props="defaultProps"      show-checkbox      node-key="catId"      :expand-on-click-node="false"      :default-expanded-keys="expandedKey"    >      <span        class="custom-tree-node"        slot-scope="{ node, data }"      >        <span>{{ node.label }}</span>        <span>          <el-button            v-if="node.level <= 2"            type="text"            size="mini"            @click="() => append(data)"          >新增Append          </el-button>          <el-button            type="text"            size="mini"            @click="() => edit(data)"          >编辑Edit          </el-button>          <el-button            v-if="node.childNodes.length == 0"            type="text"            size="mini"            @click="() => remove(node, data)"          >删除Delete          </el-button>        </span>      </span>    </el-tree>    <!-- 弹出对话框 -->    <el-dialog      :title="title"      :visible.sync="dialogVisible"      width="30%"      :close-on-click-modal="false"    >      <!-- 对话框的表单 -->      <!-- model为表单的数据对象-->      <el-form :model="category">        <el-form-item label="分类名称">          <el-input            v-model="category.name"            autocomplete="off"          ></el-input>        </el-form-item>      </el-form>      <el-form :model="category">        <el-form-item label="图标">          <el-input            v-model="category.icon"            autocomplete="off"          ></el-input>        </el-form-item>      </el-form>      <el-form :model="category">        <el-form-item label="计量单位">          <el-input            v-model="category.productUnit"            autocomplete="off"          ></el-input>        </el-form-item>      </el-form>      <span        slot="footer"        class="dialog-footer"      >        <el-button @click="dialogVisible = false">取 消</el-button>        <el-button          type="primary"          @click="submitData"        >确 定</el-button>      </span>    </el-dialog>  </div></template><script>//这里可以导入其他文件（比如：组件，工具js，第三方插件js，json文件，图片文件等等）//例如：import 《组件名称》 from '《组件路径》'export default {  //import引入的组件需要注入到对象中才能使用  components: {},  props: {},  //这里放数据  data() {    return {      menus: [],//三级分类菜单数据      defaultProps: {//默认规则        children: 'children',//指定子树为节点对象的某个属性值（后端封装好实体中有children集合）        label: 'name'//指定节点标签为节点对象的某个属性值（集合中每一个实体都显示name属性）      },      expandedKey: [],//设置展开的菜单数据      dialogVisible: false,//默认对话框隐藏      category: {        name: "",        parentCid: 0,        catLevel: 0,        showStatus: 1,        sort: 0,        catId: null,        icon: "",        productUnit: ""      },//新增对话框绑定的对象，v-model双向绑定name这个属性,后面四个默认属性      title: "",//对话框的标题      dialogType: "",//对话框的类型，append/edit    };  },  //计算属性 类似于data概念  computed: {},  //监控data中的数据变化  watch: {},  //方法集合  methods: {    //这个方法的最终目的就是请求服务数据，响应数据给到vue实例menus，然后menus到了数据存放处，再通过template渲染    //查询    getMenus() {      this.$http({        url: this.$http.adornUrl("/product/category/list/tree"),//http://localhost:10000/renren-fast/product/category/list/tree        method: "get"      }).then(({ data }) => {// success 响应到数据后填充到绑定的标签中，上面获取到的对象有很多内容，包括data，data中还有data，这才是最终要显示的内容，所以把第一个data从对象中结构出来        console.log("成功获取到菜单数据...", data.data);        this.menus = data.data;//把数据给menus，就是给了vue实例，最后绑定到视图上      });    },    //删除    remove(node, data) {      //获取当前节点的id      var ids = [data.catId];      //删除弹窗确认      this.$confirm(`是否删除【${data.name}】菜单?`, '提示', {        confirmButtonText: '确定',        cancelButtonText: '取消',        type: 'warning'      }).then(() => {//点确定        this.$http({          url: this.$http.adornUrl('/product/category/delete'),          method: 'post',          //传递的数据是ids，不开启默认参数          data: this.$http.adornData(ids, false)        }).then(({ data }) => {//删除成功响应回来的数据          //删除成功$message          this.$message({            message: "菜单删除成功",            type: "success"          });          //刷出新菜单          this.getMenus();          //设置需要默认展开的菜单          this.expandedKey = [node.parent.data.catId];        })      }).catch(() => { })//点取消    },    //新增    addCategory() {      console.log("提交的三级分类数据", this.category)//打印提交内容的对象数据      //发送post请求      this.$http({        url: this.$http.adornUrl('/product/category/save'),        method: 'post',        data: this.$http.adornData(this.category, false)      }).then(({ data }) => {        //新增分类成功        this.$message({          message: "新增分类成功",          type: "success"        });        //关闭对话框        this.dialogVisible = false;        //刷出新菜单        this.getMenus();        //设置需要默认展开的菜单        this.expandedKey = [this.category.parentCid];      });    },    //编辑    editCategory() {      var { catId, name, icon, productUnit } = this.category;      this.$http({        url: this.$http.adornUrl('product/category/update'),        method: 'post',        data: this.$http.adornData({ catId, name, icon, productUnit }, false)      }).then(({ data }) => {        this.$message({          message: "编辑分类成功",          type: "success"        });        //关闭对话框        this.dialogVisible = false;        //刷出新菜单        this.getMenus();        //设置需要默认展开的菜单        this.expandedKey = [this.category.parentCid];      })    },    //弹出新增对话框    append(data) {      console.log("append", data)//打印当前分类的数据      this.dialogVisible = true;//调用该方法会打开对话框      this.dialogType = "append";//设置对话框类型      this.title = "新增";//设置对话框标题      this.category.parentCid = data.catId;//新增分类的父id      this.category.catLevel = data.catLevel * 1 + 1;//新增分类的层级      this.category.name = "";      this.category.catId = null;//自动递增的所以null      this.category.icon = "";      this.category.productUnit = "";      this.category.showStatus = 1;      this.category.sort = 0;    },    //弹出编辑对话框    edit(data) {      console.log("要修改的数据", data)      this.dialogVisible = true;//打开对话框      this.dialogType = "edit";//设置对话框类型      this.title = "编辑";//设置对话框标题      //发送请求获取回显最新的数据      this.$http({        url: this.$http.adornUrl(`/product/category/info/${data.catId}`),        method: 'get',      }).then(({ data }) => {        console.log("要回显的数据", data)        this.category.name = data.data.name;        this.category.catId = data.data.catId;        this.category.icon = data.data.icon;        this.category.productUnit = data.data.productUnit;        this.category.parentCid = data.data.parentCid;        this.category.catLevel = data.data.catLevel;        this.category.showStatus = data.data.showStatus;        this.category.sort = data.data.sort;      })    },    //提交对话框的数据    submitData() {      if (this.dialogType == "append") {//对话框类型是        //就调用新增        this.addCategory();      } else if (this.dialogType == "edit") {        //调用编辑        this.editCategory();      }    },  },  //声明周期 - 创建完成（可以访问当前this实例）  created() {    this.getMenus();  },  //声明周期 - 挂载完成（可以访问DOM元素）  mounted() { },  beforeCreate() { }, //生命周期 - 创建之前  beforeMount() { }, //生命周期 - 挂载之前  beforeUpdate() { }, //生命周期 - 更新之前  updated() { }, //生命周期 - 更新之后  beforeDestroy() { }, //生命周期 - 销毁之前  destroyed() { }, //生命周期 - 销毁完成  activated() { } //如果页面有keep-alive缓存功能，这个函数会触发};</script><style scoped></style>
   ```

   ### 后端

   #### 查询-后端递归树形结构数据获取

   product控制器下
   ![在这里插入图片描述](/Users/r1ff/Desktop/学习笔记/img/20210420071417926.png)

   ```java
   //服务实现类    @Override    public List<CategoryEntity> listWithTree() {        //1、查出所有分类,这个实现类已经继承类服务层的泛型dao，直接调用用baseMapper就好        List<CategoryEntity> entities = baseMapper.selectList(null);        //2、组装成父子的树形结构        //2.1）、找到所有的一级分类        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->//所有分类用stream的过滤方法，根据条件找到一级分类                categoryEntity.getParentCid() == 0//一级分类的条件        ).map(menu -> {//接着用这个一级分类            menu.setChildren(getChildrens(menu, entities));//菜单的实体类，添加一个private List<CategoryEntity> children;用来装23级分类，获取用到递归方法            return menu;        }).sorted((menu1, menu2) -> {//分类排序            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());        }).collect(Collectors.toList());//收集        return level1Menus;    }    //递归查找所有菜单的子菜单    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {//传递过来一级分类和所有分类        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {//找到二级分类            return categoryEntity.getParentCid() == root.getCatId();//条件        }).map(categoryEntity -> {//拿着二级分类            //1、找到子菜单            categoryEntity.setChildren(getChildrens(categoryEntity, all));//递归找三级分类            return categoryEntity;        }).sorted((menu1, menu2) -> {//排序            //2、菜单的排序            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());        }).collect(Collectors.toList());//收集好        return children;    }
   ```

   #### 删除-逻辑删除

   多数时候，我们并不希望删除数据，而是标记它被删除了，这就是逻辑删除；

   >说明:
   >
   >只对自动注入的sql起效:
   >
   >- 插入: 不作限制
   >- 查找: 追加where条件过滤掉已删除数据,且使用 wrapper.entity 生成的where条件会忽略该字段
   >- 更新: 追加where条件防止更新到已删除数据,且使用 wrapper.entity 生成的where条件会忽略该字段
   >- 删除: 转变为 更新
   >
   >例如:
   >
   >- 删除: `update user set deleted=1 where id = 1 and deleted=0`
   >- 查找: `select id,name,deleted from user where deleted=0`
   >
   >字段类型支持说明:
   >
   >- 支持所有数据类型(推荐使用 `Integer`,`Boolean`,`LocalDateTime`)
   >- 如果数据库字段使用`datetime`,逻辑未删除值和已删除值支持配置为字符串`null`,另一个值支持配置为函数来获取值如`now()`
   >
   >附录:
   >
   >- 逻辑删除是为了方便数据恢复和保护数据本身价值等等的一种方案，但实际就是删除。
   >- 如果你需要频繁查出来看就不应使用逻辑删除，而是以一个状态去表示。

   使用方法：

   步骤1: 配置`com.baomidou.mybatisplus.core.config.GlobalConfig$DbConfig`

   - 例: application.yml

   ```yaml
   mybatis-plus:  global-config:    db-config:      logic-delete-field: flag  # 全局逻辑删除的实体字段名(since 3.3.0,配置后可以忽略不配置步骤2)      logic-delete-value: 1 # 逻辑已删除值(默认为 1)      logic-not-delete-value: 0 # 逻辑未删除值(默认为 0)
   ```

   步骤2: 实体类字段上加上`@TableLogic`注解

   ```java
   @TableLogicprivate Integer deleted;
   ```

   #### 新增

   逆向生成

   #### 编辑-基本

   逆向生成

   ##### 拖拽（理解不了，先跳过）

   ## 品牌管理

   ### 使用逆向工程的前后端代码（跳）

   ### 效果优化与快速显示开关（跳）

   ### 云存储开通与使用

   开通阿里云OSS对象存储

   创建Bucket（桶）

   三种上传方式：

   - 手动（很弱智）
   - 服务端代码上传（每次从服务器经过，浪费资源）
   - **客户端获得服务端签名后直传（包装了AccessKeyID和AcessKeySecret等重要信息，防止直接暴露）最佳方式**

   ### OSS获取服务端签名

   原理如下：

   1. 用户发送上传**Policy请求**到应用服务器。
   2. 应用服务器返回上传**Policy**和签名给用户。
   3. 用户直接上传数据到OSS。

   <img src="http://static-aliyun-doc.oss-cn-hangzhou.aliyuncs.com/assets/img/zh-CN/3156348951/p139016.png" alt="时序图" style="zoom:50%;" />

   java用法：https://help.aliyun.com/document_detail/91868.html?spm=a2c4g.11186623.2.10.97e17d9cfwODvA

   编写“com.atguigu.gulimall.thirdparty.controller.`OssController`”类：

   ```java
   package com.atguigu.gulimall.thirdparty.controller;import com.aliyun.oss.OSS;import com.aliyun.oss.common.utils.BinaryUtil;import com.aliyun.oss.model.MatchMode;import com.aliyun.oss.model.PolicyConditions;import com.atguigu.common.utils.R;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.beans.factory.annotation.Value;import org.springframework.web.bind.annotation.RequestMapping;import org.springframework.web.bind.annotation.RestController;import java.text.SimpleDateFormat;import java.util.Date;import java.util.LinkedHashMap;import java.util.Map;@RestControllerpublic class OssController {  	//就不用创建OSSClient实例了    @Autowired    OSS ossClient;      //从application.yml中获取信息    @Value("${spring.cloud.alicloud.oss.endpoint}")    private String endpoint;    @Value("${spring.cloud.alicloud.oss.bucket}")    private String bucket;    @Value("${spring.cloud.alicloud.access-key}")    private String accessId;    @RequestMapping("/oss/policy")    public R policy(){        //https://gulimall-xixihaha.oss-cn-hangzhou.aliyuncs.com/%E8%AF%81.JPG        String host = "https://" + bucket + "." + endpoint; // host的格式为 bucketname.endpoint        // callbackUrl为上传回调服务器的URL，请将下面的IP和Port配置为您自己的真实信息。//        String callbackUrl = "http://88.88.88.88:8888";        String format = new SimpleDateFormat("yyyy-MM-dd").format(new Date());        String dir = format+"/"; // 用户上传文件时指定的前缀。        Map<String,String> respMap = null;        try {            long expireTime = 30;            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;            Date expiration = new Date(expireEndTime);            // PostObject请求最大可支持的文件大小为5 GB，即CONTENT_LENGTH_RANGE为5*1024*1024*1024。            PolicyConditions policyConds = new PolicyConditions();            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);            byte[] binaryData = postPolicy.getBytes("utf-8");            String encodedPolicy = BinaryUtil.toBase64String(binaryData);            String postSignature = ossClient.calculatePostSignature(postPolicy);            respMap = new LinkedHashMap<String, String>();            respMap.put("accessid", accessId);            respMap.put("policy", encodedPolicy);            respMap.put("signature", postSignature);            respMap.put("dir", dir);            respMap.put("host", host);            respMap.put("expire", String.valueOf(expireEndTime / 1000));            // respMap.put("expire", formatISO8601Date(expiration));        } catch (Exception e) {            // Assert.fail(e.getMessage());            System.out.println(e.getMessage());        } finally {            ossClient.shutdown();        }        return R.ok().put("data",respMap);    }}
   ```

   上面的意思是说用户通过url请求得到一个policy，要拿这个东西直接传到阿里云，不要去服务器了

   测试： http://localhost:30000/oss/policy   返回签名

   ```json
   {    "accessid":"LTAI4G3ewgWMxsrnaaeDuT1B",    "policy":"eyJleHBpcmF0aW9uIjoiMjAyMS0wMi0xNFQxMDoyOToxMS43ODhaIiwiY29uZGl0aW9ucyI6W1siY29udGVudC1sZW5ndGgtcmFuZ2UiLDAsMTA0ODU3NjAwMF0sWyJzdGFydHMtd2l0aCIsIiRrZXkiLCIyMDIxLTAyLTE0Il1dfQ==",    "signature":"0OXDXrQ1vRNl61N5IaZXRFckCKM=",    "dir":"2021-02-14",    "host":"https://gulimall-fermhan.oss-cn-qingdao.aliyuncs.com",    "expire":"1613298551"}
   ```

   在该微服务中测试通过，但是我们不能对外暴露端口或者说为了统一管理，我们还是让用户请求网关然后转发过来

   以后在上传文件时的访问路径为“ http://localhost:88/api/thirdparty/oss/policy”，通过网关转发

   在“gulimall-gateway”中配置路由规则：

   ```yaml
           - id: third_party_route          uri: lb://gulimall-third-party          predicates:            - Path=/api/thirdparty/**          filters:            - RewritePath=/api/thirdparty/(?<segment>.*),/$\{segment}
   ```

   测试是否能够正常跳转： http://localhost:88/api/thirdparty/oss/policy 

   ### OSS前后联调测试上传

   

   ### 表单校验&自定义校验器

   前端的校验是element-ui表单验证https://element.eleme.cn/#/zh-CN/component/form

   - Form 组件提供了表单验证的功能，只需要通过 `rules` 属性传入约定的验证规则，并将 Form-Item 的 `prop` 属性设置为需校验的字段名即可。校验规则参见 [async-validator](https://github.com/yiminghe/async-validator)

   - 使用自定义校验规则可以解决字母限制的问题

     ```js
     var validatePass2 = (rule, value, callback) => {    if (value === '') {        callback(new Error('请再次输入密码'));    } else if (value !== this.ruleForm.pass) {        callback(new Error('两次输入密码不一致!'));    } else {        callback();    }};return {    rules: {        checkPass: [            { validator: validatePass2, trigger: 'blur' }        ],
     ```

   ### JSR303数据校验

   #### 步骤1.给需要校验的实体字段加上注解

   在Java中提供了一系列的校验方式，它这些校验方式在“`javax.validation.constraints`”包中，提供了如@Email，@NotNull等注解。

   在非空处理方式上提供了@NotNull，@NotBlank和@NotEmpty

   （1）@NotNull 该属性不能为null

   （2）@NotEmpty 该字段不能为null或`""`

   支持以下几种类型

   - CharSequence (length of character sequence is evaluated)字符序列（字符序列长度的计算）
   - Collection (collection size is evaluated) 集合长度的计算
   - Map (map size is evaluated)  map长度的计算
   - Array (array length is evaluated)  数组长度的计算

   （3）@NotBlank：不能为空，不能仅为一个空格

   #### 步骤2.controller中加校验注解@Valid，开启校验

   ```java
   @RequestMapping("/save")public R save(@Valid @RequestBody BrandEntity brand){    brandService.save(brand);    return R.ok();}
   ```

   测试： http://localhost:88/api/product/brand/save 

   在postman种发送上面的请求，可以看到返回的甚至不是R对象

   ```json
   {    "timestamp": "2020-04-29T09:20:46.383+0000",    "status": 400,    "error": "Bad Request",    "errors": [        {            "codes": [                "NotBlank.brandEntity.name",                "NotBlank.name",                "NotBlank.java.lang.String",                "NotBlank"            ],            "arguments": [                {                    "codes": [                        "brandEntity.name",                        "name"                    ],                    "arguments": null,                    "defaultMessage": "name",                    "code": "name"                }            ],            "defaultMessage": "不能为空",            "objectName": "brandEntity",            "field": "name",            "rejectedValue": "",            "bindingFailure": false,            "code": "NotBlank"        }    ],    "message": "Validation failed for object='brandEntity'. Error count: 1",    "path": "/product/brand/save"}
   ```

   能够看到"defaultMessage": "不能为空"，这些错误消息定义在“hibernate-validator”的“\org\hibernate\validator\ValidationMessages_zh_CN.properties”文件中。在该文件中定义了很多的错误规则：

   ```properties
   javax.validation.constraints.AssertFalse.message     = 只能为falsejavax.validation.constraints.AssertTrue.message      = 只能为truejavax.validation.constraints.DecimalMax.message      = 必须小于或等于{value}javax.validation.constraints.DecimalMin.message      = 必须大于或等于{value}javax.validation.constraints.Digits.message          = 数字的值超出了允许范围(只允许在{integer}位整数和{fraction}位小数范围内)javax.validation.constraints.Email.message           = 不是一个合法的电子邮件地址javax.validation.constraints.Future.message          = 需要是一个将来的时间javax.validation.constraints.FutureOrPresent.message = 需要是一个将来或现在的时间javax.validation.constraints.Max.message             = 最大不能超过{value}javax.validation.constraints.Min.message             = 最小不能小于{value}javax.validation.constraints.Negative.message        = 必须是负数javax.validation.constraints.NegativeOrZero.message  = 必须是负数或零javax.validation.constraints.NotBlank.message        = 不能为空javax.validation.constraints.NotEmpty.message        = 不能为空javax.validation.constraints.NotNull.message         = 不能为nulljavax.validation.constraints.Null.message            = 必须为nulljavax.validation.constraints.Past.message            = 需要是一个过去的时间javax.validation.constraints.PastOrPresent.message   = 需要是一个过去或现在的时间javax.validation.constraints.Pattern.message         = 需要匹配正则表达式"{regexp}"javax.validation.constraints.Positive.message        = 必须是正数javax.validation.constraints.PositiveOrZero.message  = 必须是正数或零javax.validation.constraints.Size.message            = 个数必须在{min}和{max}之间org.hibernate.validator.constraints.CreditCardNumber.message        = 不合法的信用卡号码org.hibernate.validator.constraints.Currency.message                = 不合法的货币 (必须是{value}其中之一)org.hibernate.validator.constraints.EAN.message                     = 不合法的{type}条形码org.hibernate.validator.constraints.Email.message                   = 不是一个合法的电子邮件地址org.hibernate.validator.constraints.Length.message                  = 长度需要在{min}和{max}之间org.hibernate.validator.constraints.CodePointLength.message         = 长度需要在{min}和{max}之间org.hibernate.validator.constraints.LuhnCheck.message               = ${validatedValue}的校验码不合法, Luhn模10校验和不匹配org.hibernate.validator.constraints.Mod10Check.message              = ${validatedValue}的校验码不合法, 模10校验和不匹配org.hibernate.validator.constraints.Mod11Check.message              = ${validatedValue}的校验码不合法, 模11校验和不匹配org.hibernate.validator.constraints.ModCheck.message                = ${validatedValue}的校验码不合法, ${modType}校验和不匹配org.hibernate.validator.constraints.NotBlank.message                = 不能为空org.hibernate.validator.constraints.NotEmpty.message                = 不能为空org.hibernate.validator.constraints.ParametersScriptAssert.message  = 执行脚本表达式"{script}"没有返回期望结果org.hibernate.validator.constraints.Range.message                   = 需要在{min}和{max}之间org.hibernate.validator.constraints.SafeHtml.message                = 可能有不安全的HTML内容org.hibernate.validator.constraints.ScriptAssert.message            = 执行脚本表达式"{script}"没有返回期望结果org.hibernate.validator.constraints.URL.message                     = 需要是一个合法的URLorg.hibernate.validator.constraints.time.DurationMax.message        = 必须小于${inclusive == true ? '或等于' : ''}${days == 0 ? '' : days += '天'}${hours == 0 ? '' : hours += '小时'}${minutes == 0 ? '' : minutes += '分钟'}${seconds == 0 ? '' : seconds += '秒'}${millis == 0 ? '' : millis += '毫秒'}${nanos == 0 ? '' : nanos += '纳秒'}org.hibernate.validator.constraints.time.DurationMin.message        = 必须大于${inclusive == true ? '或等于' : ''}${days == 0 ? '' : days += '天'}${hours == 0 ? '' : hours += '小时'}${minutes == 0 ? '' : minutes += '分钟'}${seconds == 0 ? '' : seconds += '秒'}${millis == 0 ? '' : millis += '毫秒'}${nanos == 0 ? '' : nanos += '纳秒'}
   ```

   想要自定义错误消息，可以覆盖默认的错误提示信息，如@NotBlank的默认message是

   ```java
   public @interface NotBlank {	String message() default "{javax.validation.constraints.NotBlank.message}";
   ```

   可以在添加注解的时候，修改message：

   ```java
   	@NotBlank(message = "品牌名必须非空")	private String name;
   ```

   当再次发送请求时，得到的错误提示信息：

   ```json
   {    "timestamp": "2020-04-29T09:36:04.125+0000",    "status": 400,    "error": "Bad Request",    "errors": [        {            "codes": [                "NotBlank.brandEntity.name",                "NotBlank.name",                "NotBlank.java.lang.String",                "NotBlank"            ],            "arguments": [                {                    "codes": [                        "brandEntity.name",                        "name"                    ],                    "arguments": null,                    "defaultMessage": "name",                    "code": "name"                }            ],            "defaultMessage": "品牌名必须非空",            "objectName": "brandEntity",            "field": "name",            "rejectedValue": "",            "bindingFailure": false,            "code": "NotBlank"        }    ],    "message": "Validation failed for object='brandEntity'. Error count: 1",    "path": "/product/brand/save"}
   ```

   **但是返回的错误不是R对象，影响接收端的接收，我们可以通过局部异常处理或者统一一次处理解决**

   #### 步骤3：局部异常处理BindResult

   给校验的Bean后，紧跟一个BindResult，就可以获取到校验的结果。拿到校验的结果，就可以自定义的封装。

   ```java
   @RequestMapping("/save")public R save(@Valid @RequestBody BrandEntity brand,              BindingResult result){ // 手动处理异常    if( result.hasErrors()){        Map<String,String> map=new HashMap<>();        //1.获取错误的校验结果        result.getFieldErrors().forEach((item)->{            //获取发生错误时的message            String message = item.getDefaultMessage();            //获取发生错误的字段            String field = item.getField();            map.put(field,message);        });        return R.error(400,"提交的数据不合法").put("data",map);    }else {    }    brandService.save(brand);    return R.ok();}
   ```

   这种是针对于该请求设置了一个内容校验，如果针对于每个请求都单独进行配置，显然不是太合适，实际上可以**统一异常处理**

   ### 统一异常处理@ExceptionHandler

   这也是 Spring 3.2 带来的新特性。从名字上可以看出大体意思是控制器增强。 也就是说，@controlleradvice + @ ExceptionHandler 也可以实现全局的异常捕捉。

   #### （1）抽取一个异常处理类

   - `@ControllerAdvice`标注在类上，通过“basePackages”能够说明处理哪些路径下的异常。
   - `@ExceptionHandler(value = 异常类型.class) `标注在方法上

   ```java
   package com.atguigu.gulimall.product.exception;@Slf4j@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller")//管理的controllerpublic class GulimallExceptionControllerAdvice {    @ExceptionHandler(value = Exception.class) // 也可以返回ModelAndView    public R handleValidException(MethodArgumentNotValidException exception){        Map<String,String> map=new HashMap<>();        // 获取数据校验的错误结果        BindingResult bindingResult = exception.getBindingResult();        // 处理错误        bindingResult.getFieldErrors().forEach(fieldError -> {            String message = fieldError.getDefaultMessage();            String field = fieldError.getField();            map.put(field,message);        });        log.error("数据校验出现问题{},异常类型{}",exception.getMessage(),exception.getClass());        return R.error(400,"数据校验出现问题").put("data",map);    }}
   ```

   测试: 	 http://localhost:88/api/product/brand/save 

   <img src="https://fermhan.oss-cn-qingdao.aliyuncs.com/guli/image-20200429183334783.png" alt="image-20200429183334783" style="zoom:50%;" />

   

   #### （2）未知异常处理

   ```java
   @ExceptionHandler(value = Throwable.class)//异常的范围更大public R handleException(Throwable throwable){    log.error("未知异常{},异常类型{}",              throwable.getMessage(),              throwable.getClass());    return R.error(BizCodeEnum.UNKNOW_EXEPTION.getCode(),                   BizCodeEnum.UNKNOW_EXEPTION.getMsg());}
   ```

   #### （3）错误状态码

   上面代码中，针对于错误状态码，是我们进行随意定义的，然而正规开发过程中，错误状态码有着严格的定义规则，如该在项目中我们的错误状态码定义

   上面的用法主要是通过`@Controller+@ExceptionHandler`来进行异常拦截处理

   BizCodeEnum

   为了定义这些错误状态码，我们可以单独定义一个常量类，用来存储这些错误状态码

   ```java
   package com.atguigu.common.exception;/*** * 错误码和错误信息定义类 * 1. 错误码定义规则为5为数字 * 2. 前两位表示业务场景，最后三位表示错误码。例如：100001。10:通用 001:系统未知异常 * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式 * 错误码列表： *  10: 通用 *      001：参数格式校验 *  11: 商品 *  12: 订单 *  13: 购物车 *  14: 物流 */public enum BizCodeEnum {    UNKNOW_EXEPTION(10000,"系统未知异常"),    VALID_EXCEPTION( 10001,"参数格式校验失败");    private int code;    private String msg;    BizCodeEnum(int code, String msg) {        this.code = code;        this.msg = msg;    }    public int getCode() {        return code;    }    public String getMsg() {        return msg;    }}
   ```

   测试： http://localhost:88/api/product/brand/save 

   <img src="https://fermhan.oss-cn-qingdao.aliyuncs.com/guli/image-20200429191830967.png" alt="image-20200429191830967" style="zoom:67%;" />

   可以参考下：https://blog.csdn.net/github_36086968/article/details/103115128

   ### JSR303分组校验

   需求：是对同一实体类参数区分不同情况下的校验

   1.公共类创建AddGroup和UpdateGroup接口，就是拿来做分组标识用，内容为空，

   2.给参数注解加上(groups={AddGroup.class})，指定什么情况下才需要进行校验

   如：新增时不需要带id，修改时必须带id

   ```java
   @NotNull(message = "修改必须定制品牌id", groups = {UpdateGroup.class})@Null(message = "新增不能指定id", groups = {AddGroup.class})@TableIdprivate Long brandId;
   ```

   3.controller的方法业务方法参数@Valid改成@Validated({AddGroup.class})

   @Validated的value值指定要使用的一个或多个分组 

   ```java
   // 新增场景添加 新增分组注解@RequestMapping("/save")  public R save(@Validated(AddGroup.class) @RequestBody BrandEntity brand) {    brandService.save(brand);    return R.ok();}
   ```

   **总结**：1.controller接收到之后，根据@Validated表明的分组信息，品牌对应的校验注解。

   ​			2.@Valid和@Validated择其一

   ### JSR303自定义校验注解

   需求：要校验`showStatus`的0/1状态，内置的注解不行，要用自定义校验注解

   ```java
   /**	 * 显示状态[0-不显示；1-显示]	 */@NotNull(groups = {AddGroup.class, UpdateStatusGroup.class})@ListValue(vals = {0,1}, groups = {AddGroup.class, UpdateGroup.class, UpdateStatusGroup.class})private Integer showStatus;
   ```

   添加依赖

   ```xml
   <!--校验--><dependency>    <groupId>javax.validation</groupId>    <artifactId>validation-api</artifactId>    <version>2.1.0.Final</version></dependency><dependency>    <groupId>org.hibernate</groupId>    <artifactId>hibernate-validator</artifactId>    <version>5.4.1.Final</version></dependency><!--高版本需要javax.el--><dependency>    <groupId>org.glassfish</groupId>    <artifactId>javax.el</artifactId>    <version>3.0.1-b08</version></dependency>
   ```

   #### 1、自定义校验注解

   必须有3个属性

   - message()错误信息
   - groups()分组校验
   - payload()自定义负载信息

   ```java
   // 自定义注解@Documented@Constraint(validatedBy = { ListValueConstraintValidator.class}) // 关联校验器和校验注解@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE }) // 哪都可以标注@Retention(RUNTIME)public @interface ListValue {    // 使用该属性去Validation.properties中取    String message() default "{com.atguigu.common.valid.ListValue.message}";    Class<?>[] groups() default { };    Class<? extends Payload>[] payload() default { };    // 数组，需要用户自己指定    int[] value() default {};}
   ```

   因为上面的message值对应的最终字符串需要去ValidationMessages.properties中获得，所以我们在common中新建文件`ValidationMessages.properties`

   文件内容

   ```properties
   com.atguigu.common.valid.ListValue.message=必须提交指定的值 [0,1]
   ```

   #### 2、自定义校验器ConstraintValidator

   ```java
   package com.atguigu.common.valid;//第一个泛型参数是所对应的校验注解类型，第二个是校验对象类型public class ListValueConstraintValidator implements ConstraintValidator<ListValue,Integer> {    // 用来存储指定的值    private Set<Integer> set = new HashSet<>();    //初始化方法    @Override// 通过重写initialize()方法，获得注解上的内容，放在集合里，完成指定实体参数需要指定值    public void initialize(ListValue constraintAnnotation) {        int[] vals = constraintAnnotation.vals();//写的注解是@ListValue(value={0,1})        for (int val : vals) {//遍历放入集合            set.add(val);        }    }    /**     *     * @param value 需要校验的值     * @param context     * @return     *真正的验证逻辑由`isValid`完成，如果传入形参的属性值在这个set里就返回true，否则返回false     */    @Override    public boolean isValid(Integer value, ConstraintValidatorContext context) {        return set.contains(value);    }}
   ```

   #### 3、关联校验器和校验注解

   ```java
   @Constraint(validatedBy = { ListValueConstraintValidator.class})
   ```

   一个校验注解可以匹配多个校验器

   ## 概念-SPU&SKU&规格参数&销售属性

   重新执行“sys_menus.sql”

   - SPU：standard product unit(**标准化产品单元**)：是商品信息聚合的最小单位，是一组可复用、易检索的**标准化信息的集合**，该集合描述了一个产品的特性。
     - 如iphoneX是SPU
   - SKU：stock keeping unit(**库存量单位**)：库存进出计量的基本单元，可以是件/盒/托盘等单位。SKU是对于大型连锁超市DC配送中心物流管理的一个必要的方法。现在已经被引申为**产品统一编号**的简称，每种产品对应有唯一的SKU号。
     - 如iphoneX 64G 黑色 是SKU 

   

   - 基础属性：同一个SPU拥有的特性叫**基本属性**。如机身长度，这个是手机共用的属性。而每款手机的属性值不同
     - 也可以叫规格参数
   - 销售属性：能决定库存量的叫**销售属性**。如颜色

   

   3、基本属性〖规格参数〗与销售属性
   每个分类下的商品共享规格参数，与销售属性。只是有些商品不一定要用这个分类下全部的属性；


   - 属性是以三级分类组织起来的
   - 规格参数中有些是可以提供检索的
   - **规格参数**也是**基本属性**，他们具有自己的分组
   - 属性的分组也是以三级分类组织起来的
   - 属性名确定的，但是值是每一个商品不同来决定的

   #### ==pms数据库表==

   pms数据库下的attr属性表，attr-group表

   - attr-group-id：几号分组
   - catelog-id：什么类别下的，比如手机

   根据商品找到spu-id，attr-id

   属性关系-规格参数-销售属性-三级分类     关联关系

   > 每个分类有特点的属性

   先通过分类找打对应的数学分组，然后根据属性分组查到拥有的数学

   ![](https://fermhan.oss-cn-qingdao.aliyuncs.com/img/20210215122413.png)

   SPU-SKU属性表

   ![](https://fermhan.oss-cn-qingdao.aliyuncs.com/img/20210215122619.png)

   荣耀V20有两个属性，网络和像素，但是这两个属性的spu是同一个，代表是同款手机。

   sku表里保存spu是同一手机，sku可能相同可能不同，相同代表是同一款，不同代表是不同款。

   ![](https://fermhan.oss-cn-qingdao.aliyuncs.com/img/20210217111305.png)

   属性表说明每个属性的 枚举值

   

   分类表有所有的分类，但有父子关系

   ## 属性分组

   ### 前端组件抽取&父子组件交互

   导入menus.sql，补全左侧菜单，复制modules中的代码

   需求：现在想要实现点击商品系统/平台属性/属性分组，能够实现在右边展示数据

   接口文档地址: https://easydoc.xyz/s/78237135

   #### 前端组件抽取

   ![image-20200430215649355](https://fermhan.oss-cn-qingdao.aliyuncs.com/guli/image-20200430215649355.png)

   

   根据他的请求地址http://localhost:8001/#/product-attrgroup

   所以应该有product/attrgroup.vue，再抽取product/category.vue，到common/category.vue，导入到attrgroup.vue（也就是左侧内容的tree单独成一个vue组件）

   1）左侧tree：

   **attrgroup.vue：**要在左面显示三级分类，右面显示spu属性。

   ```vue
   <el-row :gutter="20">    <el-col :span="6"> <div class="grid-content bg-purple"></div></el-col>    <el-col :span="18"><div class="grid-content bg-purple"></div></el-col></el-row>
   ```

   20表示列间距，分为2个模块，分别占6列和18列（分别是tree和当前spu等信息）

   **common/category.vue：**生成vue模板。再把要用的内容抽取出来

   在attrgroup.vue中导入使用

   ```vue
   <script>import Category from "../common/category";export default {  //import引入的组件需要注入到对象中才能使用。组件名:自定义的名字，一致可以省略  components: { Category},
   ```

   导入了之后，就可以在`attrgroup.vue`中找合适位置放好

   ```vue
   <template><el-row :gutter="20">    <el-col :span="6">        <category @tree-node-click="treenodeclick"></category>    </el-col>
   ```

   2）右侧spu属性

   复制modules中

   #### 父子组件交互

   要实现功能：点击左侧，右侧表格对应内容显示。

   父子组件传递数据：category.vue点击时，引用它的attgroup.vue能感知到

   1）子组件（category）给父组件（attrgroup）传递数据，事件机制；

   在category中绑定node-click事件

   ```vue
   <el-tree :data="menus" :props="defaultProps" node-key="catId" ref="menuTree"          @node-click="nodeClick"	></el-tree>
   ```

   2）子组件给父组件发送一个事件，携带上数据；

   ```javascript
   nodeClick(data,Node,component){    console.log("子组件被点击",data,Node,component);    this.$emit("tree-node-click",data,Node,component);},     第一个参数事件名字随便写，    后面可以写任意多的东西，事件发生时都会传出去
   ```

   3）父组件中的获取发送的事件

   ```vue
   在attr-group中写<category @tree-node-click="treeNodeClick"></category>表明他的子组件可能会传递过来点击事件，用自定义的函数接收传递过来的参数
   ```

   ```javascript
    父组件中进行处理//获取发送的事件数据    treeNodeClick(data,Node,component){     console.log("attgroup感知到的category的节点被点击",data,Node,component);     console.log("刚才被点击的菜单ID",data.catId);    },
   ```

   ### 获取分类属性分组

   需求：点击分类，右边会展示内容，一级和二级就查询所有，三级查询对应的，然后搜索框内可以搜属性分组id和name

   请求： /product/attrgroup/list/{catelogId}

   按照这个url，去product项目下的`attrgroup-controller`里修改

   ```java
   /**     * 列表     * @param  catelogId 0的话查所有     */@RequestMapping("/list/{catelogId}")public R list(@RequestParam Map<String, Object> params,              @PathVariable Long catelogId){    //        PageUtils page = attrGroupService.queryPage(params);    PageUtils page = attrGroupService.queryPage(params,catelogId);    return R.ok().put("page", page);}
   ```

   

   增加接口与实现

   - AttrGroupServiceImpl extends ServiceImpl，其中ServiceImpl的父类中有方法page(IPage, Wrapper<T>)。对于wrapper而言，没有条件的话就是查询所有
   - queryPage()返回前还会return new PageUtils(page);，把page对象解析好页码信息，就封装为了响应数据

   ```JAVA
   public class AttrGroupServiceImpl     extends ServiceImpl<AttrGroupDao, AttrGroupEntity>    implements AttrGroupService {    @Override    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {        if (catelogId == 0) {//三级分类id等于0就查询所有            //Query封装分页信息，QueryWrapper封装查询条件，IPage为mybatis-plus对象            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),                    new QueryWrapper<AttrGroupEntity>());            return new PageUtils(page);//这些信息放到工具类，返回属性列表，和分页信息        } else {//三级分类不等于0的情况            //前端有个key            String key = (String) params.get("key");            //key是点完了三级分类，然后在搜索框里再输入搜的，所以要模糊查询，先查三级分类，再去查其他两个字段            //select * from pms_attr_group where catelog_id =? and (attr_group_id=key or attr_group_name=key)            //用代码表达这个sql语句            //先查三级分类信息            QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId);            //然后在模糊查询            if (!StringUtils.isEmpty(key)) {//如果包含key                //就接着查                wrapper.and((obj) -> {                    obj.eq("attr_group_id", key).or().like("attr_group_name", key);                });//现在wrapper就是一个完整的查询信息            }            //最后封装到page返回            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);            return new PageUtils(page);        }    }
   ```

   测试

   查1分类的属性分组：localhost:88/api/product/attrgroup/list/1

   查1分类的属性分组并且分页为1、关键字为aa：localhost:88/api/product/attrgroup/list/1?page=1&key=aa。

   ```json
   {    "msg": "success",    "code": 0,    "page": {        "totalCount": 0,        "pageSize": 10,        "totalPage": 0,        "currPage": 1,        "list": []    }}
   ```

   属性列表为空，因为只有三级分类id=3时才能查到东西

   调整前端

   发送请求时url携带id信息，${this.catId}，get请求携带page信息

   点击第3级分类时才查，修改attr-group.vue中的函数即可

   ```js
   //感知树节点被点击treenodeclick(data, node, component) {    if (node.level == 3) {        this.catId = data.catId;        this.getDataList(); //重新查询    }},    // 获取数据列表getDataList() {    this.dataListLoading = true;    this.$http({        url: this.$http.adornUrl(`/product/attrgroup/list/${this.catId}`),        method: "get",        params: this.$http.adornParams({            page: this.pageIndex,            limit: this.pageSize,            key: this.dataForm.key        })    }).then(({ data }) => {        if (data && data.code === 0) {            this.dataList = data.page.list;            this.totalPage = data.page.totalCount;        } else {            this.dataList = [];            this.totalPage = 0;        }        this.dataListLoading = false;    });},
   ```

   ### 分组新增&级联选择器

   需求：新增的属性分组时要指定分类

   <img src="https://fermhan.oss-cn-qingdao.aliyuncs.com/img/20210216172146.png" style="zoom:67%;" />

   这个功能是Cascader级联选择器

   attrgroup-add-or-update.vue，修改对应的位置为`<el-cascader 。。。>`

   级联选择的下拉是个options数组用categorys数组绑定数据，下面请求方法获取三级分类tree一样的方法

   更详细的设置可以用props绑定catId，name，children

   测试发现，三级分类后面还有分类，这是element判定的你还有分类，只要让后台返回数据时，实体要是空的话，就不现实children子集字段，那就没有空菜单了

   实体内添加**@JsonInclude去空字段**

   ```java
   @TableField(exist =false)@JsonInclude(JsonInclude.Include.NON_EMPTY) // 不为空的时候才返回该字段，返回到前端的这个实体数据什么都没有的化，分类的子分类空菜单就不显示private List<CategoryEntity> children;
   ```

   新增弹窗结束，回调`$this.emit`在刷新右侧属性分组列表

   ### 分组修改&级联选择器回显

   需求：

   ![](https://fermhan.oss-cn-qingdao.aliyuncs.com/img/20210216182337.png)

   ```html
   <el-button           type="text"           size="small"           @click="addOrUpdateHandle(scope.row.attrGroupId)"           >修改</el-button><script>    // 新增 / 修改    addOrUpdateHandle(id) {        // 先显示弹窗        this.addOrUpdateVisible = true;        // .$nextTick(代表渲染结束后再接着执行        this.$nextTick(() => {            // this是attrgroup.vue            // $refs是它里面的所有组件。在本vue里使用的时候，标签里会些ref=""            // addOrUpdate这个组件            // 组件的init(id);方法            this.$refs.addOrUpdate.init(id);        });    },</script>在init方法里进行回显但是分类的id还是不对，应该是用数组封装的路径
   ```

   根据属性分组id查到属性分组后填充到页面

   ```js
   init(id) {    this.dataForm.attrGroupId = id || 0;    this.visible = true;    this.$nextTick(() => {        this.$refs["dataForm"].resetFields();        if (this.dataForm.attrGroupId) {            this.$http({                url: this.$http.adornUrl(                    `/product/attrgroup/info/${this.dataForm.attrGroupId}`                ),                method: "get",                params: this.$http.adornParams()            }).then(({ data }) => {                if (data && data.code === 0) {                    this.dataForm.attrGroupName = data.attrGroup.attrGroupName;                    this.dataForm.sort = data.attrGroup.sort;                    this.dataForm.descript = data.attrGroup.descript;                    this.dataForm.icon = data.attrGroup.icon;                    this.dataForm.catelogId = data.attrGroup.catelogId;                    //查出catelogId的完整路径                    this.dataForm.catelogPath = data.attrGroup.catelogPath;                }            });        }    });
   ```

   修改AttrGroupEntity

   ```java
   /**	 * 三级分类修改的时候回显路径	 */@TableField(exist = false) // 这个数据数据库中不存在，是自己根据已有信息封装的完整分类路径private Long[] catelogPath;
   ```

   修改controller，找到属性分组id对应的分类，然后把该分类下的所有属性分组都填充好

   ```java
       /**     * 信息     */    @RequestMapping("/info/{attrGroupId}")    //@RequiresPermissions("product:attrgroup:info")    public R info(@PathVariable("attrGroupId") Long attrGroupId){		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);		//要根据属性分组所属的分类id查出这个分类id的完整路径，爷子孙这样的完整路径,        Long[] paths = categoryService.findCatelogPath(attrGroup.getCatelogId());        //然后弄到属性分类里去，供前端调取        attrGroup.setCatelogPath(paths);        return R.ok().put("attrGroup", attrGroup);    }
   ```

   添加service实现类

   ```java
       //[2,25,225]    @Override    public Long[] findCatelogPath(Long catelogId) {        //创建一个集合用来收集路径        List<Long> paths = new ArrayList<>();        //调用递归收集父分类        paths = findParentPath(catelogId, paths); //[255,25,2]        //反转一下        Collections.reverse(paths);        return paths.toArray(new Long[paths.size()]);//把集合变数组    }    //[255,25,2]    //递归收集所有父分类    private List<Long> findParentPath(Long catelogId, List<Long> paths) {        //先把当前id放进集合        paths.add(catelogId);        //搜索这个id的实体        CategoryEntity byId = this.getById(catelogId);        //找到这个实体的父id        if (byId.getParentCid()!=0){//不等于0就代表有父分类            //就继续查            findParentPath(byId.getParentCid(),paths);//查到一个放一个        }        return paths;    }
   ```

   前端优化：级联el + filterable会话关闭时清空内容，防止下次开启还遗留数据

   

   ## 品牌管理-品牌分类关联与级联更新

   #### mybatis-plus分页插件用法

   需要先添加个mybatis的拦截器

   ```java
   package com.atguigu.gulimall.product.config;//Spring boot方式@Configuration//这是配置@EnableTransactionManagement//开启事务@MapperScan("com.atguigu.gulimall.product.dao")//扫描public class MybatisPlusConfig {    // 旧版    @Bean    public PaginationInterceptor paginationInterceptor() {        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();        // 设置请求的页面大于最大页后操作， true调回到首页，false 继续请求  默认false         paginationInterceptor.setOverflow(true);        // 设置最大单页限制数量，默认 500 条，-1 不受限制         paginationInterceptor.setLimit(1000);        // 开启 count 的 join 优化,只针对部分 left join        paginationInterceptor.setCountSqlParser(new JsqlParserCountOptimize(true));        return paginationInterceptor;    }}
   ```

   #### 模糊查询

   ```java
       @Override//BrandServiceImpl    public PageUtils queryPage(Map<String, Object> params) {        //找到这个key参数        String key = (String) params.get("key");        //先查询所有        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();        //再判断key有没有        if (!StringUtils.isEmpty(key)){            //存在key,接着条件查询sql：select * from pms_brand WHERE brand_id = key or name = key;            wrapper.and((obj)->{                obj.eq("brand_id",key).or().like("name",key);            });        }        //最后封装给page        IPage<BrandEntity> page = this.page(                new Query<BrandEntity>().getPage(params),                wrapper        );
   ```

   

   #### 关联分类

   修改CategoryBrandRelationController的逻辑

   ```java
   /**     * 获取当前品牌的所有分类列表     */@GetMapping("/catelog/list")public R list(@RequestParam("brandId") Long brandId){    // 根据品牌id获取其分类信息    List<CategoryBrandRelationEntity> data = categoryBrandRelationService.list(        new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId)    );    return R.ok().put("data", data);}// 获得分类列表后再继续进行后面的工作
   ```

   #### 关联表的优化

   分类名本可以在brand表中，但因为**关联查询对数据库性能有影响**，在电商中大表数据从不做关联，哪怕**分步查**也不用关联

   所以像name这种冗余字段可以保存，优化save，**保存时用关联表存好，但select时不用关联**

   CategoryBrandRelationController

   ```java
       /**     * 保存     */    @RequestMapping("/save")    //@RequiresPermissions("product:categorybrandrelation:save")    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){        //只保存了品牌id和分类id，没有名字，但要是关联查询名字消耗很大，不能使用//		categoryBrandRelationService.save(categoryBrandRelation);        //自己搞个保存方法，分步获取名字，把数据直接保存在实体里，在保存到数据库        categoryBrandRelationService.saveDetail(categoryBrandRelation);        return R.ok();    }
   ```

   CategoryBrandRelationServiceImpl

   ```java
       @Override    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {        //拿到品牌id和分类id        Long brandId = categoryBrandRelation.getBrandId();        Long catelogId = categoryBrandRelation.getCatelogId();        //通过id获取各自详情实体        //那就得注入服务接口,拿来用        BrandEntity brandEntity = brandService.getById(brandId);        CategoryEntity categoryEntity = categoryService.getById(catelogId);        //拿到他们的名字，再放到关系实体中        categoryBrandRelation.setBrandName(brandEntity.getName());        categoryBrandRelation.setCatelogName(categoryEntity.getName());        //最后保存这个实体，到数据库，这个时候携带了名字        this.save(categoryBrandRelation);    }
   ```

   最终效果：

   ![](https://fermhan.oss-cn-qingdao.aliyuncs.com/img/20210216204033.png)

   #### 保持冗余字段的数据一致

   但是如果分类表里的name发送变化，那么品牌分类关系表里的分类的name字段应该同步变化。

   品牌名和分类名变化

   ```java
   1.控制层   	 /**品牌   * 修改   * 分组校验   */  @RequestMapping("/update")  //@RequiresPermissions("product:brand:update")  public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand){brandService.updateDetail(brand);      return R.ok();  }    /**分类     * 修改     */    @RequestMapping("/update")    //@RequiresPermissions("product:category:update")    public R update(@RequestBody CategoryEntity category){        categoryService.updateCascade(category);        return R.ok();    }  2.服务实现层      @Override    public void updateDetail(BrandEntity brand) {        //品牌名字变了，那么关系表中的冗余字段也要跟着变，因为冗余字段是自己单独另外存进去的数据，不是关联查询出来的，所以要额外设定一个细节就是保证冗余的数据一致        //先保证正常的信息更新        this.updateById(brand);        //如果其中有name改动，那么分类与品牌的关系表的品牌name也要更新，name是冗余字段        //sql：error: UPDATE  SET 字段名 = 新值 WHERE 字段名 = 某值        //UPDATE pms_category_brand_relation set brand_name = "华为1" WHERE brand_id = 2;        if (!StringUtils.isEmpty(brand.getName())) {            categoryBrandRelationService.updateDetail(brand.getBrandId(), brand.getName());        }    }    /**     * 级联更新所有数据     *     * @param category     */    @Transactional//开启事务    @Override    public void updateCascade(CategoryEntity category) {        //先进行常规的更新自己        //updateById和update方法的区别，前者用于有实体主id根据id查，后者没主id，只能传入特定的更新条件        this.updateById(category);        //再更新冗余数据        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());    }3.CategoryBrandRelationServiceImpl    //冗余数据更新-品牌名 update UpdateWrapper方法    @Override    public void updateDetail(Long brandId, String name) {        //创建实体对象        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();        //把新数据设置进去        categoryBrandRelationEntity.setBrandId(brandId);        categoryBrandRelationEntity.setBrandName(name);        //然后操作数据库保存,这个方法要求有实体和更新的操作        //sql：UPDATE pms_category_brand_relation set brand_name = ? WHERE brand_id = ?;        this.update(categoryBrandRelationEntity,new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId));    }    //冗余数据更新-分类名 自定义sql方法    @Override    public void updateCategory(Long catId, String name) {        this.baseMapper.updateCategory(catId,name);    }
   ```

   ## 平台属性

   ### 规格参数新增与VO

   控制层：创建一个Vo实体多一个所属属性分组id字段，这样就可以拿这个字段和属性id保存到关联表中（符合规范）

   ```java
     /**   * 新增   * Vo多一个属性分组id字段，这样就可以拿这个字段和属性id保存到关联表中   */  @RequestMapping("/save")  //@RequiresPermissions("product:attr:save")  public R save(@RequestBody AttrVo attr){attrService.saveAttr(attr);      return R.ok();  }
   ```

   

   1．PO持久对象

   >  PO就是对应数据库中某个表中的一条记录，多个记录可以用PO的集合。PO中应该不包含任何对数据的操作。

   2、DO（Domain 0bject)领域对象

   > 就是从现实世界中推象出来的有形或无形的业务实体。

   3.TO(Transfer 0bject)，数据传输对象传输的对象

   > 不同的应用程序之间传输的对象。微服务

   4.DTO(Data Transfer Obiect)数据传输对象

   > 这个概念来源于J2EE的设汁模式，原来的目的是为了EJB的分布式应用握供粗粒度的数据实体，以减少分布式调用的次数，从而握分布式调用的性能和降低网络负载，但在这里，泛指用于示层与服务层之间的数据传输对象。

   5.VO(value object)值对象

   > 通常用于业务层之间的数据传递，和PO一样也是仅仅包含数据而已。但应是抽象出的业务对象，可以和表对应，也可以不，这根据业务的需要。用new关韃字创建，由GC回收的

   View object：视图对象

   接受页面传递来的对象，封装对象

   将业务处理完成的对象，封装成页面要用的数据

   6.BO(business object)业务对象

   > 从业务模型的度看．见IJML元#领嵫模型的领嵫对象。封装业务逻辑的java对象，通过用DAO方法，结合PO,VO进行业务操作。businessobject:业务对象主要作用是把业务逻辑封装为一个对苤。这个对象可以包括一个或多个其它的对彖。比如一个简历，有教育经历、工怍经历、社会关系等等。我们可以把教育经历对应一个PO工作经历

   7、POJO简单无规则java对象

   8、DAO

   

   服务层：

   ```java
   @Transactional@Overridepublic void saveAttr(AttrVo attr) {    //需求1：要保存本身属性表的数据    AttrEntity attrEntity = new AttrEntity();    //把数据拷贝过去    BeanUtils.copyProperties(attr, attrEntity);    //保存,因为这里存里数据，所以实体里有attriId    this.save(attrEntity);    //需求2：保存属性、属性分组关联表数据,就要用到Vo实体    AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();    //把数据拿出来再放到实体里，最后保存    attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());    attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());    attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);}
   ```

   ### 查询规格参数列表

   控制层：

   ```java
   //product/attr/base/list/{catelogId}查询规格参数列表@GetMapping("/base/list/{catelogId}")public R baseAttrList(@RequestParam Map<String, Object> params,@PathVariable("cateLogId") Long catelogId){    PageUtils page = attrService.queryBaseAttrPage(params,catelogId);    return R.ok().put("page",page);}
   ```

   服务层：

   ```java
   @Overridepublic PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId) {    //1.查询全部    QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();    if (catelogId != 0) {        wrapper.eq("catelog_id", catelogId);        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);        return new PageUtils(page);    }    //模糊查询    String key = (String) params.get("key");    if (!StringUtils.isEmpty(key)) {        wrapper.and((obj) -> {            obj.eq("attr_id", key).or().like("attr_name", key);        });    }    //最后封装到page返回    IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);    return new PageUtils(page);}
   ```

   需求增加：属性列表还需要显示所属分类名字，所属分组名字

   分析：这两个字段在原实体里都没有，联表查询太危险，不可用，也没有冗余字段可用，都得分开查，所以需要创建一个响应RespVo实体，加入所属分类名和所属属性分组名两个字段，自己分开查到数据在封装到这个新实体中响应给前端

   实体：

   ```java
   @Datapublic class AttrRespVo extends AttrVo{    /*             "catelogName": "手机/数码/手机", //所属分类名字             "groupName": "主体", //所属分组名字     */    private String cateLogNmae;//所属分类名字    private String groupName;//所属分组名字}
   ```

   服务层修改：原本封装的page里面是原实体，信息是不够的，所以要再之后再封装成新实体

   ```java
   //最后封装到page返回IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);PageUtils pageUtils = new PageUtils(page);//得到它获取到到记录List<AttrEntity> records = page.getRecords();//用流式编程，映射其中每一个元素List<AttrRespVo> respVos = records.stream().map((attrEntity) -> {    //把原实体的信息拷贝过去    AttrRespVo attrRespVo = new AttrRespVo();    BeanUtils.copyProperties(attrEntity, attrRespVo);    //然后就剩下两条新字段需要查询    //查询所属属性分组名    AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));    if (attrAttrgroupRelationEntity != null) {        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());        //搞到groupName        attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());    }    //查询所属分类名    //根据所属分类id，查询分类表实体    CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());    if (categoryEntity != null) {        //搞到cateLogName        attrRespVo.setCatelogName(categoryEntity.getName());    }    return attrRespVo;}).collect(Collectors.toList());pageUtils.setList(respVos);return pageUtils;
   ```

   ### 接口

   #### 规格修改

   #### 销售属性维护

   #### 查询分组关联属性&删除关联

   #### 查询分组未关联的属性

   #### 新增分组与属性关联

   ## 新增商品

   ### 接口

   #### 调试会员等级相关接口

   #### 获取分类关联的品牌

   #### 获取分类下所有分组以及属性

   #### 商品新增vo抽取

   #### 商品新增业务流程分析

   #### 保存SPU基本信息

   #### 保存SKU基本信息

   #### 调用远程服务保存优惠等信息

   #### 商品保存debug完成

   #### 商品保存其他问题处理

   #### 商品管理

   #### SPU检索

   # 仓储服务-API

   ### 接口

   #### 整合ware服务&获取仓库列表

   #### 查询库存&创建采购需求

   #### 合并采购需求

   #### 领取采购单

   #### 完成采购

   #### 商品服务-API-商品管理-SPU规格维护

   

   # 分布式基础篇总结

   ![在这里插入图片描述](https://img-blog.csdnimg.cn/20210506080500803.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3JlYWxfcm9va2ll,size_16,color_FFFFFF,t_70)

   crud程序员已经很落后了，都有逆向工程可以生成前后端基本的代码，重点是后面的高级篇**高并发、高性能、高可用**

   