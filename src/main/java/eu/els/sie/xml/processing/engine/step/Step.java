package eu.els.sie.xml.processing.engine.step;

import java.util.List;
import java.util.Map;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.utils.Info;
import eu.els.sie.xml.processing.engine.utils.Param;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

public abstract class Step {

	protected ProcessingStep owner;
	protected Step parent;
	protected Info info;

	protected void setInfo(XdmNode node) {
		this.info = new Info(owner.uri, node.getLineNumber(), node.getColumnNumber());
	}

	protected void setOwner(ProcessingStep owner) {
		this.owner = owner;
	}

	protected void setParent(Step parent) {
		this.parent = parent;
		this.owner = parent.owner;
	}

	public abstract Result execute(List<Map<QName, Param>> params) throws ProcessingException;

	public abstract void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException;

}
