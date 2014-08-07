package java.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ThreadPoolExecutor implements ExecutorService {
  private static final RejectedExecutionHandler DEFAULT_REJECTION_HANDLER;
  
  static {
    DEFAULT_REJECTION_HANDLER = new RejectedExecutionHandler() {
      @Override
      public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        throw new RejectedExecutionException("Could not execute runnable: " + r + " when provided to thread pool: " + executor);
      }
    };
  }
  
  public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, 
                            BlockingQueue<Runnable> workQueue) {
    this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, 
         Executors.defaultThreadFactory(), DEFAULT_REJECTION_HANDLER);
  }
  
  public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, 
                            BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
    this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, 
         Executors.defaultThreadFactory(), handler);
  }
  
  public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, 
                            BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
    this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, 
         threadFactory, DEFAULT_REJECTION_HANDLER);
    
  }
  
  public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, 
                            BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
    // TODO 
  }

  @Override
  public void execute(Runnable task) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void shutdown() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean isShutdown() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isTerminated() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    FutureTask<T> ft = new FutureTask<T>(task);
    execute(ft);
    return ft;
  }

  @Override
  public Future<?> submit(Runnable task) {
    return submit(task, null);
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    FutureTask<T> ft = new FutureTask<T>(Executors.callable(task, result));
    execute(ft);
    return ft;
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
    return invokeAll(tasks, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, 
                                       long timeout, TimeUnit unit) throws InterruptedException {
    long startTime = System.currentTimeMillis();
    long timeoutInMs = unit.toMillis(timeout);
    // execute all the tasks provided
    List<Future<T>> resultList = new ArrayList<Future<T>>(tasks.size());
    for (Callable<T> c : tasks) {
      Future<T> f = submit(c);
      resultList.add(f);
    }
    // block till all tasks finish, or we reach our timeout
    Iterator<Future<T>> it = resultList.iterator();
    long remainingMillis = timeoutInMs - (System.currentTimeMillis() - startTime); 
    while (it.hasNext() && remainingMillis > 0) {
      Future<T> f = it.next();
      try {
        // block till future completes or we reach a timeout
        f.get(remainingMillis, TimeUnit.MILLISECONDS);
      } catch (ExecutionException e) {
        // ignored here, task finished and that's what we care about
      } catch (TimeoutException e) {
        f.cancel(true);
        break;
      }
      remainingMillis = timeoutInMs - (System.currentTimeMillis() - startTime);
    }
    // cancel any tasks which have not completed by this point, notice we use the same iterator
    while (it.hasNext()) {
      it.next().cancel(true);
    }
    
    return resultList;
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException,
                                                                         ExecutionException {
    try {
      return invokeAny(tasks, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, 
                         long timeout, TimeUnit unit) throws InterruptedException,
                                                             ExecutionException, 
                                                             TimeoutException {
    final long startTime = System.currentTimeMillis();
    if (tasks.size() < 1) {
      throw new IllegalArgumentException();
    }
    final long timeoutInMs = unit.toMillis(timeout);
    int failureCount = 0;
    List<Future<T>> submittedFutures = new ArrayList<Future<T>>(tasks.size());
    
    try {
      ExecutorCompletionService<T> ecs = new ExecutorCompletionService<T>(this);
      ExecutionException lastEE = null;
      Iterator<? extends Callable<T>> it = tasks.iterator();
      // submit first one
      Future<T> submittedFuture = ecs.submit(it.next());
      submittedFutures.add(submittedFuture);
      
      while (it.hasNext() && 
             System.currentTimeMillis() - startTime < timeoutInMs) {
        Future<T> completedFuture = ecs.poll();
        if (completedFuture == null) {
          // submit another
          submittedFutures.add(ecs.submit(it.next()));
        } else {
          try {
            return completedFuture.get();
          } catch (ExecutionException e) {
            failureCount++;
            lastEE = e;
          }
        }
      }
      
      long remainingTime = timeoutInMs - (System.currentTimeMillis() - startTime);
      while (remainingTime > 0 && failureCount < submittedFutures.size()) {
        Future<T> completedFuture = ecs.poll(remainingTime, TimeUnit.MILLISECONDS);
        if (completedFuture == null) {
          throw new TimeoutException();
        } else {
          try {
            return completedFuture.get();
          } catch (ExecutionException e) {
            failureCount++;
            lastEE = e;
          }
        }
        
        remainingTime = timeoutInMs - (System.currentTimeMillis() - startTime);
      }
      
      if (remainingTime <= 0) {
        throw new TimeoutException();
      } else {
        /* since we know we have at least one task provided, and since nothing returned by this point
         * we know that we only got ExecutionExceptions, and thus this should NOT be null
         */
        throw lastEE;
      }
    } finally {
      Iterator<Future<T>> it = submittedFutures.iterator();
      while (it.hasNext()) {
        it.next().cancel(true);
      }
    }
  }
}
