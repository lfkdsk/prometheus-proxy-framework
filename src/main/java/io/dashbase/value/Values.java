package io.dashbase.value;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class Values {

    // Scalar is a data point that's explicitly not associated with a metric.
    @JsonSerialize(using = Scalar.ScalarSerializer.class)
    public static class Scalar {
        public final long timestamp;
        public final String value;

        private Scalar(long timestamp, String value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        public static Scalar of(long timestamp, String value) {
            return new Scalar(timestamp, value);
        }

        public static class ScalarSerializer extends JsonSerializer<Scalar> {
            @Override
            public void serialize(Scalar value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                List<Object> result = Lists.newLinkedList();
                result.add(value.timestamp);
                result.add(value.value);
                gen.writeObject(result);
            }
        }
    }

    @JsonSerialize(using = PointSerializer.class)
    public static class Point {
        public final long timestamp;
        public final String value;

        private Point(long timestamp, String value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        public static Point of(long timestamp, String value) {
            return new Point(timestamp, value);
        }
    }

    public static class PointSerializer extends JsonSerializer<Point> {
        @Override
        public void serialize(Point value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            List<Object> result = Lists.newLinkedList();
            result.add(value.timestamp);
            result.add(value.value);
            gen.writeObject(result);
        }
    }

    public static class Sample {
        public final Point value;
        public final Map<String, String> metrics;

        private Sample(Point value, Map<String, String> metrics) {
            this.value = value;
            this.metrics = metrics;
        }

        public static Sample of(Point value, Map<String, String> metrics) {
            return new Sample(value, metrics);
        }
    }
}