package Cliente;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Class to generate a sequential Integer Value
 */
public class CodeGen
{
    private int code; // Value of the next code.
    private ReentrantLock lock; // Lock for the code value

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
