package com.stillcoolme.drpc.nolinear;

import com.datastax.oss.protocol.internal.request.Execute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.storm.drpc.PrepareRequest;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: stillcoolme
 * Date: 2019/7/5 15:10
 * Description: 这个类用来测试，不断将数据发送到 FirstDRPCBolt
 */
public class DataGeneSpout extends BaseRichSpout {
    public static final String DATA_SPOUT_NAME = "DATA_SPOUT_NAME";
    private static final Logger logger = LogManager.getLogger(DataGeneSpout.class);
    private SpoutOutputCollector collector;
    private static ExecutorService executeService = Executors.newFixedThreadPool(5);

    private static long start = System.currentTimeMillis();

    public DataGeneSpout() {
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void nextTuple() {
        try {
            // 每20秒，则将测试数据 发送给 FirstBolt
            long now = System.currentTimeMillis();
            if (now - start > 20000) {
                start = now;
                for (int i = 0; i < 5; i++) {
                    executeService.execute(new Runnable() {
                        @Override
                        public void run() {
                            collector.emit(PrepareRequest.ARGS_STREAM, new Values(new Random().nextLong(), LocalDRPCRequest.ADD_FEATURE_2_LOCAL_DB));
                        }
                    });
                }

            }
        } catch (Exception e) {
            logger.error("DataSpout异常 {}", e.getMessage(), e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream(PrepareRequest.ARGS_STREAM, new Fields("request", "args"));
    }
}
