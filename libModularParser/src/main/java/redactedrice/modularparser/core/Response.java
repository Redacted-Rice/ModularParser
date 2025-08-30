package redactedrice.modularparser.core;

import java.util.Objects;

public final class Response<T> {
	protected T val;
	protected boolean handled;
	protected String error;
	
	protected Response(T val, boolean handled, String error) {
		this.val = val;
		this.handled = handled;
		this.error = error;
	}
	
	@Override
	public String toString() {
	    return "Response{" +
	           "val=" + val +
	           ", handled=" + handled +
	           ", error='" + error + '\'' +
	           '}';
	}

	
	@Override
	public boolean equals(Object obj) {
	    if (this == obj) {
	    	return true;
	    
	    }
	    if (obj == null || getClass() != obj.getClass()) {
	    	return false;
	    }
	    Response<?> other = (Response<?>) obj;

	    return handled == other.handled &&
	           Objects.equals(val, other.val) &&
	           Objects.equals(error, other.error);
	}

	@Override
	public int hashCode() {
	    return Objects.hash(val, handled, error);
	}

	
	public static <T> Response<T> notHandled() {
		return new Response<>(null, false, "");
	}
	
	public static <T> Response<T> is(T val) {
		return new Response<>(val, true, "");
	}
	
	public static <T> Response<T> error(String error) {
		return new Response<>(null, true, error);
	}
	
	public boolean wasNotHandled() {
		return !handled;
	}
	
	public boolean wasHandled() {
		return handled;
	}
	
	public boolean wasValueReturned() {
		return wasHandled() && !wasError();
	}
	
	public boolean wasError() {
		return !error.isBlank();
	}
	
	public T value() {
		return val;
	}
	
	public String getError() {
		return error;
	}
	
	public Response<T> convertToErrorIfNoValueReturned(String notHandledArg, String errorArg) {
        if (wasNotHandled()) {
        	return Response.error(notHandledArg);
        } else if (wasError()) {
        	return Response.error(errorArg + ":" + error);
        }
        return this;
	}
}
