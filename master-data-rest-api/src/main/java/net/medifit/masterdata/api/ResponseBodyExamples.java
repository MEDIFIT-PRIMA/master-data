package net.medifit.masterdata.api;

public interface ResponseBodyExamples {

  String RESPONSE_401_UNAUTHORIZED_REQUEST =
      "{\n"
          + "  \"type\": \"masterdataException:SecurityException\",\n"
          + "  \"title\": \"Unauthorised request\",\n"
          + "  \"status\": 401\n"
          + "}";

  String RESPONSE_403_CLIENT_UNAUTHORIZED =
      "{\n"
          + "  \"type\": \"masterdataException:SecurityException\",\n"
          + "  \"title\": \"Access to resource forbidden\",\n"
          + "  \"status\": 403\n"
          + "}";

  String RESPONSE_404_RESOURCE_NOT_FOUND =
      "{\n"
          + "  \"type\": \"masterdataException:NoSuchResourceException\",\n"
          + "  \"title\": \"Resource not found\",\n"
          + "  \"status\": 404\n"
          + "}";

  String RESPONSE_406_NOT_ACCEPTABLE =
      "{\n"
          + "  \"type\": \"masterdataException:NotAcceptableException\",\n"
          + "  \"title\": \"Conflicting request and response headers\",\n"
          + "  \"status\": 406\n"
          + "}";

  String RESPONSE_500_IMPLEMENTATION_EXCEPTION =
      "{\n"
          + "  \"type\": \"masterdataException:ImplementationException\",\n"
          + "  \"title\": \"A server-side error occurred\",\n"
          + "  \"status\": 500\n"
          + "}";

  String RESPONSE_501_NOT_IMPLEMENTED =
      "{\n"
          + "  \"type\": \"masterdataException:ImplementationException\",\n"
          + "  \"title\": \"Functionality not supported by server\",\n"
          + "  \"status\": 501\n"
          + "}";
}
