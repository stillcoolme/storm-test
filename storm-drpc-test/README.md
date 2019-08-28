## storm的drpc特性测试模块

### drpc.linear
使用storm自带的linearDRPC

打包前将storm-core设为```<scope>provided</scope>```，打成fatjar，提交到storm集群中执行
```
/usr/local/storm/bin/storm jar ./storm-drpc-test-1.0-SNAPSHOT-jar-with-dependencies.jar com.stillcoolme.drpc.linear.LinearDrpcServer test
```

然后在本地测试client端，将storm-core的```<scope>provided</scope>```去掉，然后执行LinearDrpcClient的main方法请求DRPC服务端。

### drpc.nolinear
自定义drpc实现
