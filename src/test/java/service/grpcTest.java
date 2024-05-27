package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import dev.dodo.geoservice.GeoInfoRequest;
import dev.dodo.geoservice.GeoInfoResponse;
import dev.dodo.geoservice.GeoServiceGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.okhttp.internal.proxy.Request;
import io.qameta.allure.Attachment;
import io.qameta.allure.grpc.AllureGrpc;
import io.qameta.allure.grpc.GrpcRequestAttachment;
import net.javacrumbs.jsonunit.JsonMatchers;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

import static com.google.protobuf.util.JsonFormat.printer;
import static java.util.stream.Collectors.toList;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class grpcTest {

    // Обращение к сервису
    private final Channel ch = ManagedChannelBuilder
            .forAddress("localhost", 9090)
            .usePlaintext() // Это позволяет без SSL сертификата дергать запросы
            .build();
    private GeoServiceGrpc.GeoServiceBlockingStub stub =
            GeoServiceGrpc.newBlockingStub(ch)
                    .withInterceptors(new AllureGrpc());
    private JsonFormat.Printer jsonPrinter = printer().includingDefaultValueFields();

    @Test
    @DisplayName("Пример унарного(обычного) теста")
    public void unaryCallTest() throws JsonProcessingException, InvalidProtocolBufferException {

        GeoInfoResponse expected = GeoInfoResponse.newBuilder()
                .setGeoCode(84)
                .setName("VN")
                .build();

        GeoInfoResponse response = stub.getGeoInfo(GeoInfoRequest.newBuilder()
                .setGeoCode(84)
                .build());

        assertThat(response).usingRecursiveComparison()
                .ignoringFields("id_", "memoizedHashCode")//memoizedHashCode - служебное поле, хеш код объекта, если не указать это служебное поле то получается что в нашем объекте нет поля, а в респонсе оно есть и оно различается, тем самым указав это служебное поле мы говорим что не сравнивай и хеш код тоже
                .isEqualTo(expected);
//
//        System.out.println(jsonPrinter.print(response.getId2()));
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode node = objectMapper.readTree(printer().print(response));
//
//        System.out.println(node.has("id"));
//
////        JSONObject jsonObject = new JSONObject(printer().print(response));
////        System.out.println(jsonObject.has("id"));

    }
    @Test
    @DisplayName("Пример теста grpc с ответом ввиде стрима")
    public void streamingTest() {
//      В стриминговом запросе используется итератор, итератор нужен для того, что бы при открытом стриме довести до конца запрос
//      и получить весь ответ, то есть туда запишутся все данные из стрима пока он не закончится.
        Iterator<GeoInfoResponse> allGeo = stub.getAllGeo(Empty.getDefaultInstance());

        List<GeoInfoResponse> infoResponses = ImmutableList.copyOf(allGeo);
        assertThat(infoResponses).hasSize(3);

        System.out.println(infoResponses.get(0));
    }
    @Test
    @DisplayName("Пример теста grpc")
    public void streamingTest2() throws InvalidProtocolBufferException {

        String expectedMessage = "UNKNOWN";

        GeoInfoRequest request = GeoInfoRequest.newBuilder()
                .setGeoCode(84)
                .build();
        System.out.println("Request : " + jsonPrinter.print(request));

//        StatusRuntimeException response = org.junit.jupiter.api.Assertions.assertThrows(StatusRuntimeException.class, () -> stub.getGeoInfo(request));
        GeoInfoResponse response = stub.getGeoInfo(request);
        assertThat(response).usingRecursiveComparison()
                .ignoringFields("id_", "memoizedHashCode")//memoizedHashCode - служебное поле, хеш код объекта, если не указать это служебное поле то получается что в нашем объекте нет поля, а в респонсе оно есть и оно различается, тем самым указав это служебное поле мы говорим что не сравнивай и хеш код тоже
                .isEqualTo(request);


//
//        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(StatusRuntimeException.class, () -> stub.getGeoInfo(request));
//        assertTrue(exception.getMessage().contains(expectedMessage));




        //        Assertions.assertThrows(StatusRuntimeException.class, () ->
//                stub.getGeoInfo(request));
    }
}
