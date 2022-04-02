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
