# [GET 和 POST请求的本质区别是什么？原来我一直理解错了](https://mp.weixin.qq.com/s?__biz=MzAxNjk4ODE4OQ==&mid=2247495825&idx=4&sn=42a068d69843ef63d2ce378b4848ec7d&chksm=9beed1e3ac9958f522ea55f23b883b5ebf491217cc7290376aabdaee910b7903c6fe9645354f&xtrack=1&scene=90&subscene=93&sessionid=1606563367&clicktime=1606564607&enterid=1606564607&ascene=56&devicetype=android-29&version=270014e5&nettype=WIFI&abtest_cookie=AAACAA%3D%3D&lang=zh_CN&exportkey=AjnRkFdjDJxZZ7%2FaQbLK1XE%3D&pass_ticket=kuzOLULUzmfKvYNdNGbHVnqXspi3F%2F547vs8Ly76SfXnSvAm%2B7FYGi4tCa7zv8%2Bi&wx_header=1)

1. 底层其实都是tcp，没啥区别，那些区别都是表象。
    >也有一些区别只是不同浏览器的限制。比如对于GET的requrest body的处理，url长度，等。
2. 真正的区别是：
    1. GET产生一个TCP数据包。
        >对于GET方式的请求，浏览器会把http header和data一并发送出去，服务器响应200（返回数据）。
    2. POST产生两个TCP数据包。
        >而对于POST，浏览器先发送header，服务器响应100 continue，浏览器再发送data，服务器响应200 ok（返回数据）。
        >>Firefox只发一次。特立独行。
3. GET效率高一丢丢（网络好的时候微乎其微）。
4. POST数据完整性上更安全（网络不好的时候）。

## 引用

>[GET 和 POST请求的本质区别是什么？原来我一直理解错了](https://mp.weixin.qq.com/s?__biz=MzAxNjk4ODE4OQ==&mid=2247495825&idx=4&sn=42a068d69843ef63d2ce378b4848ec7d&chksm=9beed1e3ac9958f522ea55f23b883b5ebf491217cc7290376aabdaee910b7903c6fe9645354f&xtrack=1&scene=90&subscene=93&sessionid=1606563367&clicktime=1606564607&enterid=1606564607&ascene=56&devicetype=android-29&version=270014e5&nettype=WIFI&abtest_cookie=AAACAA%3D%3D&lang=zh_CN&exportkey=AjnRkFdjDJxZZ7%2FaQbLK1XE%3D&pass_ticket=kuzOLULUzmfKvYNdNGbHVnqXspi3F%2F547vs8Ly76SfXnSvAm%2B7FYGi4tCa7zv8%2Bi&wx_header=1)
>[GET 和 POST请求的本质区别是什么？原来我一直理解错了](https://www.cnblogs.com/logsharing/p/8448446.html)