package com.nervos.benchmark.rpc;

import lombok.Data;

@Data
public class OpenChannelRequest {
    public String peer_id;
    public String funding_amount;
//    public Boolean public;

}
