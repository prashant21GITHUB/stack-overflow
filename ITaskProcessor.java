package stackoverflow;

public interface ITaskProcessor {

    void start();

    TaskResult getTaskResult(String uniqueIdentifier);

    void enqueueTask(Task task);

    void stop();
}
