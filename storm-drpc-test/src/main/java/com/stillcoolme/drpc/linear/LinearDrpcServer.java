package com.stillcoolme.drpc.linear;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.LocalDRPC;
import org.apache.storm.StormSubmitter;
import org.apache.storm.drpc.DRPCSpout;
import org.apache.storm.drpc.LinearDRPCTopologyBuilder;
import org.apache.storm.drpc.PrepareRequest;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.shade.com.google.common.collect.Lists;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.tuple.Fields;

import java.util.HashMap;
import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2019/8/23 16:34
 * @description:
 **/
public class LinearDrpcServer {

    public static final String NIMBUS_HOST = "10.2.5.2";
    public static final Integer DRPC_PORT = 3772;
    // 客户端通过这个 drpc server 名 来请求 drpc 服务
    public static final String DRPC_SERVER_NAME = "rpcServer";

    public void initRemoteDrpc(String name) {
        LinearDRPCTopologyBuilder linearBuilder = new LinearDRPCTopologyBuilder(DRPC_SERVER_NAME);

        MyBolt myBolt = new MyBolt();
        linearBuilder.addBolt(myBolt, 3);

        Config conf = new Config();
        conf.setDebug(false);
        conf.setNumWorkers(2);
        List<String> hosts = Lists.newArrayList();
        hosts.add(NIMBUS_HOST);
        conf.put(Config.NIMBUS_SEEDS, hosts);
        try {
            // name 是这个topo的名字，显示在stormUI页面
            StormSubmitter.submitTopology(name, conf, linearBuilder.createRemoteTopology());
        } catch (AlreadyAliveException e) {
            e.printStackTrace();
        } catch (InvalidTopologyException e) {
            e.printStackTrace();
        } catch (AuthorizationException e) {
            e.printStackTrace();
        }
    }

    public void initLocalDrpc() {
        LinearDRPCTopologyBuilder builder =
                new LinearDRPCTopologyBuilder(LinearDrpcServer.DRPC_SERVER_NAME);
        builder.addBolt(new MyBolt(), 3);

        LocalDRPC drpc = new LocalDRPC();
        LocalCluster cluster = new LocalCluster();
        Config conf = new Config();
        cluster.submitTopology("localDRPC",
                conf, builder.createLocalTopology(drpc));

        // localdrpc 要 LocalDRPC 实例来调用，需要debug来看结果
        for (int i = 0; i < 100; i++) {
            String execute = drpc.execute(LinearDrpcServer.DRPC_SERVER_NAME, "teststring");
            System.err.println("成功!!! "+execute);
        }

//        cluster.shutdown();
//        drpc.shutdown();

    }

    public static void main(String[] args) {
        LinearDrpcServer linearDrpcServer = new LinearDrpcServer();
        if(args != null && args.length > 0) {
            linearDrpcServer.initRemoteDrpc(args[0]);
        } else {
            // 启动本地集群
            linearDrpcServer.initLocalDrpc();
        }
    }
}
