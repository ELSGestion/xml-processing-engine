package eu.els.sie.xml.processing.engine.step;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.utils.Const;
import eu.els.sie.xml.processing.engine.utils.Info;
import eu.els.sie.xml.processing.engine.utils.Param;
import eu.els.sie.xml.processing.engine.utils.Variable;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

public class ForEachStep extends MultiStep {

	private Variable iterator;

	@Override
	public void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException {
		setInfo(node);
		iterator = new Variable(node.getAttributeValue(Const.NAME_NAME), node.getAttributeValue(Const.SELECT_NAME),
				namespaces, new Info(owner.uri, node.getLineNumber(), node.getColumnNumber()));
		super.load(node, namespaces);
	}

	@Override
	public Result execute(List<Map<QName, Param>> params) throws ProcessingException {

		owner.evaluate(iterator, params);

		Iterator<XdmItem> it = iterator.getValue().iterator();
		while (it.hasNext()) {
			XdmItem item = it.next();
			VariableStep step = new VariableStep(iterator.getName(), item, info);
			step.setOwner(owner);
			steps.add(0, step);
			Result result = super.execute(params);
			steps.remove(0);
			switch (result.getType()) {
			case BREAK:
				return new Result();
			case EXIT:
				return new Result(ResultTypeEnum.EXIT);
			case CONTINUE:
				break;
			case OK:
				break;
			default:
				break;
			}
		}

		return new Result();

	}

}
