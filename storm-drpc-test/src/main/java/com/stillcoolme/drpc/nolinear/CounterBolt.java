package com.stillcoolme.drpc.nolinear;

import com.alibaba.fastjson.JSONObject;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.datastax.oss.driver.api.querybuilder.update.Update;
import com.stillcoolme.drpc.nolinear.basic.AbstractLastBaseRichBolt;
import com.stillcoolme.drpc.nolinear.cassandra.CassandraUtil;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.*;

/**
 * @author: stillcoolme
 * @date: 2019/11/29 15:30
 * @description:
 *   接受 FirstDRPCBolt的 获取自增id的请求，将 Cassandra表中的 Counter自增，再返回自增id给FirstDRPCBolt
 */
public class CounterBolt extends AbstractLastBaseRichBolt {
    private static final Logger logger = LoggerFactory.getLogger(CounterBolt.class);
    public static final String COUNT_BOLT_NAME = "CounterBolt";
    public static final String COUNT_BOLT_2_FIRST_BOLT_STREAM_ID = "CounterBolt2FirstBoltStreamId";

    private OutputCollector collector;

    // 获取到 counter 表连接
    private static CqlSession cqlSession = CassandraUtil.getCqlSession();
    //
    private static ReentrantLock lock = new ReentrantLock();
    //
    private static AtomicLong counter = new AtomicLong();

    private static SimpleStatement selectBuild;
    private static PreparedStatement updateStatement;


    @Override
    public Fields getLastDeclareFields() {
        return new Fields("id", "counter_id");
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        Select selectQuery = selectFrom("ks_facedb", "ks_face_counter")
                .columns("table_name", "id");

        Update updateQuery = update("ks_facedb", "ks_face_counter")
                .increment("id")
                .whereColumn("table_name").isEqualTo(bindMarker());
        updateStatement = cqlSession.prepare(updateQuery.build());

        selectBuild = selectQuery.build();
    }

    @Override
    public void execute(Tuple tuple) {

        String request = tuple.getStringByField("data");
        Object tupleId = tuple.getValue(0);

        JSONObject jsonObject = JSONObject.parseObject(request);
        jsonObject.put("method", "addWithId");

        String tableName = Optional.ofNullable(jsonObject.getString("dbName")).orElseThrow(() -> new IllegalArgumentException("参数[dbName]不能为空."));
        tableName = "test1";
        Row row = null;
        lock.lock();
        try {
            // cassandra 设置最新的 counter
            BoundStatement bound = updateStatement.bind(tableName);
            ResultSet updateResult = cqlSession.execute(bound);

            // cassandra 查询最新的 counter
            ResultSet set = cqlSession.execute(selectBuild);
            row = set.one();
            logger.info(" [{}] 最新id: [{}]", row.getString(0), row.getLong(1));

            jsonObject.put("id", String.valueOf(row.getLong(1)));
        } catch (Exception e) {

        } finally {
            lock.unlock();
        }
        collector.emitDirect(tuple.getSourceTask(), COUNT_BOLT_2_FIRST_BOLT_STREAM_ID, new Values(tupleId, jsonObject.toJSONString()));

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // 分送 获取自增ID的请求 到 CounterBolt
        declarer.declareStream(COUNT_BOLT_2_FIRST_BOLT_STREAM_ID, new Fields("id", "args"));
    }
}
