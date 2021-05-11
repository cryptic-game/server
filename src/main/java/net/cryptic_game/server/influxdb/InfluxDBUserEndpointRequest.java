package net.cryptic_game.server.influxdb;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

import java.time.Instant;
import java.util.Objects;

@Measurement(name = "user_endpoint_request")
public class InfluxDBUserEndpointRequest {

    @Column(name = "user", tag = true)
    private String userId;

    @Column(name = "time", timestamp = true)
    private Instant timestamp;

    @Column(name = "endpoint")
    private String endpoint;

    public InfluxDBUserEndpointRequest() {
    }

    public InfluxDBUserEndpointRequest(final String userId, final Instant timestamp, final String endpoint) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.endpoint = endpoint;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InfluxDBUserEndpointRequest that = (InfluxDBUserEndpointRequest) o;
        return getUserId().equals(that.getUserId()) && getTimestamp().equals(that.getTimestamp()) && getEndpoint().equals(that.getEndpoint());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getTimestamp(), getEndpoint());
    }
}
