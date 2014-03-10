/* Copyright (c) 2008-2014, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.concurrent.locks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ReentrantLock implements Lock {
  private enum LockState { Unlocked, LockedWithoutQueue, LockedWithQueue };
  
  private final Queue<Thread> waitingThreads;
  private final AtomicReference<LockState> lockState;
  private final boolean fair;
  private volatile Thread lockedThread;
  private int lockCount;
  
  public ReentrantLock() {
    this(false);
  }
  
  public ReentrantLock(boolean fair) {
    /* TODO - switch to ConcurrentLinkedQueue when more 
     * implemented to avoid synchronization.
     */
    waitingThreads = new LinkedList<Thread>();
    lockState = new AtomicReference<LockState>(LockState.Unlocked);
    this.fair = true; // TODO - support unfair
    lockedThread = null;
    lockCount = 0;
  }

  @Override
  public void lock() {
    boolean locked = false;
    while (! locked) {
      try {
        lockInterruptibly();
        locked = true;
      } catch (InterruptedException e) {
        // reset interrupted status and retry
        Thread.interrupted();
      }
    }
  }
  
  private boolean lockInterruptibly(long time, TimeUnit unit) throws InterruptedException {
    Thread currThread = Thread.currentThread();
    
    long waitTimeInMillis = unit.toMillis(time);
    long remainingInMillis = waitTimeInMillis;
    long startTime = 0;
    if (waitTimeInMillis < Long.MAX_VALUE) {
      startTime = System.currentTimeMillis();
    }
    
    boolean addedToQueue = false;
    try {
      while (remainingInMillis > 0) {
        // locked thread will get set to current thread if: 
        // * We already have the lock
        // * The previous locked thread now gave us the lock
        if (lockedThread == currThread) {
          lockCount++;
        } else if (lockState.compareAndSet(LockState.Unlocked, LockState.LockedWithoutQueue)) {
          lockedThread = currThread;
          lockCount = 1;
          break;  // now hold lock, so break from loop
        } else {  // we need to queue for the lock
          try {
            if (! addedToQueue) {
              synchronized (waitingThreads) {
                waitingThreads.add(currThread);
              }
              addedToQueue = true;
            }
            
            // this may fail if the lock was released already
            if (lockState.get() != LockState.LockedWithQueue && 
                ! lockState.compareAndSet(LockState.LockedWithoutQueue, LockState.LockedWithQueue)) {
              continue; // try again
            }
            
            LockSupport.park();
            // verify interrupted state after waking up from park
            if (currThread.isInterrupted()) {
              throw new InterruptedException();
            }
          } finally {
            if (waitTimeInMillis < Long.MAX_VALUE) {
              remainingInMillis = waitTimeInMillis - (System.currentTimeMillis() - startTime);
            }
          }
        }
      }
    } finally {
      if (addedToQueue) {
        synchronized (waitingThreads) {
          waitingThreads.remove(currThread);
        }
      }
    }
    
    return lockedThread == currThread;
  }

  @Override
  public void lockInterruptibly() throws InterruptedException {
    lockInterruptibly(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
  }

  @Override
  public Condition newCondition() {
    // TODO - implement
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public boolean tryLock() {
    Thread currThread = Thread.currentThread();
    if (lockedThread == currThread) {
      lockCount++;
      return true;
    } else if (lockState.compareAndSet(LockState.Unlocked, LockState.LockedWithoutQueue)) {
      lockedThread = currThread;
      lockCount = 1;
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean tryLock(long time, TimeUnit unit) {
    try {
      return lockInterruptibly(time, unit);
    } catch (InterruptedException e) {
      // reset interrupted status
      Thread.interrupted();
      return false;
    }
  }

  @Override
  public void unlock() {
    if (lockedThread != Thread.currentThread()) {
      throw new IllegalMonitorStateException();
    }
    
    lockCount--;
    if (lockCount == 0) {
      /* must set to null before we change the lockState....
       * no matter what happens, we know we no longer will be the 
       * one holding the thread. 
       */
      lockedThread = null;
      
      while (true) {
        if (lockState.get() == LockState.LockedWithoutQueue) {
          if (lockState.compareAndSet(LockState.LockedWithoutQueue, LockState.Unlocked)) {
            break;
          }
        }
        
        // if did not break above, then locked with queue
        Thread nextThread;
        synchronized (waitingThreads) {
          nextThread = waitingThreads.remove();
        }
        
        lockedThread = nextThread;
        LockSupport.unpark(nextThread);
      }
    }
  }
  
  public int getHoldCount() {
    if (lockedThread == Thread.currentThread()) {
      return lockCount;
    } else {
      return 0;
    }
  }
  
  protected Thread getOwner() {
    return lockedThread;
  }
  
  protected Collection<Thread> getQueuedThreads() {
    List<Thread> threads;
    synchronized (waitingThreads) {
      threads = new ArrayList<Thread>(waitingThreads);
    }
    return Collections.unmodifiableCollection(threads);
  }
  
  public boolean hasQueuedThread(Thread thread) {
    return getQueuedThreads().contains(thread);
  }
  
  public int getQueueLenght() {
    return getQueuedThreads().size();
  }
  
  public boolean hasQueuedThreads() {
    return ! getQueuedThreads().isEmpty();
  }
  
  protected Collection<Thread> getWaitingThreads(Condition condition) {
    // TODO - implement
    throw new UnsupportedOperationException("Not implemented yet");
  }
  
  public int getWaitQueueLength(Condition condition) {
    return getWaitingThreads(condition).size();
  }
  
  public boolean hasWaiters(Condition condition) {
    return ! getWaitingThreads(condition).isEmpty();
  }
  
  public boolean isFair() {
    return fair;
  }
  
  public boolean isHeldByCurrentThread() {
    return getHoldCount() > 0;
  }
  
  public boolean isLocked() {
    return getOwner() != null;
  }
}
