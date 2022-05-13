# CentOS docker部署homeassistant

## 1. 更新一下yum

1. 直接`yum update`

## 2. 安装docker

1. 如果没装过，直接`yum install docker`
2. `docker version`
   >验证是否安装成功
3. `systemctl start docker` `systemctl enable docker`
   >加入开机启动

## 3. docker常用命令

1. `docker search homeassistant`
   >搜索镜像
2. `docker pull homeassistant`
   >下载镜像
3. `docker ps`
   >查看当前正在运行的容器
4. `docker ps -a`
   >查看所有容器的状态
5. `docker logs xxxx`
   >查看日志

## 4. 开始部署

1. `mkdir /root/haconfig/ -p`
   >新建一个文件夹存放config，docker容器中对应的配置会映射到这里
2. `docker pull homeassistant/home-assistant`
   >下载镜像
3. `docker image  ls`
   >查看homeassistant是否安装成功
4. `docker run -d --name myha -p 8888:8123 -v E:/Docker/practice/sync_ha_file:/config faa660349657`
   >运行镜像
   1. -d 后台运行
   2. --name 容器名称
   3. -p 端口映射
   4. -v 配置文件映射
5. `docker stop/start home-assistant`
   >停止/启动容器
6. 网页访问ip:8888进入管理后台
7. 本地配置文件，编写configuration.yaml。

## 5. 发现问题

1. web端访问不到
2. `docker container ls -a`查看发现容器已经停止
3. `docker exec -it 容器id /bin/bash`进不去
4. `docker logs myha`查看日志，显示`Fatal Error: Unable to create library directory /config/deps`
5. 找资料发现是容器内权限不够（有些资料说是本地文件权限，但是改了也还是不行）

## 6. 解决问题

1. 做了三个操作
   1. `chmod -R a+w /root/haconfig/`，开放了本地volume的权限。
   2. `docker run -d --name myha --privileged=true -v /root/haconfig:/config --network=host 77302f5cbeee`
   3. `docker run -d --name myha -u root -v /root/haconfig:/config --network=host 77302f5cbeee`
2. 试验了一下，第二个成功了`--privileged=true`。当然引用3里面这么做没成功，搞不懂。


# 引用
1. [CentOS7.x服务器安装Docker并安装homeassistant](https://www.jianshu.com/p/7ea1175aa708)
2. [在本地电脑Docker中运行Homeassistant系统](https://zhuanlan.zhihu.com/p/141337969)
3. [Docker挂载宿主机目录出现Cannot create directory](https://ask.csdn.net/questions/1095153)
