package eu.els.sie.xml.processing.engine.step;

public class Result {

	private ResultTypeEnum type;
	private Object object;

	public Result() {
		type = ResultTypeEnum.OK;
	}

	public Result(ResultTypeEnum type) {
		super();
		this.type = type;
	}

	public Result(ResultTypeEnum type, Object object) {
		super();
		this.type = type;
		this.object = object;
	}

	public ResultTypeEnum getType() {
		return type;
	}

	public void setType(ResultTypeEnum type) {
		this.type = type;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

}
