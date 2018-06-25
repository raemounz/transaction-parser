package halim.raymond.txnparser.model;

import java.util.Date;

public class Rate {

    private Date date;
    private String from_ccy;
    private String to_ccy;
    private float rate;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getFrom_ccy() {
        return from_ccy;
    }

    public void setFrom_ccy(String from_ccy) {
        this.from_ccy = from_ccy;
    }

    public String getTo_ccy() {
        return to_ccy;
    }

    public void setTo_ccy(String to_ccy) {
        this.to_ccy = to_ccy;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

}
