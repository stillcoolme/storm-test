package com.stillcoolme.bolt;

import com.grgbanking.algo.face.FaceImageHandler;
import com.grgbanking.algo.face.FaceImageHandlerFactory;
import com.grgbanking.algo.face.bean.FaceSearchResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.List;
import java.util.Map;

/**
 * @author: stillcoolme
 * @date: 2019/8/27 13:40
 * @description:
 **/
public class FaceAlgoBolt extends BaseRichBolt {
    private static Logger logger = LogManager.getLogger(FaceAlgoBolt.class);
    private FaceImageHandler faceImageHandler;
    private OutputCollector collector;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        faceImageHandler = FaceImageHandlerFactory.getInstance();
        logger.info("===taskid : [{}], handler: [{}]", context.getThisTaskId(), faceImageHandler.toString());
    }

    @Override
    public void execute(Tuple input) {
        String feature = "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";
        List<FaceSearchResult> results = null;
        try {
            results = faceImageHandler.searchImagesInLocalDB("archive_db", feature, 10, 0.79f);
            // System.out.println("result: " + results.get(0).getFeatureKey());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Integer a = 1;
        collector.emit(new Values(a));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("num"));
    }
}
