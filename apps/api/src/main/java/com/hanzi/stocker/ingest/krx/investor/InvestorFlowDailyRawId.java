package com.hanzi.stocker.ingest.krx.investor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * investor_flow_daily_raw 복합 키.
 */
public class InvestorFlowDailyRawId implements Serializable {

    private LocalDate trdDd;
    private String market;
    private String investorName;

    public InvestorFlowDailyRawId() {
    }

    public InvestorFlowDailyRawId(LocalDate trdDd, String market, String investorName) {
        this.trdDd = trdDd;
        this.market = market;
        this.investorName = investorName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvestorFlowDailyRawId that = (InvestorFlowDailyRawId) o;
        return Objects.equals(trdDd, that.trdDd)
                && Objects.equals(market, that.market)
                && Objects.equals(investorName, that.investorName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trdDd, market, investorName);
    }
}
