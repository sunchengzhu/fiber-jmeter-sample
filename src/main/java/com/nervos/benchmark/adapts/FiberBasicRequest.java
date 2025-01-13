

package com.nervos.benchmark.adapts;

import com.nervos.benchmark.rpc.FiberApi;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.util.Map;

public abstract class FiberBasicRequest extends AbstractJavaSamplerClient {


    private SampleResult result;

    public FiberApi fiberClient;

    public abstract Arguments getConfigArguments();

    public abstract void setupOtherData(JavaSamplerContext context);

    public abstract void prepareRun(JavaSamplerContext context);

    public abstract boolean run(JavaSamplerContext context);

    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        arguments.addArgument(Constant.RPC_URL, "http://127.0.0.1:8228");
        Arguments newArgument = getConfigArguments();
        for (Map.Entry<String, String> entry : newArgument.getArgumentsAsMap().entrySet()) {
            arguments.addArgument(entry.getKey(), entry.getValue());
        }
        return arguments;
    }


    @Override
    public void setupTest(JavaSamplerContext context) {
        result = new SampleResult();
        String url = context.getParameter(Constant.RPC_URL);
        this.fiberClient = new FiberApi(url);
        setupOtherData(context);
    }


    @Override
    public void teardownTest(JavaSamplerContext context) {

        System.out.println("teardownTest");
    }


    @Override
    public SampleResult runTest(JavaSamplerContext context) {

        prepareRun(context);
        SampleResult result = new SampleResult();
        result.sampleStart(); // Jmeter 开始计时
        boolean success = run(context);
        result.setSuccessful(success); // 是否成功
        result.sampleEnd(); // Jmeter 结束计时
        return result;
    }


}
