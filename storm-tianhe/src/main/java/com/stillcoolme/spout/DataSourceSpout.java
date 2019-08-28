package com.stillcoolme.spout;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.*;

/**
 * @author: stillcoolme
 * @date: 2019/8/27 15:21
 * @description:
 **/
public class DataSourceSpout extends BaseRichSpout {
    private SpoutOutputCollector collector;
    private Random random = new Random();
    List<Integer> lsit = new ArrayList(Arrays.asList(new Integer[]{1000, 2000, 3000}));
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
        System.out.println("taskid: "+ context.getThisTaskId());
    }

    int number = 0;

    public void nextTuple() {
        for (int i = 0; i < lsit.get(random.nextInt(3)); i++) {
            this.collector.emit(new Values(++number));
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 声明输出字段
     * @param declarer
     */
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("num"));
    }
}
