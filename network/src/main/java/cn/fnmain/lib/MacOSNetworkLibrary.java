package cn.fnmain.lib;

import cn.fnmain.Mux;
import cn.fnmain.Socket;
import cn.fnmain.Timeout;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public class MacOSNetworkLibrary implements OsNetworkLibrary {
    @Override
    public int connectBlockCode() {
        return 0;
    }

    @Override
    public int sendBlockCode() {
        return 0;
    }

    @Override
    public int interruptCode() {
        return 0;
    }

    @Override
    public int ipv4AddressLen() {
        return 0;
    }

    @Override
    public int ipv6AddressLen() {
        return 0;
    }

    @Override
    public int ipv4AddressSize() {
        return 0;
    }

    @Override
    public int ipv6AddressSize() {
        return 0;
    }

    @Override
    public int ipv4AddressAlign() {
        return 0;
    }

    @Override
    public int ipv6AddressAlign() {
        return 0;
    }

    @Override
    public Mux createMux() {
        return null;
    }

    @Override
    public MemoryLayout eventLayout() {
        return null;
    }

    @Override
    public int ctl(Mux mux, Socket socket, int from, int to) {
        return 0;
    }

    @Override
    public int muxWait(Mux mux, MemorySegment events, int maxEvents, Timeout timeout) {
        return 0;
    }

    @Override
    public IntPair access(MemorySegment events, int index) {
        return null;
    }

    @Override
    public int closeMux(Mux mux) {
        return 0;
    }

    @Override
    public int setIpv4SockAddr(MemorySegment sockAddr, MemorySegment address, short port) {
        return 0;
    }

    @Override
    public int setIpv6SockAddr(MemorySegment sockAddr, MemorySegment address, short port) {
        return 0;
    }

    @Override
    public Socket createIpv4Socket() {
        return null;
    }

    @Override
    public Socket createIpv6Socket() {
        return null;
    }

    @Override
    public int setReuseAddr(Socket socket, boolean b) {
        return 0;
    }

    @Override
    public int setKeepAlive(Socket socket, boolean b) {
        return 0;
    }

    @Override
    public int setTcpNoDelay(Socket socket, boolean b) {
        return 0;
    }

    @Override
    public int setIpv6Only(Socket socket, boolean b) {
        return 0;
    }

    @Override
    public int setNonBlocking(Socket socket) {
        return 0;
    }

    @Override
    public short getIpv4Port(MemorySegment addr) {
        return 0;
    }

    @Override
    public short getIpv6Port(MemorySegment addr) {
        return 0;
    }

    @Override
    public int getIpv4Address(MemorySegment clientAddr, MemorySegment address) {
        return 0;
    }

    @Override
    public int getIpv6Address(MemorySegment clientAddr, MemorySegment address) {
        return 0;
    }

    @Override
    public int bind(Socket socket, MemorySegment addr) {
        return 0;
    }

    @Override
    public int listen(Socket socket, int backlog) {
        return 0;
    }

    @Override
    public int connect(Socket socket, MemorySegment sockAddr) {
        return 0;
    }

    @Override
    public Socket accept(Socket socket, MemorySegment addr) {
        return null;
    }

    @Override
    public int recv(Socket socket, MemorySegment data, int len) {
        return 0;
    }

    @Override
    public int send(Socket socket, MemorySegment data, int len) {
        return 0;
    }

    @Override
    public int getErrOpt(Socket socket) {
        return 0;
    }

    @Override
    public int shutdownWrite(Socket socket) {
        return 0;
    }

    @Override
    public int closeSocket(Socket socket) {
        return 0;
    }

    @Override
    public void exit() {

    }
}
