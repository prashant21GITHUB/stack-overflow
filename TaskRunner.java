package stackoverflow;

public class TaskRunner {

    public void execute(Task task, ITaskProgressListener taskProgressListener) {

        // ... Some complicated calculations
        TaskResult taskResult = new TaskResult(task.getUniqueIdentifier());
        try {
            // Save result in db
            taskProgressListener.onTaskCompletion(taskResult);
        } catch(Exception e) {
            taskProgressListener.onTaskFailure(e);
        }

    }
}
