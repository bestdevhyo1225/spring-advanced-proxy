package hello.proxy.pureproxy.concreteproxy.code;

public class ConcreteClient {

    private final ConcreteLogic concreteLogic; // ConcreteLogic, Proxy 모두 주입 가능

    public ConcreteClient(ConcreteLogic concreteLogic) {
        this.concreteLogic = concreteLogic;
    }

    public void execute() {
        concreteLogic.operation();
    }
}
