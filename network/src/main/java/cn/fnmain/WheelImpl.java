package cn.fnmain;

import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.lib.Constants;
import org.jctools.queues.atomic.MpscUnboundedAtomicArrayQueue;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class WheelImpl implements Wheel {
    private static final long ONE_TIME_MISSION = -1;
    private static final long CANCEL_ONE_TIME_MISSION = -2;
    private static final long CANCEL_PERIOD_MISSION = -3;
    private static final AtomicBoolean ID_GENERATOR = new AtomicBoolean(false);
    private static final WheelTask exitTask = new WheelTask(Long.MIN_VALUE, Long.MIN_VALUE, ()->{});

    private final int mask;
    private final long tick;
    private final long tickNano;
    private final long bound;
    private final int cMask;
    private final Queue<WheelTask> tasksQueue;
    private final Job[] wheel;
    private final Map<Runnable, Job> jobMap = new HashMap<>();
    private final TreeSet<Job> waitSet = new TreeSet<>(Job::compareTo);
    private final Thread wheelThread;


    public static final Wheel INSTANCE = new WheelImpl(Wheel.slots, Wheel.trick);

    public WheelImpl(int slots, long trick) {
        this.mask = slots-1;
        this.tick = trick;
        this.tickNano = Duration.ofNanos(trick).toNanos();
        this.bound = slots * tick;
        this.cMask = mask >> 1;
        this.tasksQueue = new MpscUnboundedAtomicArrayQueue<>(Constants.KB);
        this.wheel = new Job[slots];
        //用于时间轮的轮询线程
        this.wheelThread = Thread.ofPlatform().name("wheel").unstarted(this::run);
    }

    /*
    计算当前时间轮的任务
     */
    private int calculatePos(int currentSlot, long delayMillis) {
        return (int) ((currentSlot + delayMillis / tick) & mask);
    }

    private void cancelJob(Job job, int pos) {
        if (!waitSet.remove(job)) {
            Job curJob = wheel[pos];
            while (curJob != null) {
                if (curJob.mission == job.mission) {
                    curJob.pos -= 1;
                } else {
                    curJob = curJob.next;
                }
            }
            throw new FrameworkException(ExceptionType.WHEEL, "Wrong use of wheel");
        }
    }

    
    private void cancelPeriodJob(Job job) {
        Job target = jobMap.remove(job.mission);
        if (target != null) {
            if (target.pos == -1) {
                if (!waitSet.remove(target)) {
                    
                }
            }
        }
    }


    private void insertJob(Job job, int pos, long delayMills) {
    }

    private void registerPeriodJobIfNeeded(Job job) {

    }

    /*
    用于运行时间轮中的任务
    */
    private void run() {
        long nano = Clock.nano();
        long milli = Clock.current();
        int slot = 0;

        for(;;) {
            final long currentMilli = milli;
            final int currentSlot = slot;
            nano = nano + tickNano;
            milli = milli + tick;
            slot = (slot + 1) & mask;


            for (;;) {
                final WheelTask task = tasksQueue.poll();

                if (task == null) {
                    break;
                } else if (task == exitTask) {
                    return;
                } else {
                    Job job = new Job(task.execMilli(), task.period(), task.mission());
                    long delayMills = Math.max(task.execMilli() - currentMilli, 0);
                    int pos = calculatePos(currentSlot, delayMills);

                    if (task.period() == CANCEL_ONE_TIME_MISSION) {
                        cancelJob(job, pos);
                    } else if (task.period() == CANCEL_PERIOD_MISSION) {
                        cancelPeriodJob(job);
                    } else {
                        registerPeriodJobIfNeeded(job);
                        insertJob(job, pos, delayMills);
                    }
                }
            }
        }
    }




    @Override
    public void init() {
        wheelThread.start();
    }

    @Override
    public void exit() throws InterruptedException {
        if (!tasksQueue.offer(exitTask)) {
            throw new FrameworkException(ExceptionType.WHEEL, Constants.UNREACHED);
        }
        wheelThread.join();
    }

    @Override
    public Runnable addJob(Runnable mission, Duration delay) {
        return null;
    }

    @Override
    public Runnable addPeriodicJob(Runnable mission, Duration delay, Duration period) {
        return null;
    }

    private static final class Job implements Comparable<Job> {
        private long execMilli;
        private int pos;
        private Job next;
        private final long period;
        private final Runnable mission;

        Job(long execMilli, long period, Runnable mission) {
            this.execMilli = execMilli;
            this.mission = mission;
            this.period = period;
        }

        @Override
        public int compareTo(Job other) {
            return Long.compare(execMilli, other.execMilli);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Job job && mission == job.mission;
        }
    }
}
