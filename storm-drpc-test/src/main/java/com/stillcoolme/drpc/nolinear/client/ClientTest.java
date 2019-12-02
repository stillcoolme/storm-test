package com.stillcoolme.drpc.nolinear.client;

import com.stillcoolme.drpc.nolinear.LocalDRPCRequest;
import com.stillcoolme.drpc.nolinear.MyDrpcTopology;
import org.apache.storm.Config;
import org.apache.storm.thrift.TException;
import org.apache.storm.utils.DRPCClient;
import org.apache.storm.utils.Utils;

import java.util.Map;

/**
 * @author: stillcoolme
 * @date: 2019/12/2 11:21
 * @description: 执行：
 * 0. 先将 本模块打fatjar，main类为 MyDrpcServer，部署到服务端
 *          /usr/local/storm/bin/storm jar ./storm-drpc-test-1.0-SNAPSHOT-jar-with-dependencies.jar com.stillcoolme.drpc.nolinear.MyDrpcTopology 123
 * 1. 将 storm-core 的依赖 <scope>provided</scope> 注释掉
 * 2. 执行该客户端请求
 */
public class ClientTest {

    public static void main(String[] args) throws TException {
        Config config = new Config();
        Map defaultConfig = Utils.readDefaultConfig();
        config.putAll(defaultConfig);
        config.setDebug(true);
        DRPCClient client = new DRPCClient(config, "10.2.5.2", 3772, 30000);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            String result = client.execute(MyDrpcTopology.TOPONAME, LocalDRPCRequest.ADD_FEATURE_2_LOCAL_DB);
            System.out.println(result);
        }
        System.out.println(System.currentTimeMillis() - start + "ms");
    }
}
