package com.harinda.inventoryservice;

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

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Slf4j
class InventoryServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

	@Autowired
	private InventoryRepository inventoryRepository;

    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:5.5");
    static {
        mySQLContainer.start();
    }
    private static final String HAVE_INVENTORY_SKU_CODE = "iphone_15";
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
                .skuCode(HAVE_INVENTORY_SKU_CODE)
                .quantity(1000).build();

        Inventory inventory2 = Inventory.builder()
                .skuCode(NO_INVENTORY_SKU_CODE)
                .quantity(0).build();

        inventoryRepository.save(inventory1);
        inventoryRepository.save(inventory2);
    }

    @Test
    void testPlaceOrder() throws Exception {
        MvcResult mvcResult1 = mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory/" + HAVE_INVENTORY_SKU_CODE))
                .andReturn();

        MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory/" + NO_INVENTORY_SKU_CODE))
                .andReturn();

        Assertions.assertEquals(mvcResult1.getResponse().getContentAsString(), "true");
        Assertions.assertEquals(mvcResult2.getResponse().getContentAsString(), "false");
    }

}
