package com.hanzi.stocker.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class InvestorFlowDailyRawId implements Serializable {

    private LocalDate trdDd;
    private String market;
    private String investorName;

    public InvestorFlowDailyRawId() {}

    public InvestorFlowDailyRawId(LocalDate trdDd, String market, String investorName) {
        this.trdDd = trdDd;
        this.market = market;
        this.investorName = investorName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvestorFlowDailyRawId that)) return false;
        return Objects.equals(trdDd, that.trdDd) && Objects.equals(market, that.market) && Objects.equals(investorName, that.investorName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trdDd, market, investorName);
    }
}
