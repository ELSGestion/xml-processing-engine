package eu.els.sie.xml.processing.engine.utils;

public class Info {

	private String systemUri;
	private int line;
	private int colomn;
	
	public Info(String systemUri, int line, int colomn) {
		super();
		this.systemUri = systemUri;
		this.line = line;
		this.colomn = colomn;
	}

	public String getSystemUri() {
		return systemUri;
	}

	public int getLine() {
		return line;
	}

	public int getColomn() {
		return colomn;
	}

	@Override
	public String toString() {
		return " @[systemUri=" + systemUri + ", line=" + line + ", colomn=" + colomn + "] ";
	}
	
	

}
