package com.stillcoolme.drpc.nolinear;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.drpc.DRPCSpout;
import org.apache.storm.drpc.PrepareRequest;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.shade.com.google.common.collect.Lists;
import org.apache.storm.topology.TopologyBuilder;

import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2019/8/26 9:20
 * @description:
 **/
public class MyDrpcServer {

    public static final String NIMBUS_HOST = "10.2.5.2";
    public static final Integer DRPC_PORT = 3772;
    // 客户端通过这个 drpc method 名 来请求 drpc 方法
    public static final String DRPC_METHOD_NAME = "remoteMethod";

    public void initRemoteDrpc(String name) {
        TopologyBuilder topologyBuilder = new TopologyBuilder();

        Config conf = new Config();
        conf.setDebug(false);
        conf.setNumWorkers(2);
        List<String> hosts = Lists.newArrayList();
        hosts.add(NIMBUS_HOST);
        conf.put(Config.NIMBUS_SEEDS, hosts);
        try {

            preCreate(topologyBuilder);


            // name 是这个topo的名字，显示在stormUI页面
            StormSubmitter.submitTopology(name, conf, topologyBuilder.createTopology());
        } catch (AlreadyAliveException e) {
            e.printStackTrace();
        } catch (InvalidTopologyException e) {
            e.printStackTrace();
        } catch (AuthorizationException e) {
            e.printStackTrace();
        }
    }

    private void preCreate(TopologyBuilder topologyBuilder) {
        final String SPOUT_ID = "spout";
        final String PREPARE_ID = "prepare-request";
        DRPCSpout spout = new DRPCSpout(DRPC_METHOD_NAME);

        topologyBuilder.setSpout(SPOUT_ID, spout);
        // 这里 PrepareRequest bolt 接受 spout数据不均匀，要通过修改源码将 DRPCSpout 的emit方式设置为emitDirect
        topologyBuilder.setBolt(PREPARE_ID, new PrepareRequest())
                .shuffleGrouping(SPOUT_ID);
        int i=0;
    }

    public static void main(String[] args) {
        MyDrpcServer myDrpcServer = new MyDrpcServer();
        if(args != null && args.length > 0) {
            myDrpcServer.initRemoteDrpc(args[0]);
        } else {

        }
    }

}
