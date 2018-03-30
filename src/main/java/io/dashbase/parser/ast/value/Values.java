package io.dashbase.parser.ast.value;

import com.eclipsesource.json.JsonArray;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;

public final class Values {

    // flag interface
    public interface Value {}

    // Scalar is a data point that's explicitly not associated with a metric.
    @JsonSerialize(using = ScalarSerializer.class)
    public static class Scalar implements Value {
        public final long timestamp;
        public final String value;

        private Scalar(long timestamp, String value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        public static Scalar of(long timestamp, String value) {
            return new Scalar(timestamp, value);
        }
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
