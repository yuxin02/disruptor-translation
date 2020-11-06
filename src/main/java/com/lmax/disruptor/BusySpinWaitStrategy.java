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


import com.lmax.disruptor.util.ThreadHints;

/**
 * 该策略使用自旋(空循环)来在barrier上等待。
 * 该策略通过占用CPU资源去比避免系统调用带来的延迟抖动。最好在线程能绑定到特定的CPU核心时使用。
 * (会持续占用CPU资源，基本不会让出CPU资源)
 * <p>
 * 特征：极低的延迟，极高的吞吐量，以及极高的CPU占用。
 * </p>
 * 如果你要使用该等待策略，确保有足够的CPU资源，且你能接受它带来的CPU使用率。
 *
 * Busy Spin strategy that uses a busy spin loop for {@link com.lmax.disruptor.EventProcessor}s waiting on a barrier.
 * <p>
 * This strategy will use CPU resource to avoid syscalls which can introduce latency jitter.  It is best
 * used when threads can be bound to specific CPU cores.
 */
public final class BusySpinWaitStrategy implements WaitStrategy
{
    @Override
    public long waitFor(
        final long sequence, Sequence cursor, final Sequence dependentSequence, final SequenceBarrier barrier)
        throws AlertException, InterruptedException
    {
        long availableSequence;

        // 确保该序号已经被我前面的消费者消费(协调与其他消费者的关系)
        while ((availableSequence = dependentSequence.get()) < sequence)
        {
            barrier.checkAlert();
            // 自旋等待
            ThreadHints.onSpinWait();
        }

        return availableSequence;
    }

    @Override
    public void signalAllWhenBlocking()
    {
    }
}
