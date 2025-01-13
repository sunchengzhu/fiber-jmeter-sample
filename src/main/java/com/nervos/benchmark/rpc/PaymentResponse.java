package com.nervos.benchmark.rpc;

public class PaymentResponse {
    public String payment_hash;
    public String status;
    public String created_at;
    public String last_updated_at;
    public String failed_error;
    public String fee;

    @Override
    public String toString() {
        return "PaymentResponse{" +
                "paymentHash='" + payment_hash + '\'' +
                ", status='" + status + '\'' +
                ", createdAt='" + created_at + '\'' +
                ", lastUpdatedAt='" + last_updated_at + '\'' +
                ", failedError='" + failed_error + '\'' +
                ", fee='" + fee + '\'' +
                '}';
    }
}
