package com.auction.usedauction.repository.product;

import com.auction.usedauction.domain.Product;
import com.auction.usedauction.domain.ProductStatus;
import com.auction.usedauction.repository.dto.ProductSearchCond;
import com.auction.usedauction.repository.dto.ProductOrderCond;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.auction.usedauction.domain.QCategory.*;
import static com.auction.usedauction.domain.QMember.*;
import static com.auction.usedauction.domain.QProduct.*;
import static org.springframework.util.StringUtils.*;

@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    //상품 리스트 조회
    @Override
    public Page<Product> findBySearchCond(ProductSearchCond searchCond, Pageable pageable) {
        List<Product> content = queryFactory
                .selectFrom(product)
                .join(product.category, category).fetchJoin()
                .join(product.member, member).fetchJoin()
                .orderBy(orderCond(searchCond.getOrderBy()))
                .where(productNameContains(searchCond.getProductName()),
                        categoryIdEq(searchCond.getCategoryId()),
                        productStatusEq(ProductStatus.BID)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(product.count())
                .from(product)
                .join(product.category, category)
                .join(product.member, member)
                .where(productNameContains(searchCond.getProductName()),
                        categoryIdEq(searchCond.getCategoryId()),
                        productStatusEq(ProductStatus.BID));

        return PageableExecutionUtils.getPage(content,pageable, countQuery::fetchOne);

    }

    private OrderSpecifier orderCond(ProductOrderCond orderCond) {
        if (orderCond == ProductOrderCond.VIEW_ORDER) {
            return product.viewCount.desc();
        } else if (orderCond == ProductOrderCond.NEW_PRODUCT_ORDER) {
            return product.createdDate.desc();
        } else if (orderCond == ProductOrderCond.BID_CLOSING_ORDER) {
            return product.auctionEndDate.asc();
        } else if (orderCond == ProductOrderCond.HIGH_PRICE_ORDER) {
            return product.nowPrice.desc();
        } else {
            return product.nowPrice.asc();
        }
    }
    private BooleanExpression productStatusEq(ProductStatus status) {
        return status!=null ? product.productStatus.eq(status) : null;
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId != null ? category.id.eq(categoryId) : null;
    }

    private BooleanExpression productNameContains(String productName) {
        return hasText(productName) ? product.name.contains(productName) : null;
    }
}
