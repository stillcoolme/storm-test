package com.stillcoolme.drpc.nolinear;

import com.alibaba.fastjson.JSONObject;
import com.stillcoolme.drpc.nolinear.basic.AbstractLastBaseBasicBolt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.storm.shade.org.apache.commons.lang.time.DateUtils;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.Map;
import java.util.Optional;

/**
 * @author ChenJiaHao
 * Date: 2019-06-22
 * <p>
 * 处理facedDPRC请求方法的bolt
 */
public class FirstDRPCBolt extends AbstractLastBaseBasicBolt {

    public static final String FIRST_BOLT_NAME = "FIRST-BOLT";
    public static final String TO_COUNTER_STREAM = "TO_COUNTER_STREAM";


    @Override
    public Fields getLastDeclareFields() {
        return new Fields("id", "info");
    }

    private static final Logger logger = LogManager.getLogger(FirstDRPCBolt.class);

    long start = 0;

    public FirstDRPCBolt() {
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);

    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        try {
            String request = tuple.getStringByField("args");
            logger.info("first Bolt 接受到请求： [{}]: ", request);
            Object tupleId = tuple.getValue(0);

            JSONObject jsonObject = JSONObject.parseObject(request);
            String method = Optional.ofNullable(jsonObject.getString("method")).orElseThrow(() -> new IllegalArgumentException("参数[method]不能为空."));

            switch (method) {
                case "add":
                    // 检查dbName是否存在。。
                    // 去获取自增ID
                    start = System.currentTimeMillis();

                    collector.emit(TO_COUNTER_STREAM, new Values(tupleId, jsonObject.toJSONString()));
                    break;
                case "addWithId":
                    String id = Optional.ofNullable(jsonObject.getString("id")).orElseThrow(() -> new IllegalArgumentException("参数[dbName]不能为空."));
                    logger.info("获取到自增ID： [{}]", id);

                    logger.info("完成counter时间消耗： [{}], start: [{}]", System.currentTimeMillis() - start, start);
                    // 正常的添加数据流程

                    // 结束添加数据请求
                    collector.emit(Utils.DEFAULT_STREAM_ID, new Values(tupleId, id));
                    break;
                default:
                    throw new IllegalArgumentException("未知的请求参数, method:" + method);
            }
        } catch (Exception e) {
            logger.error("服务端异常，请求失败", e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

        // 发送 获取自增ID的请求 到 CounterBolt
        declarer.declareStream(TO_COUNTER_STREAM, new Fields("id", "data"));

        declarer.declareStream(Utils.DEFAULT_STREAM_ID, getLastDeclareFields());


    }


}
