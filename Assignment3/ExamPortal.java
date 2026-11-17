import java.io.IOException;
class PortalException extends Exception {
    PortalException(String message) { super(message); }
    PortalException(String message, Throwable cause) { super(message, cause); }
}
class ExamClosedException extends PortalException { ExamClosedException() { super("Exam window closed"); } }
class NotRegisteredException extends PortalException { NotRegisteredException() { super("Student not registered"); } }
class PortalStorageException extends PortalException { PortalStorageException(Throwable cause) { super("Could not save submission", cause); } }
class AnswerStorage {
    void save(long kb) throws PortalStorageException {
        try {
            if (kb > 500) throw new IOException("File exceeds limit");
            System.out.println("Submission saved");
        } catch (IOException e) { throw new PortalStorageException(e); }
    }
}
class ExamService {
    void submit(boolean open, boolean registered, long kb) throws PortalException {
        if (!open) throw new ExamClosedException();
        if (!registered) throw new NotRegisteredException();
        new AnswerStorage().save(kb);
    }
}
public class ExamPortal {
    public static void main(String[] args) {
        try { new ExamService().submit(true, true, 800); }
        catch (PortalException e) {
            System.out.println("Error: " + e.getMessage());
            if (e.getCause() != null) System.out.println("Cause: " + e.getCause().getMessage());
            e.printStackTrace();
        }
    }
}
