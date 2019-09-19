package com.redshark.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FuncResult {
	private boolean success;
	private Object data;
}
