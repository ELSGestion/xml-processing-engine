package eu.els.sie.xml.processing.engine.utils;

import java.util.Map;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.exception.XPathEvaluationException;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

public class Param {

	protected QName name;
	protected String xpath;
	protected XdmValue value;
	protected ParamTypeEnum type = ParamTypeEnum.param;
	protected Info info;

	public Param(String name, String xpath, Map<String, String> namespaces, Info info) throws ProcessingException {
		super();

		if (name.charAt(0) == 'Q' && name.charAt(1) == '{') {
			this.name = QName.fromEQName(name);
		} else if (name.charAt(0) == '{') {
			this.name = QName.fromClarkName(name);
		} else {
			String[] parts = name.split(":");
			if (parts.length == 1) {
				this.name = new QName(name);
			} else {
				if (namespaces != null) {
					if (namespaces.containsKey(parts[0])) {
						this.name = new QName(namespaces.get(parts[0]), name);
					} else {
						throw new ProcessingException("namespace not recongnized [" + parts[0] + "]", info);
					}
				} else {
					throw new ProcessingException("namespace not recongnized [" + parts[0] + "]", info);
				}
			}
		}

		this.xpath = xpath;
		this.info = info;
	}

	public Param(String name, XdmValue value, Map<String, String> namespaces, Info info) throws ProcessingException {
		super();

		if (name.charAt(0) == 'Q' && name.charAt(1) == '{') {
			this.name = QName.fromEQName(name);
		} else if (name.charAt(0) == '{') {
			this.name = QName.fromClarkName(name);
		} else {
			String[] parts = name.split(":");
			if (parts.length == 1) {
				this.name = new QName(name);
			} else {
				if (namespaces != null) {
					if (namespaces.containsKey(parts[0])) {
						this.name = new QName(namespaces.get(parts[0]), name);
					} else {
						throw new ProcessingException("namespace not recongnized [" + parts[0] + "]", info);
					}
				} else {
					throw new ProcessingException("namespace not recongnized [" + parts[0] + "]", info);
				}
			}
		}

		this.value = value;
		this.info = info;
	}

	public Param(String name, XdmValue value, Info info) {
		this.name = new QName(name);
		this.value = value;
		this.info = info;
	}

	public Param(String name, XdmValue value) {
		this(name, value, null);
	}

	public Param(String name, String xpath, Info info) {
		this.name = new QName(name);
		this.xpath = xpath;
		this.info = info;
	}

	public Param(String name, String xpath) {
		this(name, xpath, null);
	}

	public Param(QName name, XdmValue value, Map<String, String> namespaces, Info info) throws ProcessingException {
		super();
		this.name = name;
		this.value = value;
		this.info = info;
	}

	public Param(QName name, String xpath, Map<String, String> namespaces, Info info) throws ProcessingException {
		super();
		this.name = name;
		this.xpath = xpath;
		this.info = info;
	}

	public Param(QName name, XdmValue value, Info info) {
		this.name = name;
		this.value = value;
		this.info = info;
	}

	public Param(QName name, XdmValue value) {
		this(name, value, null);
	}

	public Param(QName name, String xpath, Info info) {
		this.name = name;
		this.xpath = xpath;
		this.info = info;
	}

	public Param(QName name, String xpath) {
		this(name, xpath, null);
	}

	public QName getName() {
		return name;
	}

	public XdmValue getValue() {
		return value;
	}

	public String getXpath() {
		return xpath;
	}

	public XdmItem getValue(int index) throws XPathEvaluationException {
		if (value.size() <= index) {
			throw new XPathEvaluationException(
					"Index [" + String.valueOf(index) + "] out of bounds when evaluating " + this.toString());
		} else {
			return value.itemAt(index);
		}
	}

	public ParamTypeEnum getType() {
		return type;
	}

	@Override
	public String toString() {
		if (xpath == null) {
			return "Item [name=" + name + ", value=" + value.toString() + "]";
		} else {
			return "Item [name=" + name + ", select=" + xpath + ", value=" + value.toString() + "]";
		}
	}

	public void setValue(XdmValue value) {
		this.value = value;
	}

	public Info getInfo() {
		return info;
	}

}
