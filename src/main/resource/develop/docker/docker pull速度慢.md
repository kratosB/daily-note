# [docker pull速度慢解决方法](https://blog.csdn.net/qq_35985044/article/details/118310729)

加入阿里云镜像

vim /etc/docker/daemon.json

加入如下内容：
{"registry-mirrors": ["https://fy707np5.mirror.aliyuncs.com"]}

systemctl daemon-reload
systemctl restart docker

# 引用
[docker pull速度慢解决方法](https://blog.csdn.net/qq_35985044/article/details/118310729)