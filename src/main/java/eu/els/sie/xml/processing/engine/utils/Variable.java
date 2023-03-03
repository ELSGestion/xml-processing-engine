package eu.els.sie.xml.processing.engine.utils;

import java.util.Map;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmValue;

public class Variable extends Param {

	public Variable(QName name, String xpath, Info info) {
		super(name, xpath, info);
		type = ParamTypeEnum.variable;
	}

	public Variable(QName name, String xpath, Map<String, String> namespaces, Info info) throws ProcessingException {
		super(name, xpath, namespaces, info);
		type = ParamTypeEnum.variable;
	}

	public Variable(QName name, String xpath) {
		super(name, xpath);
		type = ParamTypeEnum.variable;
	}

	public Variable(QName name, XdmValue value, Info info) {
		super(name, value, info);
		type = ParamTypeEnum.variable;
	}

	public Variable(QName name, XdmValue value, Map<String, String> namespaces, Info info) throws ProcessingException {
		super(name, value, namespaces, info);
		type = ParamTypeEnum.variable;
	}

	public Variable(QName name, XdmValue value) {
		super(name, value);
		type = ParamTypeEnum.variable;
	}

	public Variable(String name, String xpath, Info info) {
		super(name, xpath, info);
		type = ParamTypeEnum.variable;
	}

	public Variable(String name, String xpath, Map<String, String> namespaces, Info info) throws ProcessingException {
		super(name, xpath, namespaces, info);
		type = ParamTypeEnum.variable;
	}

	public Variable(String name, String xpath) {
		super(name, xpath);
		type = ParamTypeEnum.variable;
	}

	public Variable(String name, XdmValue value, Info info) {
		super(name, value, info);
		type = ParamTypeEnum.variable;
	}

	public Variable(String name, XdmValue value, Map<String, String> namespaces, Info info) throws ProcessingException {
		super(name, value, namespaces, info);
		type = ParamTypeEnum.variable;
	}

	public Variable(String name, XdmValue value) {
		super(name, value);
		type = ParamTypeEnum.variable;
	}

}
