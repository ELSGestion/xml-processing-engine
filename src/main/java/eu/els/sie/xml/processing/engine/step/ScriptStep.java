package eu.els.sie.xml.processing.engine.step;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.listener.MessageLevelEnum;
import eu.els.sie.xml.processing.engine.utils.Const;
import eu.els.sie.xml.processing.engine.utils.Param;
import eu.els.sie.xml.processing.engine.utils.Variable;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

public class ScriptStep extends Step {

	private String uri;
	private String args;
	private Variable result;

	Param uriParam;
	Param argsParam;

	@Override
	public void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException {
		setInfo(node);
		this.uri = node.getAttributeValue(Const.URI_NAME);
		this.args = node.getAttributeValue(Const.ARGS_NAME);
		result = new Variable(node.getAttributeValue(Const.RESULT_ATT_NAME), "", namespaces, info);
		uriParam = new Param("uri", uri, info);
		argsParam = new Param("args", args, info);
	}

	@Override
	public Result execute(List<Map<QName, Param>> params) throws ProcessingException {

		owner.evaluate(uriParam, params);
		owner.evaluate(argsParam, params);

		try {
			this.uri = uriParam.getValue(0).getStringValue();
		} catch (Exception e) {
			throw new ProcessingException("The script uri gives null !", e, info);
		}
		

		URI rURI;
		try {
			rURI = new URI(owner.resolve(uri).getSystemId());
		} catch (Exception e) {
			throw new ProcessingException("Error when solving uri [" + uri + "]", e, info);
		}

		File scriptFile = null;
		boolean temp = false;

		if ("file".equals(rURI.getScheme())) {
			try {
				scriptFile = new File(rURI);
			} catch (Exception e) {
				throw new ProcessingException("[" + uri + "] is not a valid URI !", e, info);
			}
		} else {
			try {
				temp = true;
				scriptFile = File.createTempFile(FilenameUtils.getBaseName(rURI.getPath()),
						"." + FilenameUtils.getExtension(rURI.getPath()));
				InputStream in = rURI.toURL().openStream();
				OutputStream out = new FileOutputStream(scriptFile);
				IOUtils.copy(in, out);
				in.close();
				out.close();
				scriptFile.setExecutable(true);
			} catch (Exception e) {
				throw new ProcessingException("Unable to create temp file for script [ " + uri + "]", e, info);
			}
		}

		XdmValue argsValue = argsParam.getValue();

		String[] commands = new String[1 + argsValue.size()];

		commands[0] = scriptFile.getAbsolutePath();
		for (int i = 0; i < argsValue.size(); i++) {
			commands[i + 1] = argsValue.itemAt(i).getStringValue();
		}

		owner.message(this.getClass().toString(), "Start script with the following params {} ",
				new Object[] { Arrays.asList(commands).toString() }, MessageLevelEnum.debug, null);

		Runtime rt = Runtime.getRuntime();

		Process process = null;
		try {
			process = rt.exec(commands);

			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while (null != (line = br.readLine())) {
				XdmValue value = new XdmAtomicValue(line);
				owner.message(this.getClass().toString(), value.toString(), new Object[0], MessageLevelEnum.info, null);
			}
			br.close();

			br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			while (null != (line = br.readLine())) {
				XdmValue value = new XdmAtomicValue(line);
				owner.message(this.getClass().toString(), value.toString(), new Object[0], MessageLevelEnum.error,
						null);
			}
			br.close();

			int value = process.waitFor();

			result.setValue(new XdmAtomicValue(value));
			params.get(params.size() - 1).put(result.getName(), result);

		} catch (Exception e) {
			throw new ProcessingException(e, info);
		}

		if (temp) {
			if (!scriptFile.delete()) {
				owner.message(this.getClass().toString(), "Unable to delete script temp file [{}]",
						new Object[] { scriptFile.toURI().toString() }, MessageLevelEnum.debug, null);
			}
		}
		return new Result();

	}

}
