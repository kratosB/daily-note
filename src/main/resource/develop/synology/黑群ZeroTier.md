## 1. 准备文件

1. 无

## 2. 安装docker

1. 套件中心，直接搜索安装docker

## 3. 安装zerotier

1. docker -> 注册表 -> 搜索zerotier-synology双击选择最新版本（latestr）下载
2. ssh登录群晖系统
3. sudo -i获取权限
4. `echo -e '#!/bin/sh -e \ninsmod /lib/modules/tun.ko' > /usr/local/etc/rc.d/tun.sh`不懂照抄
5. `chmod a+x /usr/local/etc/rc.d/tun.sh`不懂照抄
6. `/usr/local/etc/rc.d/tun.sh`不懂照抄
7. `mkdir /var/lib/zerotier-one`
8. `docker run -d           \
   --name zt             \
   --restart=always      \
   --device=/dev/net/tun \
   --net=host            \
   --cap-add=NET_ADMIN   \
   --cap-add=SYS_ADMIN   \
   -v /var/lib/zerotier-one:/var/lib/zerotier-one zerotier/zerotier-synology:latest`
9. `docker exec -it zt zerotier-cli join e5cd7a9e1cae134f`，注意改一下id
10. 页面通过

## 参考资料
1. [小白轻松搞定！ZeroTier外网访问家中群晖NAS教程 看电影都不卡](https://baijiahao.baidu.com/s?id=1738840233744208152&wfr=spider&for=pc)
2. [ZeroTier Documentation - Synology NAS](https://docs.zerotier.com/devices/synology/)