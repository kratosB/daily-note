[Java流的概念：什么是输入/输出流？](http://c.biancheng.net/view/1119.html)

### 输入流

所有输入流类都是 InputStream 抽象类（字节输入流）和 Reader 抽象类（字符输入流）的子类。其中 InputStream 类是字节输入流的抽象类，是所有字节输入流的父类，其层次结构如下图所示。

    InputStream(字节输入流)  ->  FileInputStream(文件输入流)
                            ->  PipedInputStream(管道输入流)
                            ->  ObjectInputStream(对象输入流)
                            ->  FilterInputStream(过滤器输入流)   ->  PushBackInputStream(回压输入流)
                                                                 ->  BufferedInputStream(缓冲输入流)
                                                                 ->  DataInputStream(数据输入流)
                            ->  SequenceInputStream(顺序输入流)
                            ->  ByteArrayInputInputStream(字节数组输入流)
                            ->  StringBufferInputInputStream(缓冲字符串输入流)

InputStream 类中所有方法遇到错误时都会引发 IOException 异常。如下是该类中包含的常用方法。
* int read()：从输入流读入一个 8 字节的数据，将它转换成一个 0~255 的整数，返回一个整数，如果遇到输入流的结尾返回 -1。
* int read(byte[] b)：从输入流读取若干字节的数据保存到参数 b 指定的字芳数组中，返回的字芾数表示读取的字节数，如果遇到输入流的结尾返回 -1。
* int read(byte[] b,int off,int len)：从输入流读取若干字节的数据保存到参数 b 指定的字节数组中，其中 off 是指在数组中开始保存数据位置的起始下标，len 是指读取字节的位数。返回的是实际读取的字节数，如果遇到输入流的结尾则返回 -1。
* void close()：关闭数据流，当完成对数据流的操作之后需要关闭数据流。
* int available()：返回可以从数据源读取的数据流的位数。
* skip(long n)：从输入流跳过参数 n 指定的字节数目。
* boolean markSupported()：判断输入流是否可以重复读取，如果可以就返回 true。
* void mark(int readLimit)：如果输入流可以被重复读取，从流的当前位置开始设置标记，readLimit 指定可以设置标记的字节数。
* void reset()：使输入流重新定位到刚才被标记的位置，这样可以重新读取标记过的数据。
>上述最后 3 个方法一般会结合在一起使用，首先使用 markSupported() 判断，如果可以重复读取，则使用 mark(int readLimit) 方法进行标记，标记完成之后可以使用 read() 方法读取标记范围内的字节数，最后使用 reset() 方法使输入流重新定位到标记的位置，继而完成重复读取操作。

Java 中的字符是 Unicode 编码，即双字节的，而 InputStream 是用来处理单字节的，在处理字符文本时不是很方便。这时可以使用 Java 的文本输入流 Reader 类，该类是字符输入流的抽象类，即所有字输入流的实现都是它的子类。

Reader类的具体层次结构如下图所示，该类的方法与 InputSteam 类的方法类似，这里不再介绍。

    Reader(字符输入流)   ->  CharArrayReader
                        ->  BufferedReader      ->  LineNumberReader
                        ->  FilterReader        ->  PushBackReader
                        ->  InputStreamReader   ->  FileReader
                        ->  PipedReader
                        ->  StringReader

### 输出流

在 Java 中所有输出流类都是 OutputStream 抽象类（字节输出流）和 Writer 抽象类（字符输出流）的子类。其中 OutputStream 类是字节输出流的抽象类，是所有字节输出流的父类，其层次结构如下图所示。

    OutputStream(字节输入流) ->  FileOutputStream(文件输出流)
                            ->  PipedOutputStream(管道输出流)
                            ->  ObjectOutputStream(对象输出流)
                            ->  FilterOutputStream(过滤器输出流)   ->  PrintStream(打印输出流)
                                                                  ->  BufferedOutputStream(缓冲输出流)
                                                                  ->  DataOutputStream(数据输出流)
                            ->  ByteArrayOutputStream(字节数组输出流)

OutputStream 类是所有字节输出流的超类，用于以二进制的形式将数据写入目标设备，该类是抽象类，不能被实例化。OutputStream 类提供了一系列跟数据输出有关的方法，如下所示。
* int write (b)：将指定字节的数据写入到输出流。
* int write (byte[] b)：将指定字节数组的内容写入输出流。
* int write (byte[] b,int off,int len)：将指定字节数组从 off 位置开始的 len 字芳的内容写入输出流。
* close()：关闭数据流，当完成对数据流的操作之后需要关闭数据流。
* flush()：刷新输出流，强行将缓冲区的内容写入输出流。

字符输出流的父类是 Writer，其层次结构如下图所示。

    Writer(字符输出流)   ->  CharArrayWriter
                        ->  BufferedWriter
                        ->  FilterWriter
                        ->  OutputStreamWriter  ->  FileReader
                        ->  PipedWriter
                        ->  StringWriter













