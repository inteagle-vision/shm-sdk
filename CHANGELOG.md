# Changelog

All notable changes to the Inteagle Cloud SDK will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.1] - 2026-01-16

### Added
- `AutoCloseable` interface implementation for `InteagleClient`, `RestClient`, and `MqttSubscriber`
  - Proper cleanup of HTTP client resources (connection pool and executor service)
  - Graceful shutdown of MQTT executor service with timeout handling
- `MqttConnectionException` for better MQTT error handling
- Comprehensive package-info.java files for all packages with usage examples
- Parameter validation in all constructors and public methods
- `toString()` methods to all model classes for better debugging
- Named constants for all magic numbers and strings
- Detailed Javadoc comments with examples, parameters, and exception documentation

### Changed
- Enhanced error messages in `RestClient` with detailed context (HTTP method, path, status code)
- Improved error reporting with JSON error parsing and message extraction
- Better null checks and IllegalArgumentException for invalid inputs
- Updated imports to avoid fully qualified names where appropriate

### Improved
- Resource management: all clients now properly release resources when closed
- Code maintainability: extracted constants for timeouts, headers, and other magic values
- Documentation: comprehensive package and method documentation with usage examples
- Error handling: better exception messages with context for easier troubleshooting

### Security
- No security vulnerabilities detected by CodeQL analysis
- HMAC-SHA256 authentication implementation verified
- Secure credential handling maintained

## [0.1.0] - 2026-01-15

### Added
- Initial release of Inteagle Cloud SDK
- REST API client for querying projects, devices, telemetry, and alarms
- MQTT subscriber for real-time data streaming
- AK/SK authentication with HMAC-SHA256 signatures
- Support for TLS/SSL connections
- Unified client interface combining REST and MQTT

[Unreleased]: https://github.com/inteagle-vision/shm-sdk/compare/v0.1.1...HEAD
[0.1.1]: https://github.com/inteagle-vision/shm-sdk/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/inteagle-vision/shm-sdk/releases/tag/v0.1.0
