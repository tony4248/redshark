package com.redshark.util;

import java.util.Random;

/**
 * 此类提供一系列产生随机数的方法，以满足不同用例需要
 * @author crazyMonkey
 */
public final class StdRandom {
    //随机数对象
    private static Random random;
    //用于产生随机数的种子
    private static long seed;
    // 静态初始化区域
    static {
//产生随机数种子
        seed = System.currentTimeMillis();
        random = new Random(seed);
    }
    private StdRandom() { }
/***********************************************************
 * 产生基本的随机数
 ***********************************************************/
    /**
     * 获取此类实例的伪随机种子生成器
     */
    public static void setSeed(long s) {
        seed = s;
        random = new Random(seed);
    }
    /**
     * 获取此类实例提供的伪随机种子生成器
     */
    public static long getSeed() {
        return seed;
    }
    /**
     * 返回一个随机的范围在[0,1)之间的double类型的数
     */
    public static double uniform() {
        return random.nextDouble();
    }
    /**
     * 返回一个随机的范围在[0,N)之间的int类型的数
     */
    public static int uniform(int N) {
        return random.nextInt(N);
    }
    /**
     * 返回一个范围在 [0, 1)的实数
     */
    public static double random() {
        return uniform();
    }
    /**
     * 返回一个范围在 [a, b)的int类型值
     */
    public static int uniform(int a, int b) {
        return a + uniform(b - a);
    }
    /**
     * 返回一个范围在 [a, b)的实数
     */
    public static double uniform(double a, double b) {
        return a + uniform() * (b-a);
    }
    /**
     * 返回一个随机boolean值,该p表示此布尔值为真的概率
     * @param p 0~1 之间的double值,表示产生boolean真值的可能性
     */
    public static boolean bernoulli(double p) {
        return uniform() < p;
    }
    /**
     * 返回一个随机boolean值,此布尔值为真的概率为0.5
     */
    public static boolean bernoulli() {
        return bernoulli(0.5);
    }
/***********************************************************
 * 产生满足特定概率分布的实数
 ***********************************************************/
    /**
     * 返回一个满足标准正态分布的实数
     */
    public static double gaussian() {
        double r, x, y;
        do {
            x = uniform(-1.0, 1.0);
            y = uniform(-1.0, 1.0);
            r = x*x + y*y;
        } while (r >= 1 || r == 0);
        return x * Math.sqrt(-2 * Math.log(r) / r);
    }
    /**
     * 返回一个满足平均值为mean,标准差为stddev的正态分布的实数
     * @param mean 正态分布的平均值
     * @param stddev 正太分布的标准差
     */
    public static double gaussian(double mean, double stddev) {
        return mean + stddev * gaussian();
    }
    /**
     * 返回一个满足几何分布的整型值 平均值为1/p
     */
    public static int geometric(double p) {
// Knuth
        return (int) Math.ceil(Math.log(uniform()) / Math.log(1.0 - p));
    }
    /**
     * 根据指定的参数返回一个满足泊松分布的实数
     */
    public static int poisson(double lambda) {
// 使用 Knuth 的算法
// 参见 http://en.wikipedia.org/wiki/Poisson_distribution
        int k = 0;
        double p = 1.0;
        double L = Math.exp(-lambda);
        do {
            k++;
            p *= uniform();
        } while (p >= L);
        return k-1;
    }
    /**
     * 根据指定的参数按返回一个满足帕雷托分布的实数
     */
    public static double pareto(double alpha) {
        return Math.pow(1 - uniform(), -1.0/alpha) - 1.0;
    }
    /**
     * 返回一个满足柯西分布的实数
     */
    public static double cauchy() {
        return Math.tan(Math.PI * (uniform() - 0.5));
    }
    /**
     * 返回一个满足离散分布的int类型的数
     * @param a 算法产生随机数过程中需要使用此数组的数据，a[i]代表i出现的概率
     * 前提条件 a[i] 非负切和接近 1.0
     */
    public static int discrete(double[] a) {
        double EPSILON = 1E-14;
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] < 0.0) throw new IllegalArgumentException("数组元素 " + i + " 为负数: " + a[i]);
            sum = sum + a[i];
        }
        if (sum > 1.0 + EPSILON || sum < 1.0 - EPSILON)
            throw new IllegalArgumentException("数组各个元素之和为: " + sum);
        while (true) {
            double r = uniform();
            sum = 0.0;
            for (int i = 0; i < a.length; i++) {
                sum = sum + a[i];
                if (sum > r) return i;
            }
        }
    }
    /**
     * 返回一个满足指数分布的实数，该指数分布比率为lambda
     */
    public static double exp(double lambda) {
        return -Math.log(1 - uniform()) / lambda;
    }
/***********************************************************
 * 数组操作
 ***********************************************************/
    /**
     * 随机打乱指定的Object型数组
     * @param a 待打乱的Object型数组
     */
    public static void shuffle(Object[] a) {
        int N = a.length;
        for (int i = 0; i < N; i++) {
            int r = i + uniform(N-i);
            Object temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }
    /**
     * 随机打乱指定的double型数组
     * @param a 待打乱的double型数组
     */
    public static void shuffle(double[] a) {
        int N = a.length;
        for (int i = 0; i < N; i++) {
            int r = i + uniform(N-i);
            double temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }
    /**
     * 随机打乱指定的int型数组
     * @param a 待打乱的int型数组
     */
    public static void shuffle(int[] a) {
        int N = a.length;
        for (int i = 0; i < N; i++) {
            int r = i + uniform(N-i);
            int temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }
    /**
     * 随机打乱指定Object类型数组中指定范围的数据
     *
     * @param a 指定的数组
     * @param lo 起始位置
     * @param hi 结束位置
     */
    public static void shuffle(Object[] a, int lo, int hi) {
        if (lo < 0 || lo > hi || hi >= a.length) {
            throw new IndexOutOfBoundsException("不合法的边界");
        }
        for (int i = lo; i <= hi; i++) {
            int r = i + uniform(hi-i+1);
            Object temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }
    /**
     * 随机打乱指定double类型数组中指定范围的数据
     *
     * @param a 指定的数组
     * @param lo 起始位置
     * @param hi 结束位置
     */
    public static void shuffle(double[] a, int lo, int hi) {
        if (lo < 0 || lo > hi || hi >= a.length) {
            throw new IndexOutOfBoundsException("不合法的边界");
        }
        for (int i = lo; i <= hi; i++) {
            int r = i + uniform(hi-i+1);
            double temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }
    /**
     * 随机打乱指定int类型数组中指定范围的数据
     *
     * @param a 指定的数组
     * @param lo 起始位置
     * @param hi 结束位置
     */
    public static void shuffle(int[] a, int lo, int hi) {
        if (lo < 0 || lo > hi || hi >= a.length) {
            throw new IndexOutOfBoundsException("不合法的边界");
        }
        for (int i = lo; i <= hi; i++) {
            int r = i + uniform(hi-i+1);
            int temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }


    public static int generate6BitInt() {
        int[] arr = { 0,1, 2, 3, 4, 5, 6, 7, 8, 9};
        // 将数组随机打乱，据算法原理可知：
        // 重复概率 = 1/10 * 1/9 * 1/8 * 1/7 * 1/6 * 1/5 * 1/4 * 1/3 * 1/2 * 1/1 = 1/3628800，
        // 即重复概率为三百多万分之一，满足要求。
        for(int num = 10; num > 1; --num) {
            int idx = StdRandom.uniform(num);
            int temp = arr[idx];
            arr[idx] = arr[num - 1];
            arr[num - 1] = temp;
        }
        // 第一个元素不能为0，否则位数不够
        if(0 == arr[0]) {
            int ndx = StdRandom.uniform(10);
            arr[0] = arr[ndx];
            arr[ndx] = 0;
        }
        // 将数组前六位转化为整数
        int rs = 0;
        for(int idx = 0; idx < 6; ++idx) {
            rs = rs * 10 + arr[idx];
        }
        return rs;
    }
}
