package eu.els.sie.xml.processing.engine.exception;

public class XPathEvaluationException extends Exception  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public XPathEvaluationException(String message) {
		super(message);
	}

	public XPathEvaluationException(String message, Exception cause) {
		super(message, cause);
	}

	public XPathEvaluationException() {
		super();
	}

	public XPathEvaluationException(String message, Exception cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public XPathEvaluationException(Exception cause) {
		super(cause);
	}
	
	
}
