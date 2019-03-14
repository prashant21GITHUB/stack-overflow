package stackoverflow;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

public class TaskProcessor implements ITaskProcessor {

    //This map will contain all the tasks which are in queue and not yet completed
    private final Map<String, Task> taskInProgresssByUniqueIdentifierMap = new ConcurrentHashMap<>();

    private final BlockingQueue<Task> taskQueue = new ArrayBlockingQueue<Task>(100);
    private final TaskRunner taskRunner = new TaskRunner();

    private Executor executor;
    private AtomicBoolean isStarted;
    private final DBManager dbManager = new DBManager();

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
                    //TODO: we can also propagate the taskResult to waiting users, Implement it if it is required.
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
                        //
                        task.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return task;
            }
        });
        //If task is null, it means the task was not there in queue, so we direcltly query the db for the task result
        if(task != null && !task.isCompleted()) {
            return null;  // Handle this condition gracefully, If task is not completed, it means there was some exception
        }
        TaskResult taskResult = getResultFromDB(uniqueIdentifier); // At this point the result must be already saved in DB if the corresponding task has been processed ever.
        return taskResult;
    }

    private TaskResult getResultFromDB(String uniqueIdentifier) {
        return dbManager.getTaskResult();
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
