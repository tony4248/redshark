package com.redshark.engine;

/**
 * To facilitate more complex configuration of thread puddles, one can use a
 * factory, chaining configurations.
 * 
 * @author houghton
 *
 */
public class ThreadPuddleFactory {
	private int threads = ThreadPuddle.defaultNumberOfThreads(), taskLimit = ThreadPuddle.defaultMaxTasks(getThreads());
	private boolean limitless = false, fifo = true;

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public ThreadPuddleFactory threads(int threads) {
		setThreads(threads);
		return this;
	}

	public int getTaskLimit() {
		return taskLimit;
	}

	public void setTaskLimit(int taskLimit) {
		this.taskLimit = taskLimit;
	}

	public ThreadPuddleFactory taskLimit(int taskLimit) {
		setTaskLimit(taskLimit);
		return this;
	}

	public boolean isLimitless() {
		return limitless;
	}

	public void setLimitless(boolean limitless) {
		this.limitless = limitless;
	}

	public ThreadPuddleFactory limitless(boolean limitless) {
		setLimitless(limitless);
		return this;
	}

	public boolean isFifo() {
		return fifo;
	}

	public void setFifo(boolean fifo) {
		this.fifo = fifo;
	}

	public ThreadPuddleFactory fifo(boolean fifo) {
		setFifo(fifo);
		return this;
	}

	public ThreadPuddle build() {
		return new ThreadPuddle(threads, taskLimit, fifo, limitless);
	}
}
