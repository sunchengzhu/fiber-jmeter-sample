package com.nervos.benchmark.rpc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.nervos.ckb.service.RpcService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FiberApi {

    private final RpcService rpcService;

    public FiberApi(String nodeUrl) {
        this(nodeUrl, true);
    }

    public FiberApi(String nodeUrl, boolean isDebug) {
        rpcService = new RpcService(nodeUrl, isDebug);
    }

    public FiberApi(RpcService rpcService) {
        this.rpcService = rpcService;
    }

    // Remove trailing nulls for better backward compatibility.
    @VisibleForTesting
    public static List<Object> noTrailingNullParams(Object... ps) {
        ArrayList<Object> params = Lists.newArrayList(ps);
        for (int i = params.size() - 1; i >= 0; i--) {
            if (params.get(i) == null) {
                params.remove(i);
            } else {
                break;
            }
        }
        return params;
    }

    public NodeInfoResponse node_info() throws IOException {
        return rpcService.post("node_info", Collections.<String>emptyList(), NodeInfoResponse.class);
    }

    public PaymentResponse send_payment(SendPaymentRequest paymentRequest) throws IOException {
        return rpcService.post("send_payment", Collections.singletonList(paymentRequest), PaymentResponse.class);
    }

    // open_channel
    public OpenChannelResponse open_channel(OpenChannelRequest openChannelRequest) throws IOException {
        return rpcService.post("open_channel", Collections.singletonList(openChannelRequest), OpenChannelResponse.class);
    }

    // connect peer
    public void connect_peer(ConnectPeerRequest connectPeerRequest) throws IOException {
        rpcService.post("connect_peer", Collections.singletonList(connectPeerRequest), Object.class);
    }

    // list_channels
    public ListChannelsResponse list_channels(ListChannelsRequest listChannelsRequest) throws IOException {
        return rpcService.post("list_channels", Collections.singletonList(listChannelsRequest), ListChannelsResponse.class);
    }

    // new_invoice
    public NewInvoiceResponse new_invoice(NewInvoiceRequest newInvoiceRequest) throws IOException {
        return rpcService.post("new_invoice", Collections.singletonList(newInvoiceRequest), NewInvoiceResponse.class);

    }
    //  get_invoice
    public GetInvoiceResponse get_invoice(GetInvoiceRequest getInvoiceRequest) throws IOException{
        return rpcService.post("get_invoice", Collections.singletonList(getInvoiceRequest), GetInvoiceResponse.class);
    }

    // get_payment
    public PaymentResponse get_payment(GetPaymentRequest getPaymentRequest) throws IOException{
        return rpcService.post("get_payment", Collections.singletonList(getPaymentRequest), PaymentResponse.class);
    }


    public static void main(String[] args) throws IOException {
        FiberApi fiberApi = new FiberApi("http://127.0.0.1:8228");
        NodeInfoResponse nodeIfo = fiberApi.node_info();
        System.out.println(nodeIfo.node_id);
        PaymentResponse response = fiberApi.send_payment(new SendPaymentRequest("035af2a1233e723b3096dfc1dcae653f0cd9431bbfff7481698e0ff4b0297d5a98",
                "Fibd",
                "0x1",
                true,true));
        System.out.println(response.payment_hash);

    }

}