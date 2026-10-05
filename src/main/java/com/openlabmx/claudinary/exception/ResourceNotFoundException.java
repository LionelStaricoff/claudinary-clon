package com.openlabmx.claudinary.exception;
import java.util.UUID;
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
    public ResourceNotFoundException(String resourceType, UUID id) { super(resourceType + " not found with id: " + id); }
    public ResourceNotFoundException(String resourceType, String field, String value) { super(resourceType + " not found with " + field + ": " + value); }
}
