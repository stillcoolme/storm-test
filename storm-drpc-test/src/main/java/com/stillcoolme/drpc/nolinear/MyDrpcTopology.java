package com.stillcoolme.drpc.nolinear;

import com.stillcoolme.drpc.nolinear.basic.LastDRPCBoltInfo;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.LocalDRPC;
import org.apache.storm.StormSubmitter;
import org.apache.storm.drpc.DRPCSpout;
import org.apache.storm.drpc.JoinResult;
import org.apache.storm.drpc.PrepareRequest;
import org.apache.storm.drpc.ReturnResults;
import org.apache.storm.shade.com.google.common.collect.Lists;
import org.apache.storm.topology.BoltDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.utils.Utils;

import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2019/8/26 9:20
 * @description:
 **/
public class MyDrpcTopology {
    public static String TOPONAME = "counterTest";
    private TopologyBuilder topologyBuilder;
    private static boolean isLocalMode = false;
    private LocalDRPC drpc = new LocalDRPC();

    public MyDrpcTopology() {
        this.topologyBuilder = new TopologyBuilder();
    }


    public void initDrpcTopo() {

        preCreate(topologyBuilder);
        List<LastDRPCBoltInfo> lastDRPCBoltInfos = doCreate(topologyBuilder);
        postCreate(topologyBuilder, lastDRPCBoltInfos);

    }

    private void postCreate(TopologyBuilder builder, List<LastDRPCBoltInfo> lastDRPCBoltInfoList) {
        BoltDeclarer joinBoltDeclarer = builder.setBolt("join-result", new JoinResult("prepare-request"));

        // 将每个分支的最后Bolt数据流， 汇入JoinResult(Bolt)
        for (LastDRPCBoltInfo lastDRPCBoltInfo : lastDRPCBoltInfoList) {
            joinBoltDeclarer.fieldsGrouping(lastDRPCBoltInfo.getComponentId(), lastDRPCBoltInfo.getDeclareFields());
        }

        // joinResult订阅 PrepareRequest(Bolt) 的"ret"数据流，以此做drpc消息的join处理
        joinBoltDeclarer.fieldsGrouping("prepare-request", "ret", new Fields(new String[]{"request"}));

        // ReturnResults, 订阅 JoinResult 的数据流， 通过thrift返回给drpc client
        builder.setBolt("return-result", new ReturnResults())
                .shuffleGrouping("join-result");
    }

    private void preCreate(TopologyBuilder topologyBuilder) {
        final String SPOUT_ID = "drpc-spout";
        final String PREPARE_ID = "prepare-request";
        DRPCSpout drpcSpout;
        if (isLocalMode) {
            drpcSpout = new DRPCSpout(MyDrpcTopology.TOPONAME, drpc);
        } else {
            drpcSpout = new DRPCSpout(MyDrpcTopology.TOPONAME);
        }
        topologyBuilder.setSpout(SPOUT_ID, drpcSpout);


        topologyBuilder.setBolt(PREPARE_ID, new PrepareRequest())
                .shuffleGrouping(SPOUT_ID);
    }

    private List<LastDRPCBoltInfo> doCreate(TopologyBuilder topologyBuilder) {

        List<LastDRPCBoltInfo> lastDRPCBoltInfoList = Lists.newArrayList();

        topologyBuilder.setSpout(DataGeneSpout.DATA_SPOUT_NAME, new DataGeneSpout(), 1);

        // 第一个自定义的bolt，与名为"prepare-reuqest"的bolt通过noneGrouping对接
        FirstDRPCBolt firstDRPCBolt = new FirstDRPCBolt();
        topologyBuilder.setBolt(FirstDRPCBolt.FIRST_BOLT_NAME, firstDRPCBolt, 3)
                // 接收测试数据
                .shuffleGrouping(DataGeneSpout.DATA_SPOUT_NAME, PrepareRequest.ARGS_STREAM)
                // 接收drpc客户端请求数据
                .shuffleGrouping("prepare-request", PrepareRequest.ARGS_STREAM)
                // 接收 CounterBolt 返回的自增id
                .directGrouping(CounterBolt.COUNT_BOLT_NAME, CounterBolt.COUNT_BOLT_2_FIRST_BOLT_STREAM_ID);


        lastDRPCBoltInfoList.add(new LastDRPCBoltInfo(FirstDRPCBolt.FIRST_BOLT_NAME,
                firstDRPCBolt.getLastDeclareFields()));


        CounterBolt counterBolt = new CounterBolt();
        topologyBuilder.setBolt(CounterBolt.COUNT_BOLT_NAME, counterBolt, 1)
                // 接收 FirstDRPCBolt 的获取自增id的请求
                .shuffleGrouping(FirstDRPCBolt.FIRST_BOLT_NAME, FirstDRPCBolt.TO_COUNTER_STREAM);

        return lastDRPCBoltInfoList;
    }

    private void runDrpcTopo() {
        Config conf = new Config();
        conf.setMessageTimeoutSecs(15);
        conf.put(Config.DRPC_REQUEST_TIMEOUT_SECS, 40);
        conf.put(Config.WORKER_HEAP_MEMORY_MB, 2 * 1024);
        if (!isLocalMode) {
            // 远程提交
            try {
                List<String> hosts = Lists.newArrayList();
                String nimbusHosts = "10.2.5.2";
                hosts.add(nimbusHosts);
                conf.put(Config.NIMBUS_SEEDS, hosts);
                conf.setNumWorkers(1);
                StormSubmitter.submitTopology(TOPONAME, conf, topologyBuilder.createTopology());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            conf.putAll(Utils.readDefaultConfig());
            conf.setNumWorkers(Integer.valueOf(2));
            // 本地测试
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology(TOPONAME, conf,
                    topologyBuilder.createTopology());

            String result = null;
            try {
                //Thread.sleep(20 * 1000);
                result = drpc.execute(TOPONAME, LocalDRPCRequest.ADD_FEATURE_2_LOCAL_DB);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println(">>>>>>>result:" + result);
            cluster.shutdown();
            drpc.shutdown();
        }
    }

    public static void main(String[] args) {
        MyDrpcTopology myDrpcServer = new MyDrpcTopology();
        if (args == null || args.length == 0) {
            isLocalMode = true;
        }
        myDrpcServer.initDrpcTopo();
        myDrpcServer.runDrpcTopo();
    }


}
