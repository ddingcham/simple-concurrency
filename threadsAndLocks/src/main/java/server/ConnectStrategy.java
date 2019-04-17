package server;

public interface ConnectStrategy {
    void execute(Runnable handler);
}
