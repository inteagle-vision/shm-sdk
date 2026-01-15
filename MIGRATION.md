# Migration Guide

## Upgrading from v0.1.0 to v0.1.1 (Unreleased)

### Resource Management - IMPORTANT

The SDK now implements `AutoCloseable` for proper resource cleanup. While not breaking existing code, we **strongly recommend** updating your code to use try-with-resources or explicitly call `close()`.

#### Before (still works, but not recommended):
```java
InteagleClient client = InteagleClient.builder()
    .apiEndpoint("https://api.shm.inteagle.com")
    .mqttEndpoint("broker.shm.inteagle.com", 8883)
    .credentials(accessKeyId, accessKeySecret)
    .build();

// Use client...

// Resources may not be properly cleaned up
```

#### After (recommended):
```java
// Option 1: Try-with-resources (recommended)
try (InteagleClient client = InteagleClient.builder()
        .apiEndpoint("https://api.shm.inteagle.com")
        .mqttEndpoint("broker.shm.inteagle.com", 8883)
        .credentials(accessKeyId, accessKeySecret)
        .build()) {
    
    // Use client...
    
} // Resources are automatically cleaned up

// Option 2: Explicit close
InteagleClient client = InteagleClient.builder()
    .apiEndpoint("https://api.shm.inteagle.com")
    .mqttEndpoint("broker.shm.inteagle.com", 8883)
    .credentials(accessKeyId, accessKeySecret)
    .build();

try {
    // Use client...
} finally {
    client.close(); // Explicitly clean up resources
}
```

### What Gets Cleaned Up

When you call `client.close()` or use try-with-resources, the SDK now properly:
- Closes all MQTT connections
- Shuts down the MQTT executor service
- Releases HTTP client connection pool
- Terminates HTTP client executor service

### Parameter Validation

The SDK now performs stricter parameter validation. The following will now throw `IllegalArgumentException`:

```java
// These will throw IllegalArgumentException:
InteagleClient.builder()
    .apiEndpoint(null)  // Cannot be null
    .apiEndpoint("")    // Cannot be empty
    .apiEndpoint("http://api.example.com")  // Must be HTTPS
    .mqttEndpoint(null, 8883)  // Host cannot be null
    .mqttEndpoint("broker.example.com", 0)  // Port must be 1-65535
    .credentials(null, "secret")  // AccessKeyId cannot be null
    .credentials("key", null)     // AccessKeySecret cannot be null
    .build();

// Subscription methods also validate parameters:
mqtt.subscribeProject(null, handler);  // ProjectId cannot be null
mqtt.subscribeDevice(projectId, null, handler);  // DeviceId cannot be null
mqtt.subscribe(topicPattern, null);  // Handler cannot be null
```

Make sure your code provides valid parameters to avoid exceptions.

### No Breaking Changes

All existing public APIs remain unchanged. The improvements are:
- **Backward compatible**: Old code continues to work
- **Enhanced**: Better error messages and validation
- **Improved**: Proper resource cleanup when you adopt new patterns

### Benefits of Upgrading

1. **Better Resource Management**: Prevent memory leaks and connection exhaustion
2. **Clearer Error Messages**: Easier troubleshooting with detailed error context
3. **Early Error Detection**: Parameter validation catches issues before making API calls
4. **Improved Documentation**: Comprehensive Javadoc with usage examples

### Recommended Actions

1. ✅ Update code to use try-with-resources or explicit `close()` calls
2. ✅ Review parameter validation requirements
3. ✅ Test error handling with the new detailed error messages
4. ✅ Check that your application properly handles `IllegalArgumentException` for invalid parameters

### Questions?

If you encounter any issues during migration, please:
- Check the updated Javadoc documentation
- Review the package-info.java files for usage examples
- Open an issue on GitHub: https://github.com/inteagle-vision/shm-sdk/issues
