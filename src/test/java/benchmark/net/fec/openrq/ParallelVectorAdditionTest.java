/*
 * Copyright 2014 OpenRQ Team
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
package net.fec.openrq;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.fec.openrq.util.linearalgebra.TestingCommon;
import net.fec.openrq.util.math.ExtraMath;
import net.fec.openrq.util.math.OctetOps;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.BenchmarkParams;


@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.AverageTime)
@Fork(1)
@State(Scope.Benchmark)
public class ParallelVectorAdditionTest {

    // default parameter values
    private static final int DEF_NUMVECS = 1000;
    private static final int DEF_VECSIZE = 1500;
    private static final int DEF_PAR_TASKS = 4;

    private static final int PROC_MULTIPLIER = 1;

    public static final int MAX_THREAD_POOL_SIZE =
                                                   ExtraMath.multiplyExact(
                                                       PROC_MULTIPLIER,
                                                       Runtime.getRuntime().availableProcessors());

    private final ThreadPoolExecutor pool = (ThreadPoolExecutor)Executors.newFixedThreadPool(MAX_THREAD_POOL_SIZE);
    private final List<ParVectorAdditionTask> parTasks = new ArrayList<>();
    private final Random rand = TestingCommon.newSeededRandom();

    @Param("" + DEF_NUMVECS)
    private int numvecs;

    @Param("" + DEF_VECSIZE)
    private int vecsize;

    @Param("" + DEF_PAR_TASKS)
    private int numpartasks;

    private byte[][] srcVecs;
    private byte[][] dstVecs;


    @Setup(Level.Trial)
    public void init(BenchmarkParams benchParams) {

        srcVecs = new byte[numvecs][vecsize];
        randomBytes(srcVecs, rand);

        dstVecs = new byte[numvecs][vecsize];
        randomBytes(dstVecs, rand);

        if (!benchParams.getBenchmark().equals(ParallelVectorAdditionTest.class.getName() + ".testSeq")) {
            pool.prestartAllCoreThreads();
            parTasks.clear();

            final int maxTasks = Math.min(MAX_THREAD_POOL_SIZE, Math.max(1, numpartasks));
            final int numTasks = Math.min(numvecs, maxTasks);

            final Partition part = new Partition(numvecs, numTasks);
            final int NL = part.get(1);
            final int NS = part.get(2);
            final int TL = part.get(3);

            int t, off;
            for (t = 0, off = 0; t < TL; t++, off += NL) {
                parTasks.add(new ParVectorAdditionTask(srcVecs, dstVecs, off, off + NL));
            }

            for (; t < numTasks; t++, off += NS) {
                parTasks.add(new ParVectorAdditionTask(srcVecs, dstVecs, off, off + NS));
            }
        }
    }

    private static void randomBytes(byte[][] bytes, Random rand) {

        for (byte[] bs : bytes) {
            rand.nextBytes(bs);
        }
    }

    @TearDown(Level.Trial)
    public void finish() {

        pool.shutdown();
    }

    @Benchmark
    public void testSeq() {

        for (int i = 0; i < numvecs; i++) {
            OctetOps.vectorVectorAddition(srcVecs[i], dstVecs[i], dstVecs[i]);
        }
    }

    @Benchmark
    public void testPar() throws InterruptedException {

        pool.invokeAll(parTasks);
    }


    private static final class ParVectorAdditionTask implements Callable<Void> {

        private final byte[][] srcVecs, dstVecs;
        private final int from, to;


        ParVectorAdditionTask(byte[][] srcVecs, byte[][] dstVecs, int from, int to) {

            this.srcVecs = srcVecs;
            this.dstVecs = dstVecs;
            this.from = from;
            this.to = to;
        }

        @Override
        public Void call() {

            for (int i = from; i < to; i++) {
                OctetOps.vectorVectorAddition(srcVecs[i], dstVecs[i], dstVecs[i]);
            }

            return null;
        }
    }
}
