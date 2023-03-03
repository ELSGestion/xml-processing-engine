package eu.els.sie.xml.processing.engine.listener;

public interface MessageListener {

	public void message(String source, String message, Object[] params, MessageLevelEnum level, Exception e);

}
