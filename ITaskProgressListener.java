package stackoverflow;

public interface ITaskProgressListener {
    void onTaskCompletion(TaskResult taskResult);

    void onTaskFailure(Exception e);
}
