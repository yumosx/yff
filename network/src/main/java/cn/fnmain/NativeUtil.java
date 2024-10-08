package cn.fnmain;

import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.lib.Constants;
import cn.fnmain.lib.OsType;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


import static java.lang.StringTemplate.STR;

public final class NativeUtil {
    /**
     *   Global NULL pointer, don't use it if the application would modify the actual address of this pointer
     */
    public static final MemorySegment NULL_POINTER = MemorySegment.ofAddress(0L);
    /**
     *   Current operating system name
     */
    private static final String osName = System.getProperty("os.name").toLowerCase();
    private static final boolean runningFromJar = runningFromJar();
    /**
     *   Current dynamic library path that tenet application will look up for
     */
    private static final String libPath = System.getProperty(Constants.TENET_LIBRARY_PATH);
    private static final OsType osType = detectOsType();
    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
    private static final Linker linker = Linker.nativeLinker();
    /**
     *   Global dynamic library cache to avoid repeated loading
     */
    private static final Map<String, SymbolLookup> libraryCache = new ConcurrentHashMap<>();
    /**
     *   Global native method cache to avoid repeated capturing
     */
    private static final Map<String, MethodHandle> nativeMethodCache = new ConcurrentHashMap<>();
    private static final Arena globalArena = Arena.global();
    private static final Arena autoArena = Arena.ofAuto();
    private static final ByteOrder byteOrder = ByteOrder.nativeOrder();
    private static final VarHandle byteHandle = MethodHandles.memorySegmentViewVarHandle(ValueLayout.JAVA_BYTE);
    private static final VarHandle shortHandle = MethodHandles.memorySegmentViewVarHandle(ValueLayout.JAVA_SHORT_UNALIGNED);
    private static final VarHandle intHandle = MethodHandles.memorySegmentViewVarHandle(ValueLayout.JAVA_INT_UNALIGNED);
    private static final VarHandle longHandle = MethodHandles.memorySegmentViewVarHandle(ValueLayout.JAVA_LONG_UNALIGNED);
    private static final VarHandle floatHandle = MethodHandles.memorySegmentViewVarHandle(ValueLayout.JAVA_FLOAT_UNALIGNED);
    private static final VarHandle doubleHandle = MethodHandles.memorySegmentViewVarHandle(ValueLayout.JAVA_DOUBLE_UNALIGNED);
    private static final VarHandle shortArrayHandle = MethodHandles.byteArrayViewVarHandle(short[].class, byteOrder);
    private static final VarHandle intArrayHandle = MethodHandles.byteArrayViewVarHandle(int[].class, byteOrder);
    private static final VarHandle longArrayHandle = MethodHandles.byteArrayViewVarHandle(long[].class, byteOrder);
    private static final VarHandle floatArrayHandle = MethodHandles.byteArrayViewVarHandle(float[].class, byteOrder);
    private static final VarHandle doubleArrayHandle = MethodHandles.byteArrayViewVarHandle(double[].class, byteOrder);
    private static final long BYTE_SIZE = ValueLayout.JAVA_BYTE.byteSize();
    private static final long SHORT_SIZE = ValueLayout.JAVA_SHORT.byteSize();
    private static final long INT_SIZE = ValueLayout.JAVA_INT.byteSize();
    private static final long LONG_SIZE = ValueLayout.JAVA_LONG.byteSize();
    private static final long FLOAT_SIZE = ValueLayout.JAVA_FLOAT.byteSize();
    private static final long DOUBLE_SIZE = ValueLayout.JAVA_DOUBLE.byteSize();
    private static final long I_MAX = Integer.MAX_VALUE;
    private static final long I_MIN = Integer.MIN_VALUE;

    /**
     *  Safely cast long to int, throw an exception if overflow
     */
    public static int castInt(long l) {
        if(l < I_MIN || l > I_MAX) {
            throw new FrameworkException(ExceptionType.NATIVE, Constants.UNREACHED);
        }
        return (int) l;
    }

    /**
     *   Detect if current program is running from a jar file
     */
    private static boolean runningFromJar() {
        String className = NativeUtil.class.getName().replace('.', '/');
        String classJar = Objects.requireNonNull(NativeUtil.class.getResource(STR."/\{className}.class")).toString();
        return classJar.startsWith("jar:");
    }

    public static boolean isRunningFromJar() {
        return runningFromJar;
    }

    private static OsType detectOsType() {
        if(osName.contains("windows")) {
            return OsType.Windows;
        }else if(osName.contains("linux")) {
            return OsType.Linux;
        }else if(osName.contains("mac") && osName.contains("os")) {
            return OsType.MacOS;
        }else {
            return OsType.Unknown;
        }
    }

    private NativeUtil() {
        throw new UnsupportedOperationException();
    }

    public static long getByteSize() {
        return BYTE_SIZE;
    }

    public static long getShortSize() {
        return SHORT_SIZE;
    }

    public static long getIntSize() {
        return INT_SIZE;
    }

    public static long getLongSize() {
        return LONG_SIZE;
    }

    public static long getFloatSize() {
        return FLOAT_SIZE;
    }

    public static long getDoubleSize() {
        return DOUBLE_SIZE;
    }

    public static MemorySegment toNativeSegment(MemorySegment memorySegment) {
        return toNativeSegment(autoArena, memorySegment);
    }

    public static MemorySegment toNativeSegment(Arena arena, MemorySegment memorySegment) {
        if(memorySegment.isNative()) {
            return memorySegment;
        }else {
            long byteSize = memorySegment.byteSize();
            MemorySegment nativeSegment = arena.allocateArray(ValueLayout.JAVA_BYTE, byteSize);
            MemorySegment.copy(memorySegment, 0, nativeSegment, 0, byteSize);
            return nativeSegment;
        }
    }

    private static String getDynamicLibraryName(String identifier) {
        return switch (osType) {
            case Windows -> STR."lib\{identifier}.dll";
            case Linux -> STR."lib\{identifier}.so";
            case MacOS -> STR."lib\{identifier}.dylib";
            default -> throw new FrameworkException(ExceptionType.NATIVE, "Unrecognized operating system");
        };
    }

    public static Arena globalArena() {
        return globalArena;
    }

    public static Arena autoArena() {
        return autoArena;
    }

    public static OsType ostype() {
        return osType;
    }


    public static int getCpuCores() {
        return CPU_CORES;
    }


    public static MethodHandle methodHandle(SymbolLookup lookup, String methodName, FunctionDescriptor functionDescriptor, Linker.Option... options) {
        MemorySegment methodPointer = lookup.find(methodName)
                .orElseThrow(() -> new FrameworkException(ExceptionType.NATIVE, STR."Unable to load target native method : \{methodName}"));
        return linker.downcallHandle(methodPointer, functionDescriptor, options);
    }


    public static MethodHandle methodHandle(SymbolLookup lookup, List<String> methodNames, FunctionDescriptor functionDescriptor, Linker.Option... options) {
        for (String methodName : methodNames) {
            Optional<MemorySegment> methodPointer = lookup.find(methodName);
            if (methodPointer.isPresent()) {
                return linker.downcallHandle(methodPointer.get(), functionDescriptor, options);
            }
        }
        throw new FrameworkException(ExceptionType.NATIVE, STR."Unable to load target native method : \{methodNames}");
    }

    /**
     *  Load a native library by environment variable, return null if system library was not found in environment variables
     */
    public static SymbolLookup loadLibrary(String identifier) {
        if(libPath == null) {
            throw new FrameworkException(ExceptionType.NATIVE, "Global libPath not found");
        }
        return libraryCache.computeIfAbsent(identifier, i -> SymbolLookup.libraryLookup(libPath + Constants.SEPARATOR + getDynamicLibraryName(i), globalArena));
    }

    public static MethodHandle nativeMethodHandle(String methodName, FunctionDescriptor functionDescriptor) {
        return nativeMethodHandle(methodName, functionDescriptor, false);
    }

    /**
     *  Load function from system library, such as strlen()
     */
    public static MethodHandle nativeMethodHandle(String methodName, FunctionDescriptor functionDescriptor, boolean isTrivial) {
        return nativeMethodCache.computeIfAbsent(methodName, k -> {
            MemorySegment method = linker.defaultLookup().find(k).orElseThrow(() -> new FrameworkException(ExceptionType.NATIVE, STR."Unable to locate [\{methodName}] native method"));
            return isTrivial ? linker.downcallHandle(method, functionDescriptor, Linker.Option.isTrivial()): linker.downcallHandle(method, functionDescriptor);
        });
    }

    public static boolean checkNullPointer(MemorySegment memorySegment) {
        return memorySegment == null || memorySegment.address() == 0;
    }

    public static byte toAsciiByte(int i) {
        return (byte) (i + 48);
    }

    public static byte getByte(MemorySegment memorySegment, long index) {
        return (byte) byteHandle.get(memorySegment, index);
    }

    public static void setByte(MemorySegment memorySegment, long index, byte value) {
        byteHandle.set(memorySegment, index, value);
    }

    public static short getShort(MemorySegment memorySegment, long index) {
        return (short) shortHandle.get(memorySegment, index);
    }

    public static void setShort(MemorySegment memorySegment, long index, short value) {
        shortHandle.set(memorySegment, index, value);
    }

    public static short getShort(short[] arr, long index) {
        return (short) shortArrayHandle.get(arr, index);
    }

    public static void setShort(short[] arr, long index, short value) {
        shortArrayHandle.set(arr, index, value);
    }

    public static int getInt(MemorySegment memorySegment, long index) {
        return (int) intHandle.get(memorySegment, index);
    }

    public static void setInt(MemorySegment memorySegment, long index, int value) {
        intHandle.set(memorySegment, index, value);
    }

    public static int getInt(int[] arr, long index) {
        return (int) intArrayHandle.get(arr, index);
    }

    public static void setInt(int[] arr, long index, int value) {
        intArrayHandle.set(arr, index, value);
    }

    public static long getLong(MemorySegment memorySegment, long index) {
        return (long) longHandle.get(memorySegment, index);
    }

    public static void setLong(MemorySegment memorySegment, long index, long value) {
        longHandle.set(memorySegment, index, value);
    }

    public static long getLong(long[] arr, long index) {
        return (long) longArrayHandle.get(arr, index);
    }

    public static void setLong(long[] arr, long index, long value) {
        longArrayHandle.set(arr, index, value);
    }

    public static float getFloat(MemorySegment memorySegment, long index) {
        return (float) floatHandle.get(memorySegment, index);
    }

    public static void setFloat(MemorySegment memorySegment, long index, float value) {
        floatHandle.set(memorySegment, index, value);
    }

    public static float getFloat(float[] arr, long index) {
        return (float) floatArrayHandle.get(arr, index);
    }

    public static void setFloat(float[] arr, long index, float value) {
        floatArrayHandle.set(arr, index, value);
    }

    public static double getDouble(MemorySegment memorySegment, long index) {
        return (double) doubleHandle.get(memorySegment, index);
    }

    public static void setDouble(MemorySegment memorySegment, long index, double value) {
        doubleHandle.set(memorySegment, index, value);
    }

    public static double getDouble(double[] arr, long index) {
        return (double) doubleArrayHandle.get(arr, index);
    }

    public static void setDouble(double[] arr, long index, double value) {
        doubleArrayHandle.set(arr, index, value);
    }

    /**
     *   Using brute-force search for target bytes in a memorySegment
     *   This method could be optimized for better efficiency using other algorithms like BM or KMP, however when the length of bytes are small and unrepeated, BF is simple and good enough
     *   Usually this method is used to find a target separator in a sequence
     */
    public static boolean matches(MemorySegment m, long offset, byte[] bytes) {
        for(int index = 0; index < bytes.length; index++) {
            if (getByte(m, offset + index) != bytes[index]) {
                return false;
            }
        }
        return true;
    }

    public static MemorySegment allocateStr(Arena arena, String str) {
        return arena.allocateUtf8String(str);
    }

    public static MemorySegment allocateStr(Arena arena, String str, int len) {
        MemorySegment strSegment = MemorySegment.ofArray(str.getBytes(StandardCharsets.UTF_8));
        long size = strSegment.byteSize();
        if(len < size + 1) {
            throw new FrameworkException(ExceptionType.NATIVE, "String out of range");
        }
        MemorySegment memorySegment = arena.allocateArray(ValueLayout.JAVA_BYTE, len);
        MemorySegment.copy(strSegment, 0, memorySegment, 0, size);
        setByte(memorySegment, size, Constants.NUT);
        return memorySegment;
    }

    public static String getStr(MemorySegment memorySegment) {
        return getStr(memorySegment, 0);
    }

    public static String getStr(MemorySegment ptr, int maxLength) {
        if(maxLength > 0) {
            byte[] bytes = new byte[maxLength];
            for(int i = 0; i < maxLength; i++) {
                byte b = getByte(ptr, i);
                if(b == Constants.NUT) {
                    return new String(bytes, 0, i, StandardCharsets.UTF_8);
                }else {
                    bytes[i] = b;
                }
            }
        }else {
            for(int i = 0; i < Integer.MAX_VALUE; i++) {
                byte b = getByte(ptr, i);
                if(b == Constants.NUT) {
                    return new String(ptr.asSlice(0, i).toArray(ValueLayout.JAVA_BYTE), StandardCharsets.UTF_8);
                }
            }
        }
        throw new FrameworkException(ExceptionType.NATIVE, Constants.UNREACHED);
    }
}

