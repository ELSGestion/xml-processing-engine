package eu.els.sie.xml.processing.engine.exception;

import eu.els.sie.xml.processing.engine.utils.Info;

public class ProcessingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ProcessingException() {
		super();
	}

	public ProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
			Info info) {
		super(message + ((info != null) ? info.toString() : ""), cause, enableSuppression, writableStackTrace);
	}

	public ProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessingException(String message, Throwable cause, Info info) {
		super(message + ((info != null) ? info.toString() : ""), cause);
	}

	public ProcessingException(String message, Info info) {
		super(message + ((info != null) ? info.toString() : ""));
	}

	public ProcessingException(Throwable cause, Info info) {
		super(cause.getMessage() + ((info != null) ? info.toString() : ""), cause);
	}

}
