package eu.els.sie.xml.processing.engine.run;

import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;

public class JavaStepExample {

	public static XdmValue method(XdmValue value) {
		System.out.println(value.itemAt(0).toString());
		return new XdmAtomicValue(true);
	}

}
