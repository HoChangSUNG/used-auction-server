package com.auction.usedauction.repository.auction;

import com.auction.usedauction.domain.Auction;

import com.auction.usedauction.domain.AuctionStatus;
import com.auction.usedauction.repository.dto.AuctionIdAndBidCountDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface AuctionRepository extends JpaRepository<Auction, Long>, AuctionRepositoryCustom {

    boolean existsAuctionByIdAndStatus(Long actionId, AuctionStatus auctionStatus);

    Optional<Auction> findAuctionByIdAndStatus(Long auctionId, AuctionStatus auctionStatus);

    @Modifying(clearAutomatically = true)
    @Query(value = "update auction a set a.status = :#{#status.name()} where a.auction_id in (:auction_ids)", nativeQuery = true)
    int updateAuctionStatus(@Param("status") AuctionStatus auctionStatus, @Param("auction_ids") List<Long> auctionIdList);

    // 경매 상태, 시간 기준으로 auctionId, 입찰 수 리턴
    @Query(value =
            "select a.auction_id as auctionId, count(ah.bid_price) as count" +
                    " from auction a" +
                    " left join auction_history ah on a.auction_id = ah.auction_id" +
                    " where a.status = :#{#status.name()} and a.auction_end_date < :localDateTime" +
                    " group by a.auction_id", nativeQuery = true)
    List<AuctionIdAndBidCountDTO> findIdAndBidCountListByStatusAndEndDate(@Param("status") AuctionStatus status, @Param("localDateTime") LocalDateTime localDateTime);
}
