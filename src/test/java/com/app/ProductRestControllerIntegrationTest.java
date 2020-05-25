package com.app;

import com.app.model.*;
import com.app.model.dto.ProductDto;
import com.app.model.modelMappers.ModelMapper;
import com.app.payloads.requests.LoginPayload;
import com.app.repository.ProductRepository;
import com.app.repository.RoleRepository;
import com.app.repository.UserRepository;
import com.app.service.AmazonClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ProductRestControllerIntegrationTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @MockBean
    private AmazonClient amazonClient;

    @Before
    public void init() {
        if (roleRepository.count() != RoleName.values().length) {
            roleRepository.deleteAll();
            Arrays
                    .stream(RoleName.values())
                    .forEach(role -> roleRepository.save(Role.builder().roleName(role).build()));
        }
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        userRepository.save(User.builder().email("test@test.com").username("test").roles(Collections.singletonList(roleRepository.findByRoleName(RoleName.ROLE_USER).get())).password(bCryptPasswordEncoder.encode("123")).build());
        productRepository.save(Product.builder().brand("Aaa").description("Adesc").quantity(10).price(10.0).productType("BEER").build());
        productRepository.save(Product.builder().brand("Bbb").description("Bdesc").imgUrl("test.jpg").quantity(10).price(10.0).productType("BEER").build());

    }

    @Test
    public void getProductsTest() throws Exception {
        Gson gsonBuilder = new GsonBuilder().create();
        List<ProductDto> productsDto = productRepository.findAll().stream().map(modelMapper::fromProductToProductDto).collect(Collectors.toList());
        mvc.perform(get("/api/product")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Auth-Token", getAuthToken())
                .header("Accept", "application/json"))
                .andExpect(status().isOk())
                .andExpect(content().json(gsonBuilder.toJson(productsDto)));
        Assert.assertEquals(2, productRepository.findAll().size());

    }

    @Test
    public void getProductTest() throws Exception {
        Gson gsonBuilder = new GsonBuilder().create();
        ProductDto productDto = productRepository.findById(1L).map(modelMapper::fromProductToProductDto).orElseThrow(NullPointerException::new);
        mvc.perform(get("/api/product/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Auth-Token", getAuthToken())
                .header("Accept", "application/json"))
                .andExpect(status().isOk())
                .andExpect(content().json(gsonBuilder.toJson(productDto)));
    }

    @Test
    public void addProductTest() throws Exception {
        Gson gsonBuilder = new GsonBuilder().create();
        ClassLoader classLoader = getClass().getClassLoader();
        FileInputStream fis = new FileInputStream(classLoader.getResource("images/pic3.png").getFile());
        MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
        ProductDto productDto = ProductDto.builder().brand("Test").description("TestDesc").price(10.0).quantity(10).productType(ProductTypes.BEER).build();
        when(amazonClient.uploadFile(multipartFile)).thenReturn(null);
        mvc.perform(MockMvcRequestBuilders.multipart("/api/product")
                .file("file", multipartFile.getBytes())
                .param("productDto", gsonBuilder.toJson(productDto))
                .header("X-Auth-Token", getAuthToken()))
                .andExpect(status().isOk());
        Assert.assertEquals(3, productRepository.findAll().size());
    }

    @Test
    public void updateProductTest() throws Exception {
        Gson gsonBuilder = new GsonBuilder().create();
        ClassLoader classLoader = getClass().getClassLoader();
        ProductDto productDto = productRepository.findById(1L).map(modelMapper::fromProductToProductDto).orElseThrow(NullPointerException::new);
        FileInputStream fis = new FileInputStream(classLoader.getResource("images/pic3.png").getFile());
        MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
        when(amazonClient.uploadFile(multipartFile)).thenReturn(null);
        final String updateDesc = "UpdateDesc";
        productDto.setDescription(updateDesc);
        mvc.perform(MockMvcRequestBuilders.multipart("/api/product")
                .file("file", multipartFile.getBytes())
                .param("productDto", gsonBuilder.toJson(productDto))
                .header("X-Auth-Token", getAuthToken()))
                .andExpect(status().isOk());
        Assert.assertEquals(2, productRepository.findAll().size());
        Assert.assertEquals(updateDesc, productRepository.findById(1L).get().getDescription());
    }

    @Test
    public void deleteProductTest() throws Exception {
        Gson gsonBuilder = new GsonBuilder().create();
        final int countBefore = productRepository.findAll().size();
        ProductDto productDto = productRepository.findById(2L).map(modelMapper::fromProductToProductDto).orElseThrow(NullPointerException::new);
        mvc.perform(delete("/api/product/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Auth-Token", getAuthToken())
                .header("Accept", "application/json"))
                .andExpect(status().isOk())
                .andExpect(content().json(gsonBuilder.toJson(productDto)));
        Assert.assertEquals(countBefore - 1, productRepository.findAll().size());
    }

    private String getAuthToken() throws Exception {
        Gson gsonBuilder = new GsonBuilder().create();
        return mvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gsonBuilder.toJson(LoginPayload.builder()
                        .email("test@test.com")
                        .password("123").build()))).andReturn()
                .getResponse()
                .getHeader("x-auth-token");
    }
}
