### 项目框架
1. 第一章
    1. spring boot环境搭建
    2. 集成Thymeleaf,Result结果封装
    3. 集成Mybatis+Druid
    4. 集成Jedis+Redis安装，通用缓存key封装
2. 第二章
    1. 数据库设计
    2. 明文密码两次MD5处理
    3. JSR303校验+全局异常处理
    4. 分布式Session
3. 第三章
    1. 数据库设计(没有完全遵守三范式)    
    2. 商品列表页
    3. 商品详情页
    4. 订单详情页
4. 第四章
    1. JMeter入门  
    2. 自定义变量，模拟多用户
    3. JMeter命令行使用
5. 第五章 页面优化
    1. 页面缓存+URL缓存+对象缓存
    2. 页面静态化，前后端分离
    3. 静态资源优化
    4. CDN优化
6. 第六章 接口优化
    1. Redis预减库存减少数据库访问
    2. 内存标记减少Redis访问
    3. RabbitMQ队列缓冲，异步下单，增强用户体验
    4. RabbitMQ安装与Spring Boot集成
    5. 访问Nginx水平扩展
    6. 压测
7. 第七章 安全优化
    1. 秒杀地址隐藏
    2. 数学公式验证码
    3. 接口防刷
    
### redis部署
`/usr/local`下`mkdir myredis`，依次执行  
`wget http://download.redis.io/releases/redis-5.0.4.tar.gz`  
`tar xzf redis-5.0.4.tar.gz`  
`cd redis-5.0.4`  
`make`  
`make install`  
安装完后，修改redis.conf文件  
69行，`bind 127.0.0.1`改为`bind 0.0.0.0` 允许任意服务器访问  
136行，`daemonize no`改为`daemonize yes`  
507行，设置密码   
安装一个redis服务：  
进入到与redis.conf同级目录的utils中，执行脚本`./install_server.sh`（classpath:/graph/redis-server-install.jpg）  
`systemctl status redis_6379`查看redis状态  
`systemctl start redis_6379`启动服务  
这项服务其实在`/etc/init.d/redis_6379`这个shell文件中，也可以手动修改文件中的内容。
### redis集成
`application.properties`中配置好redis相关参数，  
`redisConfig`类读取`application.properties`配置的参数，  
`RedisPoolFactory`中注入`redisConfig`对象获取配置创建jedis连接池,  
`RedisService`类中注入`redisPoolFactory`获取jedis连接池，获取jedis对象提供`get set incr decr exists`等方法  
希望在不同模块添加数据到redis时区分开来，加上各自标识，所以创建了`KeyPrefix`接口，设置前缀和过期时间，抽象类`BasePrefix`
实现了这个接口，`BasePrefix`中设置了前缀为 类名+prefix，具体的prefix和过期时间由子类完成设置。比如存一个SecKill对象，
键为 类名+prefix+本来的键 ，即 `"SecKillUser" + "_token_" + token`.值为SecKill对象的json字符串
### 数据库设计
有goods表，order表，分别存储商品基本信息(比如id，name,img，price等)，订单基本信息(比如id，用户id，商品id，创建时间等)；  
同时建了seckill_goods表，用来存储秒杀特有的相关信息，比如商品id，秒杀库存，秒杀价格等；seckill_order表中只存id,用户id，商品id,订单id  
### 两次md5加密
http在网络上通过明文传输，第一次加密防止用户密码明文传输到服务端，在客户端进行加密，采用明文+固定salt；  
第二次在服务端再进行加密，采用客户端传过来的加密密码+随机salt，然后存入数据库(这个随机salt也存入数据库)。  
加密参考`MD5Util`类  
### JSR303自定义注解
登录逻辑见`LoginController`和`SecKillUserService.login`。在`LoginVo`类中使用自定义注解`@IsMobile`判断输入的`mobile`是否合法。
`@IsMobile`由`IsMobile`和`IsMobileValidator`两个类完成自定义注解的实现。
### 全局异常处理
`@ControllerAdvice`和`@ExceptionHandler`实现全局异常处理，`cn.andios.seckill.exception.GlobeExceptionHandler`
### 分布式session
用户登录认证成功后，页面跳转到商品列表页(对应`GoodsController`)，后台给用户生成一个唯一标识token
   - 需要把用户信息存入redis  
    前缀+prefix+唯一标识token作为键，用户对象json字符串作为值，设置过期时间，存入redis。
   - 向客户端回写cookie  
   生成一个cookie，设置cookie过期时间与存入redis的键过期时间一致，将cookie通过HttpServletResponse回写给客户端。  
登录认证成功后，后请求/goods/to_list跳转到商品列表页，请求`GoodsController`时，客户端会把response回写的cookie带过来，服务端根据这个cookie再去
redis中取用户信息，如果取不到，则表明未登录或session失效，如果取到了就更新过期时间。这个操作在服务端的多个地方都会调用，所以配置了一个参数解析器，
从request中获取token等信息，判断用户是否登录。见`UserArgumentResolver`类，它继承自`HandlerMethodArgumentResolver`，再在`WebConfig`中将
它添加到容器中，那么`GoodsController`中就不需要判断用户token信息了。(后期为了实现自定义注解`@AccessLimit`，实现了拦截器`AccessInterceptor`，
拦截器会在参数解析器之前执行，也需要根据token获取用户信息，所以这部分封装在了拦截器中，拦截器中获取的secKillUser放在ThreadLocal中，参数解析器直接
从ThreadLocal中取即可)
### redis压测
使用`redis-benchmark`测试redis的性能:  
一百个并发，10万个请求：`redis-benchmark -h 127.0.0.1 -p 6379 -c 100  -n 100000`   
存取大小为100字节的数据包：`redis-benchmark -h 127.0.0.1 -p 6379 -q -d 100`   
只测试部分命令：`redis-benchmark -t set,lpush -q -n 100000`  
### JMeter命令行测试
1. windows上用JMeter录好测试文件`xxx.jmx`
2. CentOS中下载解压JMeter,进入bin中执行`./jmeter.sh -n -t xxx.jmx -l result.jtl`,即执行压测，压测完当前目录下生成result.jtl文件即压测报告，
将压测报告下载到windows中用JMeter打开即可。如果需要另外的xxx.txt文件，把txt和jmx放在同一目录下即可。
### nohup &
原程序的的标准输出被自动改向到当前目录下的`nohup.out`文件中，起到了log的作用。  
要运行后台中的`nohup`命令，添加 & （ 表示”and”的符号）到命令的尾部。  
比如`nohup /root/start.sh &`,该脚本的输出会在当前目录的nohup.out文件中，该脚本会在后台运行。
### 文件传输
#### sz,rz
rz，sz是Linux/Unix同Windows进行ZModem文件传输的命令行工具，优点就是不用再开一个sftp工具登录上去上传下载文件。  
`sz`：将选定的文件发送（send）到本地机器。  
`rz`：运行该命令会弹出一个文件选择窗口，从本地选择文件上传到Linux服务器。  
安装：`yum install lrzsz`  
从服务端(CentOS)发送文件到客户端(windows)：`sz filename`  
从客户端(windows)上传文件到服务端(CentOS)：`rz`,在弹出的框中选择文件，上传文件的用户和组是当前登录的用户  
SecureCRT设置默认路径：  
Options -> Session Options -> Terminal -> X/Y/Zmodem ->Directories  
Xshell设置默认路径：  
右键会话 -> 属性 -> ZMODEM -> 接收文件夹  
#### sftp
SecureCRT中`alt+p`即可进入，在 Options->Session Options->Connection->SSH2->SFTP Session  
pwd: 查询linux主机所在目录(也就是远程主机目录)  
lpwd: 查询本地目录（一般指windows上传文件的目录）  
ls: 查询连接到当前linux主机所在目录有哪些文件  
lls: 查询当前本地上传目录有哪些文件  
lcd: 改变本地上传目录的路径  
cd: 改变远程上传目录  
get: 将远程目录中文件下载到本地目录  
put: 将本地目录中文件上传到远程主机(linux)  
quit: 断开FTP连接  
### 页面优化
1. 页面缓存+url缓存+对象缓存---减轻数据库压力  
    - 页面缓存：GoodsController中改进的list，detail方法。取的时候先从缓存中取，如果没有取到，就先使用ThymeleafViewResolver根据参数生成html页面，
再存入redis中。缓存时间一般较短。  
    - 对象缓存：SecKillUserService中的getById,updatePassword(更新密码时先更新数据库，再更新缓存，原因参见下面的博客)方法。取对象时，先从缓存中
    取，如果没有，就从数据库中取。更新对象时，注意要更新缓存。
2. 页面静态化 前后端分离  
    - 常用技术angular js、vue (这里使用的jQuery)  
    - 比如GoodsController中改进的detail2方法，SecKillController中的secKill2方法，OrderController中的orderInfo方法。原来(比如SecKillController中
    的secKill1方法)返回的是字符串，由thymeleaf自动映射成template目录下的页面，现在是返回一个自定义的结果对象(`cn.andios.seckill.result.Result`),
    将数据放在这个结果对象中，前台通过ajax请求获取数据，再通过jQuery渲染页面。两者请求方式也不一样，比如`goods_list.html`中，如果是页面静态化，
    则直接链接到指定页面，然后在这个页面中调用ajax向controller请求数据，如果不是页面静态化，通过thymeleaf解析，就路由到controller的路径，由
    controller直接处理请求跳转到指定页面
    - 浏览器缓存  
3. 页面资源优化  
    - js/css压缩，减少浏览  
    - 多个js/css组合，减少连接数(比如淘宝Tengine,webpack)  
4. cdn优化    
### 接口优化
1. Redis预减库存，内存标记减少数据库访问
    1. 系统初始化，把商品库存数量加载到Redis中
    2. 收到请求，内存标记减少Redis访问预减库存，库存不足，直接返回，否则进入3
    3. 请求入队，放到消息队列中，立即返回排队中
    4. 请求出队，生成订单，减少库存 
    5. 客户端轮询，是否秒杀成功 
    `SecKillController.secKill2`到`SecKillController.secKill3`
3. 请求先入队缓冲，异步下单，增强用户体验
4. RabbitMq继承spring boot
5. nginx水平扩展，方向代理
6. 压测/usr/local/myerlang/erlang20/bin
### 消息队列
- 好处：异步处理提高系统性能（削峰、减少响应所需时间）；降低系统耦合
- 问题：
    - 系统可用性降低：需要考虑消息丢失或者MQ挂掉
    - 系统复杂性提高：需要保证消息没被重复消费且保证消息传递的顺序性
    - 一致性：万一消费者没有正确消费消息，可能会导致数据不一致
#### JMS vs AMQP
- jms是java的消息服务，属API规范，有点对点、发布订阅两种模式，支持TextMessage、MapMessage 等复杂的消息正文格式(5种)。
- AMQP是高级消息队列协议，提供5种消息模型(direct/fanout/topic/headers/system)，仅支持byte[]类型信息，几种消息队列都是基于AMQP来实现的。
#### 常见消息队列
- 吞吐量：activeMQ、rabbitMQ比rocketMQ、kafka低  
- 时效性：RabbitMQ基于erlang开发，并发能力强，延时很低，达到微秒级，其他三个都是 ms 级。
- 可用性：activeMQ、rabbitMQ基于主从架构实现高可用，rocketMQ、kafka基于分布式架构实现高可用
- RabbitMQ基于信道channel传输，没有用tcp连接来进行数据传输，tcp链接创建和销毁对于系统性能的开销比较大消费者链接RabbitMQ其实就是一个TCP链接，一旦链接创建成功之后，
    就会基于链接创建Channel，每个线程把持一个Channel,Channel复用TCP链接，减少了系统创建和销毁链接的消耗，提高了性能
    
1. 如何保证消息可靠性传输
    1. 生产者丢失数据  
        - 生产者将数据发送到rabbitmq的时可能因为网络问题丢失数据，此时可以选择用rabbitmq提供的事务功能，就是生产者发送数据之前开启rabbitmq事务，然后发送消息，
        如果消息没有成功被rabbitmq接收到，那么生产者会收到异常报错，此时就可以回滚事务，但使用rabbitmq事务机制，基本上吞吐量会下来，因为太耗性能。如果要确保写rabbitmq
        的消息别丢，可以开启confirm模式。
        - 在生产者那里设置开启confirm模式之后，你每次写的消息都会分配一个唯一的id，然后如果写入了rabbitmq中，rabbitmq会给你回
        传一个ack消息，告诉你说这个消息ok了。如果rabbitmq没能处理这个消息，会回调你一个nack接口，告诉你这个消息接收失败，你可以重试。而且你可以结合这个机制自己在
        内存里维护每个消息id的状态，如果超过一定时间还没接收到这个消息的回调，那么你可以重发。  
        - 事务机制和cnofirm机制最大的不同在于：事务机制是同步的，你提交一个事务之后会阻塞在那儿，但是confirm机制是异步的，你发送个消息之后就可以发送下一个消息，
        然后rabbitmq接收了那个消息之后会异步回调你一个接口通知你这个消息接收到了。
    2. rabbitmq丢失数据  
        - 开启rabbitmq的持久化。就是消息写入之后会持久化到磁盘，哪怕是rabbitmq自己挂了，恢复之后会自动读取之前存储的数据，一般数据不会丢。
        - 设置持久化有两个步骤，
            - 第一个是创建queue的时候将其设置为持久化的，这样就可以保证rabbitmq持久化queue的元数据，但是不会持久化queue里的数据；
            - 第二个是发送消息的时候将消息的deliveryMode设置为2，就是将消息设置为持久化的，此时rabbitmq就会将消息持久化到磁盘上去。必须要同时设置这两个持久化才行，
            rabbitmq哪怕是挂了，再次重启，也会从磁盘上重启恢复queue，恢复这个queue里的数据。而且持久化可以跟生产者那边的confirm机制配合起来，只有消息被持久化到磁盘之后，
            才会通知生产者ack了，所以哪怕是在持久化到磁盘之前，rabbitmq挂了，数据丢了，生产者收不到ack，你也是可以自己重发的。
    3. 消费端弄丢了数据  
        - 消费端如果丢失了数据，主要是因为你消费的时候，刚消费到，还没处理，结果进程挂了，比如重启了，此时rabbitmq认为你都消费了，这数据就丢了。这个时候得用rabbitmq提供
        的ack机制，简单来说，就是你关闭rabbitmq自动ack，可以通过一个api来调用就行，然后每次你自己代码里确保处理完的时候，再程序里ack一把。这样的话，如果你还没处理完，
        不就没有ack？那rabbitmq就认为你还没处理完，这个时候rabbitmq会把这个消费分配给别的consumer去处理，消息是不会丢的。
2. 如何保证消息队列高可用
    1. 普通集群模式
        - 在多台机器上启动多个rabbitmq实例，每个机器启动一个。但是你创建的queue，只会放在一个rabbtimq实例上，但是每个实例都同步queue的元数据。消费的时候，如果连接到了
        另外一个实例，那么那个实例会从queue所在实例上拉取数据过来。  
        - 消费者每次随机连接一个实例然后拉取数据，要么固定连接那个queue所在实例消费数据，
        - 前者有数据拉取的开销，后者导致单实例性能瓶颈。      
        - 而且如果那个放queue的实例宕机了，会导致接下来其他实例就无法从那个实例拉取，如果你开启了消息持久化，让rabbitmq落地存储消息的话，消息不一定会丢，得等这个实例恢复了，
        然后才可以继续从这个queue拉取数据。                   
    2. 镜像集群模式
        - 创建的queue，无论元数据还是queue里的消息都会存在于多个实例上，然后每次你写消息到queue的时候，都会自动把消息到多个实例的queue里进行消息同步。何一个机器宕机了，
        没事儿，别的机器都可以用。
        - 坏处在于：
            - 第一，这个性能开销太大了，消息同步所有机器，导致网络带宽压力和消耗很重！
            - 第二，就没有扩展性可言，如果某个queue负载很重，加机器，新增的机器也包含了这个queue的所有数据，并没有办法线性扩展你的queue。
        - 开启方法：在rabbitmq管理后台新增一个策略，这个策略是镜像集群模式的策略，指定的时候可以要求数据同步到所有节点的，也可以要求就同步到指定数量的节点，然后你再次
        创建queue的时候，应用这个策略，就会自动将数据同步到其他的节点上去了。
3. 如何保证消息不被重复消费  
    每次重启系统，可能会有消息被重复消费，此时就需要保证幂等性
    1. 如果消费者是写入数据库，可以先根据主键查一下，如果这数据已经有了就直接update
    2. 如果是写入redis，那没问题了，反正每次都是set，天然幂等性
    3. 让生产者发送每条数据的时候，里面加一个全局唯一的id，类似订单id之类的东西，然后你这里消费到了之后，先根据这个id去比如redis里查一下，之前消费过吗？
        如果没有消费过，你就处理，然后这个id写redis。如果消费过了，那你就别处理了，保证别重复处理相同的消息即可。
    4. 基于数据库的唯一键来保证重复数据不会重复插入多条。重复数据插入的时候，因为有唯一键约束了，所以重复数据只会插入报错，不会导致数据库中出现脏数据。
4. 如何保证消息顺序性
    1. 场景一：一个queue，多个consumer。  
        方案：拆分多个queue，每个queue一个consumer，就是多一些queue而已，确实是麻烦点；或者就一个queue但是对应一个consumer，然后这个consumer内部用内
        存队列做排队，然后分发给底层不同的worker来处理
5. 如何解决消息队列的延时以及过期失效问题？消息队列满了以后该怎么处理？有几百万消息持续积压几小时，说说怎么解决?   
    - 场景一：比如消费端每次消费要写入mysql，结果mysql挂了，消费端不能消费了或者消费速度及其慢，这时大量消息就会堆积在队列中。
    - 解决方案(临时紧急扩容)：
        1. 先修复consumer问题，确保其恢复消费速度，然后将现有consumer都停掉。
        2. 新建一个topic，partition是原来的10倍，临时建立好原先10倍或者20倍的queue数量
        3. 然后写一个临时的分发数据的consumer程序，这个程序部署上去消费积压的数据，消费之后不做耗时的处理，直接均匀轮询写入临时建立好的10倍数量的queue
        4. 接着临时征用10倍的机器来部署consumer，每一批consumer消费一个临时queue的数据
        5. 这种做法相当于是临时将queue资源和consumer资源扩大10倍，以正常的10倍速度来消费数据
        6. 等快速消费完积压数据之后，得恢复原先部署架构，重新用原先的consumer机器来消费消息
    - 场景二：rabbitmq中大量积压的消息设置了过期时间TTL，积压超过一定的时间就会被rabbitmq给清理掉，这个数据就没了。
    - 解决方案(批量重导)：
        高峰期过后，将白天丢失的数据查出来再放到mq里面。
    - 场景三：消息长时间积压，导致mq快写满了
6. 如果让你写一个消息队列，该如何进行架构设计啊？说一下你的思路
    - 需要支持可扩展性，需要时可以快速扩容
    - 需要落地磁盘，才能保证进程挂掉时数据不会丢失。那落磁盘的时候怎么落啊？顺序写，这样就没有磁盘随机读写的寻址开销，磁盘顺序读写的性能是很高的     
    - 需要保证可用性，
    - 需要支持数据0丢失，

### 安全优化
#### 秒杀接口地址隐藏
思路：`goods_detail.htm`中点击立即秒杀，先去请求`secKill/path`接口校验验证码，获取秒杀地址path,然后带着path访问真正的秒杀接口`/secKill/{path}/do_secKill4`
#### 数学公式校验码(安全、分散用户请求)
1. 添加生成验证码的接口
2. 获取秒杀路径时，验证验证码
3. ScriptEngine使用
`goods_detail.htm`页面加载完成时就会请求`/secKill/verifyCode`接口中生成验证码，用户点击秒杀会先请求`secKill/path`接口校验验证码
#### 接口限流防刷
单个地方使用：`SecKillController.secKillPath`    
通用设置：自定义注解`AccessLimit`，自定义拦截器`AccessInterceptor`,发现类上面有`AccessLimit`注解后就会进行判断

-----------------------------------
## 常见问题
1. 防止超卖：  
    1. 防止同一用户多次秒杀：设置`user_id`和`goods_id`的唯一索引
    2. 保证stock_count>0：`update seckill_goods set stock_count = stock_count - 1 where id=#{goodsId} and stock_count > 0`
    3. 乐观锁：`update seckill_goods set count = count - 1, version = version + 1 where id=#{goodsId} and version = #{version}`
    4. redis预减库存减少数据库访问　内存标记减少redis访问
    5. 悲观锁虽然可以解决超卖，但加锁的时间可能会很长，限制其他用户访问，如果请求过多系统可能会出现异常。乐观锁不加锁，如果失败直接返回，可以承受高并发。

2.
    mysql悲观锁：对数据被外界（包括本系统当前的其他事务，以及来自外部系统的事务处理）修改持保守态度，因此，在整个数据处理过程中，将数据处于锁定状态。
    悲观锁的实现，往往依靠数据库提供的锁机制
    排它锁：`...for update`,没有加锁的事务只要在select时不加排它锁也可以查询事务
    共享锁：`lock in share mode`
    `begin`
    `select stock_count from seckill_goods where id = #{goodsId} for update`
    `update seckill_goods set stock_count = stock_count - 1`
    `commit`  
4. 
    mysql乐观锁：总是认为不会产生并发问题，每次去取数据的时候总认为不会有其他线程对数据进行修改，因此不会上锁，但是在更新时会判断其他线程在这之前
    有没有对数据进行修改，使用版本号机制或CAS操作实现。
    
  
----------------------------------  
## 秒杀系统特点
- 高性能：支持大量的并发读写
- 一致性：有限数量的商品在同一时刻被很多倍的请求来减库存，要保证数据的准确性
- 高可用：秒杀时一瞬间会涌入大量流量，要避免系统宕机
## 优化
- 后端优化：将请求尽量拦截在系统上游
    - 限流：只允许少部分的流量走到后端。  
        实现方案：
        1. `Guava RateLimiter` (这里用的Guava RateLimiter)  
        2. redis计数限流 [参考](https://github.com/TaXueWWL/shield-ratelimter)
    - 削峰：避免瞬时流量压垮系统,因此延缓用户请求，让落到数据库的请求尽量少
        - 思路：缓存瞬时流量，让服务器资源平缓处理请求。
        - 方案：
            1. 用消息队列，把同步的直接调用转成异步的间接推送，中间通过一个队列承接瞬时的流量洪峰,这里用的`RabbitMQ`
            2. 让用户答题，比如这里的数学公式，一方面**防止用户作弊**，同时可以**延缓请求**，如果做一些图片题目，要把图片推送到CDN上，防止
                加载慢，影响体验。
            3. 分层过滤。比如请求经过CDN->前台读系统->后台写系统->数据库，那么CDN这层就可以过滤掉大量的图片，静态资源的请求。前台读系统对读不做强一致性要求，
                防止一致性校验产生瓶颈的问题。最后在写数据时要保证强一致性，
    - 异步：将同步请求转为异步请求，也即削峰处理。
    - 缓存：创建订单时，会先判断库存，可将商品信息放在缓存中，减少数据库的访问。
    - 负载均衡：利用Nginx等使用多个服务器并发处理请求，减少单个服务器压力。
- 前端优化：
    - 让用户答题，一方面**防止用户作弊**，同时可以**延缓请求**
    - 禁止重复提交，每发起一次秒杀后，需等待一定时间
    - 用户秒杀到商品后，将提交按钮置灰
    - 将前端静态数据直接放缓从离用户最近的地方，比如用户浏览器、CDN
- 防作弊优化：
    - 隐藏秒杀接口，防止恶意用户刷接口
    - 一个账号，一次性发送多个请求：用户通过浏览器插件或其他工具一次性发送多个请求。
        - 解决方案：在程序入口处，一个账号只允许接受1个请求，其他请求过滤。不仅解决了同一个账号，发送N个请求的问题，还保证了后续的逻辑流程的安全。
        - 实现方案：可以通过Redis这种内存缓存服务，写入一个标志位（只允许1个请求写成功，结合watch的乐观锁的特性），成功写入的则可以继续参加。
    - 多个账号一次性发送多个请求：比如微博上的僵尸账号。
        - 解决方案：如果同一IP地址请求频率过高，可以弹出验证码或者封IP
    - 检测账号活跃度或等级信息，对僵尸用户进行限制
----------------------------------- 
## 一致性问题
### Cache Aside Pattern
- 实现起来比较简单，但是需要维护两个数据存储，一个是缓存，一个是数据库。
- 读操作从缓存中取数据，取到后返回，如果没有取到，从数据库中取，成功后，把数据放到缓存中。
- 写操作先更新数据库，再让缓存失效。
- 误区：
    1. 写操作先删缓存再更新数据库(防止并发读写)：线程A写操作删除缓存-->线程B读操作从缓存中读，没有取到，从数据库中读->线程B更新缓存-->线程A更新数据库-->结束  
        此时数据库和缓存中数据不一致，应用程序读取的是脏数据。
    2. 写操作更新数据库后更新缓存(防止两个并发写)：线程A写操作更新数据库->线程B写操作更新数据库->线程B更新缓存->线程A更新缓存->结束  
        此时数据库和缓存中数据不一致，应用程序读取的是脏数据。
    3. 写操作更新数据库后删除缓存(防止读中包含写)：线程A读缓存，取不到去读数据库->线程B写操作更新数据库->线程B删除缓存->线程A将读到的数据更新回了缓存->结束  
        此时数据库和缓存中的数据不一致应用程序读取的是脏数据。  
        但这种情况是读-写-写-读，实际数据库的写操作会比读操作慢得多，所以这种情况概率不大。但是为了避免这种极端情况造成脏数据所产生的影响，我们还是要为缓存设置过期时间。  
        另外，由于每次都删除缓存，因此导致多次缓存都不能命中，对性能有一定影响。
### Read/Write Through
- 只需要维护一个数据存储（缓存），但是实现起来要复杂一些，数据持久化操作是同步的。
- Read Through：读操作从缓存中取数据，如果取到直接返回，如果没有取到，就从数据库中取数据到缓存中，再将数据返回
- Write Through：写操作更新数据库时，命中缓存则更新缓存，再由缓存更新数据库；没有命中缓存，直接更新数据库
### Write Behind Caching
- 更新数据时，只更新缓存，不更新数据库，然后由缓存异步批量更新数据库。  
    好处：直接操作内存，速度快；异步操作，还可以合并对同一个数据的多次操作到数据库，提高性能。  
    问题：数据不是强一致性的。
    
    