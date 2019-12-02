package com.stillcoolme.drpc.nolinear.basic;

import org.apache.storm.tuple.Fields;

public class LastDRPCBoltInfo {

    /**
     * 最后一个 component(bolt) id
     */
    private String componentId;

    /**
     * 此bolt declare的字段
     * (由于是最终结果聚合，故只能有一个declare字段)
     */
    private Fields declareFields;

    public LastDRPCBoltInfo() {
    }

    public LastDRPCBoltInfo(String componentId, Fields declareFields) {
        this.componentId = componentId;
        this.declareFields = declareFields;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public Fields getDeclareFields() {
        return declareFields;
    }

    public void setDeclareFields(Fields declareFields) {
        this.declareFields = declareFields;
    }
}
