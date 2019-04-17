package server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class EchoServerTest {

    private static final int DEFAULT_PORT = 8081;
    private ServerSocket server;

    @Before
    public void setUp() {
        try {
            server = new ServerSocket(DEFAULT_PORT);
        } catch (IOException ignore) {
            ignore.printStackTrace();
            fail();
        }
    }

    @Test
    public void echo_server_connect_by_new_thread() throws InterruptedException {

        serverOpen(handler -> {
            System.out.println("connect");
            Thread handlerExecutor = new Thread(handler);
            handlerExecutor.start();
        });

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        int numOfThreadsBeforeConnect = threadBean.getThreadCount();

        final int numOfRequests = 10;
        requestFor(numOfRequests);

        TimeUnit.MILLISECONDS.sleep(50000);

        int numOfThreadsAfterConnect = threadBean.getThreadCount();

        assertEquals(numOfRequests, numOfThreadsAfterConnect - numOfThreadsBeforeConnect);
    }

    @Test
    public void echo_server_connect_by_thread_pool() throws InterruptedException {
        final int threadPoolSize = 5;

        serverOpen(new ConnectStrategy() {
            private ExecutorService executor = Executors
                    .newFixedThreadPool(threadPoolSize);

            @Override
            public void execute(Runnable handler) {
                executor.execute(handler);
            }
        });

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        int numOfThreadsBeforeConnect = threadBean.getThreadCount();

        final int numOfRequests = 10;
        requestFor(numOfRequests);

        TimeUnit.MILLISECONDS.sleep(50000);

        int numOfThreadsAfterConnect = threadBean.getThreadCount();

        assertEquals(threadPoolSize, numOfThreadsAfterConnect - numOfThreadsBeforeConnect);

    }

    private void serverOpen(ConnectStrategy strategy) {
        Thread serverThread = new SimpleServer(strategy, server);
        serverThread.start();
    }

    private void requestFor(int numOfRequests) {
        for (int i = 0; i < numOfRequests; i++) {
            request();
        }
    }

    private void request() {
        class MockClient extends Thread {
            @Override
            public void run() {
                Socket client = null;
                try {
                    client = new Socket();
                    client.connect(new InetSocketAddress(DEFAULT_PORT));
                } catch (IOException ignore) {
                    ignore.printStackTrace();
                    fail();
                } finally {
                    if (client != null) {
                        try {
                            client.close();
                        } catch (IOException ignore) {
                            ignore.printStackTrace();
                            fail();
                        }
                    }
                }
            }
        }

        new MockClient().start();
    }

    static class SimpleServer extends Thread {
        ConnectStrategy strategy;
        ServerSocket server;

        SimpleServer(ConnectStrategy strategy, ServerSocket server) {
            this.strategy = strategy;
            this.server = server;
        }

        @Override
        public void run() {
            waitingRequest();
        }

        public void waitingRequest() {
            while (!server.isClosed()) {
                strategy.execute(() -> {
                    /*
                        handler implements Runnable example : echo
                        run() {
                            try {
                                int n;
                                byte[] buffer = new byte[1024];
                                while((n = inputStream.read(buffer)) != -1 ) {
                                    outputStream.write(buffer, 0, n);
                                    outputStream.flush();
                                }
                            } catch (IOException ignore) {
                              //
                            }
                        }

                     */
                });
            }
        }
    }

    @After
    public void tearDown() throws IOException {
        server.close();
    }
}
