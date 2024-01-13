package com.harinda.inventoryservice.service;

import com.harinda.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public boolean isInStock(String skuCode){
        log.debug("Inventory IsInStock Request Received {}", skuCode);
        return inventoryRepository.findBySkuCode(skuCode).isPresent() && inventoryRepository.findBySkuCode(skuCode).get().getQuantity() > 0;
    }
}
