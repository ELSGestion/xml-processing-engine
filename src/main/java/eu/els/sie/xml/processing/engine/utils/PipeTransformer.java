package eu.els.sie.xml.processing.engine.utils;

import java.util.List;

import net.sf.saxon.s9api.Destination;

public class PipeTransformer {
	
	private Destination transformer;
	private List<Destination> transformerList;
	public PipeTransformer(Destination transformer, List<Destination> transformerList) {
		super();
		this.transformer = transformer;
		this.transformerList = transformerList;
	}
	public Destination getTransformer() {
		return transformer;
	}
	public List<Destination> getTransformerList() {
		return transformerList;
	}
	
	
}
