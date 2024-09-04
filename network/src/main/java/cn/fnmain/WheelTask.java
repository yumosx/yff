package cn.fnmain;

public record WheelTask(long execMilli, long period, Runnable mission) {
}
