package eu.els.sie.xml.processing.engine.listener;

public interface PipeListener {

	public void start(String pipeName, int total, String inputUri, int nb);

	public void succed(String pipeName, int total, String inputUri, int nb);

	public void failed(String pipeName, int total, String inputUri, int nb, Throwable e);

	public void interrupted(String pipeName, int total, String inputUri, int nb) ;

}
