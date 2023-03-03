package eu.els.sie.xml.processing.engine.utils;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.listener.MessageLevelEnum;
import eu.els.sie.xml.processing.engine.step.ProcessingStep;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.TeeDestination;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltTransformer;

public class Pipe {
	private final static String ENCODING = "utf-8";

	private Param nameParam;
	private Param configUriParam;
	private Param sourceUriParam;
	private Param destinationUriParam;
	private Param parallelParam;
	private Param threadsParam;
	private Param sizeParam;
	private Param fileNameFilterParam;
	private Param dirNameFilterParam;
	private Param recursiveParam;
	private Param deleteSourceParam;
	private Param sourceFileNameVariable;
	private Param destinationFileNameParam;
	private Param debugDirUriParam;
	private Param debugParam;
	private Param breakOnErrorParam;
	private Param listenerClassParam;

	private String name;
	private String configUri;
	private XdmValue source;
	private XdmValue destination;

	private List<Transform> transformList;
	private List<Pipe> pipeList;
	private TransformTypeEnum type;

	private Pipe parent;
	private Info info;

	private boolean parallel;
	private int threads;
	private int size;
	private String fileNameFilter;
	private String dirNameFilter;
	private boolean recursive;
	private boolean deleteSource;
	private String debugDirUri;
	private boolean debug;
	private boolean breakOnError;
	private String listenerClass;

	private List<XdmValue> destinationList;
	private List<Param> destinationFileNameParamList;

	private static Integer index = -1;

	public Pipe(Info info) {
		super();
		this.info = info;
	}

	public void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException {

		if (parent == null) {
			index += 1;
			name = "PIPE-" + String.valueOf(index);
		}

		transformList = new LinkedList<Transform>();
		pipeList = new LinkedList<Pipe>();
		String name = node.getAttributeValue(Const.NAME_NAME);
		if (name != null) {
			nameParam = new Param(Const.NAME_NAME, name, info);
		}
		String source = node.getAttributeValue(Const.SOURCE_URI_NAME);
		if (source != null) {
			sourceUriParam = new Param(Const.SOURCE_URI_NAME, source, info);
		}
		String destination = node.getAttributeValue(Const.DESTINATION_URI_NAME);
		if (destination != null) {
			destinationUriParam = new Param(Const.DESTINATION_URI_NAME, destination, info);
		}
		String parallel = node.getAttributeValue(Const.PARALLEL_NAME);
		if (parallel != null) {
			parallelParam = new Param(Const.PARALLEL_NAME, parallel, info);
		}
		String threads = node.getAttributeValue(Const.THREADS_NAME);
		if (threads != null) {
			threadsParam = new Param(Const.THREADS_NAME, threads, info);
		}
		String size = node.getAttributeValue(Const.FILE_SIZE_MAX_NAME);
		if (size != null) {
			sizeParam = new Param(Const.FILE_SIZE_MAX_NAME, size, info);
		}
		String fileNameFilter = node.getAttributeValue(Const.FILE_NAME_FILTER_NAME);
		if (fileNameFilter != null) {
			fileNameFilterParam = new Param(Const.FILE_NAME_FILTER_NAME, fileNameFilter, info);
		}
		String dirNameFilter = node.getAttributeValue(Const.DIR_NAME_FILTER_NAME);
		if (dirNameFilter != null) {
			dirNameFilterParam = new Param(Const.DIR_NAME_FILTER_NAME, dirNameFilter, info);
		}
		String recursive = node.getAttributeValue(Const.RECURSIVE_NAME);
		if (recursive != null) {
			recursiveParam = new Param(Const.RECURSIVE_NAME, recursive, info);
		}

		String deleteSource = node.getAttributeValue(Const.DELETE_SOURCE_NAME);
		if (deleteSource != null) {
			deleteSourceParam = new Param(Const.DELETE_SOURCE_NAME, deleteSource, info);
		}

		String sourceFileNameVariableName = node.getAttributeValue(Const.SOURCE_FILE_NAME_VARIABLE_NAME_NAME);
		if (sourceFileNameVariableName != null) {
			sourceFileNameVariable = new Variable(sourceFileNameVariableName, "", namespaces, info);
		}

		String destinationFileName = node.getAttributeValue(Const.DESTINATION_FILE_NAME_NAME);
		if (destinationFileName != null) {
			destinationFileNameParam = new Param(Const.DESTINATION_FILE_NAME_NAME, destinationFileName, info);
		}
		String debug = node.getAttributeValue(Const.DEBUG_NAME);
		if (debug != null) {
			debugParam = new Param(Const.DEBUG_NAME, debug, info);
		}
		String debugDirUri = node.getAttributeValue(Const.DEBUG_DIR_URI_NAME);
		if (debugDirUri != null) {
			debugDirUriParam = new Param(Const.DEBUG_DIR_URI_NAME, debugDirUri, info);
		}
		String breakOnError = node.getAttributeValue(Const.BREAK_ON_ERROR_NAME);
		if (breakOnError != null) {
			breakOnErrorParam = new Param(Const.BREAK_ON_ERROR_NAME, breakOnError, info);
		}

		String listenerClass = node.getAttributeValue(Const.LISTENER_CLASS_NAME);
		if (listenerClass != null) {
			listenerClassParam = new Param(Const.LISTENER_CLASS_NAME, listenerClass, info);
		}

		XdmSequenceIterator<XdmNode> it = node.axisIterator(Axis.CHILD);

		while (it.hasNext()) {
			XdmNode child = it.next();
			XdmNodeKind kind = child.getNodeKind();
			switch (kind) {
			case ELEMENT:
				Info cInfo = new Info(info.getSystemUri(), child.getLineNumber(), child.getColumnNumber());
				if (child.getNodeName().equals(Const.XSLT_NAME) || child.getNodeName().equals(Const.XQUERY_NAME)) {
					Transform transform = new Transform(cInfo);
					transform.load(child, namespaces);
					transformList.add(transform);
				} else if (child.getNodeName().equals(Const.PIPE_NAME)) {
					Pipe pipe = new Pipe(cInfo);
					pipe.parent = this;
					pipe.load(child, namespaces);
					pipeList.add(pipe);
				} else if (child.getNodeName().equals(Const.CONFIG_NAME)) {
					configUriParam = new Param(Const.URI_NAME, child.getAttributeValue(Const.URI_NAME), cInfo);
				}
				break;
			default:
				break;
			}
		}

		type = transformList.get(0).getType();

	}

	public void evaluate(final ProcessingStep ps, List<Map<QName, Param>> params) throws ProcessingException {
		if (configUriParam != null) {
			if (parent == null) {
				ps.evaluate(configUriParam, params);
				try {
					configUri = configUriParam.getValue(0).getStringValue();
				} catch (Exception e) {
					throw new ProcessingException("The config uri gives null !", e, info);
				}
			} else {
				ps.message(this.getClass().toString(), "config element [{}] ignored in sub pipe",
						new Object[] { configUriParam.getInfo().toString() }, MessageLevelEnum.debug, null);
			}
		}

		if (nameParam != null) {
			if (parent == null) {
				ps.evaluate(nameParam, params);
				try {
					name = nameParam.getValue(0).getStringValue();
				} catch (Exception e) {
					throw new ProcessingException("the name gives null !", e, info);
				}
			} else {
				ps.message(this.getClass().toString(), "pipe name [{}] ignored in sub pipe", new Object[] { info },
						MessageLevelEnum.debug, null);
			}
		} else if (parent == null) {
			ps.message(this.getClass().toString(),
					"pipe name sould be defined for the main pipe [{}] will be used instead!", new Object[] { name },
					MessageLevelEnum.warn, null);
		}

		if (sourceUriParam != null) {
			if (parent == null) {
				ps.evaluate(sourceUriParam, params);
				source = sourceUriParam.getValue();
			} else {
				ps.message(this.getClass().toString(), "pipe source [{}] ignored in sub pipe", new Object[] { info },
						MessageLevelEnum.debug, null);
			}
		} else if (parent == null) {
			throw new ProcessingException("The source attribute is required for the main pipe !", info);
		}

		if (sourceFileNameVariable != null && parent != null) {
			ps.message(this.getClass().toString(), "pipe source-file-name-variable-name [{}] ignored in sub pipe",
					new Object[] { info }, MessageLevelEnum.debug, null);
		} else if (sourceFileNameVariable == null && parent == null) {
			sourceFileNameVariable = new Variable("file-name", "", info);
		}

		if (destinationUriParam != null) {
			ps.evaluate(destinationUriParam, params);
			destination = destinationUriParam.getValue();
		}

		parallel = false;
		if (parallelParam != null) {
			if (parent == null) {
				ps.evaluate(parallelParam, params);
				if (parallelParam.getValue().size() == 0) {
					parallel = false;
				} else if (parallelParam.getValue().itemAt(0).isAtomicValue()) {
					XdmAtomicValue bool = (XdmAtomicValue) parallelParam.getValue().itemAt(0);
					try {
						parallel = bool.getBooleanValue();
					} catch (Exception e) {
						throw new ProcessingException("the parallel attribute xpath [" + parallelParam.getXpath()
								+ "] can't be considered as boolean !", e, info);
					}
				}
			} else {
				ps.message(this.getClass().toString(), "pipe parallel [{}] ignored in sub pipe", new Object[] { info },
						MessageLevelEnum.debug, null);
			}
		}

		threads = 1;
		if (threadsParam != null) {
			if (parent == null) {
				ps.evaluate(threadsParam, params);
				try {
					threads = (int) ((XdmAtomicValue) threadsParam.getValue()).getLongValue();
				} catch (Exception e) {
					throw new ProcessingException("the threads attribute xpath [" + threadsParam.getXpath()
							+ "] can't be considered as int !", e, info);
				}
			} else {
				ps.message(this.getClass().toString(), "pipe threads [{}] ignored in sub pipe", new Object[] { info },
						MessageLevelEnum.debug, null);
			}
		}

		if (sizeParam != null) {
			if (parent == null) {
				ps.evaluate(sizeParam, params);
				try {
					String value = sizeParam.getValue(0).getStringValue();
					if (!value.matches("\\d+\\s+(o|ko|mo|go|to)")) {
						throw new ProcessingException(
								"The file-size-max attribute does not match [\\d+\\s+(o|ko|mo|go|to)] !", info);
					}
					String[] token = value.split("\\s+");
					size = Integer.parseInt(token[0]);
					if (token[1].equals("ko")) {
						size *= 1024;
					} else if (token[1].equals("mo")) {
						size *= 1024 * 1024;
					} else if (token[1].equals("go")) {
						size *= 1024 * 1024 * 1024;
					} else if (token[1].equals("to")) {
						size *= 1024 * 1024 * 1024 * 1024;
					}

				} catch (Exception e) {
					throw new ProcessingException("The file-size-max attribute gives null !", e, info);
				}
			} else {
				ps.message(this.getClass().toString(), "pipe file-size-max [{}] ignored in sub pipe",
						new Object[] { info }, MessageLevelEnum.debug, null);
			}
		} else if (parent == null && parallel) {
			throw new ProcessingException("The file-size-max attribute is required for the main pipe !", info);
		}

		fileNameFilter = ".*";
		if (fileNameFilterParam != null) {
			if (parent == null) {
				ps.evaluate(fileNameFilterParam, params);
				try {
					fileNameFilter = fileNameFilterParam.getValue(0).getStringValue();

					try {
						Pattern.compile(fileNameFilter);
					} catch (Exception e) {
						throw new ProcessingException("The file-name-filter attribute is not a valide regex !", e,
								info);
					}

				} catch (Exception e) {
					throw new ProcessingException("The file-name-filter attribute gives null !", e, info);
				}
			} else {
				ps.message(this.getClass().toString(), "pipe file-name-filter [{}] ignored in sub pipe",
						new Object[] { info }, MessageLevelEnum.debug, null);
			}
		}

		dirNameFilter = ".*";
		if (dirNameFilterParam != null) {
			if (parent == null) {
				ps.evaluate(dirNameFilterParam, params);
				try {
					dirNameFilter = dirNameFilterParam.getValue(0).getStringValue();

					try {
						Pattern.compile(dirNameFilter);
					} catch (Exception e) {
						throw new ProcessingException("The dir-name-filter attribute is not a valide regex !", e, info);
					}

				} catch (Exception e) {
					throw new ProcessingException("The dir-name-filter attribute gives null !", e, info);
				}
			} else {
				ps.message(this.getClass().toString(), "pipe dir-name-filter [{}] ignored in sub pipe",
						new Object[] { info }, MessageLevelEnum.debug, null);
			}
		}

		recursive = false;
		if (recursiveParam != null) {
			if (parent == null) {
				ps.evaluate(recursiveParam, params);
				if (recursiveParam.getValue().size() == 0) {
					recursive = false;
				} else if (recursiveParam.getValue().itemAt(0).isAtomicValue()) {
					XdmAtomicValue bool = (XdmAtomicValue) recursiveParam.getValue().itemAt(0);
					try {
						recursive = bool.getBooleanValue();
					} catch (Exception e) {
						throw new ProcessingException("the recursive attribute xpath [" + recursiveParam.getXpath()
								+ "] can't be considered as boolean !", e, info);
					}
				}
			} else {
				ps.message(this.getClass().toString(), "pipe recursive [{}] ignored in sub pipe", new Object[] { info },
						MessageLevelEnum.debug, null);
			}
		}

		deleteSource = false;
		if (deleteSourceParam != null) {
			if (parent == null) {
				ps.evaluate(deleteSourceParam, params);
				if (deleteSourceParam.getValue().size() == 0) {
					deleteSource = false;
				} else if (deleteSourceParam.getValue().itemAt(0).isAtomicValue()) {
					XdmAtomicValue bool = (XdmAtomicValue) deleteSourceParam.getValue().itemAt(0);
					try {
						deleteSource = bool.getBooleanValue();
					} catch (Exception e) {
						throw new ProcessingException("the delete-source attribute xpath ["
								+ deleteSourceParam.getXpath() + "] can't be considered as boolean !", e, info);
					}
				}
			} else {
				ps.message(this.getClass().toString(), "pipe recursive [{}] ignored in sub pipe", new Object[] { info },
						MessageLevelEnum.debug, null);
			}
		}

		debug = false;
		if (debugParam != null) {
			if (parent == null) {
				ps.evaluate(debugParam, params);
				if (debugParam.getValue().size() == 0) {
					debug = false;
				} else if (debugParam.getValue().itemAt(0).isAtomicValue()) {
					XdmAtomicValue bool = (XdmAtomicValue) debugParam.getValue().itemAt(0);
					try {
						debug = bool.getBooleanValue();
					} catch (Exception e) {
						throw new ProcessingException("the debug attribute xpath [" + debugParam.getXpath()
								+ "] can't be considered as boolean !", e, info);
					}
				}
			} else {
				ps.message(this.getClass().toString(), "pipe debug [{}] ignored in sub pipe", new Object[] { info },
						MessageLevelEnum.debug, null);

			}
		}

		if (debugDirUriParam != null) {
			if (parent == null) {
				ps.evaluate(debugDirUriParam, params);
				try {
					debugDirUri = debugDirUriParam.getValue(0).getStringValue();

					try {
						URI.create(debugDirUri);
					} catch (Exception e) {
						throw new ProcessingException("The debug-dir-uri attribute is not a valide uri !", e, info);
					}

				} catch (Exception e) {
					ps.message(this.getClass().toString(), "pipe debug-dir-uri [{}] ignored because il's null",
							new Object[] { info }, MessageLevelEnum.debug, null);
					debug = false;
				}
			} else {
				ps.message(this.getClass().toString(), "pipe debug-dir-uri [{}] ignored in sub pipe",
						new Object[] { info }, MessageLevelEnum.debug, null);

			}
		}

		breakOnError = false;
		if (breakOnErrorParam != null) {
			if (parent == null) {
				ps.evaluate(breakOnErrorParam, params);
				if (breakOnErrorParam.getValue().size() == 0) {
					breakOnError = false;
				} else if (breakOnErrorParam.getValue().itemAt(0).isAtomicValue()) {
					XdmAtomicValue bool = (XdmAtomicValue) breakOnErrorParam.getValue().itemAt(0);
					try {
						breakOnError = bool.getBooleanValue();
					} catch (Exception e) {
						throw new ProcessingException("the break-on-error attribute xpath [" + debugParam.getXpath()
								+ "] can't be considered as boolean !", e, info);
					}
				}
			} else {
				ps.message(this.getClass().toString(), "pipe break-on-error [{}] ignored in sub pipe",
						new Object[] { info }, MessageLevelEnum.debug, null);

			}
		}

		if (listenerClassParam != null) {
			if (parent == null) {
				ps.evaluate(listenerClassParam, params);
				try {
					listenerClass = listenerClassParam.getValue(0).getStringValue();
				} catch (Exception e) {
					ps.message(this.getClass().toString(), "pipe listener-class [{}] ignored because il's null",
							new Object[] { info }, MessageLevelEnum.debug, null);
				}
			} else {
				ps.message(this.getClass().toString(), "pipe listener-class [{}] ignored in sub pipe",
						new Object[] { info }, MessageLevelEnum.debug, null);

			}
		}

		LinkedList<Transform> cTransformList = new LinkedList<Transform>();
		for (Transform transform : transformList) {
			transform.evaluate(ps, params);
			cTransformList.add(transform);
		}

		if (cTransformList.isEmpty()) {
			throw new ProcessingException("All transforms have use-when to false : the pipe cannot be empty", info);
		} else {
			transformList = cTransformList;
		}

		for (Pipe pipe : pipeList) {
			pipe.evaluate(ps, params);
		}

	}

	public void compile(ProcessingStep ps, Processor processor, String baseUri) throws ProcessingException {
		destinationList = new LinkedList<XdmValue>();
		destinationFileNameParamList = new LinkedList<Param>();

		for (Transform transform : transformList) {
			transform.compile(ps, processor, baseUri);
		}

		if (pipeList.isEmpty()) {
			destinationList.add(destination);
			destinationFileNameParamList.add(destinationFileNameParam);
		} else {
			if (destinationUriParam != null) {
				ps.message(this.getClass().toString(), "pipe destination [{}] ignored in non terminal pipe",
						new Object[] { info }, MessageLevelEnum.debug, null);
			}
			if (destinationFileNameParam != null) {
				ps.message(this.getClass().toString(), "pipe destination-file-name [{}] ignored in non terminal pipe",
						new Object[] { info }, MessageLevelEnum.debug, null);
			}
			for (Pipe pipe : pipeList) {
				pipe.compile(ps, processor, baseUri);
				destinationList.addAll(pipe.destinationList);
				destinationFileNameParamList.addAll(pipe.destinationFileNameParamList);
			}
		}
	}

	public String evaluateDestinationFileName(File sFile, int s, ProcessingStep ps, List<Map<QName, Param>> params)
			throws ProcessingException {

		if (destinationFileNameParamList.get(s) != null) {

			Map<QName, Param> map = new HashMap<QName, Param>();
			sourceFileNameVariable.setValue(new XdmAtomicValue(sFile.getName()));
			map.put(sourceFileNameVariable.getName(), sourceFileNameVariable);
			params.add(map);

			ps.evaluate(destinationFileNameParamList.get(s), params);

			params.remove(map);

			try {
				return destinationFileNameParamList.get(s).getValue(0).getStringValue();
			} catch (Exception e) {
				ps.message(this.getClass().toString(), "pipe file name [{}] gives null : will be ignored.",
						new Object[] { destinationFileNameParamList.get(s) }, MessageLevelEnum.error, null);
				return sFile.getName();
			}
		} else {
			return sFile.getName();
		}
	}

	public synchronized PipeTransformer load(ProcessingStep ps, Processor processor, File file, Path relativize)
			throws ProcessingException {
		LinkedList<Destination> transformerList = new LinkedList<Destination>();
		Destination transformer = null;
		Destination previousTransformer = null;
		Transform previousTransform = null;

		int transformIndex = -1;
		String level = "0";

		Pipe mainPipe = this;
		while (true) {
			if (mainPipe.parent == null) {
				break;
			} else {
				level += "_" + String.valueOf(mainPipe.parent.pipeList.indexOf(mainPipe));
				mainPipe = mainPipe.parent;
			}
		}

		for (Transform transform : transformList) {

			if (!transform.getUseWhen()) {
				continue;
			}

			Destination tr = transform.load(ps, processor, file);
			if (previousTransformer != null) {
				Destination dest;
				if (debug) {
					URI debugFileDirUri = null;
					try {
						debugFileDirUri = new URI(debugDirUri + "/" + URLEncoder.encode(mainPipe.name, ENCODING) + "/"
								+ level + "_" + String.valueOf(transformIndex) + "_"
								+ URLEncoder.encode(
										new URI(previousTransform.getUri()).getPath().replaceAll("[/\\\\:]", "_"),
										ENCODING));
						File debugFile = new File(debugFileDirUri).toPath().resolve(relativize).toFile();
						dest = new TeeDestination(tr, processor.newSerializer(debugFile));
					} catch (Exception e) {
						ps.message(this.getClass().toString(),
								"debug mode error when creating output uri [{}, {}, {}].",
								new Object[] { debugDirUri, previousTransform.getUri(), file.getName() },
								MessageLevelEnum.error, null);
						dest = tr;
					}
				} else {
					dest = tr;
				}

				switch (previousTransform.getType()) {
				case xquery:
					((XQueryEvaluator) previousTransformer).setDestination(dest);
					break;
				case xslt:
					((XsltTransformer) previousTransformer).setDestination(dest);
					break;
				default:
					break;
				}

				previousTransformer = tr;
				previousTransform = transform;
			} else {
				transformer = tr;
				previousTransform = transform;
				previousTransformer = tr;
			}
			transformIndex++;
		}

		if (pipeList.isEmpty()) {
			transformerList.add(previousTransformer);
		} else {
			Destination td = null;
			for (Pipe pipe : pipeList) {
				pipe.debug = debug;
				pipe.debugDirUri = debugDirUri;
				PipeTransformer pt = pipe.load(ps, processor, file, relativize);
				Destination tr = pt.getTransformer();
				transformerList.addAll(pt.getTransformerList());
				if (td == null) {
					td = tr;
				} else {
					td = new TeeDestination(td, tr);
				}
			}

			Destination dest;

			if (debug) {
				URI debugFileDirUri = null;
				try {
					debugFileDirUri = new URI(debugDirUri + "/" + URLEncoder.encode(mainPipe.name, ENCODING) + "/"
							+ level + "_" + String.valueOf(transformIndex) + "_"
							+ URLEncoder.encode(
									new URI(previousTransform.getUri()).getPath().replaceAll("[/\\\\:]", "_"),
									ENCODING));
					File debugFile = new File(debugFileDirUri).toPath().resolve(relativize).toFile();
					dest = new TeeDestination(td, processor.newSerializer(debugFile));
				} catch (Exception e) {
					ps.message(this.getClass().toString(), "debug mode error when creating output uri [{}, {}, {}].",
							new Object[] { debugDirUri, previousTransform.getUri(), file.getName() },
							MessageLevelEnum.error, null);
					dest = td;
				}
			} else {
				dest = td;
			}

			switch (previousTransform.getType()) {
			case xquery:
				((XQueryEvaluator) previousTransformer).setDestination(dest);
				break;
			case xslt:
				((XsltTransformer) previousTransformer).setDestination(dest);
				break;
			default:
				break;
			}
		}

		return new PipeTransformer(transformer, transformerList);
	}

	public String getConfigUri() {
		return configUri;
	}

	public XdmValue getSource() {
		return source;
	}

	public boolean isParallel() {
		return parallel;
	}

	public int getThreads() {
		return threads;
	}

	public int getSize() {
		return size;
	}

	public List<XdmValue> getDestinationList() {
		return destinationList;
	}

	public String getName() {
		return name;
	}

	public TransformTypeEnum getType() {
		return type;
	}

	public String getFileNameFilter() {
		return fileNameFilter;
	}

	public String getDirNameFilter() {
		return dirNameFilter;
	}

	public boolean isRecursive() {
		return recursive;
	}

	public boolean deleteSource() {
		return deleteSource;
	}

	public boolean isBreakOnError() {
		return breakOnError;
	}

	public String getListenerClass() {
		return listenerClass;
	}

	@Override
	public String toString() {
		return "Pipe [name=" + name + ", type=" + type.name() + ", parallel=" + parallel + ", deleteSource="
				+ deleteSource + ", threads=" + threads + ", file-max-size (o) =" + size + ", fileNameFilter="
				+ fileNameFilter + ", dirNameFilter=" + dirNameFilter + ", breakOnError=" + breakOnError
				+ ", recursive=" + recursive + "]";
	}

}
