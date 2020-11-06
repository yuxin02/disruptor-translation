/*
 * Copyright 2011 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lmax.disruptor;

import java.util.concurrent.locks.LockSupport;

/**
 * 睡眠等待策略。
 * <p>
 * 等待方式：自旋 + yield + sleep
 * <p>
 * 表现：延迟不均匀，吞吐量较低，但是cpu占有率也较低。
 * 算是CPU与性能之间的一个折中，当CPU资源紧张时可以考虑使用该策略。
 *
 *
 * Sleeping strategy that initially spins, then uses a Thread.yield(), and
 * eventually sleep (<code>LockSupport.parkNanos(n)</code>) for the minimum
 * number of nanos the OS and JVM will allow while the
 * {@link com.lmax.disruptor.EventProcessor}s are waiting on a barrier.
 * <p>
 * This strategy is a good compromise between performance and CPU resource.
 * Latency spikes can occur after quiet periods.  It will also reduce the impact
 * on the producing thread as it will not need signal any conditional variables
 * to wake up the event handling thread.
 */
public final class SleepingWaitStrategy implements WaitStrategy
{
	/**
	 * 默认空循环次数
	 */
    private static final int DEFAULT_RETRIES = 200;
    /**
     * 默认每次睡眠时间(睡太久影响响应性，睡太短占用CPU资源)
     */
    private static final long DEFAULT_SLEEP = 100;

    private final int retries;
    private final long sleepTimeNs;

    public SleepingWaitStrategy()
    {
        this(DEFAULT_RETRIES, DEFAULT_SLEEP);
    }

    public SleepingWaitStrategy(int retries)
    {
        this(retries, DEFAULT_SLEEP);
    }

    public SleepingWaitStrategy(int retries, long sleepTimeNs)
    {
        this.retries = retries;
        this.sleepTimeNs = sleepTimeNs;
    }

    @Override
    public long waitFor(
        final long sequence, Sequence cursor, final Sequence dependentSequence, final SequenceBarrier barrier)
        throws AlertException
    {
        long availableSequence;
        int counter = retries;

        while ((availableSequence = dependentSequence.get()) < sequence)
        {
            // 当依赖的消费者还没消费完该序号的事件时执行等待方法
            counter = applyWaitMethod(barrier, counter);
        }

        return availableSequence;
    }

    @Override
    public void signalAllWhenBlocking()
    {
    }

    private int applyWaitMethod(final SequenceBarrier barrier, int counter)
        throws AlertException
    {
        barrier.checkAlert();

        // 空循环计数大于100时，简单的空循环
        if (counter > 100)
        {
            --counter;
        }
        // 空循环计数大于0时，尝试让出CPU
        else if (counter > 0)
        {
            --counter;
            Thread.yield();
        }
        else
        {
        	// 不再空循环占用CPU，睡眠
            LockSupport.parkNanos(sleepTimeNs);
        }

        return counter;
    }
}
