package Tests;

public class Test
{
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(new TestThreads("mikep","file1"));
        Thread t2 = new Thread(new TestThreads("rafag","file2"));
        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }
}
