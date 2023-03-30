package com.auction.usedauction.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Product extends BaseTimeEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    private String name;

    private String info;

    @Enumerated(EnumType.STRING)
    private ProductStatus productStatus;

    private int buyNowPrice; // 즉시구매가 설정 안할 경우 -> -1

    private int nowPrice;

    private int startPrice;

    private int priceUnit;

    private int viewCount;

    private LocalDateTime auctionStartDate;

    private LocalDateTime auctionEndDate;

    @Enumerated(EnumType.STRING)
    private TransStatus sellerTransStatus;

    @Enumerated(EnumType.STRING)
    private TransStatus buyerTransStatus;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public Product(String name, String info, int buyNowPrice, int nowPrice, int startPrice, int priceUnit, LocalDateTime auctionEndDate, Category category, Member member) {
        this.name = name;
        this.info = info;
        this.buyNowPrice = buyNowPrice;
        this.nowPrice = nowPrice;
        this.startPrice = startPrice;
        this.priceUnit = priceUnit;
        this.viewCount = 0;
        this.auctionEndDate = auctionEndDate;
        this.category = category;
        this.member = member;
    }

    @PrePersist
    private void initProductAndTransStatus() {
        initTransStatus();
        initProductStatus();
        initStartDate();
    }

    private void initTransStatus() {
        //판매자, 구매자 거래 확인 상태 초기값을 거래 전(TRANS_BEFORE)으로
        sellerTransStatus=TransStatus.TRANS_BEFORE;
        buyerTransStatus=TransStatus.TRANS_BEFORE;
    }

    private void initProductStatus() {
        // 상품 생성시 바로 입찰상태로
        this.productStatus=ProductStatus.BID;
    }

    private void initStartDate() {
        this.auctionStartDate = LocalDateTime.now();
    }
}
