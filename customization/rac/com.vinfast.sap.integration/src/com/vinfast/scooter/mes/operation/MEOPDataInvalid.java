package com.vinfast.scooter.mes.operation;

public class MEOPDataInvalid extends MEOPDataAbstract {
	@Override
	void jsonAppendBody(StringBuffer dataString) {
		// do nothing
	}
	
	@Override
	void xmlAppendBody(StringBuffer dataString) {
		// do nothing
	}

	@Override
	public String isValidOperation() {
		if (operationType.equals("")) {
			return "PLM: Operation Type value not filled. Please fill Operation Type.";
		} else if (operationType.equals("NA")) {
			return "\"PLM: NA operations not required to send to MES.\"";
		} else {
			return "\"PLM: Operation Type isn't supportted.\"";
		}
	}
}
