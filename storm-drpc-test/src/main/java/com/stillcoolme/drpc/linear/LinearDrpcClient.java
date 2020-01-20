package com.stillcoolme.drpc.linear;

import org.apache.storm.Config;
import org.apache.storm.thrift.TException;
import org.apache.storm.thrift.transport.TTransportException;
import org.apache.storm.utils.DRPCClient;
import org.apache.storm.utils.Utils;

import java.util.Map;

/**
 * @author: stillcoolme
 * @date: 2019/8/26 9:17
 * @description:
 **/
public class LinearDrpcClient {

    public static DRPCClient initClient() {
        String drpcServer = LinearDrpcServer.NIMBUS_HOST;
        int drpcPort = LinearDrpcServer.DRPC_PORT;
        Config config = new Config();
        Map defaultConfig = Utils.readDefaultConfig();
        config.putAll(defaultConfig);
        try {
            return new DRPCClient(config, drpcServer, drpcPort,20000);
        } catch (TTransportException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        DRPCClient client = initClient();
        try {
            String response = client.execute(LinearDrpcServer.DRPC_SERVER_NAME, "{id: 12}");
            System.out.println(response);
        } catch (TException e) {
            e.printStackTrace();
        }
    }
}
