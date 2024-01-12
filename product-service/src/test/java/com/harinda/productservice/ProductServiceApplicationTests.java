package com.harinda.productservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harinda.productservice.dto.ProductRequest;
import com.harinda.productservice.dto.ProductResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc // for auto configure the MockMvc init
class ProductServiceApplicationTests {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");

    @Autowired
    private MockMvc mockMvc; // for mocking the request servelet

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    void testCreateProduct() throws Exception {

        ProductRequest productRequest = getProductRequest();
        String productRequestString = objectMapper.writeValueAsString(productRequest);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequestString))
                .andExpect(status().isCreated());
    }

    @Test
    void testGetProduct() throws Exception {

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/product"))
                .andExpect(status().isOk())
                .andReturn();
        List<ProductResponse> productResponse = objectMapper.readerForListOf(ProductResponse.class).readValue(mvcResult.getResponse().getContentAsString());
        Assertions.assertEquals(productResponse.size(), 1);

        ProductResponse expectedProductResponse = getExpectedProductResponse();
        Assertions.assertEquals(productResponse.get(0).getName(), expectedProductResponse.getName());
        Assertions.assertEquals(productResponse.get(0).getDescription(), expectedProductResponse.getDescription());
        Assertions.assertEquals(productResponse.get(0).getPrice(), expectedProductResponse.getPrice());

    }

    private ProductRequest getProductRequest() {
        return ProductRequest.builder()
                .name("Iphone 15")
                .description("Iphone 15")
                .price(BigDecimal.valueOf(1000))
                .build();
    }

    private ProductResponse getExpectedProductResponse() {
        return ProductResponse.builder()
                .name("Iphone 15")
                .description("Iphone 15")
                .price(BigDecimal.valueOf(1000))
                .build();
    }
}
