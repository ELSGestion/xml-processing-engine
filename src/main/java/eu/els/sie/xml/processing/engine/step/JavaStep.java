package eu.els.sie.xml.processing.engine.step;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.listener.MessageLevelEnum;
import eu.els.sie.xml.processing.engine.utils.Const;
import eu.els.sie.xml.processing.engine.utils.Param;
import eu.els.sie.xml.processing.engine.utils.Variable;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmEmptySequence;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

public class JavaStep extends Step {

	private Param className;
	private Param methodName;
	private Param methodParam;
	private Variable result;

	@Override
	public void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException {
		setInfo(node);
		className = new Param("class-name", node.getAttributeValue(Const.CLASS_NAME), info);
		methodName = new Param("method-name", node.getAttributeValue(Const.METHOD_ATT_NAME), info);
		methodParam = new Param("method-param", node.getAttributeValue(Const.PARAM_ATT_NAME), info);
		result = new Variable(node.getAttributeValue(Const.RESULT_ATT_NAME), "", namespaces, info);
	}

	@Override
	public Result execute(List<Map<QName, Param>> params) throws ProcessingException {

		owner.evaluate(className, params);
		owner.evaluate(methodName, params);
		owner.evaluate(methodParam, params);

		owner.message(this.getClass().toString(), "Start java [{}], [{}] with the following params {} ",
				new Object[] { className.getValue().toString(), methodName.getValue().toString(),
						methodParam.toString(), params.toString() },
				MessageLevelEnum.debug, null);

		try {
			Class<?> c = Class.forName(className.getValue(0).getStringValue());
			Method m = c.getDeclaredMethod(methodName.getValue(0).getStringValue(), XdmValue.class);

			checkMethodSignature(m);

			Object value = m.invoke(null, methodParam.getValue());

			if (params.size() == 0) {
				params.add(new HashMap<>());
			}

			if (value == null) {
				value = XdmEmptySequence.getInstance();
			}

			result.setValue((XdmValue) value);
			params.get(params.size() - 1).put(result.getName(), result);

		} catch (InvocationTargetException e) {
			throw new ProcessingException(e.getTargetException(), info);
		} catch (Exception e) {
			throw new ProcessingException(e, info);
		}
		return new Result();
	}

	private void checkMethodSignature(Method m) throws ProcessingException {
		Type[] paramTypes = m.getParameterTypes();
		Type returnType = m.getReturnType();

		if (paramTypes.length == 1) {
			if (paramTypes[0].equals(XdmValue.class)) {
				if (returnType != null) {
					if (returnType.equals(XdmValue.class)) {
						return;
					}
				}
			}
		}

		throw new ProcessingException(
				"The java method signature is not compliant : params and return value must be a XdmValue", info);
	}

}
