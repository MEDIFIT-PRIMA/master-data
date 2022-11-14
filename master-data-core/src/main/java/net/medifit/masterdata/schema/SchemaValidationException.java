package net.medifit.masterdata.schema;

public class SchemaValidationException extends RuntimeException {

  public SchemaValidationException(String msg) {
    super(msg);
  }

  public SchemaValidationException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
