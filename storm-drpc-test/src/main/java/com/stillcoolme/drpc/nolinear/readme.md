## 业务逻辑

在分布式的storm作业中，在插入数据时，使用一个Bolt来生成全局的自增id

1. FirstDRPCBolt 接收 插入数据的请求；
2. FirstDRPCBolt 发送获取自增id的请求给 CounterBolt
3. CounterBolt 通过 Cassandra 的 Counter字段将自增id加1，然后将新的自增id返回给 FirstDRPCBolt；（Cassandra 用 <tableName, Counter> 记录每个 table 的 id
4. FirstDRPCBolt将数据插入数据库。

## 性能
1. Cassandra 的 Counter字段自增后不返回新的id，需要自己再查询一次，需要加锁；
2. CounterBolt 的 task 全局只能一个，FirstDRPCBolt 的请求过来都在等待锁；

所以导致性能差，一次请求需要20ms，并发的情况更差。