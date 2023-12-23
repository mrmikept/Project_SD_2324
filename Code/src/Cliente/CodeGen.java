package Cliente;

import java.util.concurrent.locks.ReentrantLock;

public class CodeGen
{
    private int code;
    private ReentrantLock lock;

    public CodeGen()
    {
        this.code = 1;
        this.lock = new ReentrantLock();
    }

    public int newCode()
    {
        this.lock.lock();
        try {
            int code = this.code;
            this.code++;
            return code;
        } finally {
            this.lock.unlock();
        }
    }
}
