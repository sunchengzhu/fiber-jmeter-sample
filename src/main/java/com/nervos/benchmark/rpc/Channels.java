package com.nervos.benchmark.rpc;

import org.nervos.ckb.type.Script;

public class Channels {
    public String channel_id;
    public Boolean is_public;
    public String channel_outpoint;
    public String peer_id;
    public Script funding_udt_type_script;
    public ChannelState state;
    public String local_balance;
    public String offered_tlc_balance;
    public String remote_balance;
    public String received_tlc_balance;
    public String latest_commitment_transaction_hash;
    public String created_at;

    public static class ChannelState {
        public String state_name;
        public String state_flags;
    }
}