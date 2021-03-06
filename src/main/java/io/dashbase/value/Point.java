package io.dashbase.value;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

@JsonSerialize(using = Point.PointSerializer.class)
public class Point implements Comparable<Point> {
    public final long timestamp;
    public final String value;

    private Point(long timestamp, @NonNull String value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public static Point of(long timestamp, String value) {
        return new Point(timestamp, value);
    }

    @Override
    public int compareTo(@NotNull Point o) {
        return (int) (timestamp - o.timestamp);
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

    @Override
    public String toString() {
        return String.format("Point<%s,%s>", timestamp, value);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return hashCode() == obj.hashCode();
    }
}


