package com.app.service;

import com.app.model.Product;
import com.app.model.dto.ProductDto;
import com.app.model.modelMappers.ModelMapper;
import com.app.repository.ProductRepository;
import com.app.utils.FileManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private ProductRepository productRepository;
    private ModelMapper modelMapper;
    private FileManager fileManager;
    private AmazonClient amazonClient;

    public ProductService(ProductRepository productRepository, ModelMapper modelMapper, FileManager fileManager, AmazonClient amazonClient) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
        this.fileManager = fileManager;
        this.amazonClient = amazonClient;
    }

    public List<ProductDto> getProducts() {
        return productRepository
                .findAll()
                .stream()
                .map(modelMapper::fromProductToProductDto)
                .collect(Collectors.toList());
    }

    public ProductDto getProduct(Long id) {
        return productRepository
                .findById(id)
                .map(modelMapper::fromProductToProductDto)
                .orElseThrow(NullPointerException::new);

    }

    public ProductDto addOrUpdateProduct(ProductDto productDto, MultipartFile multipartFile) throws IOException, IllegalAccessException {
        if (productDto == null)
            throw new NullPointerException("Product is null");
        Product product = modelMapper.fromProductDtoToProduct(productDto);
        if (multipartFile != null && productDto.getImgUrl() == null)
            product.setImgUrl(amazonClient.uploadFile(multipartFile));
        else if (multipartFile != null && !productDto.getImgUrl().equals("")) {
            amazonClient.deleteFileFromS3Bucket(productDto.getImgUrl());
            product.setImgUrl(amazonClient.uploadFile(multipartFile));
        }
        Product productFromDb = productRepository.save(product);
        return modelMapper.fromProductToProductDto(productFromDb);
    }

    public ProductDto deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(NullPointerException::new);
        amazonClient.deleteFileFromS3Bucket(product.getImgUrl());
        productRepository.delete(product);
        return modelMapper.fromProductToProductDto(product);
    }
}
