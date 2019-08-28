package com.stillcoolme.drpc.linear;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

/**
 * @author: stillcoolme
 * @date: 2019/8/23 16:52
 * @description:
 **/
public class MyBolt extends BaseBasicBolt {

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        String input_json = tuple.getString(1);
        input_json += input_json + "!!";
        collector.emit(new Values(tuple.getValue(0), input_json));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("id", "output_json"));
    }
}
