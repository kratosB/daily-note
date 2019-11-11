MySQL中select * for update锁表的问题
由于InnoDB预设是Row-Level Lock，所以只有「明确」的指定主键，MySQL才会执行Row lock (只锁住被选取的资料例) ，否则MySQL将会执行Table Lock (将整个资料表单给锁住)。
举个例子:
假设有个表单products ，里面有id跟name二个栏位，id是主键。
例1: (明确指定主键，并且有此笔资料，row lock)
SELECT * FROM products WHERE id='3' FOR UPDATE;
SELECT * FROM products WHERE id='3' and type=1 FOR UPDATE;

例2: (明确指定主键，若查无此笔资料，无lock)
SELECT * FROM products WHERE id='-1' FOR UPDATE;

例2: (无主键，table lock)
SELECT * FROM products WHERE name='Mouse' FOR UPDATE;

例3: (主键不明确，table lock)
SELECT * FROM products WHERE id<>'3' FOR UPDATE;

例4: (主键不明确，table lock)
SELECT * FROM products WHERE id LIKE '3' FOR UPDATE;

注1: FOR UPDATE仅适用于InnoDB，且必须在交易区块(BEGIN/COMMIT)中才能生效。
注2: 要测试锁定的状况，可以利用MySQL的Command Mode ，开二个视窗来做测试。

在MySql 5.0中测试确实是这样的
另外：MyAsim 只支持表级锁，InnerDB支持行级锁
添加了(行级锁/表级锁)锁的数据不能被其它事务再锁定，也不被其它事务修改（修改、删除）
是表级锁时，不管是否查询到记录，都会锁定表

http://blog.sina.com.cn/s/blog_88d2d8f501011sgl.html



-------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------




最近要做一个新项目，需要借助mysql的行级锁机制，又由于是第一次使用jpa去实现行级锁，所以遇到了一丢丢问题，昨天晚上用了1个多小时解决了。。分享下。。

--------------------------------------------------------------------------------------------------------------------------------------------------

1.这是spring配置文件的内容，相信大多数人也都能从网上search到：

<bean id="hibernateJpaVendorAdapter"
          class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" />
    <bean id="entityManagerFactory"
    class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
<!-- 指定下数据源 -->
        <property name="dataSource" ref="<strong>dataSource</strong>" />
        <property name="jpaVendorAdapter" ref="hibernateJpaVendorAdapter" />
        <!-- 指定Entity实体类包路径 -->
<property name="packagesToScan">
            <array>
                <value>com.xxx.xx.xxx.core.entity</value>
            </array>
        </property>
        <property name="jpaProperties">
            <props>
                <prop key="hibernate.dialect">org.hibernate.dialect.MySQLDialect</prop>
                <prop key="hibernate.ejb.naming_strategy">org.hibernate.cfg.ImprovedNamingStrategy</prop>
                <prop key="hibernate.cache.provider_class">org.hibernate.cache.NoCacheProvider</prop>
                <prop key="hibernate.show_sql">true</prop> <!-- 指是否显示SQL，可以根据需要 -->
                <prop key="hibernate.format_sql">false</prop>
            </props>
        </property>
    </bean>
<!-- 指定下Dao层的包路径-->
    <jpa:repositories base-package="com.xxx.xx.xxx.core.dao"
                      entity-manager-factory-ref="entityManagerFactory"
                      transaction-manager-ref="transactionManagerjpa" />

    <bean id="transactionManagerjpa" class="org.springframework.orm.jpa.JpaTransactionManager">
        <property name="entityManagerFactory" ref="entityManagerFactory" />
    </bean>
    <tx:annotation-driven transaction-manager="transactionManagerjpa"  />




2.简单的贴一个Entity对象和一个Dao

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "job_info")
public class JobInfo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "job_name")
    private String jobName;
    @Column(name = "job_desc")
    private String jobDesc;

.... get/set方法不再赘述
}
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.xxx.xx.core.entity.JobInfo;
@Repository
public interface JobInfoDao extends JpaRepository<JobInfo, Long> {
	@Query(value = "select j from JobInfo j where j.jobName = :jobname ")
	public JobInfo getJobForUpdate(@Param("jobname") String jobname);
	@Lock(value = LockModeType.PESSIMISTIC_WRITE)
	@Query(value = "select j from JobInfo j where j.id = :id ")
	public void getJobByIdForUpdate(@Param("id") Long id);
@Lock(value = LockModeType.PESSIMISTIC_WRITE) // 千万不要用这个哦！！！！！！！
	@Query(value = "select j from JobInfo j where j.jobName = :jobname ")
	public void getJobByNameForUpdate(@Param("jobname") String jobname);
}
3.service层，此为测试代码

import org.springframework.transaction.annotation.Transactional;

public class JobService implements IJobService {
@Autowired
	private JobInfoDao jobInfoDao;
@Transactional // 这个是需要标注的，因为Dao层有for update 的机制，那么这边就要开启事务了，否则会报错的。。。
	public JobInfo getJobForUpdate(Long id) {
		jobInfoDao.getJobByIdForUpdate(id);
		try {
			Thread.sleep(100000);
		} catch (InterruptedException e) {
		}
		return null;
	}
}


4.完成。


当调用JobService中的 getJobByIdForUpdate时，就可以达到行级锁的目的了！



----------------------------------------------------------------------------------------------------------------------------

如果你只是需要通过jpa实现行级锁，那么好，以上的东东，已经够了。但是呢，我实际开发中，并不是那么顺的，现在我来说下我遇到了什么鬼。。。



1.行级锁，大家一定都听到过，也肯定比较喜欢。对于mysql，InnoDB预设的是Row-level Lock，但是，需要明确的指定主键，才会执行行级锁，否则执行的为表锁。

比如：

select * from job_info where id = 1 for update;

那么上面这句，为行级锁。

而 select * from job_info where job_name = 'test' for update;

这句，就变成了表锁了。。。。（我当时泪也流干了，各种查DB引擎，命令行测试，多亏了(http://blog.sina.com.cn/s/blog_88d2d8f501011sgl.html  MySQL中select * for update锁表的问题) 这篇文章）



那么好，现在关于如何才能让mysql执行行级锁的问题解决了。。。



2.jpa如何搞 select for update。

也是醉了，在Dao层的方法上，要配置Lock的注解。并且要加上LockModeType.PESSIMISTIC_WRITE ，这个就相当于for update了。大家也可以在程序运行时，打印出的sql中看到。 这个东东，得益于 (http://suene.iteye.com/blog/1756295  Spring Data JPA,基础学习笔记.) 该文章。



至此呢，终于解决掉了行级锁和jpa注解实现  for update 的问题。。。



注：这里写注呢，是因为前面的demo代码里，也有坑的。。大家应该也能注意到了，在Dao层中，getJobByNameForUpdate 这个方法千万不要用哦！它可是会导致锁表的哦！


https://blog.csdn.net/fengyuxue11011/article/details/47039765


































https://www.cnblogs.com/my_life/articles/7606547.html 锁的资料

InnoDB的行锁模式及加锁方法
InnoDB实现了以下两种类型的行锁。

共享锁（s）：允许一个事务去读一行，阻止其他事务获得相同数据集的排他锁。 --读锁 排他锁（Ｘ）：允许获取排他锁的事务更新数据，阻止其他事务取得相同的数据集共享读锁和排他写锁。 --写锁

另外，为了允许行锁和表锁共存，实现多粒度锁机制，InnoDB还有两种内部使用的意向锁（Intention Locks），这两种意向锁都是表锁。

意向共享锁（IS）：事务打算给数据行共享锁，事务在给一个数据行加共享锁前必须先取得该表的IS锁。

意向排他锁（IX）：事务打算给数据行加排他锁，事务在给一个数据行加排他锁前必须先取得该表的IX锁。

InnoDB行锁模式兼容性列表

当前锁模式/是否兼容/请求锁模式	X	IX	S	IS
X	冲突	冲突	冲突	冲突
IX	冲突	兼容	冲突	兼容
S	冲突	冲突	兼容	兼容
IS	冲突	兼容	兼容	兼容
如果一个事务请求的锁模式与当前的锁兼容，InnoDB就请求的锁授予该事务；反之，如果两者两者不兼容，该事务就要等待锁释放。 意向锁是InnoDB自动加的，不需用户干预。对于UPDATE、DELETE和INSERT语句，InnoDB会自动给涉及及数据集加排他锁（Ｘ）；对于普通SELECT语句，InnoDB会自动给涉及数据集加排他锁（Ｘ）；对于普通SELECT语句，InnoDB不会任何锁；事务可以通过以下语句显示给记录集加共享锁或排锁。

共享锁（Ｓ）：SELECT * FROM table_name WHERE ... LOCK IN SHARE MODE 排他锁（X）：SELECT * FROM table_name WHERE ... FOR UPDATE 用SELECT .. IN SHARE MODE获得共享锁，主要用在需要数据依存关系时确认某行记录是否存在，并确保没有人对这个记录进行UPDATE或者DELETE操作。但是如果当前事务也需要对该记录进行更新操作，则很有可能造成死锁，对于锁定行记录后需要进行更新操作的应用，应该使用SELECT ... FOR UPDATE方式获取排他锁。

InnoDB行锁实现方式 InnoDB行锁是通过索引上的索引项来实现的，这一点ＭySQL与Oracle不同，后者是通过在数据中对相应数据行加锁来实现的。InnoDB这种行锁实现特点意味者：只有通过索引条件检索数据，InnoDB才会使用行级锁，否则，InnoDB将使用表锁！ 在实际应用中，要特别注意InnoDB行锁的这一特性，不然的话，可能导致大量的锁冲突，从而影响并发性能。

什么时候使用表锁
对于InnoDB表，在绝大部分情况下都应该使用行级锁，因为事务和行锁往往是我们之所以选择InnoDB表的理由。但在个另特殊事务中，也可以考虑使用表级锁。

第一种情况是：事务需要更新大部分或全部数据，表又比较大，如果使用默认的行锁，不仅这个事务执行效率低，而且可能造成其他事务长时间锁等待和锁冲突，这种情况下可以考虑使用表锁来提高该事务的执行速度。 第二种情况是：事务涉及多个表，比较复杂，很可能引起死锁，造成大量事务回滚。这种情况也可以考虑一次性锁定事务涉及的表，从而避免死锁、减少数据库因事务回滚带来的开销。 当然，应用中这两种事务不能太多，否则，就应该考虑使用ＭyISAＭ表。 在InnoDB下 ，使用表锁要注意以下两点。 （１）使用LOCK TALBES虽然可以给InnoDB加表级锁，但必须说明的是，表锁不是由InnoDB存储引擎层管理的，而是由其上一层ＭySQL Server负责的，仅当autocommit=0、innodb_table_lock=1（默认设置）时，InnoDB层才能知道MySQL加的表锁，ＭySQL Server才能感知InnoDB加的行锁，这种情况下，InnoDB才能自动识别涉及表级锁的死锁；否则，InnoDB将无法自动检测并处理这种死锁。 （２）在用LOCAK TABLES对InnoDB锁时要注意，要将AUTOCOMMIT设为0，否则ＭySQL不会给表加锁；事务结束前，不要用UNLOCAK TABLES释放表锁，因为UNLOCK TABLES会隐含地提交事务；COMMIT或ROLLBACK产不能释放用LOCAK TABLES加的表级锁，必须用UNLOCK TABLES释放表锁，正确的方式见如下语句。 例如，如果需要写表t1并从表t读，可以按如下做： SET AUTOCOMMIT=0; LOCAK TABLES t1 WRITE, t2 READ, ...; [do something with tables t1 and here]; COMMIT; UNLOCK TABLES;

关于死锁
ＭyISAM表锁是deadlock free的，这是因为ＭyISAM总是一次性获得所需的全部锁，要么全部满足，要么等待，因此不会出现死锁。但是在InnoDB中，除单个SQL组成的事务外，锁是逐步获得的，这就决定了InnoDB发生死锁是可能的。 发生死锁后，InnoDB一般都能自动检测到，并使一个事务释放锁并退回，另一个事务获得锁，继续完成事务。但在涉及外部锁，或涉及锁的情况下，InnoDB并不能完全自动检测到死锁，这需要通过设置锁等待超时参数innodb_lock_wait_timeout来解决。需要说明的是，这个参数并不是只用来解决死锁问题，在并发访问比较高的情况下，如果大量事务因无法立即获取所需的锁而挂起，会占用大量计算机资源，造成严重性能问题，甚至拖垮数据库。我们通过设置合适的锁等待超时阈值，可以避免这种情况发生。 通常来说，死锁都是应用设计的问题，通过调整业务流程、数据库对象设计、事务大小、以及访问数据库的SQL语句，绝大部分都可以避免。下面就通过实例来介绍几种死锁的常用方法。

（１）在应用中，如果不同的程序会并发存取多个表，应尽量约定以相同的顺序为访问表，这样可以大大降低产生死锁的机会。如果两个session访问两个表的顺序不同，发生死锁的机会就非常高！但如果以相同的顺序来访问，死锁就可能避免。

（２）在程序以批量方式处理数据的时候，如果事先对数据排序，保证每个线程按固定的顺序来处理记录，也可以大大降低死锁的可能。

（３）在事务中，如果要更新记录，应该直接申请足够级别的锁，即排他锁，而不应该先申请共享锁，更新时再申请排他锁，甚至死锁。

（４）在REPEATEABLE-READ隔离级别下，如果两个线程同时对相同条件记录用SELECT...ROR UPDATE加排他锁，在没有符合该记录情况下，两个线程都会加锁成功。程序发现记录尚不存在，就试图插入一条新记录，如果两个线程都这么做，就会出现死锁。这种情况下，将隔离级别改成READ COMMITTED，就可以避免问题。

（５）当隔离级别为READ COMMITED时，如果两个线程都先执行SELECT...FOR UPDATE，判断是否存在符合条件的记录，如果没有，就插入记录。此时，只有一个线程能插入成功，另一个线程会出现锁等待，当第１个线程提交后，第２个线程会因主键重出错，但虽然这个线程出错了，却会获得一个排他锁！这时如果有第３个线程又来申请排他锁，也会出现死锁。对于这种情况，可以直接做插入操作，然后再捕获主键重异常，或者在遇到主键重错误时，总是执行ROLLBACK释放获得的排他锁。

尽管通过上面的设计和优化等措施，可以大减少死锁，但死锁很难完全避免。因此，在程序设计中总是捕获并处理死锁异常是一个很好的编程习惯。 如果出现死锁，可以用SHOW INNODB STATUS命令来确定最后一个死锁产生的原因和改进措施。