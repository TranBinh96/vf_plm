package com.teamcenter.vinfast.utils;

import java.util.List;

import com.teamcenter.rac.kernel.TCComponent;

public interface IValidator {
	public List<ValidationResult> validate(TCComponent targetItem) throws Exception;
}
