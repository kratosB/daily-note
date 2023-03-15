## 1. 准备

1. 引导文件ds3617_6.1.vmdk
2. 群晖镜像文件DSM_DS3617xs_15284.pat
3. 群晖助手synology-assistant-7.0-50029
4. VirtualBox（我用的是6.1.36 r152435）

## 2. 新建虚拟机

1. 选择Linux 2.6/3.x/4.x (64bit)或者other Linux (64bit)
2. 内存随便配
3. 现在创建虚拟硬盘
4. VMDK（VDI好像也可以）
5. 动态分配（大小无所谓，反正动态分配，没用的时候不占空间）
6. 配置一下文件地址

## 3. 设置虚拟机

1. 右键虚拟机 -> 设置
2. 系统，去掉软驱
3. 存储，一个PIIX4控制器，一个SATA控制器
4. PIIX4控制器，添加引导文件（ds3617_6.1.vmdk）
5. 网络，桥接网卡（RealTek pcie）

## 4. 开始安装

1. 启动虚拟机
2. 打开synology-assistant，找到一个服务器，但是联机失败。
3. 复制这个mac地址，关闭虚拟机，修改mac地址，重启。
4. 打开synology-assistant搜索，自动跳转到web页面安装。
5. 设置，手动安装，选择镜像文件（DSM_DS3617xs_15284.pat），立即安装
6. 不选自动更新
7. 跳过quick connect
8. 安装完成

## 参考资料
1. [VirtualBox安装黑群晖并建立smb共享目录的方法](https://www.lmlphp.com/user/56/article/item/9301/)
2. [在VirtualBox虚拟机中安装黑群晖【全网简单教程】](https://www.lmlphp.com/user/56/article/item/9301/)
3. [文菌装NAS 篇五：手把手教您安装黑群晖918+ 6.2保姆级教程，这应该是装黑群晖NAS最详细的教程了，含群晖设置](https://post.smzdm.com/p/aqx07xmk/)