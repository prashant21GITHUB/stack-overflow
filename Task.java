package stackoverflow;

public class Task implements ITask{

    private boolean completed;
    private String uniqueIdentifier;

    public Task(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }
}
