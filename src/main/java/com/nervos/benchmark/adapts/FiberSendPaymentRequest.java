package com.nervos.benchmark.adapts;

import com.nervos.benchmark.rpc.*;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

import java.io.IOException;

public class FiberSendPaymentRequest extends FiberBasicRequest {
    FiberApi targetRpc;
    String targetNodeId;
    Integer timeOut;
    Integer interval;
    Boolean dry_run;

    @Override
    public Arguments getConfigArguments() {
        Arguments arguments = new Arguments();
        arguments.addArgument("targetRpcUrl", "http://127.0.0.1:8229");
        arguments.addArgument("timeOut", "1000");
        arguments.addArgument("interval", "1000");
        arguments.addArgument("dry_run", "false");

        return arguments;
    }


    @Override
    public void setupOtherData(JavaSamplerContext context) {
        System.out.println("---setupOtherData----");
        String targetRpcUrl = context.getParameter("targetRpcUrl");
        System.out.println(targetRpcUrl);
        this.targetRpc = new FiberApi(targetRpcUrl);
        this.timeOut = context.getIntParameter("timeOut");
        this.interval = context.getIntParameter("interval");
        this.dry_run = context.getParameter("dry_run").equals("true");
        try {
            NodeInfoResponse response = this.targetRpc.node_info();
            this.targetNodeId = response.node_id;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void prepareRun(JavaSamplerContext context) {
        System.out.println("---prepareRun----");
    }

    @Override
    public boolean run(JavaSamplerContext context) {
        try {
            PaymentResponse paymentResponse = this.fiberClient.send_payment(new SendPaymentRequest(targetNodeId, "Fibt", "0x1", true, this.dry_run));
            if (this.dry_run) {
                return true;
            }
//            this.fiberClient.get_payment(new GetPaymentRequest(paymentResponse.payment_hash));
            this.wait_payment_status(this.fiberClient, paymentResponse.payment_hash, "Success", this.interval, this.timeOut);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void wait_payment_status(FiberApi fiberApi, String payment_hash, String status, Integer interval, Integer try_count) throws IOException {
        for (int i = 0; i < try_count; i++) {
            PaymentResponse paymentResponse = null;
            try {
                paymentResponse = fiberApi.get_payment(new GetPaymentRequest(payment_hash));
            } catch (IOException e) {
                throw new IOException("get_payment err %s", e);
            }
            if (paymentResponse.status.equals(status)) {
                return;
            }
            try {
                Thread.sleep(interval);  // Sleep for the specified interval between retries
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();  // Restore interrupt status
                throw new IOException("Interrupted while waiting for payment status", e);
            }
        }
        throw new RuntimeException(String.format("Timeout waiting for payment status. Payment hash: %s, Expected status: %s", payment_hash, status));
    }


    public static void main(String[] args) {
        FiberSendPaymentRequest sample = new FiberSendPaymentRequest();

//        Arguments arguments = sample.getConfigArguments();
        Arguments arguments = new Arguments();

        arguments.addArgument(Constant.RPC_URL, "http://127.0.0.1:8228");
        arguments.addArgument("targetRpcUrl", "http://127.0.0.1:8229");
        arguments.addArgument("timeOut", "60");
        arguments.addArgument("interval", "50");
        arguments.addArgument("dry_run", "false");


        JavaSamplerContext context = new JavaSamplerContext(arguments);

        sample.setupTest(context);
        for (int i = 0; i < 1000; i++) {
            System.out.println("----");
            sample.runTest(context);
        }
        sample.teardownTest(context);
    }

}
