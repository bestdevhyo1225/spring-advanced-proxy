package hello.proxy.config.v1_proxy.concrete_proxy;

import hello.proxy.app.v2.OrderControllerV2;
import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;

public class OrderControllerConcreteProxy extends OrderControllerV2 {

    private final OrderControllerV2 target;
    private final LogTrace logTrace;

    public OrderControllerConcreteProxy(OrderControllerV2 target, LogTrace logTrace) {
        // OrderControllerV2 클래스에 기본생성자가 있으면, 자동으로 super(); 를 호출하는데, 없기 때문에 아래와 같이 명시해야 한다.
        // 그런데 OrderControllerV2 클래스의 기능을 사용하지 않는다. 따라서 null값을 넣으면 된다. -> Why? -> 부가 기능만 구현하면 되기 때문에
        super(null);
        this.target = target;
        this.logTrace = logTrace;
    }

    @Override
    public String request(String itemId) {
        TraceStatus status = null;
        try {
            status = logTrace.begin("OrderController.request()");

            String result = target.request(itemId);

            logTrace.end(status);

            return result;
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }

    @Override
    public String noLog() {
        return target.noLog();
    }
}
