package Servidor;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class to store and register Users
 */
public class Accounts
{
    private final String configPath;
    private Map<String, String> accounts; // Key -> Username; Value -> Password
    private ReentrantReadWriteLock.ReadLock readLock;
    private ReentrantReadWriteLock.WriteLock writeLock;

    public Accounts(String configPath)
    {
        this.configPath = configPath;
        this.accounts = new HashMap<>();
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
    }

    /**
     * Function to verify if the account is registered
     * @param username username of the account
     * @return true if account exists; false if not
     */
    public boolean verifyAccount(String username)
    {
        this.readLock.lock();
        try {
            return this.accounts.containsKey(username);
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Registers an account
     * @param username username of the account
     * @param password password of the account
     */
    public void registerAccount(String username, String password)
    {
        this.writeLock.lock();
        try {
            this.accounts.put(username, password);
            this.saveAccountFile(username,password);
        } finally {
            this.writeLock.unlock();
        }
    }

    /**
     * Saves and Account information in a file.
     * @param username Username of the Account
     * @param password Password of the Account
     */
    public void saveAccountFile(String username, String password)
    {
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(this.configPath + "/accounts.txt",true));
            writer.write(username + ";" + password + "\n");
            writer.flush();
            writer.close();
        } catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Gets the password of a certain Account
     * @param user username of the account
     * @return password of the account
     */
    public String getPassword(String user)
    {
        this.readLock.lock();
        try {
           return this.accounts.get(user);
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Function to verify if the username and the stored password match's
     * @param username username of the account
     * @param password password to match
     * @return true if the credentials math, false if not.
     */
    public boolean credentialsMatch(String username, String password)
    {
        this.readLock.lock();
        try {
            if (this.verifyAccount(username))
            {
                return this.getPassword(username).equals(password);
            } else return false;
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Writes the accounts object in a file
     * @param path path where the file will be saved
     * @throws IOException
     */
    public void saveInFile(String path) throws IOException {
        this.readLock.lock();
        try
        {
            FileOutputStream fos = new FileOutputStream(path + "accounts.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "accounts.txt"));

            ObjectOutputStream oos = new ObjectOutputStream(fos);
            try {
                oos.writeObject(this);
            } finally {
                fos.close();
                oos.close();
            }
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Reads a configuration file where the accounts are stored
     * @param path Path where the file is saved
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readFromFile(String path) throws IOException, ClassNotFoundException {
        try {
            FileInputStream fis = new FileInputStream(path + "accounts.txt");
            BufferedReader reader = new BufferedReader(new FileReader(path + "accounts.txt"));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] strings = line.split("[;]");
                    this.accounts.put(strings[0], strings[1]);
                }
            } finally {
                fis.close();
                reader.close();
            }
        } catch (FileNotFoundException e)
        {
            System.out.println(e.getMessage());
        }
    }
}
