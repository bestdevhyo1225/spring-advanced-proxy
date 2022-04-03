package hello.proxy.cglib;

import hello.proxy.cglib.code.TimeMethodInterceptor;
import hello.proxy.common.service.ConcreteService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.cglib.proxy.Enhancer;

@Slf4j
public class CglibTest {

    @Test
    void cglib() {
        ConcreteService target = new ConcreteService();

        // CGLIB를 만들어주는 코드이며, ConcreteService를 상속받아서 프록시 객체를 생성한다.
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(ConcreteService.class); // 구체 클래스를 지정한다.
        enhancer.setCallback(new TimeMethodInterceptor(target));
        ConcreteService proxy = (ConcreteService) enhancer.create(); // 지정한 구체 클래스를 상속 받아서 프록시를 생성한다.

        log.info("targetClass={}", target.getClass());
        log.info("proxyClass={}", proxy.getClass());

        proxy.call();
    }
}
