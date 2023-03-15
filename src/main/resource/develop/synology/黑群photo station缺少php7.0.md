## 1. 准备

1. 黑群版本：DSM 6.1.7-15284
2. php套件版本：[7.0.33-0028](https://global.synologydownload.com/download/Package/spk/PHP7.0/7.0.33-0028/PHP7.0-x86_64-7.0.33-0028.spk)
3. photo station版本：[PhotoStation-x86_64-6.8.14-3501](https://global.synologydownload.com/download/Package/spk/PhotoStation/6.8.14-3501/PhotoStation-x86_64-6.8.14-3501.spk)

## 2. 安装php

1. 下载php套件
2. 套件中心，手动安装
3. 群晖 -> 控制面板 -> 终端机和SNMP -> 启动ssh功能
4. WindTerm ssh连接群晖系统
5. `sudo -i`获得权限
6. 输入`php -v`查看版本，应该是5.6版本
7. `mv /bin/php /bin/php56`
8. `cp -a /volume1/@appstore/php7.0/usr/local/bin/php70`
9. 再次查看php版本
10. 重启

## 3. 安装photo station

1. 下载photo套件
2. 套件中心，手动安装

## 参考资料
1. [NAS瞎折腾：移花接木解决photo station、moments由于php7.0丢失不能安装问题](https://post.smzdm.com/p/a0q9dl90/)
2. [套件源](https://archive.synology.com/download/Package)