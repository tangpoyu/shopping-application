package com.example.productservice.web.util;

import com.example.productservice.persistence.entity.Product;
import com.example.productservice.web.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConverterTest {

    @Mock
    private GetImage getImage;

    @InjectMocks
    private Converter underTest;

    @Test
    void productToDto() {
        // GIVEN
        UUID productId = UUID.randomUUID();
        String productName = "Iphone";
        String description = "This is a iphone";
        BigDecimal price = BigDecimal.valueOf(11.00);

        Product product = Product.builder()
                .id(productId)
                .name(productName)
                .description(description)
                .price(price).build();
        // WHEN
        ProductResponse result = underTest.productToDto(product);

        // THEN
        verify(getImage,times(1)).getImageBase64String(product);
        ProductResponse expected = ProductResponse
                .builder()
                .id(productId)
                .name(productName)
                .description(description)
                .price(price)
                .build();

        assertThat(expected).isEqualTo(result);
    }
}