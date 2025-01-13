package com.nervos.benchmark.rpc;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetPaymentRequest {
    public String payment_hash;
}
