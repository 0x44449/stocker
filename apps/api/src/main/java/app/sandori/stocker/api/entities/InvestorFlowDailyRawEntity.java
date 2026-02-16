package app.sandori.stocker.api.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "investor_flow_daily_raw")
@IdClass(InvestorFlowDailyRawId.class)
public class InvestorFlowDailyRawEntity {

    @Id
    @Column(name = "trd_dd")
    private LocalDate trdDd;

    @Id
    @Column(name = "market")
    private String market;

    @Id
    @Column(name = "investor_name")
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
    private String source;

    @Column(name = "ingested_at", insertable = false, updatable = false, nullable = false)
    private OffsetDateTime ingestedAt;

    public InvestorFlowDailyRawEntity() {}

    public InvestorFlowDailyRawEntity(LocalDate trdDd, String market, String investorName,
                                      Long sellVolume, Long buyVolume, Long netVolume,
                                      Long sellValue, Long buyValue, Long netValue, String source) {
        this.trdDd = trdDd;
        this.market = market;
        this.investorName = investorName;
        this.sellVolume = sellVolume;
        this.buyVolume = buyVolume;
        this.netVolume = netVolume;
        this.sellValue = sellValue;
        this.buyValue = buyValue;
        this.netValue = netValue;
        this.source = source;
    }

    public LocalDate getTrdDd() {
        return trdDd;
    }

    public void setTrdDd(LocalDate trdDd) {
        this.trdDd = trdDd;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getInvestorName() {
        return investorName;
    }

    public void setInvestorName(String investorName) {
        this.investorName = investorName;
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

    public void setSource(String source) {
        this.source = source;
    }

    public OffsetDateTime getIngestedAt() {
        return ingestedAt;
    }
}
