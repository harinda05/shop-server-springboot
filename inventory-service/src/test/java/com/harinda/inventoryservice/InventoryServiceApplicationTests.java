package com.harinda.inventoryservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harinda.inventoryservice.dto.InventoryResponse;
import com.harinda.inventoryservice.model.Inventory;
import com.harinda.inventoryservice.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Slf4j
class InventoryServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:5.5");

    static {
        mySQLContainer.start();
    }

    private static final String HAVE_INVENTORY_SKU_CODE1 = "iphone_15";
    private static final String HAVE_INVENTORY_SKU_CODE2 = "iphone_15_Pro";

    private static final String NO_INVENTORY_SKU_CODE = "iphone_10";

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dymDynamicPropertyRegistry) {
        dymDynamicPropertyRegistry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        dymDynamicPropertyRegistry.add("spring.datasource.username", mySQLContainer::getUsername);
        dymDynamicPropertyRegistry.add("spring.datasource.password", mySQLContainer::getPassword);
    }

    @BeforeEach
    public void loadData() {
        Inventory inventory1 = Inventory.builder()
                .skuCode(HAVE_INVENTORY_SKU_CODE1)
                .quantity(1000).build();

        Inventory inventory2 = Inventory.builder()
                .skuCode(HAVE_INVENTORY_SKU_CODE2)
                .quantity(1000).build();

        Inventory inventory3 = Inventory.builder()
                .skuCode(NO_INVENTORY_SKU_CODE)
                .quantity(0).build();

        inventoryRepository.save(inventory1);
        inventoryRepository.save(inventory2);
        inventoryRepository.save(inventory3);
    }

    @Test
    void testPlaceOrder() throws Exception {
        MvcResult mvcResult1 = mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory").queryParam("skuCodes", HAVE_INVENTORY_SKU_CODE1, HAVE_INVENTORY_SKU_CODE2))
                .andReturn();
        List<InventoryResponse> inventoryResponsesList1 = objectMapper.readerForListOf(InventoryResponse.class).readValue(mvcResult1.getResponse().getContentAsString());
        Boolean isInStock1 = inventoryResponsesList1.stream().allMatch(InventoryResponse::isInStock);
        Assertions.assertEquals(true, isInStock1);

        MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory").queryParam("skuCodes", HAVE_INVENTORY_SKU_CODE1, HAVE_INVENTORY_SKU_CODE2, NO_INVENTORY_SKU_CODE))
                .andReturn();
        List<InventoryResponse> inventoryResponsesList2 = objectMapper.readerForListOf(InventoryResponse.class).readValue(mvcResult2.getResponse().getContentAsString());
        Boolean isInStock2 = inventoryResponsesList2.stream().allMatch(InventoryResponse::isInStock);
        Assertions.assertEquals(false, isInStock2);
    }

}
