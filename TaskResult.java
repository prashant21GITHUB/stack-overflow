package stackoverflow;

public class TaskResult implements ITaskResult {

    private final String uniqueIdentifier;

    public TaskResult(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    @Override
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }
}
