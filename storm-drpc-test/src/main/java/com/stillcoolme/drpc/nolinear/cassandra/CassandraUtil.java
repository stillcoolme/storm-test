package com.stillcoolme.drpc.nolinear.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.InsertInto;
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.datastax.oss.driver.api.querybuilder.term.Term;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletionStage;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal;

/**
 * @author ChenJiaHao
 * Date: 2019-11-25
 * <p>
 * Cassandra工具类
 *
 */
public class CassandraUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static CqlSession getCqlSession() {
        return Holder.cqlSession;
    }

    /**
     * 单例模式
     */
    private static class Holder {
        private static final CqlSession cqlSession;

        static {
            cqlSession = CqlSession.builder()
                    // TODO 待修改 通过配置读取
                    .addContactPoint(new InetSocketAddress("10.2.5.5", 9042))
                    .withLocalDatacenter("dc1")
                    .withKeyspace("ks_facedb")
                    .build();
        }
    }

    /**
     * 关闭
     * (可加入Shutdown Hook)
     */
    public static void closeSession() {
        if (Holder.cqlSession != null) {
            Holder.cqlSession.close();
        }

    }

    // 执行⬇--------------------------------------------------
    /**
     * 执行语句
     *
     * @param query
     * @return
     */
    public static ResultSet execute(String query) {
//        long startTime = System.currentTimeMillis();
        System.out.println("Cassandra Query:" + query);
        ResultSet rs = Holder.cqlSession.execute(query);
//        System.out.println("execute query 耗时:" + (System.currentTimeMillis() - startTime) + "ms");

        return rs;
    }

    /**
     * 执行语句
     *
     * @param statement
     * @return
     */
    public static ResultSet execute(Statement statement) {
//        long startTime = System.currentTimeMillis();

        ResultSet rs = Holder.cqlSession.execute(statement);
//        System.out.println("execute session 耗时:" + (System.currentTimeMillis() - startTime) + "ms");
        return rs;
    }

    /**
     * 执行语句, 并返回第一行
     *
     * @param query
     * @return
     */
    public static Row executeAndGetOne(String query) {
        ResultSet rs = Holder.cqlSession.execute(query);
        return rs.one();
    }

    /**
     * 执行语句, 并返回第一行
     *
     * @param simpleStatement
     * @return
     */
    public static Row executeAndGetOne(SimpleStatement simpleStatement) {
        ResultSet rs = Holder.cqlSession.execute(simpleStatement);
        return rs.one();
    }


    /**
     * 批量执行语句
     *
     * @param simpleStatementList
     * @return
     */
    public static ResultSet executeBatch(List<SimpleStatement> simpleStatementList) {
        BatchStatementBuilder builder = BatchStatement.builder(DefaultBatchType.UNLOGGED);
        for (SimpleStatement ss : simpleStatementList) {
            builder.addStatement(ss);
        }

        BatchStatement batchStatement = builder.build();

        return execute(batchStatement);
    }

    //TODO 异步执行
    public static CompletionStage<AsyncResultSet> executeAsync(String query) {
        return Holder.cqlSession.executeAsync(query);
    }

    // count⬇--------------------------------------------------
    /**
     * 查询count
     *
     * @param tableName
     * @return
     */
    public static long count(String tableName) {
        SimpleStatement simpleStatement = QueryBuilder.selectFrom(tableName).countAll().build();
        Row row = executeAndGetOne(simpleStatement);
        return row.getLong(0);
    }

    // 辅助方法⬇--------------------------------------------------
    /**
     * 将Object转换成Map, 用于insert
     *
     * @param obj
     * @return
     */
    public static Map<String, Term> convertObjectToMapTerm(Object obj) {
        // 将obj解析成Map
        Map<String, Object> map = OBJECT_MAPPER.convertValue(obj, Map.class);

        // 参数封装, key加上引号, value采QueryBuilder.literal封装
        Map<String, Term> params = new HashMap<>();
        map.forEach((k, v) -> {
            if (v != null) {
                // key代表字段名, 用双引号包裹, 避免字段名cql关键字冲突(如:"desc")
                if (v instanceof byte[]) {
                    // byte[] 类型包裹
                    params.put("\"" + k + "\"", literal(ByteBuffer.wrap((byte[]) v)));
                } else {
                    params.put("\"" + k + "\"", literal(v));
                }
            }
        });

        return params;
    }

    // insert⬇--------------------------------------------------
    /**
     * 普通插入数据, 返回插入结果
     *
     * @param tableName  表名
     * @param params 参数
     * @return
     */
    public static ResultSet regularInsertInto(String tableName, Map<String, Term> params) {
        SimpleStatement simpleStatement = getStatmentOfregularInsertInto(tableName, params);
//        simpleStatement.setKeyspace(keyspace); // 仅Cassandra Protocol V4以上可用

        ResultSet rs = execute(simpleStatement);

        return rs;
    }

    /**
     * 普通插入数据, 返回SimpleStatement
     *
     * @param tableName
     * @param params
     * @return
     */
    public static SimpleStatement getStatmentOfregularInsertInto(String tableName, Map<String, Term> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }

        InsertInto insertInto = QueryBuilder.insertInto(tableName);

        RegularInsert regularInsert = null;
        Iterator<Map.Entry<String, Term>> iterator = params.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Term> next = iterator.next();

            if (regularInsert == null) {
                regularInsert = insertInto.value(next.getKey(), next.getValue());
            } else {
                regularInsert = regularInsert.value(next.getKey(), next.getValue());
            }
        }

        return regularInsert.build();
    }

    // select⬇--------------------------------------------------
    /**
     * 普通select
     *
     * @param tableName   表名
     * @param columns 列名
     * @return
     */
    public static ResultSet select(String tableName, Set<String> columns) {
        if (tableName == null || tableName.isEmpty()) {
            throw new RuntimeException("table名 不能为空");
        }

        if (columns == null || columns.isEmpty()) {
            throw new RuntimeException("param 不能为空");
        }

        Select select = QueryBuilder.selectFrom(tableName)
                .columns(columns);

        SimpleStatement simpleStatement = select.build();

        ResultSet rs = execute(simpleStatement);
        return rs;
    }

}
