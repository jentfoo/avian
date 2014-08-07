package java.util.concurrent;

public interface RejectedExecutionHandler {
  public void rejectedExecution(Runnable r, ThreadPoolExecutor executor);
}
