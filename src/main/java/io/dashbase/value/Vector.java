package io.dashbase.value;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dashbase.web.response.BaseResult;
import io.dashbase.web.response.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


@JsonSerialize(using = Vector.VectorSerializer.class)
public class Vector implements Result{
    public final List<Sample> list;

    private Vector(List<Sample> list) {
        this.list = list;
    }

    public static Vector of(List<Sample> list) {
        return new Vector(list);
    }

    @Override
    public ResultType resultType() {
        return ResultType.vector;
    }

    public static class VectorSerializer extends JsonSerializer<Vector> {
        @Override
        public void serialize(Vector value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            Collections.sort(value.list);
            gen.writeObject(value.list);
        }
    }


    @Override
    public Result combine(Result other) {
        if (Objects.isNull(other) || other.resultType() != ResultType.vector) {
            return this;
        }

        list.addAll(((Vector) other).list);
        return this;
    }

    @Override
    public Response<BaseResult<Vector>> toResponse() {
        BaseResult<Vector> result = new BaseResult<>();
        result.setResult(this);
        result.setResultType(ResultType.vector);
        Response<BaseResult<Vector>> response = new Response<>();
        response.setData(result);
        return response;
    }
}
