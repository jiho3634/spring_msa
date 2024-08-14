package com.beyond.ordersystem.product.controller;

import com.beyond.ordersystem.common.dto.CommonErrorDto;
import com.beyond.ordersystem.common.dto.CommonResDto;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.dto.ProductSaveReqDto;
import com.beyond.ordersystem.product.dto.ProductSearchDto;
import com.beyond.ordersystem.product.dto.ProductUpdateStockDto;
import com.beyond.ordersystem.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

//  해당 어노테이션 사용시 아래의 Bean 은 실시간 config 변경 사항의 대상이 된다.
@RefreshScope
@RestController
public class ProductController {

    private final ProductService productService;

    // application.yml에서 message.hello 값을 주입받음
    @Value("${message.hello}")
    private String helloworld;

    @Autowired
    ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/product/config/test")
    public String configTest() {
        return helloworld; // 주입된 값 반환
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/product/create")
    public ResponseEntity<?> productCreate(ProductSaveReqDto dto) {
        try {
            Product product = productService.productCreate(dto);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Product is successfully created", product.getId());
            return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), "BAD REQUEST"), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/product/awscreate")
    public ResponseEntity<?> productAwsCreate(ProductSaveReqDto dto) {
        try {
            Product product = productService.productAwsCreate(dto);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Product is successfully created", product.getId());
            return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), "BAD REQUEST"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/product/list")
    public ResponseEntity<?> productList(ProductSearchDto searchDto, Pageable pageable) {
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "정상조회완료", productService.productList(searchDto, pageable)), HttpStatus.OK);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<?> productDetail(@PathVariable Long id) {
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "정상조회완료", productService.productDetail(id)), HttpStatus.OK);
    }

    @PutMapping("/product/updatestock")
    public ResponseEntity<?> productStockUpdate(@RequestBody ProductUpdateStockDto dto) {
        Product product = productService.productUpdateStock(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "update is successfull", product.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}
