package philosopher;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;

public class DeadLockUtils {

    private DeadLockUtils() {
    }

    public static boolean isDeadLocked(TimeUnit timeUnit, int timeout) {
        System.out.println("detecting deadLock ... ");
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            System.out.println("Detecting DeadLock Task is interrupted");
        }
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long[] threadIds = threadBean.findMonitorDeadlockedThreads();
        int deadLockedThread = threadIds != null ? threadIds.length : 0;
        System.out.println("Number of deadLocked Threads : " + deadLockedThread);
        return deadLockedThread != 0;
    }
}
