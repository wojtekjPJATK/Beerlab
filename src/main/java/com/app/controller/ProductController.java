package com.app.controller;

import com.app.model.Product;
import com.app.model.ProductTypes;
import com.app.model.dto.ProductDto;
import com.app.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/product")
@Api(tags = "Product controller")
public class ProductController {
    private ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @ApiOperation(
            value = "Fetch all products",
            response = Product.class
    )
    @GetMapping
    public List<ProductDto> getAllProducts() {
        return productService.getProducts();
    }

    @ApiOperation(
            value = "Add one product",
            response = Product.class
    )
    @PostMapping
    public ProductDto addProduct(@RequestPart(value = "file", required = false) MultipartFile uploadfile, @RequestParam("productDto") String productDto) throws IOException, IllegalAccessException {
        try {
            return productService.addOrUpdateProduct(new ObjectMapper().readValue(productDto, ProductDto.class), uploadfile);
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @ApiOperation(
            value = "Get one product",
            response = Product.class
    )
    @GetMapping("/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @ApiOperation(
            value = "Delete one product",
            response = Product.class
    )
    @DeleteMapping("/{id}")
    public ProductDto deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }

    @ApiOperation(
            value = "Update one product",
            response = Product.class
    )
    @PutMapping
    public ProductDto updateProduct(@RequestPart(value = "file", required = false) MultipartFile uploadfile, @RequestParam("productDto") String productDto) throws IOException, IllegalAccessException {
        try {
            return productService.addOrUpdateProduct(new ObjectMapper().readValue(productDto, ProductDto.class), uploadfile);
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @ApiOperation(
            value = "Fetch all product types",
            response = Product.class
    )
    @GetMapping("/types")
    public List<ProductTypes> getProductTypes() {
        return Arrays.asList(ProductTypes.values());
    }
}
