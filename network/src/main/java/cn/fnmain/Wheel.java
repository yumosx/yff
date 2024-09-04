package cn.fnmain;

import java.time.Duration;

public sealed interface Wheel extends LifeCycle permits WheelImpl{
    /*
    表示对应时间槽数
     */
    int slots = Integer.getInteger("wheel.slots", 4096);
    /*
    代表每个时间轮槽消耗的时间数量
     */
    long trick = Long.getLong("wheel.trick", 10L);

    /*
    对外提供的接口, 用于创建对应的实例
     */
    static Wheel wheel() {
        return WheelImpl.INSTANCE;
    }

    /*
    用于向时间轮中添加对应单次执行的任务
     */
    Runnable addJob(Runnable mission, Duration delay);

    /*
    用于向时间轮中添加需要周期性执行的任务
     */
    Runnable addPeriodicJob(Runnable mission, Duration delay, Duration period);
}
