package stackoverflow;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

public class TaskProcessor implements ITaskProcessor {

    private final Map<String, Task> taskInProgresssByUniqueIdentifierMap = new ConcurrentHashMap<>();

    private final BlockingQueue<Task> taskQueue = new ArrayBlockingQueue<Task>(100);
    private final TaskRunner taskRunner = new TaskRunner();

    private Executor executor;
    private AtomicBoolean isStarted;

    @Override
    public void start() {
        executor = Executors.newCachedThreadPool();
        while(isStarted.get()) {
            try {
                Task task = taskQueue.take();
                executeTaskInSeperateThread(task);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void executeTaskInSeperateThread(Task task) {
        executor.execute(() -> {
            taskRunner.execute(task, new ITaskProgressListener() {

                @Override
                public void onTaskCompletion(TaskResult taskResult) {
                    task.setCompleted(true);
                    notifyAllWaitingUsers(task);
                }

                @Override
                public void onTaskFailure(Exception e) {
                    notifyAllWaitingUsers(task);
                }
            });
        });
    }

    private void notifyAllWaitingUsers(Task task) {
        taskInProgresssByUniqueIdentifierMap.computeIfPresent(task.getUniqueIdentifier(), new BiFunction<String, Task, Task>() {
            @Override
            public Task apply(String s, Task task) {
                synchronized (task) {
                    task.notifyAll();
                }
                return null;
            }
        });
    }

    @Override
    public TaskResult getTaskResult(String uniqueIdentifier) {
        TaskResult result = null;
        Task task = taskInProgresssByUniqueIdentifierMap.computeIfPresent(uniqueIdentifier, new BiFunction<String, Task, Task>() {
            @Override
            public Task apply(String s, Task task) {
                synchronized (task) {
                    try {
                        task.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return task;
            }
        });
        if(!task.isCompleted()) {
            return null;  // Handle this condition gracefully, If task is not completed, it means there was some exception
        }
        TaskResult taskResult = getResultFromDB(uniqueIdentifier);
        return taskResult;
    }

    private TaskResult getResultFromDB(String uniqueIdentifier) {
        return null;
    }


    @Override
    public void enqueueTask(Task task) {
        if(isStarted.get()) {
            taskInProgresssByUniqueIdentifierMap.putIfAbsent(task.getUniqueIdentifier(), task);
            taskQueue.offer(task);
        }
    }

    @Override
    public void stop() {
        isStarted.compareAndSet(true, false);
    }
}
