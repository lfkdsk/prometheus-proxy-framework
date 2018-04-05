package io.dashbase.value;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dashbase.web.response.BaseResult;
import io.dashbase.web.response.Response;

import java.io.IOException;
import java.util.List;

@JsonSerialize(using = Matrix.MatrixSerializer.class)
public class Matrix implements Result {
    public final List<Series> list;

    private Matrix(List<Series> list) {
        this.list = list;
    }

    public static Matrix of(List<Series> list) {
        return new Matrix(list);
    }

    @Override
    public ResultType resultType() {
        return ResultType.Matrix;
    }

    public static class MatrixSerializer extends JsonSerializer<Matrix> {
        @Override
        public void serialize(Matrix value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeObject(value.list);
        }
    }

    @Override
    public Result combine(Result other) {
        if (other.resultType() != ResultType.Matrix) {
            return this;
        }

        list.addAll(((Matrix) other).list);
        return this;
    }

    @Override
    public Response<BaseResult<Matrix>> toResponse() {
        BaseResult<Matrix> result = new BaseResult<>();
        result.setResult(this);
        result.setResultType("metrics");
        Response<BaseResult<Matrix>> response = new Response<>();
        response.setData(result);
        return response;
    }
}