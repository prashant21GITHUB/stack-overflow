package stackoverflow;

public class DBManager {

    public ITaskResult getTaskResult(ITask task) {
        //Get result from db and return.
        //If not present, It means it is still in queue and no user will reaches at this point
        ITaskResult taskResult = new TaskResult(task.getUniqueIdentifier());
        return taskResult;
    }
}
