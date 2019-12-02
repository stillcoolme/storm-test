package com.stillcoolme.drpc.nolinear.basic;

import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;

public abstract class AbstractLastBaseRichBolt extends BaseRichBolt {
    public abstract Fields getLastDeclareFields();

}
