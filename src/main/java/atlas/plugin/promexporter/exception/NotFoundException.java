package atlas.plugin.promexporter.exception;

public class NotFoundException extends RuntimeException{
    
    private static final long serialVersionUID = -7094942597460846450L;

    public NotFoundException(String msg) {
		super(msg);
	}
}
