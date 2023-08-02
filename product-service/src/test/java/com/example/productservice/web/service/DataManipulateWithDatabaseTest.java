package com.example.productservice.web.service;


import com.example.productservice.persistence.entity.Product;
import com.example.productservice.persistence.repository.ProductRepository;
import com.example.productservice.web.dto.ProductResponse;
import com.example.productservice.web.util.Converter;
import com.example.productservice.web.util.GetImage;
import org.assertj.core.api.ListAssert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

import java.util.*;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class DataManipulateWithDatabaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private GetImage getImage;

    @Mock
    private Converter converter;

    @InjectMocks
    private DataManipulateWithDatabase dataManipulateWithDatabase;

    DataManipulateWithDatabaseTest() throws IOException {
    }

    @Test
    void createProduct() {
        // given
        UUID id = UUID.randomUUID();
        String productName = "iphone";
        String description = "this is a phone";
        String price = "35.50";
        String product = id + "," + productName + "," + description +
               "," + price;
        byte[] imageData = new byte[]{1, 2, 3, 4, 5};
        MultipartFile image = new MockMultipartFile("image","image.jpg","image/jpg",imageData);

        // when
        dataManipulateWithDatabase.createProduct(product,image);

        // then
        verify(productRepository,times(1))
                .save(Product.builder()
                        .id(id)
                        .name(productName)
                        .description(description)
                        .price(BigDecimal.valueOf(Double.valueOf(price)))
                        .imageExtension(".jpg")
                        .build()
                );
    }

    @Test
    void createProduct_failWhenProductRepositorySave(){
        // given
        UUID id = UUID.randomUUID();
        String productName = "iphone";
        String description = "this is a phone";
        String price = "35.50";
        String product = id + "," + productName + "," + description +
                "," + price;
        byte[] imageData = new byte[]{1, 2, 3, 4, 5};
        MultipartFile image = new MockMultipartFile("image",imageData);

        // when
        // then
        assertThatThrownBy(()->dataManipulateWithDatabase.createProduct(product,image))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(
                        String.format(
                                "Save product data {} in product-service is failed, Exception",id));
    }


    @Test
    void shouldReturnListOfImageStringsForExistingProducts() throws IOException {
        // given
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();
        List<UUID> productIds = Arrays.asList(productId1, productId2);
        Product product1 = Product.builder().id(productId1).build();
        Product product2 = Product.builder().id(productId2).build();
        String imageString1 = "imageString1";
        String imageString2 = "imageString2";
        given(productRepository.findById(productId1)).willReturn(Optional.of(product1));
        given(productRepository.findById(productId2)).willReturn(Optional.of(product2));
        given(getImage.getImageBase64String(product1)).willReturn(imageString1);
        given(getImage.getImageBase64String(product2)).willReturn(imageString2);

        //when
        List<String> productImages = dataManipulateWithDatabase.getProductImages(productIds);

        //then
        ListAssert.assertThatList(productImages).containsExactlyInAnyOrder(imageString1,imageString2);
    }

    @Test
    void getProduct() {
        // GIVEN
        String productId = UUID.randomUUID().toString();
        given(productRepository.findById(UUID.fromString(productId))).willReturn(
                Optional.of(Product.builder().id(UUID.fromString(productId)).build())
        );


        // WHEN
        dataManipulateWithDatabase.getProduct(productId);

        //THEN
        ArgumentCaptor<UUID> argumentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(productRepository,times(1))
                .findById(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(UUID.fromString(productId));
        verify(converter,times(1)).productToDto(Product.builder().id(UUID.fromString(productId)).build());
    }

    @Test
    void getAllProducts() {
        // GIVEN
        given(productRepository.findAll()).willReturn(new ArrayList<>());

        // WHEN
        List<ProductResponse> result = dataManipulateWithDatabase.getAllProducts();

        // THEN
        verify(productRepository,times(1)).findAll();
        assertThat(result).isEqualTo(new ArrayList<>());
    }
}