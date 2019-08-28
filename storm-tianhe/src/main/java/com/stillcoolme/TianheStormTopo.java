package com.stillcoolme;

import com.stillcoolme.bolt.FaceAlgoBolt;
import com.stillcoolme.bolt.ParseDataBolt;
import com.stillcoolme.spout.DataSourceSpout;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;

/**
 * @author: stillcoolme
 * @date: 2019/8/27 15:54
 * @description:
 **/
public class TianheStormTopo {

    public static void main(String[] args) throws Exception {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("data-source", new DataSourceSpout(), 1);
        builder.setBolt("parse-data", new ParseDataBolt(), 164)
            .shuffleGrouping("data-source");
        builder.setBolt("face-algo", new FaceAlgoBolt(), 164)
            .shuffleGrouping("parse-data");

        String name = TianheStormTopo.class.getSimpleName();
        Config config = new Config();
        config.setMessageTimeoutSecs(120);
        config.setStatsSampleRate(1.0d);
        config.setDebug(true);
        config.setNumWorkers(3);

        StormSubmitter.submitTopology(name, config, builder.createTopology());

    }

}
