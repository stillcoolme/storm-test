package com.stillcoolme.drpc.linear;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.drpc.LinearDRPCTopologyBuilder;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.shade.com.google.common.collect.Lists;

import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2019/8/23 16:34
 * @description:
 **/
public class LinearDrpcServer {

    public static final String NIMBUS_HOST = "10.2.5.2";
    public static final Integer DRPC_PORT = 3772;
    // 客户端通过这个 drpc method 名 来请求 drpc 方法
    public static final String DRPC_METHOD_NAME = "remoteMethod";

    public void initRemoteDrpc(String name) {
        LinearDRPCTopologyBuilder linearBuilder = new LinearDRPCTopologyBuilder(DRPC_METHOD_NAME);

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

    public static void main(String[] args) {
        LinearDrpcServer linearDrpcServer = new LinearDrpcServer();
        if(args != null && args.length > 0) {
            linearDrpcServer.initRemoteDrpc(args[0]);
        } else {

        }
    }
}
