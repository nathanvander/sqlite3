package apollo.win.sys;

public class TestCriticalSection implements Runnable {
		String name;
		CriticalSection lock;
		public TestCriticalSection(String name,CriticalSection cs) {this.name=name;this.lock=cs;}
		public void run() {
			//critical section must have already been initialized
			System.out.println(name+" is about to enter the house!");
			if (lock.isUnlocked()) {
				System.out.println("the door is unlocked");
			} else {
				System.out.println("the door is locked by "+lock.getOwner().getName());
			}
			lock.enter();
			System.out.println(name+" has entered the house!");
			try{
				Thread.sleep(1000);
			} catch (InterruptedException x) {}
			lock.leave();
			System.out.println(name+" has left the house!");
		}

	//-------------------------------------------
	public static void main(String[] args) {
		CriticalSection lock1=new CriticalSection("lock1");
		lock1.init();

		Thread t1=new Thread(new TestCriticalSection("big daddy t1",lock1),"t1");
		Thread t2=new Thread(new TestCriticalSection("rapper t2",lock1),"t2");
		t1.start();
		t2.start();

	}
}