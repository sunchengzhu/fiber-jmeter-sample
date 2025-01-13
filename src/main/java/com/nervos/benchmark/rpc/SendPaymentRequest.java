package com.nervos.benchmark.rpc;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SendPaymentRequest {
    public final String target_pubkey;
    public final String currency;
    public final String amount;
    public final boolean keysend;
    public final boolean dry_run;


}
