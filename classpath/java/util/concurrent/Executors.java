package java.util.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

public class Executors {
  public static Callable<Object> callable(Runnable task) {
    return callable(task, null);
  }
  
  public static <T> Callable<T> callable(final Runnable task, final T result) {
    return new Callable<T>() {
      @Override
      public T call() {
        task.run();
        
        return result;
      }
    };
  }
  
  public static ThreadFactory defaultThreadFactory() {
    return new DefaultThreadFactory();
  }
    
  private static class DefaultThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    
    private DefaultThreadFactory() {
      group = Thread.currentThread().getThreadGroup();
      namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
    }
    
    @Override
    public Thread newThread(Runnable r) {
      Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement());
      
      return t;
    }
  }
}
