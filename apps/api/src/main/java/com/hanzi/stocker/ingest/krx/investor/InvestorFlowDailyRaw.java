package com.hanzi.stocker.ingest.krx.investor;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * KRX 투자자별 거래실적 원본(raw) 엔티티.
 */
@Entity
@Table(name = "investor_flow_daily_raw")
@IdClass(InvestorFlowDailyRawId.class)
public class InvestorFlowDailyRaw {

    @Id
    @Column(name = "trd_dd", nullable = false)
    private LocalDate trdDd;

    @Id
    @Column(name = "market", nullable = false)
    private String market;

    @Id
    @Column(name = "investor_name", nullable = false)
    private String investorName;

    @Column(name = "sell_volume")
    private Long sellVolume;

    @Column(name = "buy_volume")
    private Long buyVolume;

    @Column(name = "net_volume")
    private Long netVolume;

    @Column(name = "sell_value")
    private Long sellValue;

    @Column(name = "buy_value")
    private Long buyValue;

    @Column(name = "net_value")
    private Long netValue;

    @Column(name = "source", nullable = false)
    private String source = "KRX";

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt;

    protected InvestorFlowDailyRaw() {
    }

    public InvestorFlowDailyRaw(LocalDate trdDd, String market, String investorName) {
        this.trdDd = trdDd;
        this.market = market;
        this.investorName = investorName;
        this.ingestedAt = Instant.now();
    }

    public LocalDate getTrdDd() {
        return trdDd;
    }

    public String getMarket() {
        return market;
    }

    public String getInvestorName() {
        return investorName;
    }

    public Long getSellVolume() {
        return sellVolume;
    }

    public void setSellVolume(Long sellVolume) {
        this.sellVolume = sellVolume;
    }

    public Long getBuyVolume() {
        return buyVolume;
    }

    public void setBuyVolume(Long buyVolume) {
        this.buyVolume = buyVolume;
    }

    public Long getNetVolume() {
        return netVolume;
    }

    public void setNetVolume(Long netVolume) {
        this.netVolume = netVolume;
    }

    public Long getSellValue() {
        return sellValue;
    }

    public void setSellValue(Long sellValue) {
        this.sellValue = sellValue;
    }

    public Long getBuyValue() {
        return buyValue;
    }

    public void setBuyValue(Long buyValue) {
        this.buyValue = buyValue;
    }

    public Long getNetValue() {
        return netValue;
    }

    public void setNetValue(Long netValue) {
        this.netValue = netValue;
    }

    public String getSource() {
        return source;
    }

    public Instant getIngestedAt() {
        return ingestedAt;
    }

    public void setIngestedAt(Instant ingestedAt) {
        this.ingestedAt = ingestedAt;
    }
}
