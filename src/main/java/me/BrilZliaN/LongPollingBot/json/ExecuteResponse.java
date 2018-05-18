package me.BrilZliaN.LongPollingBot.json;

import java.util.List;

public class ExecuteResponse {
	
	private List<ExecuteIsMemberResponse> response;
	private List<ExecuteError> execute_errors;

	public List<ExecuteIsMemberResponse> getResponse() {
		return response;
	}

	public void setResponse(List<ExecuteIsMemberResponse> response) {
		this.response = response;
	}

	public List<ExecuteError> getExecute_errors() {
		return execute_errors;
	}

	public void setExecute_errors(List<ExecuteError> execute_errors) {
		this.execute_errors = execute_errors;
	}

}
