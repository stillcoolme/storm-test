package com.stillcoolme.drpc.nolinear.basic;

import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;

public abstract class AbstractLastBaseBasicBolt extends BaseBasicBolt {
    public abstract Fields getLastDeclareFields();

}
