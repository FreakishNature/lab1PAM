package lab2.saga.classes;

public class Locker{
    volatile boolean locked = true;
    public void lock() throws InterruptedException {
        locked = true;
        synchronized (this){
            while (locked)
                this.wait();
        }
    }

    public void unlock(){
        synchronized (this){
            locked = false;
            this.notifyAll();
        }
    }
}