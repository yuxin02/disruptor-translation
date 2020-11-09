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

import com.lmax.disruptor.AlertException;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.util.ThreadHints;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Blocking strategy that uses a lock and condition variable for {@link EventProcessor}s waiting on a barrier.
 * <p>
 * This strategy can be used when throughput and low-latency are not as important as CPU resource.
 */
public final class BlockingWaitStrategy implements WaitStrategy {
    private final Lock lock = new ReentrantLock();
    private final Condition processorNotifyCondition = lock.newCondition();

    @Override
    public long waitFor(long wantedSequence,
                        Sequence cursorSequence,
                        Sequence dependentSequence,
                        SequenceBarrier barrier)
            throws AlertException, InterruptedException {
        // 确保生产者已生产了该数据，这期间可能阻塞
        long availableSequence;
        if (cursorSequence.get() < wantedSequence) {
            lock.lock();
            try {
                while (cursorSequence.get() < wantedSequence) {
                    barrier.checkAlert();
                    processorNotifyCondition.await();
                }
            } finally {
                lock.unlock();
            }
        }

        // 等待前驱消费者消费完对应的事件，这是实现消费者之间happens-before的关键
        // 如果没有其他dependent，dependent和cursor是同一个, 这里直接满足了
        // 如果依赖其他消费者，还需要判断和其他消费者之间的关系，等待消费最慢的（最后一名）也赶上申请的sequence
        while ((availableSequence = dependentSequence.get()) < wantedSequence) {
            barrier.checkAlert();
            ThreadHints.onSpinWait();
        }

        return availableSequence;
    }

    @Override
    public void signalAllWhenBlocking() {
        lock.lock();
        try {
            processorNotifyCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return "BlockingWaitStrategy{processorNotifyCondition=" + processorNotifyCondition + '}';
    }
}
