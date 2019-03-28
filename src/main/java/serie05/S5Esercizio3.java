package serie05;

import java.util.concurrent.atomic.AtomicReference;

interface IState {
	void increment();

	int getValue();
}

final class ImmutableValue {
	private final int value;

	ImmutableValue(int value) {
		this.value = value;
	}

	int getValue() {
		return value;
	}

	ImmutableValue increment() {
		return new ImmutableValue(value+1);
	}
}

final class ImmutableHolderSharedState implements IState {
	private final AtomicReference<ImmutableValue> holderRef = new AtomicReference<>();

	ImmutableHolderSharedState() {
		holderRef.set(new ImmutableValue(0));
	}

	@Override
	public void increment() {
		while(true) {
			ImmutableValue oldVal = holderRef.get();
			if(holderRef.compareAndSet(oldVal, oldVal.increment()))
				break;
		}
	}

	@Override
	public int getValue() {
		return holderRef.get().getValue();
	}
}


final class SharedState implements IState {
	private int value = 0;

	@Override
	public void increment() {
		value++;
	}

	@Override
	public int getValue() {
		return value;
	}
}

final class ThreadSafeSharedState implements IState {
	private int value = 0;

	@Override
	public synchronized void increment() {
		value++;
	}

	@Override
	public synchronized int getValue() {
		return value;
	}
}

class Helper implements Runnable {
	@Override
	public void run() {
		System.out.println("Helper : started and waiting until shared state is set!");
		while (true) {
			if (S5Esercizio3.sharedState != null)
				break;
		}

		int lastValue = S5Esercizio3.sharedState.getValue();

		System.out.println("Helper : shared state initialized and current value is " + lastValue
				+ ". Waiting until value changes");

		// Wait until value changes
		while (true) {
			final int curValue = S5Esercizio3.getSharedValue();
			if (lastValue != curValue) {
				lastValue = curValue;
				break;
			}
		}
		System.out.println("Helper : value changed to " + lastValue + "!");

		for (int i = 0; i < 5000; i++) {
			S5Esercizio3.incrementSharedValue();

			if ((i % 100) == 0)
				try {
					Thread.sleep(1);
				} catch (final InterruptedException e) {
				}
		}
		System.out.println("Helper : completed");
	}
}

class Starter implements Runnable {

	@Override
	public void run() {
		System.out.println("Starter : sleeping");
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
		}

		System.out.println("Starter : initialized shared state");
		// Choose which share to instantiate
		if (S5Esercizio3.THREADSAFE_SHARE)
//			S5Esercizio3.sharedState = new ThreadSafeSharedState();
			S5Esercizio3.sharedState = new ImmutableHolderSharedState();
		else
			S5Esercizio3.sharedState = new SharedState();

		// Sleep before updating
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
		}

		// Perform 5000 increments and exit
		System.out.println("Starter : begin incrementing");
		for (int i = 0; i < 5000; i++) {
			S5Esercizio3.incrementSharedValue();

			if ((i % 100) == 0)
				try {
					Thread.sleep(1);
				} catch (final InterruptedException e) {
				}
		}
		System.out.println("Starter : completed");
	}
}

public class S5Esercizio3 {
	static final boolean THREADSAFE_SHARE = true;
	static final Object lockObject = new Object();

	volatile static IState sharedState = null;

	public static void main(final String[] args) {
		// Create Threads
		final Thread readThread = new Thread(new Helper());
		final Thread updateThread = new Thread(new Starter());

		// Start Threads
		readThread.start();
		updateThread.start();

		// Wait until threads finish
		try {
			updateThread.join();
			readThread.join();
		} catch (final InterruptedException e) {

		}
		System.out.println("Main: final value " + S5Esercizio3.sharedState.getValue());
	}

	static void incrementSharedValue() {
		if (THREADSAFE_SHARE)
			sharedState.increment();
		else {
			synchronized (lockObject) {
				sharedState.increment();
			}
		}
	}

	static int getSharedValue() {
		int curValue;
		if (S5Esercizio3.THREADSAFE_SHARE)
			curValue = S5Esercizio3.sharedState.getValue();
		else {
			synchronized (S5Esercizio3.lockObject) {
				curValue = S5Esercizio3.sharedState.getValue();
			}
		}
		return curValue;
	}
}
