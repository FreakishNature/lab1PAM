package lab2.saga;

class Locker{
    volatile boolean locked = true;
    public void lock() throws InterruptedException {
        System.out.println("locked");
        locked = true;
        synchronized (this){
            while (locked)
                this.wait();
        }
    }

    public void unlock(){
        System.out.println("unlocked");
        synchronized (this){
            locked = false;
            this.notifyAll();
        }
    }
}