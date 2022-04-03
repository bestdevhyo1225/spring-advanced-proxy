# Spring 핵심 원리 고급편 - Proxy

## Controller, Service, Repository 만들기

- `V1` -> 인터페이스를 구현한 구체화 클래스를 수동으로 스프링 Bean에 등록하여 사용하기
- `V2` -> 구체화 클래스를 수동으로 스프링 Bean에 등록하여 사용하기
- `V3` -> 구체화 클래스가 자동으로 스프링 Bean에 등록되고, 이를 사용하기 (`@Contorller`, `@Service`, `@Repsotiroy`)

3가지 방식으로 Controller, Service, Repository를 만들어봤는데, 원본 코드를 전혀 수정하지 않고, 로그 추적기를 도입할 수 있을까? `AOP` 가 떠오르긴 하는데, 이를
해결하려면, `프록시(Proxy)` 의 개념을 먼저 이해해야 한다.

## 프록시 (Proxy)

### 클라이언트와 서버

클라이언트와 서버의 의미는 광범위하게 사용되고 있지만, 기본 개념을 정의하면 다음과 같다.

- **`클라이언트는 서버에 필요한 것을 요청하고, 서버는 클라이언트의 요청을 처리하는 것!`**

> 컴퓨터 네트워크에 도입하는 경우

- 클라이언트 : 웹 브라우저
- 서버 : 웹 서버

> 객체에 도입하는 경우

- 클라이언트 : 요청하는 객체
- 서버 : 요청을 처리하는 객체

보통 `클라이언트 <-> 서버` 의 개념에서는 `직접 호출` 을 통해 요청과 처리 과정이 이루어 진다. 그런데 `프록시` 라는 것을 `클라이언트 <-> 프록시 <-> 서버` 형식으로
구성한다면, **`클라이언트가 서버에 직접 호출하는 것이 아닌 대리자(프록시)를 통해 간접적으로 서버에 요청할 수 있다.`**

### 프록시는 대체 가능해야 함

객체에서 프록시가 되려면, `클라이언트는 서버에게 요청을 한 것인지, 프록시에게 요청을 한 것인지 조차 몰라야 한다.` 즉, 서버와 프록시는 `같은 인터페이스`를 사용해야 한다. 그리고 클라이언트가
사용하는 `서버 객체를 프록시 객체로 변경` 해도 `클라이언트 코드는 변경하지 않고 동작` 해야 한다.

### Dependency Injection를 사용해서 서버를 프록시로 변경

런타임 시점에 클라이언트 객체에 DI를 사용해서 `Client -> Server` 에서 `Client -> Proxy` 로 객체 의존관계를 변경해도 `클라이언트 코드는 전혀 변경하지 않아도 된다.` 클라이언트
입장에서는 변경 사실 조차 모른다. 즉, `DI를 사용하면 클라이언트 코드 변경 없이 유연하게 프록시를 주입할 수 있다.`

### 프록시의 주요 기능

- **`접근 제어`**
    - 권한에 따른 접근 차단
    - 캐싱
    - 지연 로딩
- **`부가 기능 추가`**
    - 원래 서버가 하는일에 더해서 부가 기능을 수행한다.

### GOF 패턴

프록시를 사용하는 방법에 있어서 `프록시 패턴` 과 `데코레이터 패턴` 이 있지만, 의도에 따라서 구분한다.

- **`프록시 패턴`** : `접근 제어` 가 목적
- **`데코레이터 패턴`** : `새로운 기능 추가` 가 목적

## 프록시 패턴

### 예시

만약에 `DB` 를 통해 `data` 라는 값을 조회하는데, `1초` 가 소요된다고 가정해보자. 프록시 패턴을 도입하기 전에는 아래와 같은 코드로 구현할 수 있다.

> Client

```java
public class ProxyPatternClient {

    private final Subject subject;

    public ProxyPatternClient(Subject subject) {
        this.subject = subject;
    }

    public void execute() {
        subject.operation();
    }
}
```

> Subject 인터페이스

```java
public interface Subject {
    String operation();
}
```

> Subject 인터페이스의 구현체인 RealSubject

```java

@Slf4j
public class RealSubject implements Subject {

    @Override
    public String operation() {
        log.info("실제 객체 호출");
        sleep(1_000);
        return "data";
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

위와 같이 구현된 코드를 돌려보면, 다음과 같은 결과를 얻을 수 있다.

> Test

```java
public class ProxyPatternTest {

    @Test
    void noProxyTest() {
        Subject realSubject = new RealSubject();
        ProxyPatternClient client = new ProxyPatternClient(realSubject);
        client.execute();
        client.execute();
        client.execute();
    }
}
```

클라이언트는 3번의 `execute()` 메서드를 호출하며, 총 `3초` 의 시간이 걸린다. 이 때, 프록시 패턴을 도입하고, `프록시 패턴의 핵심` 인 `접근 제어(캐싱)` 를 통해 개선해보겠다.

> CacheProxy

```java

@Slf4j
public class CacheProxy implements Subject {

    private final Subject target; // RealSubject
    private String cacheValue;

    public CacheProxy(Subject target) {
        this.target = target;
    }

    @Override
    public String operation() {
        log.info("프록시 호출");
        if (cacheValue == null) {
            cacheValue = target.operation();
        }
        // cacheValue가 있다면, RealSubject를 호출하지 않는다. -> 접근 제어
        return cacheValue;
    }
}
```

`CacheProxy` 클래스는 `Subject` 인터페이스를 구현했다.

- `프록시와 서버는 같은 인터페이스` 를 구현해야 클라이언트의 코드 변경이 없다.

추가로 `target` 은 `RealSubject` 를 참조하고 있어야 하며, `operation` 메서드 내부를 보면, `cacheValue` 값이 `null` 인 경우에만 `RealSubject` 에 접근하도록
하고, `cacheValue` 값이 있다면, `RealSubject` 를 호출하지 않고 **`접근 제어`** 를 한다.

> Test

```java
public class ProxyPatternTest {

    @Test
    void cacheProxyTest() {
        Subject realSubject = new RealSubject();
        Subject cacheProxy = new CacheProxy(realSubject);
        ProxyPatternClient client = new ProxyPatternClient(cacheProxy);
        client.execute();
        client.execute();
        client.execute();
    }
}
```

위의 테스트 코드를 실행하면, 아래와 같은 결과를 확인할 수 있고, `3초(3번 RealSubject 접근)` 걸렸던 수행
시간에서 `1초(처음 RealSubject 접근) + 0.XXX초(RealSubject 접근 제어) + 0.XXX초(RealSubject 접근 제어)` 의 결과를 확인할 수 있다.

<img width="713" alt="스크린샷 2022-04-02 오후 3 17 43" src="https://user-images.githubusercontent.com/23515771/161369728-770b1b5b-de51-4505-8ba3-5cbf7ccb6980.png">

### 핵심

`RealSubject` 코드와 `ProxyClientPattern` 코드를 `전혀 변경하지 않고`, 프록시를 도입해서 `접근 제어` 를 했다는 점이다. 따라서 클라이언트 코드의 변경 없이 자유롭게 프록시를 교체할
수 있다. 실제 클라이언트 입장에서는 `프록시 객체가 주입 되었는지, 실제 객체가 주입 되었는지` 알 수 없다.

## 데코레이터 패턴

### 예시

`DB` 에서 조회한 `data` 값을 반환하는 예시를 들어보겠다.

> Component

```java
public interface Component {
    String operation();
}
```

> RealComponent

```java

@Slf4j
public class RealComponent implements Component {

    @Override
    public String operation() {
        log.info("RealComponent 실행");
        return "data";
    }
}
```

> Client

```java

@Slf4j
public class DecoratorPatternClient {

    private final Component component;

    public DecoratorPatternClient(Component component) {
        this.component = component;
    }

    public void execute() {
        String result = component.operation();
        log.info("result = {}", result);
    }
}
```

> Test

```java
public class DecoratorPatternTest {

    @Test
    void noDecorator() {
        Component realComponent = new RealComponent();
        DecoratorPatternClient client = new DecoratorPatternClient(realComponent);
        client.execute();
    }
}
```

위와 같이 구현할 수 있다. 그런데 `data` 값을 다른 형식으로 변환해서 반환하라는 요구 사항을 받게 되었는데, 이를 해결하려면, `데코레이터 패턴` 을 적용하면 된다.

> MessageDecorator

```java

@Slf4j
public class MessageDecorator implements Component {

    private final Component component;

    public MessageDecorator(Component component) {
        this.component = component;
    }

    @Override
    public String operation() {
        log.info("MessageDecorator 실행");

        String result = component.operation();
        String decoratorResult = "*****" + result + "*****";

        log.info("MessageDecorator 메시지 꾸미기 [적용 전={}, 적용 후={}]", result, decoratorResult);

        return decoratorResult;
    }
}
```

> Test

```java
public class DecoratorPatternTest {

    @Test
    void decorator1() {
        Component realComponent = new RealComponent();
        Component messageDecorator = new MessageDecorator(realComponent);
        DecoratorPatternClient client = new DecoratorPatternClient(messageDecorator);
        client.execute();
    }
}
```

위의 `MessageDecorator` 클래스를 구현하여, `DecoratorPatternClient -> MessageDecorator -> RealComponent` 의 런타임 의존관계를 구성하고, 데코레이터
패턴의 핵심인 `부가 기능(값을 변환해서 반환)` 을 통해 요구 사항을 해결할 수 있다. 또한 시간 측정을 요구하는 요청이 들어왔다면, `데코레이터 패턴을 체인으로 연결` 해서 구현할 수 있다.

> TimeDecorator

```java

@Slf4j
public class TimeDecorator implements Component {

    private final Component component;

    public TimeDecorator(Component component) {
        this.component = component;
    }

    @Override
    public String operation() {
        log.info("TimeDecorator 실행");

        long startTime = System.currentTimeMillis();

        String result = component.operation();

        long endTime = System.currentTimeMillis();

        log.info("TimeDecorator 종료 [resultTime={}ms]", (endTime - startTime));

        return result;
    }
}
```

> Test

```java
public class DecoratorPatternTest {

    @Test
    void decorator2() {
        Component realComponent = new RealComponent();
        Component messageDecorator = new MessageDecorator(realComponent);
        Component timeDecorator = new TimeDecorator(messageDecorator);
        DecoratorPatternClient client = new DecoratorPatternClient(timeDecorator);
        client.execute();
    }
}
```

`TimeDecorator` 를 구현하고, `DecoratorPatternClient -> TimeDecorator -> MessageDecorator -> RealComponent` 의 런타임 의존관계를 구성하여,
요구 사항을 해결할 수 있다.

### 핵심

데코레이터 패턴은 `MessageDecorator`, `TimeDecorator` 를 체인 형식을 통해 **`부가 기능`** 을 계속해서 추가할 수 있다.

## 프록시 패턴 vs 데코레이터 패턴

프록시 패턴과 데코레이터 패턴의 구현하는 형식은 비슷한 것 같은데 어떻게 구분할까?

### 중요한 것은 의도!

프록시 패턴과 데코레이터 패턴의 모양은 거의 같다. 디자인 패턴에서 두 패턴을 구분하려면, 겉 모양이 아닌 **`패턴을 만든 의도가 중요하다.`**

- `프록시 패턴` : 다른 객체에 대한 **`접근 제어(권한에 따른 접근 차단, 캐싱)`** 를 하기 위해 대리자를 제공
- `데코레이터 패턴` : 객체에 **`추가 책임(기능)을 동적으로 추가`** 하고, 기능 확장을 위한 유연한 대안 제공

## V1 - Controller, Serivce, Repository

V1의 `Controller, Service, Repository` 인터페이스에 대한 `Proxy` 클래스를 구현한다. 그리고 Bean을 등록할 때, `Controller, Service, Repository` 의
실제 구현체를 등록하면 안되고, `Proxy` 구현체를 등록해야 한다. 따라서 런타인 객체 의존관계는 다음과 같다.

### 기존

> AppV1Config 클래스에는 아래와 같은 의존관계가 구성되어 있음.

`Client -> OrderControllerV1Impl(실제 객체) -> OrderServiceV1Impl(실제 객체) -> OrderRepositoryV1Impl(실제 객체)` 의 런타임 객체 의존관계가
이루어졌다.

### 프록시 도입

> InterfaceProxyConfig 클래스에는 아래와 같은 의존관계가 구성되어 있음.

`Client -> OrderControllerV1Proxy(프록시 객체) -> OrderControllerV1Impl(실제 객체) -> OrderServiceV1Proxy(프록시 객체) -> OrderServiceV1Impl(실제 객체) -> OrderRepositoryV1Proxy(프록시 객체) -> OrderRepositoryV1Impl(실제 객체)`
과 같은 런타임 객체 의존관계가 구성된다.

### 정리

- `Proxy -> Target` 이라는 의존관계를 갖게 된다.
- 실제 객체 대신 `프록시 객체` 가 주입된다.

## V2 - Controller, Serivce, Repository

인터페이스 뿐만 아니라 `Class` 기반의 `Proxy` 도 구현할 수 있다.

### 기존

> AppV2Config

`Client -> OrderControllerV2Impl(실제 객체) -> OrderServiceV2Impl(실제 객체) -> OrderRepositoryV2Impl(실제 객체)`

### 프록시 도입

> ConcreteProxyConfig

`Client -> OrderControllerV2Proxy(프록시 객체) -> OrderControllerV2Impl(실제 객체) -> OrderServiceV2Proxy(프록시 객체) -> OrderServiceV2Impl(실제 객체) -> OrderRepositoryV2Proxy(프록시 객체) -> OrderRepositoryV2Impl(실제 객체)`

### 정리

- `Target` 을 상속 받아서, `Proxy` 객체를 구현한다.
    - 다만, `Target` 객체에 `기본 생성자` 가 없는 경우 `super(null)` 을 명시적으로 구현한다.
      -> **`부가 기능 목적으로 Target 을 상속하는 것이지 Target 의 상위 기능을 사용하려고 상속하는 것이 아니기 때문이다.`**
- 실제 객체 대신 `프록시 객체` 가 주입된다.

## 인터페이스 기반 프록시 vs 클래스 기반 프록시

- 클래스 기반 프록시는 해당 클래스에만 적용할 수 있다.
- 인터페이스 기반 프록시는 인터페이스만 같으면, 모든 곳에 적용할 수 있다.
- 클래스 기반 프록시는 상속을 사용하기 때문에 몇가지 제약이 있다.
    - `부모 클래스의 생성자를 호출해야한다.`
    - `클래스에 final이 붙으면, 상속이 불가능하다.`
    - `메서드에 final이 붙으면, 해당 메서드를 오버라이딩 할 수 없다.`

## 리플렉션

런타임 시점에 클래스나 메서드의 메타정보를 동적으로 획득하고, 코드도 동적으로 호출할 수 있는 기술이다.

### 주의!

클래스와 메서드의 메타정보를 사용해서 애플리케이션을 동적으로 유연하게 만들 수 있다. 하지만 리플렉션 기술은 런타임에 동작하기 때문에 **`컴파일 시점에 오류를 잡을 수 없다.`**
가장 좋은 오류는 `개발자가 즉시 확인할 수 있는 컴파일 오류` 이며, 가장 무서운 오류는 `사용자가 직접 실행할 때, 발생하는 런타임 오류` 이다. 따라서 **`리플렉션은 일반적으로 사용하면 안된다.`**

## JDK 동적 프록시

해당 기술을 사용하면, 개발자가 직접 프록시 클래스를 만들지 않아도 된다. 이름 그대로 프록시 객체를 동적으로 런타임 시점에 개발자 대신 만들어준다.

### 중요!

JDK 동적 프록시는 `인터페이스` 기반으로 프록시를 동적으로 만들어 주기 때문에 `인터페이스가 필수` 이다.

### 예제

> AInterface

```java
public interface AInterface {
    String call();
}
```

> AImpl

```java

@Slf4j
public class AImpl implements AInterface {

    @Override
    public String call() {
        log.info("A 호출");
        return "a";
    }
}
```

> TimeInvocationHandler

```java

@Slf4j
public class TimeInvocationHandler implements InvocationHandler {

    private final Object target;

    public TimeInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("TimeProxy 실행");

        long startTime = System.currentTimeMillis();

        Object result = method.invoke(target, args);

        long endTime = System.currentTimeMillis();
        long resultTime = endTime - startTime;

        log.info("TimeProxy 종료 resultTime={}", resultTime);

        return result;
    }
}
```

> Test

```java

@Slf4j
public class JdkDynamicProxyTest {

    @Test
    void dynamicA() {
        AInterface target = new AImpl();
        TimeInvocationHandler handler = new TimeInvocationHandler(target);

        AInterface proxy = (AInterface) Proxy.newProxyInstance(AInterface.class.getClassLoader(), new Class[]{AInterface.class}, handler);
        proxy.call();

        log.info("targetClass={}", target.getClass());
        log.info("proxyClass={}", proxy.getClass());
    }
}
```

위와 같은 코드에서 `proxy` 라는 변수의 `call()` 메서드 호출하면, `TimeInvocationHandler` 클래스의 `invoke()` 메서드 가 호출되어 실행된다. 이 때, `invoke()`
메서드 내부에서는 `target` 의 `call()` 을 호출해야 하기 때문에 `method.invoke(target, args)` 를 호출한다.

### 예제 코드에 대한 실행 순서 정리

1. 클라이언트는 JDK 동적 프록시의 `call()` 을 실행한다.
2. JDK 동적 프록시는 `InvocationHanlder.invoke()` 를 호출한다. `TimeInvocationHandler` 가 구현체로 있기
   때문에 `TimeInvocationHandler.invoke()` 가 호출된다.
3. `TimeInvocationHandler` 가 내부 로직을 수행하고, `method.invoke(target, args)` 를 호출해서 `target` 의 실제 객체인 `AImpl` 을 호출한다.
4. `AImpl` 의 `call()` 이 실행된다.
5. `AImpl` 의 `call()` 의 실행이 끝나면, `TimeInvocationHandler` 로 응답이 돌아온다. 시간 로그를 출력하고, 결과를 반환한다.

### JDK 동적 프록시 도입 전, 런타임 객체 의존관계

`Client` -> **`AProxy(개발자가 AInterface를 직접 구현한 프록시 객체)`** -> `AImpl(AInterface를 구현한 실제 객체)`

### JDK 동적 프록시 도입 후, 런타임 객체 의존관계

`Client` -> **`$Proxy1(런타임에 동적으로 생성된 AInterface의 프록시 객체) - handler.invoke()`**
-> `TimeInvocationHandler(InvocationHandler) - method.invoke(target, args)` -> `AImpl`

## V1 - Controller, Service, Repository (동적 프록시 적용)

### JDK 동적 프록시 적용 전

`Client` ->

| Proxy | Target |
| :-: | :-: |
| `OrderControllerV1Proxy`(개발자가 OrderControllerV1 인터페이스를 직접 구현한 프록시 객체) -> | `OrderControllerV1Impl(실제 객체)` -> |
| `OrderServiceV1Proxy`(개발자가 OrderServiceV1 인터페이스를 직접 구현한 프록시 객체) -> | `OrderServiceV1Impl(실제 객체)` -> | 
| `OrderRepositoryV1Proxy`(개발자가 OrderRepositoryV1 인터페이스를 직접 구현한 프록시 객체) -> | `OrderRepositoryV1Impl(실제 객체)` |

### JDK 동적 프록시 적용 후

`Client` ->
`$Proxy1(런타임 시점에 동적으로 생성된 OrderControllerV1의 프록시 객체)` -> `LogTraceBasicHandler(InvocationHandler)` -> `OrderControllerV1Impl(실제 객체)`
`$Proxy2(런타임 시점에 동적으로 생성된 OrderServiceV1의 프록시 객체)` -> `LogTraceBasicHandler(InvocationHandler)` -> `OrderServiceV1Impl(실제 객체)`
`$Proxy3(런타임 시점에 동적으로 생성된 OrderRepositoryV1의 프록시 객체)` -> `LogTraceBasicHandler(InvocationHandler)` -> `OrderRepositoryV1Impl(실제 객체)`
`
