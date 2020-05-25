package com.app.schedulers;

import com.app.model.Product;
import com.app.model.dto.ProductDto;
import com.app.model.dto.ReportDto;
import com.app.model.modelMappers.ModelMapper;
import com.app.repository.ProductRepository;
import com.app.repository.ReportRepository;
import com.app.service.ProductService;
import com.app.service.StatisticService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@EnableAsync
public class StatisticsScheduler {

    private List<ProductDto> previousBeers;
    private List<ProductDto> previousBeersPopular;

    private ProductRepository productRepository;
    private ModelMapper modelMapper;
    private ProductService productService;
    private StatisticService statisticService;
    private ReportRepository reportRepository;

    public StatisticsScheduler(ProductRepository productRepository, ModelMapper modelMapper, ProductService productService, StatisticService statisticService, ReportRepository reportRepository) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
        this.productService = productService;
        this.statisticService = statisticService;
        this.reportRepository = reportRepository;
    }

    @Async
    @Scheduled(fixedRate = 60000)
    public void calculateBeerPrices() {

        List<ProductDto> beerlist = this.productService.getProducts();

            if(this.previousBeers != null){

            for (int i = 0;i < beerlist.size();  i++) {

                    if (previousBeers.get(i).getQuantity() - beerlist.get(i).getQuantity() > 10) {
                        beerlist.get(i).setPrice(beerlist.get(i).getPrice() + 1);
                    } else if (previousBeers.get(i).getQuantity() - beerlist.get(i).getQuantity() < 10 && beerlist.get(i).getMinimalPrice() < beerlist.get(i).getPrice()) {
                        beerlist.get(i).setPrice(beerlist.get(i).getPrice() - 1);
                    }
                    Product product = modelMapper.fromProductDtoToProduct(beerlist.get(i));
                    productRepository.save(product);

            }

        }

        try {
            statisticService.getLastReport();
        } catch (NullPointerException e) {
            statisticService.createNewReport();
        }

        ReportDto report = statisticService.getLastReport();
        statisticService.updateReportData(modelMapper.fromReportDtoToReport(report));
        previousBeers = this.productService.getProducts();
    }

    @Async
    @Scheduled(fixedRate = 60000)
    public void changeMostPopularBeers() {
        try {
            statisticService.getLastReport();
        } catch (NullPointerException e) {
            statisticService.createNewReport();
        }

        ReportDto report = statisticService.getLastReport();

        if (this.previousBeersPopular != null) {
            List<Product> products = this.previousBeers.stream().map(beer -> modelMapper.fromProductDtoToProduct(beer)).collect(Collectors.toList());
            List<Product> actualProducts = productRepository.findAll(); //after 1h
            List<Product> mostPopularProducts = new LinkedList<>();

            for (int i = 0; i< products.size(); i++) {
                products.get(i).setQuantity(products.get(i).getQuantity() - actualProducts.get(i).getQuantity());
                mostPopularProducts = products.stream().sorted(Comparator.comparingInt(Product::getQuantity).reversed()).limit(3).collect(Collectors.toList());
            }

            report.setMostPopularBeers(mostPopularProducts.stream().map(beer -> beer.getBrand()).collect(Collectors.toList()));
            reportRepository.save(modelMapper.fromReportDtoToReport(report));

        }


        this.previousBeersPopular = this.productService.getProducts();


    }



}
