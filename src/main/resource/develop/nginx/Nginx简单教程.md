# Nginx简单教程

1. 反向代理
   ```text
    server {  
        listen       80;                                                         
        server_name  localhost;                                               
        client_max_body_size 1024M;

        location / {
            proxy_pass http://localhost:8080;
            proxy_set_header Host $host:$server_port;
        }
    }
   ```
2. 负载均衡
   ```text
    upstream test {
        server localhost:8080;
        server localhost:8081;
        #默认轮询
    }
    server {
        listen       81;                                                         
        server_name  localhost;                                               
        client_max_body_size 1024M;

        location / {
            proxy_pass http://test;
            proxy_set_header Host $host:$server_port;
        }
    }
   ```
   ```text
    upstream test {
        server localhost:8080 weight=9;
        server localhost:8081 weight=1;
        #权重
    }
    ```
      ```text
    upstream test {
        server localhost:8080;
        server localhost:8081;
        ip_hash;
        #ip_hash，同一个用户请求到同一个服务，可以解决session等问题
    }
    ```
      ```text
    upstream test {
        server localhost:8080;
        server localhost:8081;
        fair;
        #响应时间短的优先分配(第三方)
    }
    ```
      ```text
    upstream test {
        hash $request_uri;
        server localhost:8080;
        server localhost:8081;
        hash_method crc32;
        #url_hash（第三方），相同url指向同一个服务器，可以更多的利用缓存
    }
    ```
3. Http服务器（包含动静分离）
      ```text
    server  {
        listen       80;                                                         
        server_name  localhost;                                               
        client_max_body_size 1024M;

        location / {
            root   e:\wwwroot;
            index  index.html;
        }
    }
    ```
4. 高可用
   使用keepalived
5. 基础命令
   1. nginx start
   2. nginx restart
   3. nginx stop
   3. nginx -s reload
   3. nginx -v

## 引用
>1. [刚进来的小伙伴说Nginx只能做负载均衡，还是太年轻了](https://mp.weixin.qq.com/s/zpfFOWp_IFLCtLqTqeqPEg)
>2. [就是要让你搞懂Nginx，这篇就够了！](https://mp.weixin.qq.com/s/tcRHDCVItsud_H1M0JKMrA)
>3. [搞懂Nginx一篇文章就够了](https://blog.csdn.net/yujing1314/article/details/107000737)