package com.stillcoolme.all;

import org.apache.storm.Config;
import org.apache.storm.Constants;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.Map;

/**
 * 使用Storm实现积累求和的操作
 */
public class ClusterSumAllGroupingStormTopology {

    /**
     * Spout需要继承BaseRichSpout，数据源产生数据并发射
     */
    public static class DataSourceSpout extends BaseRichSpout {
        private SpoutOutputCollector collector;
        /**
         * 初始化方法，只会被调用一次
         * @param conf  配置参数
         * @param context  上下文
         * @param collector 数据发射器
         */
        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
            this.collector = collector;
            System.out.println("taskid: "+ context.getThisTaskId()+ " this    "+this);
        }
        int number = 0;

        /**
         * 产生数据，在生产上肯定是从消息队列中不断获取数据； 这个方法会一直不停的执行
         */
        public void nextTuple() {
            // 与 declarer.declare 配合使用，发送不指定流
//            this.collector.emit(new Values(++number));

            // 与 declarer.declareStream 配合使用发送到特定的流
            this.collector.emit("streamingid",new Values(++number,"hello"));
            System.out.println("Spout: " + number);
            // 防止数据产生太快
            Utils.sleep(1000);

        }

        /**
         * 声明输出字段
         * @param declarer
         */
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
//            declarer.declare(new Fields("num", "name"));
            declarer.declareStream("streamingid", new Fields("num","name"));
        }
    }

    /**
     * 数据的累积求和Bolt：接收数据并处理
     */
    public static class SumBolt extends BaseRichBolt {
        // 继承 BaseRichBolt 需要自己定义collector，然后才能往下个bolt发送消息
        OutputCollector collector;
        /**
         * 初始化方法，会被执行一次
         * @param stormConf
         * @param context
         * @param collector
         */
        public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
            this.collector = collector;
            System.out.println("SumBolt prepare:   "+ this);
        }

        int sum = 0;

        /**
         * 其实也是一个死循环，职责：获取Spout发送过来的数据
         * @param input
         */
        public void execute(Tuple input) {
            if(input.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID)
                    && input.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID)){
                System.out.println("SumBolt input Tuple  " + input);
            }else{
                // Bolt中获取值可以根据index获取，也可以根据上一个环节中定义的field的名称获取(建议使用该方式)
                Integer value = input.getIntegerByField("num");
                sum += value;
                String name = input.getStringByField("name");
//            System.out.println(name);
                System.out.println("Bolt: sum = [" + sum + "]");
                System.out.println("Thread id: " + Thread.currentThread().getId() + " , rece data is : " + value+"   ,"+this);

            }
        }

        @Override
        public void cleanup() {
            super.cleanup();
            System.out.println("释放资源");
        }

        public void declareOutputFields(OutputFieldsDeclarer declarer) {

        }
    }

    public static void main(String[] args) throws Exception{

        // TopologyBuilder根据Spout和Bolt来构建出Topology
        // Storm中任何一个作业都是通过Topology的方式进行提交的
        // Topology中需要指定Spout和Bolt的执行顺序
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("DataSourceSpout", new DataSourceSpout());
        // 开3个并行度（3个线程），3个task实例NumTasks抢3个线程来执行
        builder.setBolt("SumBolt", new SumBolt(), 3).setNumTasks(3)
                .allGrouping("DataSourceSpout","streamingid");

        Config config = new Config();
        config.setDebug(false);
        /**
         * 代码提交到Storm集群上运行，或者 本地Storm集群：本地模式运行，不需要搭建Storm集群，通过arg来判断
         */
        if(args != null && args.length > 0) {
            String topoName = ClusterSumAllGroupingStormTopology.class.getSimpleName();
            // 开多少个worker就多少个jvm
            config.setNumWorkers(3);
            try {
                StormSubmitter.submitTopology(topoName, config, builder.createTopology());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            config.setNumWorkers(1);
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("LocalSumStormAckerTopology",config ,
                    builder.createTopology());
        }

//        Thread.sleep(30000);
//        cluster.shutdown();
    }
}
