package cn.fnmain;

import cn.fnmain.config.IpType;

public class Main {
    public static void main(String[] args) {
        Loc loc = new Loc(IpType.IPV4, "127.0.0.1", 8080);
        System.out.println(loc.ipType());
    }
}



