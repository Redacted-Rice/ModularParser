package redactedrice.modularparser.core;


import java.util.Objects;

public final class Response<T> {
    protected T val;
    protected boolean handled;
    protected boolean error;
    protected String errorStr;

    protected Response(T val, boolean handled, boolean error, String errorStr) {
        this.val = val;
        this.handled = handled;
        this.error = error;
        this.errorStr = errorStr;
    }

    public static <T> Response<T> notHandled() {
        return new Response<>(null, false, false, "");
    }

    public static <T> Response<T> is(T val) {
        return new Response<>(val, true, false, "");
    }

    public static <T> Response<T> error(String error) {
        if (error == null) {
            error = "";
        }
        return new Response<>(null, true, true, error);
    }

    @Override
    public String toString() {
        String asString = "Response(";
        if (error) {
            asString += "error='" + errorStr + "'";
        } else if (!handled) {
            asString += "not handled";
        } else {
            asString += "val=" + val.toString();
        }
        return asString + ")";
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

        return handled == other.handled && Objects.equals(val, other.val) &&
                // This case shouldn't really be hit but keep it here
                // in case the class changes
                Objects.equals(error, other.error) && Objects.equals(errorStr, other.errorStr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(val, handled, error, errorStr);
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
        return error;
    }

    public T getValue() {
        return val;
    }

    public String getError() {
        return errorStr;
    }

    public <T2> Response<T2> convert(Class<T2> clazz) {
        if (wasNotHandled()) {
            return Response.notHandled();
        } else if (wasError()) {
            return Response.error(errorStr);
        }
        if (val == null) {
            return Response.is(null);
        }
        if (clazz.isInstance(val)) {
            return Response.is(clazz.cast(val));
        }
        return Response.error("Bad cast type");
    }

    public static Response<Object> combineErrors(Response<?>... responses) {
        StringBuilder errors = new StringBuilder();
        boolean foundOne = false;
        for (Response<?> resp : responses) {
            if (resp.wasError()) {
                if (!foundOne) {
                    foundOne = true;
                } else {
                    errors.append('\n');
                }
                errors.append(resp.getError());
            }
        }
        if (foundOne) {
            return Response.error(errors.toString());
        }
        return Response.is(null);
    }
}
