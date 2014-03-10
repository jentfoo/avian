/* Copyright (c) 2008-2014, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.concurrent.locks;

import java.util.Collection;

public class ReentrantReadWriteLock implements ReadWriteLock {
  private final boolean fair;
  
  public ReentrantReadWriteLock() {
    this(false);
  }
  
  public ReentrantReadWriteLock(boolean fair) {
    this.fair = fair;
  }

  @Override
  public Lock readLock() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Lock writeLock() {
    // TODO Auto-generated method stub
    return null;
  }
  
  public int getQueueLength() {
    // TODO - implement
    throw new UnsupportedOperationException("Not implemented yet");
  }
  
  public int getReadHoldCount() {
    // TODO - implement
    throw new UnsupportedOperationException("Not implemented yet");
  }
  
  public int getReadLockCount() {
    // TODO - implement
    throw new UnsupportedOperationException("Not implemented yet");
  }
  
  protected Collection<Thread> getWaitingThreads(Condition condition) {
    // TODO - implement
    throw new UnsupportedOperationException("Not implemented yet");
  }
  
  public int getWaitQueueLength(Condition condition) {
    return getWaitingThreads(condition).size();
  }
  
  public int getWriteHoldCount() {
    // TODO - implement
    throw new UnsupportedOperationException("Not implemented yet");
  }
  
  public boolean hasQueuedThread(Thread thread) {
    // TODO - implement
    throw new UnsupportedOperationException("Not implemented yet");
  }
  
  public boolean hasQueuedThreads() {
    // TODO - implement
    throw new UnsupportedOperationException("Not implemented yet");
  }
  
  public boolean hasWaiters(Condition condition) {
    return ! getWaitingThreads(condition).isEmpty();
  }
  
  public boolean isFair() {
    return fair;
  }
  
  public boolean isWriteLocked() {
    // TODO - implement
    throw new UnsupportedOperationException("Not implemented yet");
  }
  
  public boolean isWriteLockedByCurrentThread() {
    // TODO - implement
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
