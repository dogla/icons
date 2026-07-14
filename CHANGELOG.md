# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

## [1.1.0] - 2026-07-14

### Fixed

- Base64 icon values with embedded whitespace are now accepted: MIME-wrapped values (RFC 2045
  wraps at 76 chars) embed line breaks that the strict pattern and `Base64.getDecoder()` rejected.
  Detection strips whitespace first and the decoded icon matches the unwrapped original.

### Build

- Publishing migrated with the Central Portal API in mind: `central-publishing-maven-plugin`
  updated to 0.11.0 (0.6.0 failed to parse the current API response).

## [1.0.0] - 2026-06-04

- Initial release: `Icon`/`Icon.valueOf` model with file, classpath, Base64 and Iconify sources,
  offline classpath tier for bundled Iconify SVGs.
