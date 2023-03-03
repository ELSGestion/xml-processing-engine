package eu.els.sie.xml.processing.engine.run;

import java.util.HashMap;
import java.util.Map;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.step.ProcessingStep;
import eu.els.sie.xml.processing.engine.utils.Param;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;

public class Test {

	public Test() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws ProcessingException {
		ProcessingStep proc = new ProcessingStep();
		proc.load("src/test/resources/examples/xpe/xslt-pipe.xpe");
		Map<QName, Param> params = new HashMap<>();

		Param param1 = new Param(new QName("a"), new XdmAtomicValue("A"));
		Param param2 = new Param(new QName("x"), new XdmAtomicValue(true));
		params.put(param1.getName(), param1);
		params.put(param2.getName(), param2);

		proc.execute(params);
	}

}
