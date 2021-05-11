package net.cryptic_game.server.influxdb;

import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.WriteApi;
import net.cryptic_game.server.config.Config;
import net.cryptic_game.server.config.DefaultConfig;

public class InfluxDBService {

    private static final InfluxDBService INSTANCE = new InfluxDBService();

    private final InfluxDBClient influxDBClient;
    private final WriteApi writeApi;

    private InfluxDBService() {
        this.influxDBClient = InfluxDBClientFactory.create(
                InfluxDBClientOptions.builder()
                        .url(Config.get(DefaultConfig.INFLUXDB_HOST))
                        .org(Config.get(DefaultConfig.INFLUXDB_ORG))
                        .authenticateToken(Config.get(DefaultConfig.INFLUXDB_TOKEN).toCharArray())
                        .bucket(Config.get(DefaultConfig.INFLUXDB_BUCKET))
                        .build()
        ).setLogLevel(LogLevel.NONE);

        this.writeApi = this.influxDBClient.getWriteApi();
    }

    public static InfluxDBService getInstance() {
        return InfluxDBService.INSTANCE;
    }

    public WriteApi getWriteApi() {
        return this.writeApi;
    }
}
