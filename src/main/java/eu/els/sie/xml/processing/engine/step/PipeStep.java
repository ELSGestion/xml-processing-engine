package eu.els.sie.xml.processing.engine.step;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.output.NullOutputStream;

import eu.els.sie.utils.parallel.OperationStateEnum;
import eu.els.sie.utils.parallel.Parallel;
import eu.els.sie.utils.parallel.ParallelListener;
import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.listener.MessageLevelEnum;
import eu.els.sie.xml.processing.engine.listener.PipeListener;
import eu.els.sie.xml.processing.engine.listener.PipeListenerImpl;
import eu.els.sie.xml.processing.engine.utils.Param;
import eu.els.sie.xml.processing.engine.utils.Pipe;
import eu.els.sie.xml.processing.engine.utils.PipeTransformer;
import eu.els.sie.xml.processing.engine.utils.TransformTypeEnum;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.ResourceRequest;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltTransformer;

public class PipeStep extends Step {

	private Pipe pipe;
	private Configuration config;

	private PipeListener listener;
	private boolean broken;
	private Exception brokenException;

	public PipeStep() {
		super();
		listener = new PipeListenerImpl();
		broken = false;
	}

	public void run(List<Map<QName, Param>> params) throws ProcessingException {

		if (pipe.getSource().size() == 0) {
			owner.message(this.getClass().getCanonicalName(), "main pipe source is empty !", new Object[] {},
					MessageLevelEnum.debug, null);
			return;
		}

		/***
		 * For conf
		 */

		if (pipe.getConfigUri() != null) {

			String configUri = pipe.getConfigUri();
			try {
				configUri = owner.resolve(configUri).getSystemId();
			} catch (Exception e) {
				throw new ProcessingException(
						"Error when solving uri [" + configUri + "] relative to owner.uri [" + owner.uri + "]", e,
						info);
			}
			Source source = null;
			try {
				source = owner.resolve(configUri);
			} catch (Exception e) {
				throw new ProcessingException(e, info);
			}

			Configuration config = null;
			try {
				config = Configuration.readConfiguration(source);
			} catch (Exception e) {
				throw new ProcessingException(e, info);
			}
			this.config = config;
		} else {
			Source source = null;
			try {
				source = owner.resolve(owner.configUri);
			} catch (Exception e) {
				throw new ProcessingException(e, info);
			}

			Configuration config = null;
			try {
				config = Configuration.readConfiguration(source);
			} catch (Exception e) {
				throw new ProcessingException(e, info);
			}
			this.config = config;
		}

		this.config.setNamePool(ProcessingStep.NAMEPOOL);
		this.config.setDocumentNumberAllocator(ProcessingStep.DOCUMENTNUMBERALLOCATOR);

		Processor processor = new Processor(this.config);

		pipe.compile(owner, processor, owner.uri);

		final List<Integer> sSequenceIndexes = new LinkedList<Integer>();
		final List<Integer> sParallelIndexes = new LinkedList<Integer>();
		final List<Integer> sIgnoredIndexes = new LinkedList<Integer>();

		Iterator<XdmItem> itr = pipe.getSource().iterator();

		List<File> sFiles = new LinkedList<File>();
		List<Path> sFileRelatives = new LinkedList<Path>();
		List<List<File>> dFiles = new LinkedList<List<File>>();

		boolean parallel = pipe.isParallel();
		int threads = pipe.getThreads();
		int size = pipe.getSize();
		String fileNameFilter = pipe.getFileNameFilter();
		String dirNameFilter = pipe.getDirNameFilter();
		boolean recursive = pipe.isRecursive();
		boolean deleteSource = pipe.deleteSource();

		Pattern fileNameFilterPattern = Pattern.compile(fileNameFilter);
		Pattern dirNameFilterPattern = Pattern.compile(dirNameFilter);

		int i = 0;
		while (itr.hasNext()) {

			String cSourceUri = itr.next().getStringValue();
			URI sURI = null;
			File sFile = null;
			try {
				sURI = new URI(cSourceUri);
				sFile = new File(sURI);
			} catch (Exception e) {
				owner.message(this.getClass().getCanonicalName(), "[{}] is not a valid URI !",
						new Object[] { cSourceUri }, MessageLevelEnum.error, e);
				continue;
			}

			boolean exists = sFile.exists();
			boolean isDirectory = sFile.isDirectory();
			boolean matches = fileNameFilterPattern.matcher(sFile.getName()).find();

			if (exists) {
				if (isDirectory) {
					processDirectory(sFile, sFile, sFiles, sFileRelatives, recursive, fileNameFilterPattern,
							dirNameFilterPattern);
				} else if (matches) {
					sFiles.add(sFile);
					sFileRelatives.add(sFile.toPath().getFileName());
				} else {
					sIgnoredIndexes.add(i);
				}
			} else {
				owner.message(this.getClass().getCanonicalName(), "URI [{}] not found !", new Object[] { cSourceUri },
						MessageLevelEnum.debug, null);
			}

			List<List<File>> tempFiles = new LinkedList<List<File>>();

			int s = 0;
			for (XdmValue value : pipe.getDestinationList()) {
				List<File> cdFiles = new LinkedList<File>();
				String destinationUri = null;
				if (value != null) {
					if (value.size() > i) {
						destinationUri = value.itemAt(i).getStringValue();
					} else if (value.size() == 1) {
						destinationUri = value.itemAt(0).getStringValue();
					} else {
						owner.message(this.getClass().getCanonicalName(),
								"destination @ index [{}] has no item @ index [{}] : assume as null output !",
								new Object[] { String.valueOf(s), String.valueOf(i) }, MessageLevelEnum.debug, null);
					}
				}

				URI dURI = null;
				File dFile = null;
				try {
					dURI = (destinationUri == null) ? null : new URI(destinationUri);
					dFile = (dURI == null) ? null : new File(dURI);
				} catch (Exception e) {
					owner.message(this.getClass().getCanonicalName(), "[{}] is not a valid URI !",
							new Object[] { destinationUri }, MessageLevelEnum.error, e);
					continue;
				}

				if (exists) {
					if (isDirectory) {
						processDirectory(sFile, dFile, cdFiles, recursive, fileNameFilterPattern, dirNameFilterPattern,
								s, params);
					} else if (matches) {
						if (dFile == null) {
							cdFiles.add(dFile);
						} else {
							if (dFile.isDirectory()) {
								cdFiles.add(new File(dFile, pipe.evaluateDestinationFileName(sFile, s, owner, params)));
							} else {
								cdFiles.add(dFile);
							}
						}
					}
				}

				tempFiles.add(cdFiles);

				s++;
			}

			if (s > 0) {
				for (int j = 0; j < tempFiles.get(0).size(); j++) {
					List<File> cdFiles = new LinkedList<File>();
					for (int k = 0; k < s; k++) {
						cdFiles.add(tempFiles.get(k).get(j));
					}
					dFiles.add(cdFiles);
				}
			}

			i++;
		}

		int total = sFiles.size();

		for (int k = 0; k < sFiles.size(); k++) {
			if (sIgnoredIndexes.contains(k)) {
			} else if (parallel && sFiles.get(k).length() < size) {
				sParallelIndexes.add(k);
			} else {
				sSequenceIndexes.add(k);
			}
		}

		if (!sSequenceIndexes.isEmpty()) {
			// TODO
			/**
			 * Si j'ai 10 thread et un file-size-max a 20 mo => je creer dans la liste de
			 * sequnnce index des group dont la taille taille total ne dépassent pas 10 x 20
			 * mo et je lance un parallel dessus avec nb thread = nb files
			 */
			for (Integer index : sSequenceIndexes) {
				if (broken) {
					listener.interrupted(pipe.getName(), total, sFiles.get(index).toURI().toString(), index);
				} else {
					try {
						listener.start(pipe.getName(), total, sFiles.get(index).toURI().toString(), index);
						PipeTransformer pt = pipe.load(owner, processor, sFiles.get(index), sFileRelatives.get(index));
						process(processor, pipe.getType(), pt.getTransformer(), pt.getTransformerList(), sFiles, dFiles,
								index);
						if (deleteSource) {
							Files.delete(sFiles.get(index).toPath());
						}
						listener.succed(pipe.getName(), total, sFiles.get(index).toURI().toString(), index);
					} catch (Exception e) {
						listener.failed(pipe.getName(), total, sFiles.get(index).toURI().toString(), index, e);
						if (pipe.isBreakOnError()) {
							broken = true;
							brokenException = e;
						}
					}
				}
			}
		}

		if (parallel && !broken) {
			Parallel<Integer, Boolean> p = new Parallel<>();
			p.setListener(new ParallelListener<Integer, Boolean>() {

				@Override
				public synchronized void endItem(OperationStateEnum ose, Integer element, Boolean result, Exception e) {
					switch (ose) {
					case FAILED:
						listener.failed(pipe.getName(), total, sFiles.get(element).toURI().toString(), element, e);
						broken = pipe.isBreakOnError();
						brokenException = e;
						break;
					case INTURRUPTED:
						listener.interrupted(pipe.getName(), total, sFiles.get(element).toURI().toString(), element);
						break;
					case SUCCED:
						listener.succed(pipe.getName(), total, sFiles.get(element).toURI().toString(), element);
						break;
					default:
						break;
					}
				}

				@Override
				public void end() {

				}

				@Override
				public void startItem(Integer element) {
					listener.start(pipe.getName(), total, sFiles.get(element).toURI().toString(), element);
				}

				@Override
				public void start(int numThreads, Iterable<Integer> elements, Long keepAliveTime, Long timeout,
						TimeUnit unit, boolean breakOnError) {
					// TODO Auto-generated method stub

				}

			});

			p.blockingFor(threads, sParallelIndexes, new Parallel.Operation<Integer, Boolean>() {

				@Override
				public Boolean perform(Integer index) throws Exception {
					PipeTransformer pt = pipe.load(owner, processor, sFiles.get(index), sFileRelatives.get(index));
					process(processor, pipe.getType(), pt.getTransformer(), pt.getTransformerList(), sFiles, dFiles,
							index);
					if (deleteSource) {
						Files.delete(sFiles.get(index).toPath());
					}
					return true;
				}
			}, pipe.isBreakOnError());
		}

		if (broken) {
			throw new ProcessingException("PIPE broken", brokenException, info);
		}
	}

	private void processDirectory(File sourceFile, File destinationFile, List<File> dFiles, boolean recursive,
			Pattern fileNameFilterPattern, Pattern dirNameFilterPattern, int s, List<Map<QName, Param>> params)
			throws ProcessingException {
		if (destinationFile != null) {
			if (!destinationFile.exists()) {
				if (!destinationFile.mkdirs()) {
					throw new ProcessingException("Cannot create destination uri [" + destinationFile.toURI().toString()
							+ "] as a directory]", info);
				}
			}
		}

		for (File file : sourceFile.listFiles()) {
			if (file.isDirectory()) {
				if (recursive && dirNameFilterPattern.matcher(file.getName()).find()) {
					processDirectory(file, destinationFile == null ? null : new File(destinationFile, file.getName()),
							dFiles, recursive, fileNameFilterPattern, dirNameFilterPattern, s, params);
				}
			} else {
				if (fileNameFilterPattern.matcher(file.getName()).find()) {
					dFiles.add(destinationFile == null ? null
							: new File(destinationFile, pipe.evaluateDestinationFileName(file, s, owner, params)));
				}
			}
		}
	}

	private void processDirectory(File mainSourceFile, File sourceFile, List<File> sFiles, List<Path> sFileRelatives,
			boolean recursive, Pattern fileNameFilterPattern, Pattern dirNameFilterPattern) throws ProcessingException {
		for (File file : sourceFile.listFiles()) {
			if (file.isDirectory()) {
				if (recursive && dirNameFilterPattern.matcher(file.getName()).find()) {
					processDirectory(mainSourceFile, file, sFiles, sFileRelatives, recursive, fileNameFilterPattern,
							dirNameFilterPattern);
				}
			} else {
				if (fileNameFilterPattern.matcher(file.getName()).find()) {
					sFiles.add(file);
					sFileRelatives.add(mainSourceFile.toPath().relativize(file.toPath()));
				}
			}
		}
	}

	private void process(Processor processor, TransformTypeEnum type, Destination transformer, List<Destination> list,
			List<File> sFiles, List<List<File>> dFiles, int index) throws ProcessingException {

		String sourceUri = sFiles.get(index).toURI().toString();

		boolean useSAXSource = processor.getUnderlyingConfiguration().getBooleanProperty(Feature.DTD_VALIDATION);

		Source source;
		
		ResourceRequest request = new ResourceRequest();
		request.uri = sourceUri;
		
		if (useSAXSource) {
			try {
				source = processor.getUnderlyingConfiguration().getResourceResolver().resolve(request);
			} catch (Exception e) {
				throw new ProcessingException("Error when loading file with uri [" + sourceUri + "] !", e, info);
			}
		} else {
			source = new StreamSource(sourceUri);
		}

		try {
			switch (type) {
			case xquery:
				((XQueryEvaluator) transformer).setSource(source);
				break;
			case xslt:
				((XsltTransformer) transformer).setSource(source);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			throw new ProcessingException("Error when loading file with uri [" + source.getSystemId() + "] !", e, info);
		}

		for (int i = 0; i < list.size(); i++) {
			Serializer serializer = (dFiles.get(index).get(i) == null) ? processor.newSerializer(new NullOutputStream())
					: processor.newSerializer(dFiles.get(index).get(i));
			switch (type) {
			case xquery:
				((XQueryEvaluator) list.get(i)).setDestination(serializer);
				break;
			case xslt:
				((XsltTransformer) list.get(i)).setDestination(serializer);
				break;
			default:
				break;
			}
		}

		try {
			switch (type) {
			case xquery:
				((XQueryEvaluator) transformer).run();
				break;
			case xslt:
				((XsltTransformer) transformer).transform();
				break;
			default:
				break;
			}

			transformer.close();

		} catch (SaxonApiException e) {
			String message = e.getMessage();

			if (e.getSystemId() != null) {
				message = "[" + e.getSystemId() + " @Line " + e.getLineNumber() + "] " + message;
			}

			throw new ProcessingException("Failed to run " + type.name() + " pipe transformation : " + message, e,
					info);
		} catch (Exception e) {
			throw new ProcessingException("Failed to run " + type.name() + " pipe transformation !", e, info);
		}

	}

	@Override
	public Result execute(List<Map<QName, Param>> params) throws ProcessingException {
		pipe.evaluate(owner, params);

		String listenerClass;

		if (pipe.getListenerClass() != null) {
			listenerClass = pipe.getListenerClass();
		} else {
			listenerClass = owner.pipeListenerClass;
		}

		try {
			listener = (PipeListener) Class.forName(listenerClass).newInstance();
		} catch (Exception e) {
			owner.message(this.getClass().getCanonicalName(),
					"Unable to create an instance of [{}] as PipeListener : default one will be used instead.",
					new Object[] { pipe.getListenerClass() }, MessageLevelEnum.warn, e);
			listener = new PipeListenerImpl();
		}

		owner.message(this.getClass().toString(), "Start pipe {}", new Object[] { pipe }, MessageLevelEnum.debug, null);
		run(params);
		return new Result();
	}

	@Override
	public void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException {
		setInfo(node);
		pipe = new Pipe(info);
		pipe.load(node, namespaces);
	}

}
