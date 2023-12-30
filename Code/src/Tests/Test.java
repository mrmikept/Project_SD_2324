package Tests;

/**
 * Test Class. ONLY FOR TESTS.
 */
public class Test
{
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(new TestThreads("mikep","file1"));
        Thread t2 = new Thread(new TestThreads("rafag","file2"));
        t1.start();
        Thread.sleep(2000);
        t2.start();
//
//        Thread.sleep(2000);
//
//        Thread t3 = new Thread(new TestThreads("user1","file1"));
//        Thread t4 = new Thread(new TestThreads("user2", "file2"));
//        t3.start();
//        t4.start();
    }
}
