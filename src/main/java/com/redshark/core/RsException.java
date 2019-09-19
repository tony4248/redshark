package com.redshark.core;

public class RsException extends Exception {
	
	  private static final long serialVersionUID = 1L;
	
	  public RsException(String message) {
	    super(message);
	  }
	
	  public RsException(String message, Throwable cause) {
	    super(message, cause);
	  }
	
	  public RsException(Throwable t) {
	    super(t);
	  }
}
