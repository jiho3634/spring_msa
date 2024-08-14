package com.beyond.ordersystem.product.service;

import com.beyond.ordersystem.common.service.StockInventoryService;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.dto.ProductResDto;
import com.beyond.ordersystem.product.dto.ProductSaveReqDto;
import com.beyond.ordersystem.product.dto.ProductSearchDto;
import com.beyond.ordersystem.product.dto.ProductUpdateStockDto;
import com.beyond.ordersystem.product.repository.ProductRepository;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
//import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final StockInventoryService stockInventoryService;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Autowired
    ProductService(ProductRepository productRepository, StockInventoryService stockInventoryService, S3Client s3Client) {
        this.productRepository = productRepository;
        this.stockInventoryService = stockInventoryService;
        this.s3Client = s3Client;
    }

    public Product productAwsCreate(ProductSaveReqDto dto) {
        try {
            Product product = productRepository.save(dto.toEntity());
            byte[] bytes = dto.getProductImage().getBytes();
            String fileName = product.getId() + "_" + dto.getProductImage().getOriginalFilename();

            //  aws 에  pc 에 저장된 파일을 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucket).key(fileName).build();
            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
            String s3Path = s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(fileName)).toExternalForm();
            product.updateImagePath(s3Path);
            return product;
        } catch (IOException e) {throw new RuntimeException("이미지 저장 실패");}
    }

    public Product productCreate(ProductSaveReqDto dto) {
        MultipartFile image = dto.getProductImage();
        Product product = null;
        try {
            product = productRepository.save(dto.toEntity());
            //  pc에 저장하기 위한 경로 설정
            Path path = Paths.get("C:/Users/Playdata/Desktop/tmp/", product.getId() + "_" + image.getOriginalFilename());
            //  파일 생성
            Files.write(path, image.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            product.updateImagePath(path.toString());

            if (dto.getName().contains("sale")) {
                stockInventoryService.increaseStock(product.getId(), dto.getStockQuantity());
            }
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패");
        }
        return product;
    }

    public Page<ProductResDto> productList(ProductSearchDto searchDto, Pageable pageable) {
        //  검색을 위해 Specification 객체 사용.
        //  Specification 객체는 복잡한 쿼리를 명세를 이용하며 정의하는 방식으로, 쿼리를 쉽게 생성.
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                if (searchDto.getSearchName() != null) {
                    // root : 엔터티의 속성을 접근하기 위한 객체, CriteriaBuilder 는 쿼리를 생성하기 위한 객체
                    predicateList.add(criteriaBuilder.like(root.get("name"), "%" + searchDto.getSearchName() + "%"));
                }
                if (searchDto.getCategory() != null) {
                    predicateList.add(criteriaBuilder.like(root.get("category"), "%" + searchDto.getCategory() + "%"));
                }
                Predicate[] predicates = new Predicate[predicateList.size()];
                for (int i = 0; i < predicates.length; i++) {
                    predicates[i] = predicateList.get(i);
                }
                return criteriaBuilder.and(predicates);
            }
        };
        return productRepository.findAll(specification, pageable).map(product -> new ProductResDto().fromEntity(product));
    }

    public ProductResDto productDetail(Long id) {
        return ProductResDto.fromEntity(productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("not found")));
    }

    public Product productUpdateStock(ProductUpdateStockDto dto) {
        Product product = productRepository.findById(dto.getProductId()).orElseThrow(() -> new EntityNotFoundException("not found"));
        product.updateStockQuantity(dto.getQuantity());
        return product;
    }

//    @KafkaListener(topics = "product-update-topic", groupId = "order-group", containerFactory = "kafkaListenerContainerFactory")
//    public void consumerProductQuantity(String message) {
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            ProductUpdateStockDto productUpdateStockDto = objectMapper.readValue(message, ProductUpdateStockDto.class);
//            System.out.println(productUpdateStockDto);
//            this.productUpdateStock(productUpdateStockDto);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//    }
}