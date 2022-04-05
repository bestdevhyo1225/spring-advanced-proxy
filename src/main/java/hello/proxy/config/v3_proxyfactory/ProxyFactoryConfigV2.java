package hello.proxy.config.v3_proxyfactory;

import hello.proxy.app.v2.OrderControllerV2;
import hello.proxy.app.v2.OrderRepositoryV2;
import hello.proxy.app.v2.OrderServiceV2;
import hello.proxy.config.v3_proxyfactory.advice.LogTraceAdvice;
import hello.proxy.trace.logtrace.LogTrace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ProxyFactoryConfigV2 {

    @Bean
    public OrderControllerV2 orderControllerV2(LogTrace logTrace) {
        // Create Target
        OrderControllerV2 orderController = new OrderControllerV2(orderServiceV2(logTrace));

        // ProxyFactory
        ProxyFactory proxyFactory = new ProxyFactory(orderController);
        proxyFactory.addAdvisor(getAdvisor(logTrace));

        // Create Proxy
        OrderControllerV2 proxy = (OrderControllerV2) proxyFactory.getProxy();

        log.info("ProxyFactory proxy={}, target={}", proxy.getClass(), orderController.getClass());

        return proxy;
    }

    @Bean
    public OrderServiceV2 orderServiceV2(LogTrace logTrace) {
        // Create Target
        OrderServiceV2 orderService = new OrderServiceV2(orderRepositoryV2(logTrace));

        // ProxyFactory
        ProxyFactory proxyFactory = new ProxyFactory(orderService);
        proxyFactory.addAdvisor(getAdvisor(logTrace));

        // Create Proxy
        OrderServiceV2 proxy = (OrderServiceV2) proxyFactory.getProxy();

        log.info("ProxyFactory proxy={}, target={}", proxy.getClass(), orderService.getClass());

        return proxy;
    }

    @Bean
    public OrderRepositoryV2 orderRepositoryV2(LogTrace logTrace) {
        // Create Target
        OrderRepositoryV2 orderRepository = new OrderRepositoryV2();

        // Create ProxyFactory
        ProxyFactory proxyFactory = new ProxyFactory(orderRepository);
        proxyFactory.addAdvisor(getAdvisor(logTrace));

        // Create Proxy
        OrderRepositoryV2 proxy = (OrderRepositoryV2) proxyFactory.getProxy();

        log.info("ProxyFactory proxy={}, target={}", proxy.getClass(), orderRepository.getClass());

        return proxy;
    }

    private Advisor getAdvisor(LogTrace logTrace) {
        // Pointcut
        NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
        pointcut.setMappedNames("request*", "order*", "save*");

        // Advice
        LogTraceAdvice advice = new LogTraceAdvice(logTrace);

        // Advisor는 하나의 Pointcut과 Advice를 갖는다.
        return new DefaultPointcutAdvisor(pointcut, advice);
    }
}
